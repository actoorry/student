package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesLimitConfigPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesLimitConfigSaveReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesLimitConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 客户限制配置 Service 接口
 *
 * @author Wanwan
 */
public interface PartnerSalesLimitConfigService {

    /**
     * 创建客户限制配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomerLimitConfig(@Valid PartnerSalesLimitConfigSaveReqVO createReqVO);

    /**
     * 更新客户限制配置
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomerLimitConfig(@Valid PartnerSalesLimitConfigSaveReqVO updateReqVO);

    /**
     * 删除客户限制配置
     *
     * @param id 编号
     */
    void deleteCustomerLimitConfig(Long id);

    /**
     * 获得客户限制配置
     *
     * @param id 编号
     * @return 客户限制配置
     */
    PartnerSalesLimitConfigDO getCustomerLimitConfig(Long id);

    /**
     * 获得客户限制配置分页
     *
     * @param pageReqVO 分页查询
     * @return 客户限制配置分页
     */
    PageResult<PartnerSalesLimitConfigDO> getCustomerLimitConfigPage(PartnerSalesLimitConfigPageReqVO pageReqVO);

    /**
     * 查询用户对应的配置列表
     *
     * @param type   类型
     * @param userId 用户类型
     */
    List<PartnerSalesLimitConfigDO> getCustomerLimitConfigListByUserId(Integer type, Long userId);

}
