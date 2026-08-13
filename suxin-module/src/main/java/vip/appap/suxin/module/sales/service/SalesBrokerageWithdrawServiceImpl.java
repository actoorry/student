package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.framework.common.util.number.MoneyUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.common.util.object.ObjectUtils;
import vip.appap.suxin.module.accountant.api.PayTransferApi;
import vip.appap.suxin.module.accountant.api.dto.PayTransferCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayTransferCreateRespDTO;
import vip.appap.suxin.module.accountant.api.dto.PayTransferRespDTO;
import vip.appap.suxin.module.accountant.api.AccountApi;
import vip.appap.suxin.module.accountant.api.dto.AccountRespDTO;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.enums.PayTransferStatusEnum;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageWithdrawPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageWithdrawCreateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageWithdrawDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesBrokerageWithdrawMapper;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawTypeEnum;
import vip.appap.suxin.module.sales.framework.order.config.SalesOrderProperties;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageWithdrawSummaryRespBO;
import vip.appap.suxin.module.sales.service.SalesConfigService;
import com.google.common.base.Objects;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;
import static vip.appap.suxin.module.accountant.enums.ErrorCodeConstants.PAY_TRANSFER_NOT_FOUND;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.*;

/**
 * 佣金提现 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
@Slf4j
public class SalesBrokerageWithdrawServiceImpl implements SalesBrokerageWithdrawService {

    @Resource
    private SalesBrokerageWithdrawMapper brokerageWithdrawMapper;

    @Resource
    private SalesBrokerageRecordService brokerageRecordService;
    @Resource
    private SalesConfigService tradeConfigService;

    @Resource
    private PayTransferApi payTransferApi;
    @Resource
    private AccountApi accountApi;

    @Resource
    private Validator validator;

    @Resource
    private SalesOrderProperties tradeOrderProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBrokerageWithdraw(Long id, SalesBrokerageWithdrawStatusEnum status, String auditReason, String userIp) {
        // 1.1 校验存在
        SalesBrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        // 1.2 特殊：【重新转账】如果是提现失败，并且状态是审核中，那么更新状态为审核中，并且清空 transferErrorMsg
        if (SalesBrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus().equals(withdraw.getStatus())) {
            int updateCount = brokerageWithdrawMapper.updateByIdAndStatus(id, withdraw.getStatus(),
                    new SalesBrokerageWithdrawDO().setStatus(SalesBrokerageWithdrawStatusEnum.AUDITING.getStatus()).setTransferErrorMsg(""));
            if (updateCount == 0) {
                throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
            }
            withdraw.setStatus(SalesBrokerageWithdrawStatusEnum.AUDITING.getStatus()).setTransferErrorMsg("");
        }
        // 1.2 校验状态为审核中
        if (ObjectUtil.notEqual(SalesBrokerageWithdrawStatusEnum.AUDITING.getStatus(), withdraw.getStatus())) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 2. 更新状态
        int updateCount = brokerageWithdrawMapper.updateByIdAndStatus(id, withdraw.getStatus(),
                new SalesBrokerageWithdrawDO().setStatus(status.getStatus()).setAuditReason(auditReason).setAuditTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 3.1 审批通过的后续处理
        if (SalesBrokerageWithdrawStatusEnum.AUDIT_SUCCESS.equals(status)) {
            auditBrokerageWithdrawSuccess(withdraw);
            // 3.2 审批不通过的后续处理
        } else if (SalesBrokerageWithdrawStatusEnum.AUDIT_FAIL.equals(status)) {
            brokerageRecordService.addBrokerage(withdraw.getUserId(), SalesBrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), SalesBrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        } else {
            throw new IllegalArgumentException("不支持的提现状态：" + status);
        }
    }

    private void auditBrokerageWithdrawSuccess(SalesBrokerageWithdrawDO withdraw) {
        // 情况一：通过 API 转账
        if (SalesBrokerageWithdrawTypeEnum.isApi(withdraw.getType())) {
            createPayTransfer(withdraw);
            return;
        }

        // 情况二：非 API 转账（手动打款）
        brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(), SalesBrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus(),
                new SalesBrokerageWithdrawDO().setStatus(SalesBrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus()));
    }

    private void createPayTransfer(SalesBrokerageWithdrawDO withdraw) {
        // 1.1 获取基础信息
        String userAccount = withdraw.getUserAccount();
        String userName = withdraw.getUserName();
        String channelCode = null;
        Map<String, String> channelExtras = null;
        if (Objects.equal(withdraw.getType(), SalesBrokerageWithdrawTypeEnum.ALIPAY_API.getType())) {
            channelCode = PayChannelEnum.ALIPAY_PC.getCode();
        } else if (Objects.equal(withdraw.getType(), SalesBrokerageWithdrawTypeEnum.WECHAT_API.getType())) {
            channelCode = withdraw.getTransferChannelCode();
            userAccount = withdraw.getUserAccount();
            // 特殊：微信需要有报备信息
            channelExtras = PayTransferCreateReqDTO.buildWeiXinChannelExtra1005("推广员", "佣金提现");
        } else if (Objects.equal(withdraw.getType(), SalesBrokerageWithdrawTypeEnum.WALLET.getType())) {
            AccountRespDTO wallet = accountApi.getOrCreateAccount(withdraw.getUserId());
            Assert.notNull(wallet, "钱包不存在");
            channelCode = PayChannelEnum.ACCOUNT.getCode();
            userAccount = wallet.getId().toString();
        }
        // 1.2 构建请求
        Integer transferPrice = withdraw.getPrice() - withdraw.getFeePrice(); // 计算实际转账金额（提现金额 - 手续费）
        PayTransferCreateReqDTO transferReqDTO = new PayTransferCreateReqDTO()
                .setAppKey(tradeOrderProperties.getPayAppKey()).setChannelCode(channelCode)
                .setMerchantTransferId(withdraw.getId().toString()).setSubject("佣金提现").setPrice(transferPrice)
                .setUserAccount(userAccount).setUserName(userName).setUserIp(getClientIP())
                .setUserId(withdraw.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue()) // 用户信息
                .setChannelExtras(channelExtras);
        // 1.3 发起请求
        PayTransferCreateRespDTO transferRespDTO = payTransferApi.createTransfer(transferReqDTO);

        // 2. 更新提现记录
        brokerageWithdrawMapper.updateById(new SalesBrokerageWithdrawDO().setId(withdraw.getId())
                .setPayTransferId(transferRespDTO.getId()).setTransferChannelCode(channelCode));
    }

    private SalesBrokerageWithdrawDO validateBrokerageWithdrawExists(Long id) {
        SalesBrokerageWithdrawDO withdraw = brokerageWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        return withdraw;
    }

    @Override
    public SalesBrokerageWithdrawDO getBrokerageWithdraw(Long id) {
        return brokerageWithdrawMapper.selectById(id);
    }

    @Override
    public PageResult<SalesBrokerageWithdrawDO> getBrokerageWithdrawPage(SalesBrokerageWithdrawPageReqVO pageReqVO) {
        return brokerageWithdrawMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBrokerageWithdraw(Long userId, AppSalesBrokerageWithdrawCreateReqVO createReqVO) {
        // 1.1 校验提现金额
        SalesConfigDO tradeConfig = validateWithdrawPrice(createReqVO.getPrice());
        // 1.2 校验提现参数
        createReqVO.validate(validator);

        // 2.1 计算手续费
        Integer feePrice = calculateFeePrice(createReqVO.getPrice(), tradeConfig.getBrokerageWithdrawFeePercent());
        // 2.2 创建佣金提现记录
        SalesBrokerageWithdrawDO withdraw = BeanUtils.toBean(createReqVO, SalesBrokerageWithdrawDO.class)
                .setUserId(userId).setFeePrice(feePrice);
        brokerageWithdrawMapper.insert(withdraw);

        // 3. 创建用户佣金记录
        // 注意，佣金是否充足，reduceBrokerage 已经进行校验
        brokerageRecordService.reduceBrokerage(userId, SalesBrokerageRecordBizTypeEnum.WITHDRAW, String.valueOf(withdraw.getId()),
                createReqVO.getPrice(), SalesBrokerageRecordBizTypeEnum.WITHDRAW.getTitle());
        return withdraw.getId();
    }

    /**
     * 计算提现手续费
     *
     * @param withdrawPrice 提现金额
     * @param percent       手续费百分比
     * @return 提现手续费
     */
    private Integer calculateFeePrice(Integer withdrawPrice, Integer percent) {
        Integer feePrice = 0;
        if (percent != null && percent > 0) {
            feePrice = MoneyUtils.calculateRatePrice(withdrawPrice, Double.valueOf(percent));
        }
        return feePrice;
    }

    /**
     * 校验提现金额要求
     *
     * @param withdrawPrice 提现金额
     * @return 分销配置
     */
    private SalesConfigDO validateWithdrawPrice(Integer withdrawPrice) {
        SalesConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig.getBrokerageWithdrawMinPrice() != null && withdrawPrice < tradeConfig.getBrokerageWithdrawMinPrice()) {
            throw exception(BROKERAGE_WITHDRAW_MIN_PRICE, MoneyUtils.fenToYuanStr(tradeConfig.getBrokerageWithdrawMinPrice()));
        }
        return tradeConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrokerageWithdrawTransferred(Long id, Long payTransferId) {
        // 1.1 校验提现单是否存在
        SalesBrokerageWithdrawDO withdraw = brokerageWithdrawMapper.selectById(id);
        if (withdraw == null) {
            log.error("[updateBrokerageWithdrawTransferred][withdraw({}) payTransfer({}) 不存在提现单，请进行处理！]", id, payTransferId);
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        // 1.2 校验提现单已经结束（成功或失败）
        if (ObjectUtils.equalsAny(withdraw.getStatus(), SalesBrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus(),
                SalesBrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus())) {
            // 特殊：转账单编号相同，直接返回，说明重复回调
            if (ObjectUtil.equal(withdraw.getPayTransferId(), payTransferId)) {
                log.warn("[updateBrokerageWithdrawTransferred][withdraw({}) 已结束，且转账单编号相同({})，直接返回]", withdraw, payTransferId);
                return;
            }
            // 异常：转账单编号不同，说明转账单编号错误
            log.error("[updateBrokerageWithdrawTransferred][withdraw({}) 转账单不匹配({})，请进行处理！]", withdraw, payTransferId);
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR);
        }

        // 2. 校验转账单的合法性
        PayTransferRespDTO payTransfer = validateBrokerageTransferStatusCanUpdate(withdraw, payTransferId);

        // 3. 更新提现单状态
        Integer newStatus = PayTransferStatusEnum.isSuccess(payTransfer.getStatus()) ? SalesBrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus() :
                PayTransferStatusEnum.isClosed(payTransfer.getStatus()) ? SalesBrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus() : null;
        Assert.notNull(newStatus, "转账单状态({}) 不合法", payTransfer.getStatus());
        brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(), withdraw.getStatus(),
                new SalesBrokerageWithdrawDO().setStatus(newStatus)
                        .setTransferTime(payTransfer.getSuccessTime())
                        .setTransferErrorMsg(payTransfer.getChannelErrorMsg()));
    }

    private PayTransferRespDTO validateBrokerageTransferStatusCanUpdate(SalesBrokerageWithdrawDO withdraw, Long payTransferId) {
        // 1. 校验转账单是否存在
        PayTransferRespDTO payTransfer = payTransferApi.getTransfer(payTransferId);
        if (payTransfer == null) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 不存在，请进行处理！]", withdraw.getId(), payTransferId);
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }

        // 2.1 校验转账单已成功或关闭
        if (!PayTransferStatusEnum.isSuccessOrClosed(payTransfer.getStatus())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 未结束，请进行处理！payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_STATUS_NOT_SUCCESS_OR_CLOSED);
        }
        // 2.2 校验转账金额一致
        Integer expectedTransferPrice = withdraw.getPrice() - withdraw.getFeePrice(); // 转账金额 = 提现金额 - 手续费
        if (ObjectUtil.notEqual(payTransfer.getPrice(), expectedTransferPrice)) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 转账金额不匹配，请进行处理！withdraw 数据是：{}，payTransfer 数据是：{}，期望转账金额：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(withdraw), JsonUtils.toJsonString(payTransfer), expectedTransferPrice);
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 2.3 校验转账订单匹配
        if (ObjectUtil.notEqual(payTransfer.getMerchantTransferId(), withdraw.getId().toString())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) 转账单不匹配({})，请进行处理！payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_MERCHANT_EXISTS);
        }
        // 2.4 校验转账渠道一致
        if (ObjectUtil.notEqual(payTransfer.getChannelCode(), withdraw.getTransferChannelCode())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 转账渠道不匹配，请进行处理！withdraw 数据是：{}，payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(withdraw), JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_CHANNEL_NOT_MATCH);
        }
        return payTransfer;
    }

    @Override
    public List<SalesBrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                               Collection<SalesBrokerageWithdrawStatusEnum> statuses) {
        if (CollUtil.isEmpty(userIds) || CollUtil.isEmpty(statuses)) {
            return Collections.emptyList();
        }
        return brokerageWithdrawMapper.selectCountAndSumPriceByUserIdAndStatus(userIds,
                convertSet(statuses, SalesBrokerageWithdrawStatusEnum::getStatus));
    }

}

