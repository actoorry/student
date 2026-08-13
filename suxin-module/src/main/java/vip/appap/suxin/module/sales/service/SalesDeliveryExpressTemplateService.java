package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressTemplateCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressTemplateDetailRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressTemplatePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressTemplateUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateDO;
import vip.appap.suxin.module.sales.service.bo.SalesDeliveryExpressTemplateRespBO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 快递运费模板 Service 接口
 *
 * @author jason
 */
public interface SalesDeliveryExpressTemplateService {

    /**
     * 创建快递运费模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeliveryExpressTemplate(@Valid SalesDeliveryExpressTemplateCreateReqVO createReqVO);

    /**
     * 更新快递运费模板
     *
     * @param updateReqVO 更新信息
     */
    void updateDeliveryExpressTemplate(@Valid SalesDeliveryExpressTemplateUpdateReqVO updateReqVO);

    /**
     * 删除快递运费模板
     *
     * @param id 编号
     */
    void deleteDeliveryExpressTemplate(Long id);

    /**
     * 获得快递运费模板
     *
     * @param id 编号
     * @return 快递运费模板详情
     */
    SalesDeliveryExpressTemplateDetailRespVO getDeliveryExpressTemplate(Long id);

    /**
     * 获得快递运费模板列表
     *
     * @param ids 编号
     * @return 快递运费模板列表
     */
    List<SalesDeliveryExpressTemplateDO> getDeliveryExpressTemplateList(Collection<Long> ids);

    /**
     * 获得快递运费模板列表
     *
     * @return 快递运费模板列表
     */
    List<SalesDeliveryExpressTemplateDO> getDeliveryExpressTemplateList();

    /**
     * 获得快递运费模板分页
     *
     * @param pageReqVO 分页查询
     * @return 快递运费模板分页
     */
    PageResult<SalesDeliveryExpressTemplateDO> getDeliveryExpressTemplatePage(SalesDeliveryExpressTemplatePageReqVO pageReqVO);

    /**
     * 校验快递运费模板
     *
     * 如果校验不通过，抛出 {@link vip.appap.suxin.framework.common.exception.ServiceException} 异常
     *
     * @param templateId 模板编号
     * @return 快递运费模板
     */
    SalesDeliveryExpressTemplateDO validateDeliveryExpressTemplate(Long templateId);

    /**
     * 校验快递运费模板可用于快递结算（存在、未删除且至少有一条计费规则）
     *
     * 用于商品绑定快递模板时的无环校验端口，避免把不可计算模板绑定到商品。
     *
     * @param templateId 模板编号
     * @return 快递运费模板
     */
    SalesDeliveryExpressTemplateDO validateDeliveryExpressTemplateComputable(Long templateId);

    /**
     * 基于运费模板编号数组和收件人地址区域编号，获取匹配运费模板
     *
     * @param ids    编号列表
     * @param areaId 区域编号
     * @return Map (templateId -> 运费模板设置)
     */
    Map<Long, SalesDeliveryExpressTemplateRespBO> getExpressTemplateMapByIdsAndArea(Collection<Long> ids, Integer areaId);

}
