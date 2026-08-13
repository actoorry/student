package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderActivityRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderSummaryRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import vip.appap.suxin.module.sales.framework.delivery.core.client.dto.ExpressTrackRespDTO;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singleton;

/**
 * 交易订单【读】 Service 接口
 *
 * @author 书心软件
 */
public interface SalesOrderQueryService {

    // =================== Order ===================

    /**
     * 获得指定编号的交易订单
     *
     * @param id 交易订单编号
     * @return 交易订单
     */
    SalesOrderDO getOrder(Long id);

    /**
     * 获得指定用户，指定的交易订单
     *
     * @param userId 用户编号
     * @param id     交易订单编号
     * @return 交易订单
     */
    SalesOrderDO getOrder(Long userId, Long id);

    /**
     * 获得指定用户，指定活动，指定状态的交易订单
     *
     * @param userId                用户编号
     * @param combinationActivityId 活动编号
     * @param status                订单状态
     * @return 交易订单
     */
    SalesOrderDO getOrderByUserIdAndStatusAndCombination(Long userId, Long combinationActivityId, Integer status);

    /**
     * 获得订单列表
     *
     * @param ids 订单编号数组
     * @return 订单列表
     */
    List<SalesOrderDO> getOrderList(Collection<Long> ids);

    /**
     * 【管理员】获得交易订单分页
     *
     * @param reqVO 分页请求
     * @return 交易订单
     */
    PageResult<SalesOrderDO> getOrderPage(SalesOrderPageReqVO reqVO);

    /**
     * 获得订单统计
     *
     * @param reqVO 请求参数
     * @return 订单统计
     */
    SalesOrderSummaryRespVO getOrderSummary(SalesOrderPageReqVO reqVO);

    /**
     * 【会员】获得交易订单分页
     *
     * @param userId 用户编号
     * @param reqVO  分页请求
     * @return 交易订单
     */
    PageResult<SalesOrderDO> getOrderPage(Long userId, AppSalesOrderPageReqVO reqVO);

    /**
     * 【会员】获得我的活动分页
     *
     * @param userId 用户编号
     * @param reqVO  分页请求
     * @return 我的活动分页
     */
    PageResult<AppSalesOrderActivityRespVO> getMyActivityPage(Long userId, AppSalesOrderActivityPageReqVO reqVO);

    /**
     * 【会员】获得交易订单数量
     *
     * @param userId       用户编号
     * @param status       订单状态。如果为空，则不进行筛选
     * @param commonStatus 评价状态。如果为空，则不进行筛选
     * @return 订单数量
     */
    Long getOrderCount(Long userId, Integer status, Boolean commonStatus);

    /**
     * 【前台】获得订单的物流轨迹
     *
     * @param id     订单编号
     * @param userId 用户编号
     * @return 物流轨迹数组
     */
    List<ExpressTrackRespDTO> getExpressTrackList(Long id, Long userId);

    /**
     * 【后台】获得订单的物流轨迹
     *
     * @param id 订单编号
     * @return 物流轨迹数组
     */
    List<ExpressTrackRespDTO> getExpressTrackList(Long id);

    /**
     * 【会员】在指定活动下，用户购买的商品数量
     *
     * @param userId     用户编号
     * @param activityId 活动编号
     * @param type       订单类型
     * @return 活动商品数量
     */
    int getActivityProductCount(Long userId, Long activityId, SalesOrderTypeEnum type);

    // =================== Order Item ===================

    /**
     * 获得指定用户，指定的交易订单项
     *
     * @param userId 用户编号
     * @param itemId 交易订单项编号
     * @return 交易订单项
     */
    SalesOrderItemDO getOrderItem(Long userId, Long itemId);

    /**
     * 获得交易订单项
     *
     * @param id 交易订单项编号 itemId
     * @return 交易订单项
     */
    SalesOrderItemDO getOrderItem(Long id);

    /**
     * 根据交易订单编号，查询交易订单项
     *
     * @param orderId 交易订单编号
     * @return 交易订单项数组
     */
    default List<SalesOrderItemDO> getOrderItemListByOrderId(Long orderId) {
        return getOrderItemListByOrderId(singleton(orderId));
    }

    /**
     * 根据交易订单编号数组，查询交易订单项
     *
     * @param orderIds 交易订单编号数组
     * @return 交易订单项数组
     */
    List<SalesOrderItemDO> getOrderItemListByOrderId(Collection<Long> orderIds);

    /**
     * Check whether the user owns a paid, non-refunded order item for a product.
     *
     * @param userId user id
     * @param spuId  product SPU id, nullable when skuId is provided
     * @param skuId  product SKU id, nullable when spuId is provided
     * @return whether ownership exists
     */
    boolean hasPaidOrderItemOwnership(Long userId, Long spuId, Long skuId);

}
