package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.service.ProductDisplayConfigService;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderActivityRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderSummaryRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderItemMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderMapper;
import vip.appap.suxin.module.sales.dal.redis.RedisKeyConstants;
import vip.appap.suxin.module.sales.enums.SalesOrderItemAfterSaleStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderRefundStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import vip.appap.suxin.module.sales.framework.delivery.core.client.ExpressClientFactory;
import vip.appap.suxin.module.sales.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import vip.appap.suxin.module.sales.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.ORDER_NOT_FOUND;

/**
 * 交易订单【读】 Service 实现类
 *
 * @author 书心软件
 */
@Service
public class SalesOrderQueryServiceImpl implements SalesOrderQueryService {

    @Resource
    private ExpressClientFactory expressClientFactory;

    @Resource
    private SalesOrderMapper tradeOrderMapper;
    @Resource
    private SalesOrderItemMapper tradeOrderItemMapper;

    @Resource
    private SalesDeliveryExpressService deliveryExpressService;
    @Resource
    private ProductDisplayConfigService productDisplayConfigService;

    @Resource
    private PartnerApi PartnerApi;

    // =================== Order ===================

    @Override
    public SalesOrderDO getOrder(Long id) {
        return tradeOrderMapper.selectById(id);
    }

    @Override
    public SalesOrderDO getOrder(Long userId, Long id) {
        SalesOrderDO order = tradeOrderMapper.selectById(id);
        if (order != null
                && ObjectUtil.notEqual(order.getUserId(), userId)) {
            return null;
        }
        return order;
    }

    @Override
    public SalesOrderDO getOrderByUserIdAndStatusAndCombination(Long userId, Long combinationActivityId, Integer status) {
        return tradeOrderMapper.selectByUserIdAndCombinationActivityIdAndStatus(userId, combinationActivityId, status);
    }

    @Override
    public List<SalesOrderDO> getOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return tradeOrderMapper.selectByIds(ids);
    }

    @Override
    public PageResult<SalesOrderDO> getOrderPage(SalesOrderPageReqVO reqVO) {
        // 根据用户查询条件构建用户编号列表
        Set<Long> userIds = buildQueryConditionUserIds(reqVO);
        if (userIds == null) { // 没查询到用户，说明肯定也没他的订单
            return PageResult.empty();
        }

        // 分页查询
        return tradeOrderMapper.selectPage(reqVO, userIds);
    }

    private Set<Long> buildQueryConditionUserIds(SalesOrderPageReqVO reqVO) {
        // 获得 userId 相关的查询
        Set<Long> userIds = new HashSet<>();
        if (StrUtil.isNotEmpty(reqVO.getUserMobile())) {
            PartnerRespDTO user = PartnerApi.getUserByMobile(reqVO.getUserMobile());
            if (user == null) { // 没查询到用户，说明肯定也没他的订单
                return null;
            }
            userIds.add(user.getId());
        }
        if (StrUtil.isNotEmpty(reqVO.getUserNickname())) {
            List<PartnerRespDTO> users = PartnerApi.getUserListByNickname(reqVO.getUserNickname());
            if (CollUtil.isEmpty(users)) { // 没查询到用户，说明肯定也没他的订单
                return null;
            }
            userIds.addAll(convertSet(users, PartnerRespDTO::getId));
        }
        return userIds;
    }

    @Override
    public SalesOrderSummaryRespVO getOrderSummary(SalesOrderPageReqVO reqVO) {
        // 根据用户查询条件构建用户编号列表
        Set<Long> userIds = buildQueryConditionUserIds(reqVO);
        if (userIds == null) { // 没查询到用户，说明肯定也没他的订单
            return new SalesOrderSummaryRespVO();
        }
        // 查询每个售后状态对应的数量、金额
        List<Map<String, Object>> list = tradeOrderMapper.selectOrderSummaryGroupByRefundStatus(reqVO, userIds);

        SalesOrderSummaryRespVO vo = new SalesOrderSummaryRespVO().setAfterSaleCount(0L).setAfterSalePrice(0L);
        for (Map<String, Object> map : list) {
            Long count = MapUtil.getLong(map, "count", 0L);
            Long price = MapUtil.getLong(map, "price", 0L);
            // 未退款的计入订单，部分退款、全部退款计入售后
            if (SalesOrderRefundStatusEnum.NONE.getStatus().equals(MapUtil.getInt(map, "refundStatus"))) {
                vo.setOrderCount(count).setOrderPayPrice(price);
            } else {
                vo.setAfterSaleCount(vo.getAfterSaleCount() + count).setAfterSalePrice(vo.getAfterSalePrice() + price);
            }
        }
        return vo;
    }

    @Override
    public PageResult<SalesOrderDO> getOrderPage(Long userId, AppSalesOrderPageReqVO reqVO) {
        return tradeOrderMapper.selectPage(reqVO, userId);
    }

    @Override
    public PageResult<AppSalesOrderActivityRespVO> getMyActivityPage(Long userId, AppSalesOrderActivityPageReqVO reqVO) {
        List<Long> categoryIds = productDisplayConfigService.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE);
        if (CollUtil.isEmpty(categoryIds)) {
            return PageResult.empty();
        }
        return tradeOrderItemMapper.selectMyActivityPage(reqVO, userId, categoryIds.get(0),
                List.of(SalesOrderStatusEnum.UNDELIVERED.getStatus(), SalesOrderStatusEnum.DELIVERED.getStatus(),
                        SalesOrderStatusEnum.COMPLETED.getStatus()),
                SalesOrderItemAfterSaleStatusEnum.NONE.getStatus());
    }

    @Override
    public Long getOrderCount(Long userId, Integer status, Boolean commentStatus) {
        return tradeOrderMapper.selectCountByUserIdAndStatus(userId, status, commentStatus);
    }

    @Override
    public List<ExpressTrackRespDTO> getExpressTrackList(Long id, Long userId) {
        // 查询订单
        SalesOrderDO order = tradeOrderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 查询物流
        return getExpressTrackList(order);
    }

    @Override
    public List<ExpressTrackRespDTO> getExpressTrackList(Long id) {
        // 查询订单
        SalesOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 查询物流
        return getExpressTrackList(order);
    }

    @Override
    public int getActivityProductCount(Long userId, Long activityId, SalesOrderTypeEnum type) {
        // 获得订单列表
        List<SalesOrderDO> orders = tradeOrderMapper.selectListByUserIdAndActivityId(userId, activityId, type);
        orders.removeIf(order -> SalesOrderStatusEnum.isCanceled(order.getStatus())); // 过滤掉【已取消】的订单
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }
        // 获得订单项列表
        return tradeOrderItemMapper.selectProductSumByOrderId(convertSet(orders, SalesOrderDO::getId));
    }

    /**
     * 获得订单的物流轨迹
     *
     * @param order 订单
     * @return 物流轨迹
     */
    private List<ExpressTrackRespDTO> getExpressTrackList(SalesOrderDO order) {
        if (order.getLogisticsId() == null) {
            return Collections.emptyList();
        }
        // 查询物流公司
        SalesDeliveryExpressDO express = deliveryExpressService.getDeliveryExpress(order.getLogisticsId());
        if (express == null) {
            throw exception(EXPRESS_NOT_EXISTS);
        }
        // 查询物流轨迹
        return getSelf().getExpressTrackList(express.getCode(), order.getLogisticsNo(), order.getReceiverMobile());
    }

    /**
     * 查询物流轨迹
     * <p>
     * 缓存的目的：考虑及时性要求不高，但是每次调用需要钱
     *
     * @param code           快递公司编码
     * @param logisticsNo    发货快递单号
     * @param receiverMobile 收、寄件人的电话号码
     * @return 物流轨迹
     */
    @Cacheable(cacheNames = RedisKeyConstants.EXPRESS_TRACK, key = "#code + '-' + #logisticsNo + '-' + #receiverMobile",
            unless = "#result == null")
    public List<ExpressTrackRespDTO> getExpressTrackList(String code, String logisticsNo, String receiverMobile) {
        return expressClientFactory.getDefaultExpressClient().getExpressTrackList(new ExpressTrackQueryReqDTO()
                .setExpressCode(code).setLogisticsNo(logisticsNo).setPhone(receiverMobile));
    }

    // =================== Order Item ===================

    @Override
    public SalesOrderItemDO getOrderItem(Long userId, Long itemId) {
        SalesOrderItemDO orderItem = tradeOrderItemMapper.selectById(itemId);
        if (orderItem != null
                && ObjectUtil.notEqual(orderItem.getUserId(), userId)) {
            return null;
        }
        return orderItem;
    }

    @Override
    public SalesOrderItemDO getOrderItem(Long id) {
        return tradeOrderItemMapper.selectById(id);
    }

    @Override
    public List<SalesOrderItemDO> getOrderItemListByOrderId(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return tradeOrderItemMapper.selectListByOrderId(orderIds);
    }

    @Override
    public boolean hasPaidOrderItemOwnership(Long userId, Long spuId, Long skuId) {
        if (userId == null || (spuId == null && skuId == null)) {
            return false;
        }
        Long orderItemId = tradeOrderItemMapper.selectOwnedPaidOrderItemId(userId, spuId, skuId,
                List.of(SalesOrderStatusEnum.UNDELIVERED.getStatus(), SalesOrderStatusEnum.DELIVERED.getStatus(),
                        SalesOrderStatusEnum.COMPLETED.getStatus()),
                SalesOrderItemAfterSaleStatusEnum.NONE.getStatus());
        return orderItemId != null;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private SalesOrderQueryServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
