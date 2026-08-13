package vip.appap.suxin.module.product.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitCreateReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitPageReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitUpdateReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 产品单位 Service 接口
 *
 * @author 书心软件
 */
public interface ProductUnitService {

    /**
     * 创建单位
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUnit(@Valid ProductUnitCreateReqVO createReqVO);

    /**
     * 更新单位
     *
     * @param updateReqVO 更新信息
     */
    void updateUnit(@Valid ProductUnitUpdateReqVO updateReqVO);

    /**
     * 删除单位
     *
     * @param id 编号
     */
    void deleteUnit(Long id);

    /**
     * 获得单位
     *
     * @param id 编号
     * @return 单位
     */
    ProductUnitDO getUnit(Long id);

    /**
     * 获得单位列表
     *
     * @param ids 编号
     * @return 单位列表
     */
    List<ProductUnitDO> getUnitList(Collection<Long> ids);

    /**
     * 获得单位分页
     *
     * @param pageReqVO 分页查询
     * @return 单位分页
     */
    PageResult<ProductUnitDO> getUnitPage(ProductUnitPageReqVO pageReqVO);

    /**
     * 获取指定状态的单位列表
     *
     * @param status 状态
     * @return 单位列表
     */
    List<ProductUnitDO> getUnitListByStatus(Integer status);

}
