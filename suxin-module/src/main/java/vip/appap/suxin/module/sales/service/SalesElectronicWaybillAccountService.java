package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountUpdateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAddressRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 快递100电子面单账户 Service 接口
 *
 * @author 书心软件
 */
public interface SalesElectronicWaybillAccountService {

    /**
     * 创建电子面单账户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWaybillAccount(@Valid SalesElectronicWaybillAccountCreateReqVO createReqVO);

    /**
     * 更新电子面单账户
     *
     * @param updateReqVO 更新信息
     */
    void updateWaybillAccount(@Valid SalesElectronicWaybillAccountUpdateReqVO updateReqVO);

    /**
     * 删除电子面单账户
     *
     * @param id 编号
     */
    void deleteWaybillAccount(Long id);

    /**
     * 获得电子面单账户
     *
     * @param id 编号
     * @return 账户
     */
    SalesElectronicWaybillAccountDO getWaybillAccount(Long id);

    /**
     * 获得电子面单账户分页
     *
     * @param pageReqVO 分页查询
     * @return 账户分页
     */
    PageResult<SalesElectronicWaybillAccountDO> getWaybillAccountPage(SalesElectronicWaybillAccountPageReqVO pageReqVO);

    /**
     * 获取指定状态的账户列表
     *
     * @param status 状态
     * @return 账户列表
     */
    List<SalesElectronicWaybillAccountDO> getWaybillAccountListByStatus(Integer status);

    /**
     * 获取指定快递公司下指定状态的账户列表（电子面单模式下按快递公司匹配加载）
     *
     * @param expressId 快递公司编号
     * @param status    状态
     * @return 账户列表
     */
    List<SalesElectronicWaybillAccountDO> getWaybillAccountListByExpressIdAndStatus(Long expressId, Integer status);

    /**
     * 获得当前租户商户寄件地址列表（type=1，含地区名称，用于账户表单选择）
     *
     * @return 商户寄件地址列表
     */
    List<SalesElectronicWaybillAddressRespVO> getSenderAddressList();

    /**
     * 校验账户可用于下单
     *
     * @param accountId      账户编号
     * @param orderExpressId 订单配送快递公司编号（用于匹配校验，可为 null）
     * @return 校验通过的账户
     */
    SalesElectronicWaybillAccountDO validateWaybillAccountForOrder(Long accountId, Long orderExpressId);

    /**
     * 校验寄件地址：存在、对当前租户可见、为商户寄件地址、信息完整
     *
     * @param addressId 地址编号
     * @return 校验通过的地址
     */
    vip.appap.suxin.module.partner.api.dto.PartnerAddressRespDTO validateSenderAddress(Long addressId);

}
