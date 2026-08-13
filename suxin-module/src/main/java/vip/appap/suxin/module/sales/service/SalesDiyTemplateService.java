package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplateCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplatePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplatePropertyUpdateRequestVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplateUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyTemplateDO;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 装修模板 Service 接口
 *
 * @author owen
 */
public interface SalesDiyTemplateService {

    /**
     * 创建装修模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDiyTemplate(@Valid SalesDiyTemplateCreateReqVO createReqVO);

    /**
     * 创建模板并使用调用方给定的默认页面名称。
     *
     * @param createReqVO 模板信息
     * @param defaultPageNames 默认页面名称
     * @return 模板编号
     */
    Long createDiyTemplateWithDefaultPages(@Valid SalesDiyTemplateCreateReqVO createReqVO,
                                           List<String> defaultPageNames);

    /**
     * 深拷贝模板及其页面，得到一个未启用草稿。
     *
     * @param sourceTemplateId 源模板编号
     * @param targetName 草稿名称
     * @return 新模板编号
     */
    Long copyDiyTemplate(Long sourceTemplateId, String targetName);

    /**
     * 更新装修模板
     *
     * @param updateReqVO 更新信息
     */
    void updateDiyTemplate(@Valid SalesDiyTemplateUpdateReqVO updateReqVO);

    /**
     * 删除装修模板
     *
     * @param id 编号
     */
    void deleteDiyTemplate(Long id);

    /**
     * 获得装修模板
     *
     * @param id 编号
     * @return 装修模板
     */
    SalesDiyTemplateDO getDiyTemplate(Long id);

    /**
     * 获得装修模板分页
     *
     * @param pageReqVO 分页查询
     * @return 装修模板分页
     */
    PageResult<SalesDiyTemplateDO> getDiyTemplatePage(SalesDiyTemplatePageReqVO pageReqVO);

    /**
     * 使用装修模板
     *
     * @param id 编号
     */
    void useDiyTemplate(Long id);

    /**
     * 更新装修模板属性
     *
     * @param updateReqVO 更新信息
     */
    void updateDiyTemplateProperty(SalesDiyTemplatePropertyUpdateRequestVO updateReqVO);

    /**
     * 获取使用中的装修模板
     *
     * @return 装修模板
     */
    SalesDiyTemplateDO getUsedDiyTemplate();

}
