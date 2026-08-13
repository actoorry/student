package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPageCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPagePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPagePropertyUpdateRequestVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPageUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 装修页面 Service 接口
 *
 * @author owen
 */
public interface SalesDiyPageService {

    /**
     * 创建装修页面
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDiyPage(@Valid SalesDiyPageCreateReqVO createReqVO);

    /**
     * 更新装修页面
     *
     * @param updateReqVO 更新信息
     */
    void updateDiyPage(@Valid SalesDiyPageUpdateReqVO updateReqVO);

    /**
     * 删除装修页面
     *
     * @param id 编号
     */
    void deleteDiyPage(Long id);

    /**
     * 获得装修页面
     *
     * @param id 编号
     * @return 装修页面
     */
    SalesDiyPageDO getDiyPage(Long id);

    /**
     * 获得装修页面列表
     *
     * @param ids 编号
     * @return 装修页面列表
     */
    List<SalesDiyPageDO> getDiyPageList(Collection<Long> ids);

    /**
     * 获得装修页面分页
     *
     * @param pageReqVO 分页查询
     * @return 装修页面分页
     */
    PageResult<SalesDiyPageDO> getDiyPagePage(SalesDiyPagePageReqVO pageReqVO);

    /**
     * 更新装修页面属性
     *
     * @param updateReqVO 更新信息
     */
    void updateDiyPageProperty(SalesDiyPagePropertyUpdateRequestVO updateReqVO);

    /**
     * 获得模板所属的页面列表
     *
     * @param templateId 模板编号
     * @return 装修页面列表
     */
    List<SalesDiyPageDO> getDiyPageByTemplateId(Long templateId);

}
