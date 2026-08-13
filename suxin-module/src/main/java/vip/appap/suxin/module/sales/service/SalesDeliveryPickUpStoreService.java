package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryPickUpStoreDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 自提门店 Service 接口
 *
 * @author jason
 */
public interface SalesDeliveryPickUpStoreService {

    /**
     * 创建自提门店
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeliveryPickUpStore(@Valid SalesDeliveryPickUpStoreCreateReqVO createReqVO);

    /**
     * 更新自提门店
     *
     * @param updateReqVO 更新信息
     */
    void updateDeliveryPickUpStore(@Valid SalesDeliveryPickUpStoreUpdateReqVO updateReqVO);

    /**
     * 删除自提门店
     *
     * @param id 编号
     */
    void deleteDeliveryPickUpStore(Long id);

    /**
     * 获得自提门店
     *
     * @param id 编号
     * @return 自提门店
     */
    SalesDeliveryPickUpStoreDO getDeliveryPickUpStore(Long id);

    /**
     * 获得自提门店列表
     *
     * @param ids 编号
     * @return 自提门店列表
     */
    List<SalesDeliveryPickUpStoreDO> getDeliveryPickUpStoreList(Collection<Long> ids);

    /**
     * 获得指定状态的自提门店列表
     *
     * @param status 状态
     * @return 自提门店列表
     */
    List<SalesDeliveryPickUpStoreDO> getDeliveryPickUpStoreListByStatus(Integer status);

    /**
     * 获得自提门店分页
     *
     * @param pageReqVO 分页查询
     * @return 自提门店分页
     */
    PageResult<SalesDeliveryPickUpStoreDO> getDeliveryPickUpStorePage(SalesDeliveryPickUpStorePageReqVO pageReqVO);

    /**
     * 绑定自提店员
     *
     * @param bindReqVO 绑定数据
     */
    void bindDeliveryPickUpStore(SalesDeliveryPickUpBindReqVO bindReqVO);

}
