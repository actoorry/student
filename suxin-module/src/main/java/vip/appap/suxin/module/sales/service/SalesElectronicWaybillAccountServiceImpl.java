package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.module.partner.api.PartnerAddressApi;
import vip.appap.suxin.module.partner.api.dto.PartnerAddressRespDTO;
import vip.appap.suxin.module.partner.enums.PartnerAddressTypeEnum;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountUpdateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAddressRespVO;
import vip.appap.suxin.module.sales.convert.SalesElectronicWaybillAccountConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesElectronicWaybillAccountMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesElectronicWaybillMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.*;

/**
 * 快递100电子面单账户 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SalesElectronicWaybillAccountServiceImpl implements SalesElectronicWaybillAccountService {

    @Resource
    private SalesElectronicWaybillAccountMapper waybillAccountMapper;
    @Resource
    private SalesElectronicWaybillMapper waybillMapper;
    @Resource
    private SalesDeliveryExpressService deliveryExpressService;
    @Resource
    private PartnerAddressApi addressApi;

    @Override
    public Long createWaybillAccount(SalesElectronicWaybillAccountCreateReqVO createReqVO) {
        // 校验名称唯一
        validateWaybillAccountNameUnique(createReqVO.getName(), null);
        // 校验快递公司启用
        SalesDeliveryExpressDO express = deliveryExpressService.validateDeliveryExpress(createReqVO.getExpressId());
        // 校验默认寄件地址可见且为商户寄件地址
        validateSenderAddress(createReqVO.getDefaultAddressId());
        // 创建时平台授权 key/密钥 必填
        if (StrUtil.isBlank(createReqVO.getKey()) || StrUtil.isBlank(createReqVO.getSecret())) {
            throw exception(WAYBILL_ACCOUNT_CREDENTIAL_MISSING);
        }
        // 插入
        SalesElectronicWaybillAccountDO waybillAccount = SalesElectronicWaybillAccountConvert.INSTANCE.convert(createReqVO);
        waybillAccount.setExpressCode(express.getCode());
        waybillAccountMapper.insert(waybillAccount);
        return waybillAccount.getId();
    }

    @Override
    public void updateWaybillAccount(SalesElectronicWaybillAccountUpdateReqVO updateReqVO) {
        // 校验存在
        SalesElectronicWaybillAccountDO waybillAccount = validateWaybillAccountExists(updateReqVO.getId());
        // 校验名称唯一
        validateWaybillAccountNameUnique(updateReqVO.getName(), updateReqVO.getId());
        // 快递公司变化时校验启用并更新编码快照
        SalesDeliveryExpressDO express = null;
        if (!updateReqVO.getExpressId().equals(waybillAccount.getExpressId())) {
            express = deliveryExpressService.validateDeliveryExpress(updateReqVO.getExpressId());
        }
        // 默认寄件地址变化时校验可见且为商户寄件地址
        if (!updateReqVO.getDefaultAddressId().equals(waybillAccount.getDefaultAddressId())) {
            validateSenderAddress(updateReqVO.getDefaultAddressId());
        }
        // 更新：凭据空值表示保留原值（置 null，MyBatis-Plus updateById 跳过 null 字段）
        SalesElectronicWaybillAccountDO updateObj = SalesElectronicWaybillAccountConvert.INSTANCE.convert(updateReqVO);
        if (StrUtil.isBlank(updateReqVO.getKey())) {
            updateObj.setKey(null);
        }
        if (StrUtil.isBlank(updateReqVO.getSecret())) {
            updateObj.setSecret(null);
        }
        if (StrUtil.isBlank(updateReqVO.getPartnerKey())) {
            updateObj.setPartnerKey(null);
        }
        if (StrUtil.isBlank(updateReqVO.getPartnerSecret())) {
            updateObj.setPartnerSecret(null);
        }
        if (express != null) {
            updateObj.setExpressCode(express.getCode());
        }
        waybillAccountMapper.updateById(updateObj);
    }

    @Override
    public void deleteWaybillAccount(Long id) {
        // 校验存在
        validateWaybillAccountExists(id);
        // 校验未被有效电子面单引用：避免删除后遗留孤儿面单，导致复打/取消报“账户不存在”
        if (CollUtil.isNotEmpty(waybillMapper.selectListByAccountId(id))) {
            throw exception(WAYBILL_ACCOUNT_REFERENCED);
        }
        // 删除
        waybillAccountMapper.deleteById(id);
    }

    @Override
    public SalesElectronicWaybillAccountDO getWaybillAccount(Long id) {
        return waybillAccountMapper.selectById(id);
    }

    @Override
    public PageResult<SalesElectronicWaybillAccountDO> getWaybillAccountPage(SalesElectronicWaybillAccountPageReqVO pageReqVO) {
        return waybillAccountMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesElectronicWaybillAccountDO> getWaybillAccountListByStatus(Integer status) {
        return waybillAccountMapper.selectListByStatus(status);
    }

    @Override
    public List<SalesElectronicWaybillAccountDO> getWaybillAccountListByExpressIdAndStatus(Long expressId, Integer status) {
        return waybillAccountMapper.selectListByExpressIdAndStatus(expressId, status);
    }

    @Override
    public List<SalesElectronicWaybillAddressRespVO> getSenderAddressList() {
        return addressApi.getSenderAddressList().stream().map(address -> {
            SalesElectronicWaybillAddressRespVO respVO = new SalesElectronicWaybillAddressRespVO();
            respVO.setId(address.getId());
            respVO.setName(address.getName());
            respVO.setMobile(address.getMobile());
            respVO.setAreaId(address.getAreaId());
            respVO.setAreaName(address.getAreaId() != null ? AreaUtils.format(address.getAreaId()) : null);
            respVO.setDetailAddress(address.getDetailAddress());
            return respVO;
        }).toList();
    }

    @Override
    public SalesElectronicWaybillAccountDO validateWaybillAccountForOrder(Long accountId, Long orderExpressId) {
        // 1. 校验账户存在
        SalesElectronicWaybillAccountDO waybillAccount = validateWaybillAccountExists(accountId);
        // 2. 校验账户启用
        if (!CommonStatusEnum.ENABLE.getStatus().equals(waybillAccount.getStatus())) {
            throw exception(WAYBILL_ACCOUNT_STATUS_NOT_ENABLE);
        }
        // 3. 校验关联快递公司启用
        SalesDeliveryExpressDO express = deliveryExpressService.validateDeliveryExpress(waybillAccount.getExpressId());
        // 4. 校验与订单配送快递公司匹配
        if (orderExpressId != null && !waybillAccount.getExpressId().equals(orderExpressId)) {
            throw exception(WAYBILL_ACCOUNT_EXPRESS_NOT_MATCH);
        }
        // 5. 校验模板必填
        if (StrUtil.isBlank(waybillAccount.getTempId())) {
            throw exception(WAYBILL_ACCOUNT_TEMPLATE_REQUIRED);
        }
        // 5.1 校验平台授权 key/密钥（缺一不可下单）
        if (StrUtil.isBlank(waybillAccount.getKey()) || StrUtil.isBlank(waybillAccount.getSecret())) {
            throw exception(WAYBILL_ACCOUNT_CREDENTIAL_MISSING);
        }
        // 6. 校验默认寄件地址（账户级），返回时附带校验
        validateSenderAddress(waybillAccount.getDefaultAddressId());
        // 返回
        return waybillAccount;
    }

    /**
     * 校验默认寄件地址：存在、对当前租户可见、为商户寄件地址、信息完整
     *
     * @param addressId 地址编号
     * @return 地址
     */
    @Override
    public PartnerAddressRespDTO validateSenderAddress(Long addressId) {
        PartnerAddressRespDTO address = addressApi.getAddress(addressId);
        if (address == null) {
            throw exception(WAYBILL_ACCOUNT_ADDRESS_NOT_EXISTS);
        }
        // 必须是商户寄件地址（租户内共享）
        if (!PartnerAddressTypeEnum.isSender(address.getType())) {
            throw exception(WAYBILL_ACCOUNT_ADDRESS_NOT_VISIBLE);
        }
        // 校验信息完整
        if (StrUtil.isBlank(address.getName()) || StrUtil.isBlank(address.getMobile())
                || address.getAreaId() == null || StrUtil.isBlank(address.getDetailAddress())) {
            throw exception(WAYBILL_ACCOUNT_ADDRESS_INCOMPLETE);
        }
        return address;
    }

    private SalesElectronicWaybillAccountDO validateWaybillAccountExists(Long id) {
        SalesElectronicWaybillAccountDO waybillAccount = waybillAccountMapper.selectById(id);
        if (waybillAccount == null) {
            throw exception(WAYBILL_ACCOUNT_NOT_EXISTS);
        }
        return waybillAccount;
    }

    private void validateWaybillAccountNameUnique(String name, Long id) {
        SalesElectronicWaybillAccountDO waybillAccount = waybillAccountMapper.selectByName(name);
        if (waybillAccount == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的账户
        if (id == null || !waybillAccount.getId().equals(id)) {
            throw exception(WAYBILL_ACCOUNT_NAME_DUPLICATE);
        }
    }

}
