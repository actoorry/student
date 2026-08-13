package vip.appap.suxin.module.sales.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.ObjectUtils;
import vip.appap.suxin.module.accountant.api.PayRefundApi;
import vip.appap.suxin.module.accountant.api.dto.PayRefundCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayRefundRespDTO;
import vip.appap.suxin.module.accountant.enums.PayRefundStatusEnum;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.sales.api.SalesCombinationRecordApi;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationRecordRespDTO;
import vip.appap.suxin.module.sales.enums.SalesCombinationRecordStatusEnum;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesAfterSaleDisagreeReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesAfterSalePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesAfterSaleRefuseReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSaleCreateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSaleDeliveryReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSalePageReqVO;
import vip.appap.suxin.module.sales.convert.SalesAfterSaleConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesAfterSaleMapper;
import vip.appap.suxin.module.sales.dal.redis.no.SalesNoRedisDAO;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleOperateTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleWayEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderItemAfterSaleStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import vip.appap.suxin.module.sales.framework.aftersale.core.annotations.SalesAfterSaleLog;
import vip.appap.suxin.module.sales.framework.aftersale.core.utils.SalesAfterSaleLogUtils;
import vip.appap.suxin.module.sales.framework.order.config.SalesOrderProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.json.JsonUtils.toJsonString;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.*;

/**
 * 售后订单 Service 实现类
 *
 * @author 书心软件
 */
@Slf4j
@Service
@Validated
public class SalesAfterSaleServiceImpl implements SalesAfterSaleService {

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private SalesOrderUpdateService tradeOrderUpdateService;
    @Resource
    private SalesOrderQueryService tradeOrderQueryService;
    @Resource
    private SalesDeliveryExpressService deliveryExpressService;

    @Resource
    private SalesAfterSaleMapper tradeAfterSaleMapper;
    @Resource
    private SalesNoRedisDAO tradeNoRedisDAO;

    @Resource
    private PayRefundApi payRefundApi;
    @Resource
    private SalesCombinationRecordApi combinationRecordApi;

    @Resource
    private SalesOrderProperties tradeOrderProperties;

    @Override
    public PageResult<SalesAfterSaleDO> getAfterSalePage(SalesAfterSalePageReqVO pageReqVO) {
        return tradeAfterSaleMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<SalesAfterSaleDO> getAfterSalePage(Long userId, AppSalesAfterSalePageReqVO pageReqVO) {
        return tradeAfterSaleMapper.selectPage(userId, pageReqVO);
    }

    @Override
    public SalesAfterSaleDO getAfterSale(Long userId, Long id) {
        return tradeAfterSaleMapper.selectByIdAndUserId(id, userId);
    }

    @Override
    public SalesAfterSaleDO getAfterSale(Long id) {
        return tradeAfterSaleMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesAfterSaleLog(operateType = SalesAfterSaleOperateTypeEnum.MEMBER_CREATE)
    public Long createAfterSale(Long userId, AppSalesAfterSaleCreateReqVO createReqVO) {
        // 第一步，前置校验
        SalesOrderItemDO tradeOrderItem = validateOrderItemApplicable(userId, createReqVO);

        // 第二步，存储售后订单
        SalesAfterSaleDO afterSale = createAfterSale(createReqVO, tradeOrderItem);
        return afterSale.getId();
    }

    /**
     * 校验交易订单项是否可以申请售后
     *
     * @param userId      用户编号
     * @param createReqVO 售后创建信息
     * @return 交易订单项
     */
    private SalesOrderItemDO validateOrderItemApplicable(Long userId, AppSalesAfterSaleCreateReqVO createReqVO) {
        // 校验订单项存在
        SalesOrderItemDO orderItem = tradeOrderQueryService.getOrderItem(userId, createReqVO.getOrderItemId());
        if (orderItem == null) {
            throw exception(ORDER_ITEM_NOT_FOUND);
        }
        // 已申请售后，不允许再发起售后申请
        if (!SalesOrderItemAfterSaleStatusEnum.isNone(orderItem.getAfterSaleStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_ITEM_APPLIED);
        }
        // 申请的退款金额，不能超过商品的价格
        if (createReqVO.getRefundPrice() > orderItem.getPayPrice()) {
            throw exception(AFTER_SALE_CREATE_FAIL_REFUND_PRICE_ERROR);
        }

        // 校验订单存在
        SalesOrderDO order = tradeOrderQueryService.getOrder(userId, orderItem.getOrderId());
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (PayChannelEnum.WX_VIRTUAL_LITE.getCode().equals(order.getPayChannelCode())) {
            throw exception(AFTER_SALE_WECHAT_VIRTUAL_REFUND_UNSUPPORTED);
        }
        // TODO 芋艿：超过一定时间，不允许售后
        // 已取消，无法发起售后
        if (SalesOrderStatusEnum.isCanceled(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_CANCELED);
        }
        // 未支付，无法发起售后
        if (!SalesOrderStatusEnum.havePaid(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_PAID);
        }
        // 如果是【退货退款】的情况，需要额外校验是否发货
        if (createReqVO.getWay().equals(SalesAfterSaleWayEnum.RETURN_AND_REFUND.getWay())
                && !SalesOrderStatusEnum.haveDelivered(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_DELIVERED);
        }
        // 如果是拼团订单，则进行中不允许售后
        if (SalesOrderTypeEnum.isCombination(order.getType())) {
            SalesCombinationRecordRespDTO combinationRecord = combinationRecordApi.getCombinationRecordByOrderId(
                    order.getUserId(), order.getId());
            if (combinationRecord != null && SalesCombinationRecordStatusEnum.isInProgress(combinationRecord.getStatus())) {
                throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_COMBINATION_IN_PROGRESS);
            }
        }
        return orderItem;
    }

    private SalesAfterSaleDO createAfterSale(AppSalesAfterSaleCreateReqVO createReqVO,
                                        SalesOrderItemDO orderItem) {
        // 创建售后单
        SalesAfterSaleDO afterSale = SalesAfterSaleConvert.INSTANCE.convert(createReqVO, orderItem);
        afterSale.setNo(tradeNoRedisDAO.generate(SalesNoRedisDAO.AFTER_SALE_NO_PREFIX));
        afterSale.setStatus(SalesAfterSaleStatusEnum.APPLY.getStatus());
        // 标记是售中还是售后
        SalesOrderDO order = tradeOrderQueryService.getOrder(orderItem.getUserId(), orderItem.getOrderId());
        afterSale.setOrderNo(order.getNo()); // 记录 orderNo 订单流水，方便后续检索
        afterSale.setType(SalesOrderStatusEnum.isCompleted(order.getStatus())
                ? SalesAfterSaleTypeEnum.AFTER_SALE.getType() : SalesAfterSaleTypeEnum.IN_SALE.getType());
        tradeAfterSaleMapper.insert(afterSale);

        // 更新交易订单项的售后状态
        tradeOrderUpdateService.updateOrderItemWhenAfterSaleCreate(orderItem.getId(), afterSale.getId());

        // 记录售后日志
        SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), null,
                SalesAfterSaleStatusEnum.APPLY.getStatus());
        return afterSale;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesAfterSaleLog(operateType = SalesAfterSaleOperateTypeEnum.ADMIN_AGREE_APPLY)
    public void agreeAfterSale(Long userId, Long id) {
        // 校验售后单存在，并状态未审批
        SalesAfterSaleDO afterSale = validateAfterSaleAuditable(id);

        // 更新售后单的状态
        // 情况一：退款：标记为 WAIT_REFUND 状态。后续等退款发起成功后，在标记为 COMPLETE 状态
        // 情况二：退货退款：需要等用户退货后，才能发起退款
        Integer newStatus = afterSale.getWay().equals(SalesAfterSaleWayEnum.REFUND.getWay()) ?
                SalesAfterSaleStatusEnum.WAIT_REFUND.getStatus() : SalesAfterSaleStatusEnum.SELLER_AGREE.getStatus();
        updateAfterSaleStatus(afterSale.getId(), SalesAfterSaleStatusEnum.APPLY.getStatus(), new SalesAfterSaleDO()
                .setStatus(newStatus).setAuditUserId(userId).setAuditTime(LocalDateTime.now()));

        // 记录售后日志
        SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), afterSale.getStatus(), newStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesAfterSaleLog(operateType = SalesAfterSaleOperateTypeEnum.ADMIN_DISAGREE_APPLY)
    public void disagreeAfterSale(Long userId, SalesAfterSaleDisagreeReqVO auditReqVO) {
        // 校验售后单存在，并状态未审批
        SalesAfterSaleDO afterSale = validateAfterSaleAuditable(auditReqVO.getId());

        // 更新售后单的状态
        Integer newStatus = SalesAfterSaleStatusEnum.SELLER_DISAGREE.getStatus();
        updateAfterSaleStatus(afterSale.getId(), SalesAfterSaleStatusEnum.APPLY.getStatus(), new SalesAfterSaleDO()
                .setStatus(newStatus).setAuditUserId(userId).setAuditTime(LocalDateTime.now())
                .setAuditReason(auditReqVO.getAuditReason()));

        // 记录售后日志
        SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), afterSale.getStatus(), newStatus);

        // 更新交易订单项的售后状态为【未申请】
        tradeOrderUpdateService.updateOrderItemWhenAfterSaleCancel(afterSale.getOrderItemId());
    }

    /**
     * 校验售后单是否可审批（同意售后、拒绝售后）
     *
     * @param id 售后编号
     * @return 售后单
     */
    private SalesAfterSaleDO validateAfterSaleAuditable(Long id) {
        SalesAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), SalesAfterSaleStatusEnum.APPLY.getStatus())) {
            throw exception(AFTER_SALE_AUDIT_FAIL_STATUS_NOT_APPLY);
        }
        return afterSale;
    }

    private void updateAfterSaleStatus(Long id, Integer status, SalesAfterSaleDO updateObj) {
        int updateCount = tradeAfterSaleMapper.updateByIdAndStatus(id, status, updateObj);
        if (updateCount == 0) {
            throw exception(AFTER_SALE_UPDATE_STATUS_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesAfterSaleLog(operateType = SalesAfterSaleOperateTypeEnum.MEMBER_DELIVERY)
    public void deliveryAfterSale(Long userId, AppSalesAfterSaleDeliveryReqVO deliveryReqVO) {
        // 校验售后单存在，并状态未退货
        SalesAfterSaleDO afterSale = tradeAfterSaleMapper.selectByIdAndUserId(deliveryReqVO.getId(), userId);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), SalesAfterSaleStatusEnum.SELLER_AGREE.getStatus())) {
            throw exception(AFTER_SALE_DELIVERY_FAIL_STATUS_NOT_SELLER_AGREE);
        }
        SalesDeliveryExpressDO express = deliveryExpressService.validateDeliveryExpress(deliveryReqVO.getLogisticsId());
        // Service 层再校验非空运单号（防止绕过 Controller 的内部调用提交空白单号）
        if (StrUtil.isBlank(deliveryReqVO.getLogisticsNo())) {
            throw exception(AFTER_SALE_DELIVERY_FAIL_LOGISTICS_NO_BLANK);
        }

        // 更新售后单的物流信息
        updateAfterSaleStatus(afterSale.getId(), SalesAfterSaleStatusEnum.SELLER_AGREE.getStatus(), new SalesAfterSaleDO()
                .setStatus(SalesAfterSaleStatusEnum.BUYER_DELIVERY.getStatus())
                .setLogisticsId(deliveryReqVO.getLogisticsId()).setLogisticsNo(deliveryReqVO.getLogisticsNo().trim())
                .setDeliveryTime(LocalDateTime.now()));

        // 记录售后日志
        SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), afterSale.getStatus(),
                SalesAfterSaleStatusEnum.BUYER_DELIVERY.getStatus(),
                MapUtil.<String, Object>builder().put("deliveryName", express.getName())
                        .put("logisticsNo", deliveryReqVO.getLogisticsNo()).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesAfterSaleLog(operateType = SalesAfterSaleOperateTypeEnum.ADMIN_AGREE_RECEIVE)
    public void receiveAfterSale(Long userId, Long id) {
        // 校验售后单存在，并状态为已退货
        SalesAfterSaleDO afterSale = validateAfterSaleReceivable(id);

        // 更新售后单的状态
        updateAfterSaleStatus(afterSale.getId(), SalesAfterSaleStatusEnum.BUYER_DELIVERY.getStatus(), new SalesAfterSaleDO()
                .setStatus(SalesAfterSaleStatusEnum.WAIT_REFUND.getStatus()).setReceiveTime(LocalDateTime.now()));

        // 记录售后日志
        SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), afterSale.getStatus(),
                SalesAfterSaleStatusEnum.WAIT_REFUND.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesAfterSaleLog(operateType = SalesAfterSaleOperateTypeEnum.ADMIN_DISAGREE_RECEIVE)
    public void refuseAfterSale(Long userId, SalesAfterSaleRefuseReqVO refuseReqVO) {
        // 校验售后单存在，并状态为已退货
        SalesAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(refuseReqVO.getId());
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), SalesAfterSaleStatusEnum.BUYER_DELIVERY.getStatus())) {
            throw exception(AFTER_SALE_CONFIRM_FAIL_STATUS_NOT_BUYER_DELIVERY);
        }

        // 更新售后单的状态
        updateAfterSaleStatus(afterSale.getId(), SalesAfterSaleStatusEnum.BUYER_DELIVERY.getStatus(), new SalesAfterSaleDO()
                .setStatus(SalesAfterSaleStatusEnum.SELLER_REFUSE.getStatus()).setReceiveTime(LocalDateTime.now())
                .setReceiveReason(refuseReqVO.getRefuseMemo()));

        // 记录售后日志
        SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), afterSale.getStatus(),
                SalesAfterSaleStatusEnum.SELLER_REFUSE.getStatus(),
                MapUtil.of("reason", refuseReqVO.getRefuseMemo()));

        // 更新交易订单项的售后状态为【未申请】
        tradeOrderUpdateService.updateOrderItemWhenAfterSaleCancel(afterSale.getOrderItemId());
    }

    /**
     * 校验售后单是否可收货，即处于买家已发货
     *
     * @param id 售后编号
     * @return 售后单
     */
    private SalesAfterSaleDO validateAfterSaleReceivable(Long id) {
        SalesAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), SalesAfterSaleStatusEnum.BUYER_DELIVERY.getStatus())) {
            throw exception(AFTER_SALE_CONFIRM_FAIL_STATUS_NOT_BUYER_DELIVERY);
        }
        return afterSale;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesAfterSaleLog(operateType = SalesAfterSaleOperateTypeEnum.ADMIN_REFUND)
    public void refundAfterSale(Long userId, String userIp, Long id) {
        // 校验售后单的状态，并状态待退款
        SalesAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), SalesAfterSaleStatusEnum.WAIT_REFUND.getStatus())) {
            throw exception(AFTER_SALE_REFUND_FAIL_STATUS_NOT_WAIT_REFUND);
        }

        Integer newStatus;
        if (ObjUtil.equals(afterSale.getRefundPrice(), 0)) {
            // 特殊：退款为 0 的订单，直接标记为完成（积分商城）。关联案例：https://t.zsxq.com/AQEvL
            updateAfterSaleStatus(afterSale.getId(), SalesAfterSaleStatusEnum.WAIT_REFUND.getStatus(), new SalesAfterSaleDO()
                    .setStatus(SalesAfterSaleStatusEnum.COMPLETE.getStatus()).setRefundTime(LocalDateTime.now()));
            newStatus = SalesAfterSaleStatusEnum.COMPLETE.getStatus();
        } else {
            // 发起退款单。注意，需要在事务提交后，再进行发起，避免重复发起
            createPayRefund(userIp, afterSale);
            newStatus = afterSale.getStatus();  // 特殊：这里状态不变，而是最终 updateAfterSaleRefunded 处理！！！
        }

        // 记录售后日志
        SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), afterSale.getStatus(), newStatus);
    }

    private void createPayRefund(String userIp, SalesAfterSaleDO afterSale) {
        // 创建退款单
        PayRefundCreateReqDTO createReqDTO = SalesAfterSaleConvert.INSTANCE.convert(userIp, afterSale, tradeOrderProperties)
                .setUserId(afterSale.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue())
                .setReason(StrUtil.format("退款【{}】", afterSale.getSpuName()));
        Long payRefundId = payRefundApi.createRefund(createReqDTO);

        // 更新售后单的退款单号
        tradeAfterSaleMapper.updateById(new SalesAfterSaleDO().setId(afterSale.getId()).setPayRefundId(payRefundId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesAfterSaleLog(operateType = SalesAfterSaleOperateTypeEnum.SYSTEM_REFUND_SUCCESS)
    public void updateAfterSaleRefunded(Long id, Long orderId, Long payRefundId) {
        // 1. 校验售后单的状态，并状态待退款
        SalesAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), SalesAfterSaleStatusEnum.WAIT_REFUND.getStatus())) {
            throw exception(AFTER_SALE_REFUND_FAIL_STATUS_NOT_WAIT_REFUND);
        }

        // 2. 校验退款单
        PayRefundRespDTO payRefund = validatePayRefund(afterSale, payRefundId);

        // 3. 处理退款结果
        if (PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            // 【情况一：退款成功】
            updateAfterSaleStatus(afterSale.getId(), SalesAfterSaleStatusEnum.WAIT_REFUND.getStatus(), new SalesAfterSaleDO()
                .setStatus(SalesAfterSaleStatusEnum.COMPLETE.getStatus()).setRefundTime(LocalDateTime.now()));

            // 记录售后日志
            SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), afterSale.getStatus(), SalesAfterSaleStatusEnum.COMPLETE.getStatus());

            // 更新交易订单项的售后状态为【已完成】
            tradeOrderUpdateService.updateOrderItemWhenAfterSaleSuccess(afterSale.getOrderItemId(), afterSale.getRefundPrice());
            // 【情况二：退款失败】
        } else if (PayRefundStatusEnum.isFailure(payRefund.getStatus())) {
            // 记录售后日志
            SalesAfterSaleLogUtils.setAfterSaleOperateType(SalesAfterSaleOperateTypeEnum.SYSTEM_REFUND_FAIL);
            SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), afterSale.getStatus(), afterSale.getStatus());
        }
    }

    /**
     * 校验退款单的合法性
     *
     * @param afterSale 售后单
     * @param payRefundId 退款单编号
     * @return 退款单
     */
    private PayRefundRespDTO validatePayRefund(SalesAfterSaleDO afterSale, Long payRefundId) {
        // 1. 校验退款单是否存在
        PayRefundRespDTO payRefund = payRefundApi.getRefund(payRefundId);
        if (payRefund == null) {
            log.error("[validatePayRefund][afterSale({}) payRefund({}) 不存在，请进行处理！]", afterSale.getId(), payRefundId);
            throw exception(AFTER_SALE_REFUND_FAIL_REFUND_NOT_FOUND);
        }
        // 2.1 校验退款单无退款结果（成功、失败）
        if (!PayRefundStatusEnum.isSuccess(payRefund.getStatus())
            && !PayRefundStatusEnum.isFailure(payRefund.getStatus())) {
            log.error("[validatePayRefund][afterSale({}) payRefund({}) 无退款结果，请进行处理！payRefund 数据是：{}]",
                    afterSale.getId(), payRefundId, toJsonString(payRefund));
            throw exception(AFTER_SALE_REFUND_FAIL_REFUND_NOT_SUCCESS_OR_FAILURE);
        }
        // 2.2 校验退款金额一致
        if (ObjectUtil.notEqual(payRefund.getRefundPrice(), afterSale.getRefundPrice())) {
            log.error("[validatePayRefund][afterSale({}) payRefund({}) 退款金额不匹配，请进行处理！afterSale 数据是：{}，payRefund 数据是：{}]",
                    afterSale.getId(), payRefundId, toJsonString(afterSale), toJsonString(payRefund));
            throw exception(AFTER_SALE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH);
        }
        // 2.3 校验退款订单匹配（二次）
        if (ObjectUtil.notEqual(payRefund.getMerchantRefundId(), afterSale.getId().toString())) {
            log.error("[validatePayRefund][afterSale({}) 退款单不匹配({})，请进行处理！payRefund 数据是：{}]",
                    afterSale.getId(), payRefundId, toJsonString(payRefund));
            throw exception(AFTER_SALE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        }
        return payRefund;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesAfterSaleLog(operateType = SalesAfterSaleOperateTypeEnum.MEMBER_CANCEL)
    public void cancelAfterSale(Long userId, Long id) {
        // 校验售后单的状态，并状态待退款
        SalesAfterSaleDO afterSale = tradeAfterSaleMapper.selectByIdAndUserId(id, userId);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (!ObjectUtils.equalsAny(afterSale.getStatus(), SalesAfterSaleStatusEnum.APPLY.getStatus(),
                SalesAfterSaleStatusEnum.SELLER_AGREE.getStatus(),
                SalesAfterSaleStatusEnum.BUYER_DELIVERY.getStatus())) {
            throw exception(AFTER_SALE_CANCEL_FAIL_STATUS_NOT_APPLY_OR_AGREE_OR_BUYER_DELIVERY);
        }

        // 更新售后单的状态为【已取消】
        updateAfterSaleStatus(afterSale.getId(), afterSale.getStatus(), new SalesAfterSaleDO()
                .setStatus(SalesAfterSaleStatusEnum.BUYER_CANCEL.getStatus()));

        // 记录售后日志
        SalesAfterSaleLogUtils.setAfterSaleInfo(afterSale.getId(), afterSale.getStatus(),
                SalesAfterSaleStatusEnum.BUYER_CANCEL.getStatus());

        // 更新交易订单项的售后状态为【未申请】
        tradeOrderUpdateService.updateOrderItemWhenAfterSaleCancel(afterSale.getOrderItemId());
    }

    @Override
    public Long getApplyingAfterSaleCount(Long userId) {
        return tradeAfterSaleMapper.selectCountByUserIdAndStatus(userId, SalesAfterSaleStatusEnum.APPLYING_STATUSES);
    }

}

