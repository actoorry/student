package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.framework.common.util.number.MoneyUtils;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.module.partner.api.PartnerAddressApi;
import vip.appap.suxin.module.partner.api.dto.PartnerAddressRespDTO;
import vip.appap.suxin.module.accountant.api.PayOrderApi;
import vip.appap.suxin.module.accountant.api.dto.PayOrderCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayOrderRespDTO;
import vip.appap.suxin.module.accountant.api.PayRefundApi;
import vip.appap.suxin.module.accountant.api.dto.PayRefundCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayRefundRespDTO;
import vip.appap.suxin.module.accountant.enums.PayOrderStatusEnum;
import vip.appap.suxin.module.accountant.enums.PayRefundStatusEnum;
import vip.appap.suxin.module.partner.dal.dataobject.SalesCartDO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.partner.service.PartnerMemberService;
import vip.appap.suxin.module.product.api.ProductCommentApi;
import vip.appap.suxin.module.product.api.dto.ProductCommentCreateReqDTO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.sales.api.SalesCombinationRecordApi;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationRecordRespDTO;
import vip.appap.suxin.module.sales.enums.SalesCombinationRecordStatusEnum;
import vip.appap.suxin.module.system.api.SocialClientApi;
import vip.appap.suxin.module.system.api.dto.SocialWxaSubscribeMessageSendReqDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderDeliveryReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderElectronicWaybillCancelReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderElectronicWaybillDeliveryReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderRemarkReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderUpdateAddressReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderUpdatePriceReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderCreateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderSettlementReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderSettlementRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderItemCommentCreateReqVO;
import vip.appap.suxin.module.sales.convert.SalesElectronicWaybillConvert;
import vip.appap.suxin.module.sales.convert.SalesOrderConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryPickUpStoreDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderItemMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderMapper;
import vip.appap.suxin.module.sales.dal.redis.no.SalesNoRedisDAO;
import vip.appap.suxin.module.sales.enums.SalesDeliveryTypeEnum;
import vip.appap.suxin.module.sales.enums.*;
import vip.appap.suxin.module.sales.framework.order.config.SalesOrderProperties;
import vip.appap.suxin.module.sales.framework.order.core.annotations.SalesOrderLog;
import vip.appap.suxin.module.sales.framework.order.core.utils.SalesOrderLogUtils;
import vip.appap.suxin.module.sales.framework.waybill.core.client.SalesElectronicWaybillClient;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillCancelReqDTO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillCancelRespDTO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillManInfoDTO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillOrderReqDTO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillOrderRespDTO;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.SalesElectronicWaybillReprintReqDTO;
import vip.appap.suxin.module.sales.service.bo.SalesOrderMessageWhenDeliveryOrderReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils.minusTime;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;
import static vip.appap.suxin.framework.web.core.util.WebFrameworkUtils.getTerminal;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.*;
import static vip.appap.suxin.module.sales.enums.MessageTemplateConstants.WXA_ORDER_DELIVERY;

/**
 * 交易订单【写】Service 实现类
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Service
@Slf4j
public class SalesOrderUpdateServiceImpl implements SalesOrderUpdateService {

    @Resource
    private SalesOrderMapper tradeOrderMapper;
    @Resource
    private SalesOrderItemMapper tradeOrderItemMapper;
    @Resource
    private SalesNoRedisDAO tradeNoRedisDAO;

    @Resource
    private List<SalesOrderHandler> tradeOrderHandlers;

    @Resource
    private SalesCartService salesCartService;

    @Resource
    private SalesPriceService tradePriceService;
    @Resource
    private SalesDeliveryExpressService deliveryExpressService;
    @Resource
    private SalesMessageService tradeMessageService;
    @Resource
    private SalesDeliveryPickUpStoreService pickUpStoreService;

    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private PartnerAddressApi addressApi;
    @Resource
    private ProductCommentApi productCommentApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    public SocialClientApi socialClientApi;
    @Resource
    public PayRefundApi payRefundApi;
    @Resource
    private SalesCombinationRecordApi combinationRecordApi;
    @Resource
    private PartnerMemberService partnerMemberService;

    @Resource
    private SalesElectronicWaybillAccountService waybillAccountService;
    @Resource
    private SalesElectronicWaybillService waybillService;
    @Resource
    private SalesElectronicWaybillClient waybillClient;

    @Resource
    private SalesOrderProperties tradeOrderProperties;

    // =================== Order ===================

    @Override
    public AppSalesOrderSettlementRespVO settlementOrder(Long userId, AppSalesOrderSettlementReqVO settlementReqVO) {
        // 1. 获得收货地址
        PartnerAddressRespDTO address = getAddress(userId, settlementReqVO.getAddressId());
        if (address != null) {
            settlementReqVO.setAddressId(address.getId());
        }

        // 2. 计算价格
        SalesPriceCalculateRespBO calculateRespBO = calculatePrice(userId, settlementReqVO);
        List<Integer> availableDeliveryTypes = getCommonDeliveryTypes(calculateRespBO);

        // 3. 拼接返回
        return SalesOrderConvert.INSTANCE.convert(calculateRespBO, address)
                .setAvailableDeliveryTypes(availableDeliveryTypes);
    }

    /**
     * 获得用户地址
     *
     * @param userId    用户编号
     * @param addressId 地址编号
     * @return 地址
     */
    private PartnerAddressRespDTO getAddress(Long userId, Long addressId) {
        if (addressId != null) {
            return addressApi.getAddress(addressId, userId);
        }
        return addressApi.getDefaultAddress(userId);
    }

    /**
     * 计算订单价格
     *
     * @param userId          用户编号
     * @param settlementReqVO 结算信息
     * @return 订单价格
     */
    private SalesPriceCalculateRespBO calculatePrice(Long userId, AppSalesOrderSettlementReqVO settlementReqVO) {
        Set<Long> cartIds = convertSet(settlementReqVO.getItems(),
                AppSalesOrderSettlementReqVO.Item::getCartId);
        List<SalesCartDO> cartList = CollUtil.isEmpty(cartIds)
                ? List.of() : salesCartService.getCartList(userId, cartIds);
        if (CollUtil.isNotEmpty(cartIds) && cartList.size() != cartIds.size()) {
            throw exception(CART_ITEM_NOT_FOUND);
        }

        SalesPriceCalculateReqBO calculateReqBO =
                SalesOrderConvert.INSTANCE.convert(userId, settlementReqVO, cartList);
        calculateReqBO.getItems().forEach(item -> Assert.isTrue(item.getSelected(), // 防御性编程，保证都是选中的
                "商品({}) 未设置为选中", item.getSkuId()));
        return tradePriceService.calculateOrderPrice(calculateReqBO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.MEMBER_CREATE)
    public SalesOrderDO createOrder(Long userId, AppSalesOrderCreateReqVO createReqVO) {
        // 1.1 价格计算
        SalesPriceCalculateRespBO calculateRespBO = calculatePrice(userId, createReqVO);
        defaultDeliveryTypeIfNecessary(createReqVO, calculateRespBO);
        if (createReqVO.getDeliveryType() == null) {
            throw exception(ORDER_DELIVERY_TYPE_REQUIRED);
        }
        // 缺省为唯一共同方式时，需要用最终配送方式重新计价和校验。
        calculateRespBO = calculatePrice(userId, createReqVO);
        // 1.2 构建订单
        SalesOrderDO order = buildTradeOrder(userId, createReqVO, calculateRespBO);
        List<SalesOrderItemDO> orderItems = buildTradeOrderItems(order, calculateRespBO);

        // 2. 订单创建前的逻辑
        tradeOrderHandlers.forEach(handler -> handler.beforeOrderCreate(order, orderItems));

        // 3. 保存订单
        tradeOrderMapper.insert(order);
        orderItems.forEach(orderItem -> orderItem.setOrderId(order.getId()));
        tradeOrderItemMapper.insertBatch(orderItems);

        // 4. 订单创建后的逻辑
        afterCreateTradeOrder(order, orderItems, createReqVO);
        return order;
    }

    private void defaultDeliveryTypeIfNecessary(AppSalesOrderCreateReqVO createReqVO,
                                                SalesPriceCalculateRespBO calculateRespBO) {
        if (createReqVO.getDeliveryType() != null || CollUtil.isEmpty(calculateRespBO.getItems())) {
            return;
        }
        List<Integer> common = getCommonDeliveryTypes(calculateRespBO);
        if (common.size() == 1) {
            createReqVO.setDeliveryType(common.get(0));
        }
    }

    private List<Integer> getCommonDeliveryTypes(SalesPriceCalculateRespBO calculateRespBO) {
        List<SalesPriceCalculateRespBO.OrderItem> selectedItems = calculateRespBO.getItems().stream()
                .filter(item -> Boolean.TRUE.equals(item.getSelected()))
                .toList();
        if (selectedItems.isEmpty()) {
            throw exception(ORDER_DELIVERY_TYPES_INCOMPATIBLE);
        }
        LinkedHashSet<Integer> common = new LinkedHashSet<>(
                CollUtil.emptyIfNull(selectedItems.get(0).getDeliveryTypes()));
        for (int i = 1; i < selectedItems.size(); i++) {
            common.retainAll(CollUtil.emptyIfNull(selectedItems.get(i).getDeliveryTypes()));
        }
        common.removeIf(type -> !Arrays.asList(SalesDeliveryTypeEnum.ARRAYS).contains(type));
        if (common.isEmpty()) {
            throw exception(ORDER_DELIVERY_TYPES_INCOMPATIBLE);
        }
        return common.stream().sorted().toList();
    }

    private SalesOrderDO buildTradeOrder(Long userId, AppSalesOrderCreateReqVO createReqVO,
                                         SalesPriceCalculateRespBO calculateRespBO) {
        SalesOrderDO order = SalesOrderConvert.INSTANCE.convert(userId, createReqVO, calculateRespBO);
        order.setType(calculateRespBO.getType());
        order.setNo(tradeNoRedisDAO.generate(SalesNoRedisDAO.TRADE_ORDER_NO_PREFIX));
        order.setStatus(SalesOrderStatusEnum.UNPAID.getStatus());
        order.setRefundStatus(SalesOrderRefundStatusEnum.NONE.getStatus());
        order.setProductCount(getSumValue(calculateRespBO.getItems(), SalesPriceCalculateRespBO.OrderItem::getCount, Integer::sum));
        order.setUserIp(getClientIP()).setTerminal(getTerminal());
        // 使用 + 赠送优惠券
        order.setGiveCouponTemplateCounts(calculateRespBO.getGiveCouponTemplateCounts());
        // 支付 + 退款信息
        order.setAdjustPrice(0).setPayStatus(false);
        order.setRefundStatus(SalesOrderRefundStatusEnum.NONE.getStatus()).setRefundPrice(0);
        // 物流信息
        order.setDeliveryType(createReqVO.getDeliveryType());
        if (Objects.equals(createReqVO.getDeliveryType(), SalesDeliveryTypeEnum.EXPRESS.getType())) {
            PartnerAddressRespDTO address = addressApi.getAddress(createReqVO.getAddressId(), userId);
            Assert.notNull(address, "地址({}) 不能为空", createReqVO.getAddressId()); // 价格计算时，已经计算
            order.setReceiverName(address.getName()).setReceiverMobile(address.getMobile())
                    .setReceiverAreaId(address.getAreaId()).setReceiverDetailAddress(address.getDetailAddress());
        } else if (Objects.equals(createReqVO.getDeliveryType(), SalesDeliveryTypeEnum.PICK_UP.getType())) {
            order.setReceiverName(createReqVO.getReceiverName()).setReceiverMobile(createReqVO.getReceiverMobile());
            order.setPickUpVerifyCode(RandomUtil.randomNumbers(8)); // 随机一个核销码，长度为 8 位
        }
        return order;
    }

    private List<SalesOrderItemDO> buildTradeOrderItems(SalesOrderDO tradeOrderDO,
                                                        SalesPriceCalculateRespBO calculateRespBO) {
        return SalesOrderConvert.INSTANCE.convertList(tradeOrderDO, calculateRespBO);
    }

    /**
     * 订单创建后，执行后置逻辑
     * <p>
     * 例如说：优惠劵的扣减、积分的扣减、支付单的创建等等
     *
     * @param order       订单
     * @param orderItems  订单项
     * @param createReqVO 创建订单请求
     */
    private void afterCreateTradeOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems,
                                       AppSalesOrderCreateReqVO createReqVO) {
        // 1. 执行订单创建后置处理器
        tradeOrderHandlers.forEach(handler -> handler.afterOrderCreate(order, orderItems));

        // 2. 删除本次成功下单的购物车项
        Set<Long> cartIds = convertSet(createReqVO.getItems(), AppSalesOrderSettlementReqVO.Item::getCartId);
        if (CollUtil.isNotEmpty(cartIds)) {
            salesCartService.deleteCart(order.getUserId(), cartIds);
        }

        // 3. 生成预支付
        // 特殊情况：积分兑换时，可能支付金额为零
        if (order.getPayPrice() > 0) {
            createPayOrder(order, orderItems);
        }

        // 4. 插入订单日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), null, order.getStatus());

        // TODO @LeeYan9: 是可以思考下, 订单的营销优惠记录, 应该记录在哪里, 微信讨论起来!
    }

    private void createPayOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        // 创建支付单，用于后续的支付
        PayOrderCreateReqDTO payOrderCreateReqDTO = SalesOrderConvert.INSTANCE.convert(
                order, orderItems, tradeOrderProperties);
        Long payOrderId = payOrderApi.createOrder(payOrderCreateReqDTO);

        // 更新到交易单上
        tradeOrderMapper.updateById(new SalesOrderDO().setId(order.getId()).setPayOrderId(payOrderId));
        order.setPayOrderId(payOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.MEMBER_PAY)
    public void updateOrderPaid(Long id, Long payOrderId) {
        // 1.1 校验订单是否存在
        SalesOrderDO order = validateOrderExists(id);
        // 1.2 校验订单已支付
        if (!SalesOrderStatusEnum.isUnpaid(order.getStatus()) || order.getPayStatus()) {
            // 特殊：支付单号相同，直接返回，说明重复回调
            if (ObjectUtil.equals(order.getPayOrderId(), payOrderId)) {
                log.warn("[updateOrderPaid][order({}) 已支付，且支付单号相同({})，直接返回]", order, payOrderId);
                List<SalesOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(id);
                partnerMemberService.activateMemberByPaidOrderItems(order.getUserId(), orderItems);
                return;
            }
            log.error("[updateOrderPaid][order({}) 支付单不匹配({})，请进行处理！order 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(order));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }

        // 2. 校验支付订单的合法性
        PayOrderRespDTO payOrder = validatePayOrderPaid(order, payOrderId);

        List<SalesOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(id);
        LocalDateTime payTime = LocalDateTime.now();
        boolean onlineDelivery = Objects.equals(order.getDeliveryType(), SalesDeliveryTypeEnum.ONLINE.getType());
        boolean autoDelivery = Objects.equals(order.getDeliveryType(), SalesDeliveryTypeEnum.AUTO.getType());
        boolean autoFulfillmentOrder = isAutoFulfillmentOrder(orderItems);
        Integer targetStatus = autoDelivery && autoFulfillmentOrder ? SalesOrderStatusEnum.COMPLETED.getStatus()
                : (onlineDelivery ? SalesOrderStatusEnum.DELIVERED.getStatus()
                : SalesOrderStatusEnum.UNDELIVERED.getStatus());

        // 3. 更新 SalesOrderDO 状态为已支付
        SalesOrderDO updateObj = new SalesOrderDO().setStatus(targetStatus).setPayStatus(true)
                .setPayTime(payTime).setPayChannelCode(payOrder.getChannelCode());
        if (autoDelivery && autoFulfillmentOrder) {
            updateObj.setFinishTime(payTime);
        } else if (onlineDelivery) {
            updateObj.setDeliveryTime(payTime);
        }
        int updateCount = tradeOrderMapper.updateByIdAndStatus(id, order.getStatus(),
                updateObj);
        if (updateCount == 0) {
            throw exception(ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }

        // 4. 执行 SalesOrderHandler 的后置处理
        partnerMemberService.activateMemberByPaidOrderItems(order.getUserId(), orderItems);
        tradeOrderHandlers.forEach(handler -> handler.afterPayOrder(order, orderItems));

        // 5. 记录订单日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), targetStatus);
        SalesOrderLogUtils.setUserInfo(order.getUserId(), UserTypeEnum.MEMBER.getValue());
    }

    private boolean isAutoFulfillmentOrder(List<SalesOrderItemDO> orderItems) {
        if (CollUtil.isEmpty(orderItems)) {
            return false;
        }
        return orderItems.stream()
                .allMatch(orderItem -> {
                    ProductSpuRespDTO spu = productSpuApi.getSpu(orderItem.getSpuId());
                    if (spu == null) {
                        return false;
                    }
                    return ProductTypeEnum.isVirtualLike(spu.getType());
                });
    }

    @Override
    public void syncOrderPayStatusQuietly(Long id, Long payOrderId) {
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            return;
        }
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            return;
        }
        try {
            getSelf().updateOrderPaid(id, payOrderId);
        } catch (Throwable e) {
            log.warn("[syncOrderPayStatusQuietly][id({}) payOrderId({}) 同步支付状态失败]", id, payOrderId, e);
        }
    }

    /**
     * 校验支付订单的合法性
     *
     * @param order      交易订单
     * @param payOrderId 支付订单编号
     * @return 支付订单
     */
    private PayOrderRespDTO validatePayOrderPaid(SalesOrderDO order, Long payOrderId) {
        // 1. 校验支付单是否存在
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 不存在，请进行处理！]", order.getId(), payOrderId);
            throw exception(ORDER_NOT_FOUND);
        }

        // 2.1 校验支付单已支付
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 未支付，请进行处理！payOrder 数据是：{}]",
                    order.getId(), payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        // 2.2 校验支付金额一致
        if (ObjectUtil.notEqual(payOrder.getPrice(), order.getPayPrice())) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 支付金额不匹配，请进行处理！order 数据是：{}，payOrder 数据是：{}]",
                    order.getId(), payOrderId, JsonUtils.toJsonString(order), JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 2.2 校验支付订单匹配（二次）
        if (ObjectUtil.notEqual(payOrder.getMerchantOrderId(), order.getId().toString())) {
            log.error("[validatePayOrderPaid][order({}) 支付单不匹配({})，请进行处理！payOrder 数据是：{}]",
                    order.getId(), payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.ADMIN_DELIVERY)
    public void deliveryOrder(SalesOrderDeliveryReqVO deliveryReqVO) {
        // 1.1 校验并获得交易订单（可发货）
        SalesOrderDO order = validateOrderDeliverable(deliveryReqVO.getId());
        // 1.2 校验 deliveryType 是否为快递，是快递才可以发货
        if (ObjectUtil.notEqual(order.getDeliveryType(), SalesDeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS);
        }
        // 1.3 拒绝手工单号覆盖有效电子面单
        if (waybillService.getValidByOrderId(deliveryReqVO.getId()) != null) {
            throw exception(WAYBILL_ORDER_DELIVERY_OVERWRITE_FORBIDDEN);
        }

        // 2. 更新订单为已发货
        SalesOrderDO updateOrderObj = new SalesOrderDO();
        // 2.1 快递发货
        SalesDeliveryExpressDO express = null;
        if (ObjectUtil.notEqual(deliveryReqVO.getLogisticsId(), SalesOrderDO.LOGISTICS_ID_NULL)) {
            express = deliveryExpressService.validateDeliveryExpress(deliveryReqVO.getLogisticsId());
            // 实际快递必须填写非空运单号（trim 后），不允许空白单号发货
            if (StrUtil.isBlank(deliveryReqVO.getLogisticsNo())) {
                throw exception(ORDER_DELIVERY_FAIL_LOGISTICS_NO_BLANK);
            }
            updateOrderObj.setLogisticsId(deliveryReqVO.getLogisticsId())
                    .setLogisticsNo(deliveryReqVO.getLogisticsNo().trim());
        } else {
            // 2.2 无需发货：保留 logisticsId=0 与空单号，不查询快递公司
            updateOrderObj.setLogisticsId(0L).setLogisticsNo("");
        }
        // 执行更新
        updateOrderObj.setStatus(SalesOrderStatusEnum.DELIVERED.getStatus()).setDeliveryTime(LocalDateTime.now());
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), updateOrderObj);
        if (updateCount == 0) {
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }

        // 3. 记录订单日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), SalesOrderStatusEnum.DELIVERED.getStatus(),
                MapUtil.<String, Object>builder().put("expressName", express != null ? express.getName() : "")
                        .put("logisticsNo", express != null ? deliveryReqVO.getLogisticsNo() : "").build());

        // 4.1 发送站内信
        tradeMessageService.sendMessageWhenDeliveryOrder(new SalesOrderMessageWhenDeliveryOrderReqBO()
                .setOrderId(order.getId()).setUserId(order.getUserId()).setMessage(null));
        // 4.2 发送订阅消息
        getSelf().sendDeliveryOrderMessage(order, deliveryReqVO.getLogisticsNo());

        // 5. 处理订单发货后逻辑
        order.setLogisticsId(updateOrderObj.getLogisticsId()).setLogisticsNo(updateOrderObj.getLogisticsNo())
                .setStatus(updateOrderObj.getStatus()).setDeliveryTime(updateOrderObj.getDeliveryTime());
        tradeOrderHandlers.forEach(handler -> handler.afterDeliveryOrder(order));
    }

    @Async
    public void sendDeliveryOrderMessage(SalesOrderDO order, String logisticsNo) {
        // 构建并发送模版消息
        Long orderId = order.getId();
        socialClientApi.sendWxaSubscribeMessage(new SocialWxaSubscribeMessageSendReqDTO()
                .setUserId(order.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue())
                .setTemplateTitle(WXA_ORDER_DELIVERY)
                .setPage("pages/order/detail?id=" + orderId) // 订单详情页
                .addMessage("character_string3", String.valueOf(orderId)) // 订单编号
                .addMessage("phrase6", SalesOrderStatusEnum.DELIVERED.getName()) // 订单状态
                .addMessage("date4", LocalDateTimeUtil.formatNormal(LocalDateTime.now()))// 发货时间
                .addMessage("character_string5", StrUtil.blankToDefault(logisticsNo, "-")) // 快递单号
                .addMessage("thing9", order.getReceiverDetailAddress())); // 收货地址
    }

    // =================== 电子面单发货 ===================

    @Override
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.ADMIN_DELIVERY)
    public void deliveryOrderByElectronicWaybill(SalesOrderElectronicWaybillDeliveryReqVO reqVO) {
        // 1. 校验并获得交易订单（可发货）
        SalesOrderDO order = validateOrderDeliverable(reqVO.getId());
        // 2. 校验 deliveryType 是否为快递
        if (ObjectUtil.notEqual(order.getDeliveryType(), SalesDeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(WAYBILL_ORDER_DELIVERY_TYPE_NOT_EXPRESS);
        }
        // 3. 校验电子面单账户可用于下单（含快递公司启用与匹配）
        SalesElectronicWaybillAccountDO account = waybillAccountService.validateWaybillAccountForOrder(
                reqVO.getAccountId(), order.getLogisticsId());
        // 4. 幂等保护：订单存在有效电子面单则拒绝重复下单
        if (waybillService.getValidByOrderId(order.getId()) != null) {
            throw exception(WAYBILL_ORDER_VALID_EXISTS);
        }
        SalesElectronicWaybillDO oldestUnknownWaybill = waybillService.getOldestUnknownByOrderId(order.getId());
        if (isWaybillUnknownRetryExpired(oldestUnknownWaybill, LocalDateTime.now())) {
            throw exception(WAYBILL_ORDER_RESULT_UNKNOWN);
        }
        // 5. 解析并校验寄件地址（请求指定或账户默认）
        PartnerAddressRespDTO senderAddress = resolveSenderAddress(account, reqVO.getAddressId());
        // 6. 校验收件人快照完整
        validateReceiverComplete(order);
        // 7. 调用快递100下单
        SalesElectronicWaybillOrderReqDTO orderReqDTO = buildWaybillOrderReq(order, account, senderAddress, reqVO.getPrintType());
        SalesElectronicWaybillOrderRespDTO orderResp;
        try {
            orderResp = waybillClient.order(account, orderReqDTO);
        } catch (ServiceException e) {
            // 网络/解析异常可能已被第三方受理，单独记录待确认状态
            if (WAYBILL_ORDER_RESULT_UNKNOWN.getCode().equals(e.getCode())) {
                getSelf().recordWaybillUnknown(order, account, String.valueOf(e.getCode()), e.getMessage());
            } else {
                getSelf().recordWaybillFailure(order, account, String.valueOf(e.getCode()), e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            // 网络/解析异常：记录脱敏失败，订单保持不变
            log.warn("[deliveryOrderByElectronicWaybill][order({}) 调用快递100下单异常]", order.getId(), e);
            getSelf().recordWaybillFailure(order, account, String.valueOf(WAYBILL_ORDER_API_ERROR.getCode()),
                    WAYBILL_ORDER_API_ERROR.getMsg());
            throw exception(WAYBILL_ORDER_API_ERROR);
        }
        // 8. 第三方成功：同一事务保存记录 + 更新物流字段 + 订单状态流转
        try {
            getSelf().deliverOrderWithWaybill(order, account, senderAddress, orderResp, reqVO.getPrintType());
        } catch (RuntimeException e) {
            // 第三方已明确成功但本地事务回滚，保留对账记录，禁止 48 小时后盲目重下单
            if (waybillService.getValidByOrderId(order.getId()) == null) {
                getSelf().recordWaybillUnknown(order, account,
                        String.valueOf(WAYBILL_ORDER_PERSIST_FAIL.getCode()), WAYBILL_ORDER_PERSIST_FAIL.getMsg());
            }
            throw e;
        }
    }

    static boolean isWaybillUnknownRetryExpired(SalesElectronicWaybillDO latestWaybill, LocalDateTime now) {
        if (latestWaybill == null
                || !SalesElectronicWaybillStatusEnum.UNKNOWN.getStatus().equals(latestWaybill.getStatus())) {
            return false;
        }
        return latestWaybill.getCreateTime() == null || !now.isBefore(latestWaybill.getCreateTime().plusHours(48));
    }

    /**
     * 电子面单下单成功后的落库与订单发货（同一事务）
     *
     * @param order         订单
     * @param account       电子面单账户
     * @param senderAddress 寄件地址
     * @param orderResp     快递100下单结果
     * @param printType     打印类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrderWithWaybill(SalesOrderDO order, SalesElectronicWaybillAccountDO account,
                                        PartnerAddressRespDTO senderAddress, SalesElectronicWaybillOrderRespDTO orderResp,
                                        String printType) {
        // 1. 事务内并发保护：订单仍待发货、且无有效电子面单
        SalesOrderDO current = tradeOrderMapper.selectById(order.getId());
        if (current == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(current.getStatus(), SalesOrderStatusEnum.UNDELIVERED.getStatus())
                || waybillService.getValidByOrderId(order.getId()) != null) {
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }
        // 2. 保存电子面单记录（valid_flag=1；唯一索引 (order_id, valid_flag) 兜底并发）
        SalesElectronicWaybillDO waybill = buildWaybillDO(order, account, senderAddress, orderResp, printType);
        try {
            waybillService.insertWaybill(waybill);
        } catch (DuplicateKeyException e) {
            // 并发重复下单：另一事务已保存有效面单
            log.warn("[deliverOrderWithWaybill][order({}) 存在有效电子面单，拒绝重复下单]", order.getId());
            throw exception(WAYBILL_ORDER_VALID_EXISTS);
        }
        // 3. 更新订单物流字段并完成发货状态流转
        SalesDeliveryExpressDO express = deliveryExpressService.validateDeliveryExpress(account.getExpressId());
        SalesOrderDO updateObj = new SalesOrderDO()
                .setLogisticsId(account.getExpressId())
                .setLogisticsNo(orderResp.getKuaidinum())
                .setStatus(SalesOrderStatusEnum.DELIVERED.getStatus())
                .setDeliveryTime(LocalDateTime.now());
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(),
                SalesOrderStatusEnum.UNDELIVERED.getStatus(), updateObj);
        if (updateCount == 0) {
            // 本地持久化失败/状态被并发修改：面单记录随事务一并回滚，订单保持待发货。
            // 告警留痕：快递100 已按业务订单号幂等下单（reorder=false），该 orderId 实际已占用，
            // 恢复路径为人工重试电子面单发货（reorder=false 会取回已存在的面单，不会重复下单）。
            log.error("[deliverOrderWithWaybill][order({}) 快递100下单成功但本地发货持久化失败，"
                            + "订单状态非待发货或物流字段更新被并发修改，订单保持待发货，请人工重试发货对账]",
                    order.getId());
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }
        // 4. 记录订单日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), SalesOrderStatusEnum.DELIVERED.getStatus(),
                MapUtil.<String, Object>builder().put("expressName", express.getName())
                        .put("logisticsNo", orderResp.getKuaidinum()).build());
        // 5. 发送站内信与订阅消息
        tradeMessageService.sendMessageWhenDeliveryOrder(new SalesOrderMessageWhenDeliveryOrderReqBO()
                .setOrderId(order.getId()).setUserId(order.getUserId()).setMessage(null));
        getSelf().sendDeliveryOrderMessage(order, orderResp.getKuaidinum());
        // 6. 处理订单发货后逻辑
        order.setLogisticsId(updateObj.getLogisticsId()).setLogisticsNo(updateObj.getLogisticsNo())
                .setStatus(updateObj.getStatus()).setDeliveryTime(updateObj.getDeliveryTime());
        tradeOrderHandlers.forEach(handler -> handler.afterDeliveryOrder(order));
    }

    /**
     * 记录电子面单下单失败（独立事务，确保失败记录落库而订单状态不变）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWaybillFailure(SalesOrderDO order, SalesElectronicWaybillAccountDO account,
                                     String failCode, String failMessage) {
        recordWaybillResult(order, account, SalesElectronicWaybillStatusEnum.FAILED, failCode, failMessage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWaybillUnknown(SalesOrderDO order, SalesElectronicWaybillAccountDO account,
                                     String failCode, String failMessage) {
        recordWaybillResult(order, account, SalesElectronicWaybillStatusEnum.UNKNOWN, failCode, failMessage);
    }

    private void recordWaybillResult(SalesOrderDO order, SalesElectronicWaybillAccountDO account,
                                     SalesElectronicWaybillStatusEnum status, String failCode, String failMessage) {
        try {
            SalesElectronicWaybillDO waybill = SalesElectronicWaybillDO.builder()
                    .orderId(order.getId()).orderNo(order.getNo())
                    .accountId(account.getId()).expressId(account.getExpressId()).expressCode(account.getExpressCode())
                    .status(status.getStatus())
                    .validFlag(null)
                    .thirdOrderId(order.getNo())
                    .failCode(failCode).failMessage(sanitizeWaybillMessage(failMessage))
                    .build();
            waybillService.insertWaybill(waybill);
        } catch (Exception e) {
            log.error("[recordWaybillResult][order({}) 记录电子面单结果({})异常]", order.getId(), status, e);
        }
    }

    @Override
    public SalesElectronicWaybillRespVO reprintElectronicWaybill(Long orderId) {
        // 1. 校验订单存在
        validateOrderExists(orderId);
        // 2. 校验有效电子面单
        SalesElectronicWaybillDO waybill = waybillService.getValidByOrderId(orderId);
        if (waybill == null) {
            throw exception(WAYBILL_ORDER_STATUS_NOT_VALID);
        }
        // 3. 复打不创建新单号：优先返回已保存的可打印面单短链；
        //    若缺少短链但存在 taskId，尝试官方复打接口恢复（不创建新单号、不新建记录）
        if (StrUtil.isBlank(waybill.getLabelUrl()) && StrUtil.isNotBlank(waybill.getTaskId())) {
            SalesElectronicWaybillAccountDO account = waybillAccountService.getWaybillAccount(waybill.getAccountId());
            if (account != null) {
                try {
                    SalesElectronicWaybillOrderRespDTO resp = waybillClient.reprint(account,
                            new SalesElectronicWaybillReprintReqDTO().setTaskId(waybill.getTaskId()));
                    if (resp != null && StrUtil.isNotBlank(resp.getLabel())) {
                        waybillService.updateLabelUrl(waybill.getId(), resp.getLabel());
                        waybill.setLabelUrl(resp.getLabel());
                    }
                } catch (ServiceException e) {
                    throw exception(WAYBILL_ORDER_REPRINT_FAIL, e.getMessage());
                }
            }
        }
        // 4. 转换并补充快递公司名称
        SalesElectronicWaybillRespVO waybillVO = SalesElectronicWaybillConvert.INSTANCE.convert(waybill);
        SalesDeliveryExpressDO express = deliveryExpressService.getDeliveryExpress(waybill.getExpressId());
        waybillVO.setExpressName(express != null ? express.getName() : null);
        return waybillVO;
    }

    @Override
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.ADMIN_CANCEL_WAYBILL)
    public void cancelElectronicWaybill(Long userId, SalesOrderElectronicWaybillCancelReqVO reqVO) {
        // 1. 校验订单存在
        SalesOrderDO order = validateOrderExists(reqVO.getId());
        // 2. 校验有效电子面单
        SalesElectronicWaybillDO waybill = waybillService.getValidByOrderId(order.getId());
        if (waybill == null) {
            throw exception(WAYBILL_ORDER_CANCEL_FAIL_STATUS_NOT_VALID);
        }
        // 3. 校验订单尚未实际交运（仅已发货未收货可取消）
        if (!SalesOrderStatusEnum.isDelivered(order.getStatus())) {
            throw exception(WAYBILL_ORDER_CANCEL_FAIL_STATUS_NOT_DELIVERED);
        }
        // 4. 调用快递100取消
        SalesElectronicWaybillAccountDO account = waybillAccountService.getWaybillAccount(waybill.getAccountId());
        if (account == null) {
            throw exception(WAYBILL_ACCOUNT_NOT_EXISTS);
        }
        SalesElectronicWaybillCancelReqDTO cancelReqDTO = new SalesElectronicWaybillCancelReqDTO()
                .setKuaidicom(waybill.getExpressCode())
                .setKuaidinum(waybill.getWaybillNo())
                .setOrderId(waybill.getKdComOrderNum())
                .setReason(reqVO.getReason());
        waybillClient.cancel(account, cancelReqDTO);
        // 5. 取消失败已由客户端抛异常（订单与有效面单保持不变）；
        //    取消成功：作废面单并使订单回到待发货（同一事务）
        getSelf().cancelWaybillAndRestoreOrder(order, waybill, reqVO.getReason(), userId);
    }

    /**
     * 取消成功后作废电子面单并使订单回到待发货（同一事务）
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelWaybillAndRestoreOrder(SalesOrderDO order, SalesElectronicWaybillDO waybill,
                                             String reason, Long cancelUserId) {
        // 1. 作废电子面单（释放“一单一有效面单”唯一约束）
        waybillService.markCanceled(waybill.getId(), cancelUserId, reason);
        // 2. 订单回到待发货，清除物流绑定
        SalesOrderDO updateObj = new SalesOrderDO()
                .setStatus(SalesOrderStatusEnum.UNDELIVERED.getStatus())
                .setLogisticsId(SalesOrderDO.LOGISTICS_ID_NULL)
                .setLogisticsNo("");
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(WAYBILL_ORDER_CANCEL_FAIL_STATUS_NOT_DELIVERED);
        }
        // 3. 记录订单日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), SalesOrderStatusEnum.UNDELIVERED.getStatus(),
                MapUtil.<String, Object>builder().put("logisticsNo", waybill.getWaybillNo()).build());
    }

    // =================== 电子面单辅助方法 ===================

    /**
     * 解析寄件地址：请求指定优先，否则使用账户默认地址；校验可见、商户寄件地址、信息完整
     */
    private PartnerAddressRespDTO resolveSenderAddress(SalesElectronicWaybillAccountDO account, Long addressId) {
        Long finalAddressId = addressId != null ? addressId : account.getDefaultAddressId();
        return waybillAccountService.validateSenderAddress(finalAddressId);
    }

    private void validateReceiverComplete(SalesOrderDO order) {
        if (StrUtil.isBlank(order.getReceiverName()) || StrUtil.isBlank(order.getReceiverMobile())
                || order.getReceiverAreaId() == null || StrUtil.isBlank(order.getReceiverDetailAddress())) {
            throw exception(WAYBILL_ORDER_CREATE_FAILED, "订单收件信息不完整");
        }
    }

    private SalesElectronicWaybillOrderReqDTO buildWaybillOrderReq(SalesOrderDO order, SalesElectronicWaybillAccountDO account,
                                                                   PartnerAddressRespDTO senderAddress, String printType) {
        if (!SalesElectronicWaybillPrintTypeEnum.isValid(printType)) {
            throw exception(WAYBILL_ORDER_CREATE_FAILED, "打印类型仅支持 HTML/IMAGE");
        }
        SalesElectronicWaybillOrderReqDTO reqDTO = new SalesElectronicWaybillOrderReqDTO();
        // 收件人：订单收货快照
        SalesElectronicWaybillManInfoDTO recMan = new SalesElectronicWaybillManInfoDTO();
        recMan.setName(order.getReceiverName());
        recMan.setMobile(order.getReceiverMobile());
        recMan.setPrintAddr((order.getReceiverAreaId() != null ? AreaUtils.format(order.getReceiverAreaId()) : "")
                + order.getReceiverDetailAddress());
        reqDTO.setRecMan(recMan);
        // 寄件人：商户寄件地址
        SalesElectronicWaybillManInfoDTO sendMan = new SalesElectronicWaybillManInfoDTO();
        sendMan.setName(senderAddress.getName());
        sendMan.setMobile(senderAddress.getMobile());
        sendMan.setPrintAddr((senderAddress.getAreaId() != null ? AreaUtils.format(senderAddress.getAreaId()) : "")
                + senderAddress.getDetailAddress());
        reqDTO.setSendMan(sendMan);
        reqDTO.setKuaidicom(account.getExpressCode());
        reqDTO.setOrderId(order.getNo());
        reqDTO.setTempId(account.getTempId());
        reqDTO.setPrintType(printType);
        reqDTO.setCount(1);
        reqDTO.setCargo("商品");
        return reqDTO;
    }

    private SalesElectronicWaybillDO buildWaybillDO(SalesOrderDO order, SalesElectronicWaybillAccountDO account,
                                                    PartnerAddressRespDTO senderAddress, SalesElectronicWaybillOrderRespDTO orderResp,
                                                    String printType) {
        return SalesElectronicWaybillDO.builder()
                .orderId(order.getId()).orderNo(order.getNo())
                .accountId(account.getId()).expressId(account.getExpressId()).expressCode(account.getExpressCode())
                .waybillNo(orderResp.getKuaidinum())
                .kdComOrderNum(orderResp.getKdComOrderNum())
                .taskId(orderResp.getTaskId())
                .thirdOrderId(order.getNo())
                .status(SalesElectronicWaybillStatusEnum.VALID.getStatus())
                .validFlag(1)
                .printType(printType)
                .labelUrl(orderResp.getLabel())
                .senderName(senderAddress.getName()).senderMobile(senderAddress.getMobile())
                .senderAreaId(senderAddress.getAreaId())
                .senderAreaName(senderAddress.getAreaId() != null ? AreaUtils.format(senderAddress.getAreaId()) : null)
                .senderDetailAddress(senderAddress.getDetailAddress())
                .receiverName(order.getReceiverName()).receiverMobile(order.getReceiverMobile())
                .receiverAreaId(order.getReceiverAreaId())
                .receiverAreaName(order.getReceiverAreaId() != null ? AreaUtils.format(order.getReceiverAreaId()) : null)
                .receiverDetailAddress(order.getReceiverDetailAddress())
                .build();
    }

    private String sanitizeWaybillMessage(String message) {
        if (StrUtil.isBlank(message)) {
            return "";
        }
        String value = message.trim();
        return value.length() > 255 ? value.substring(0, 255) : value;
    }

    /**
     * 校验交易订单满足被发货的条件
     * <p>
     * 1. 交易订单未发货
     *
     * @param id 交易订单编号
     * @return 交易订单
     */
    private SalesOrderDO validateOrderDeliverable(Long id) {
        SalesOrderDO order = validateOrderExists(id);
        // 1. 校验订单是否未发货
        if (ObjectUtil.notEqual(SalesOrderRefundStatusEnum.NONE.getStatus(), order.getRefundStatus())) {
            throw exception(ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE);
        }

        // 2. 执行 SalesOrderHandler 前置处理
        tradeOrderHandlers.forEach(handler -> handler.beforeDeliveryOrder(order));
        return order;
    }

    @NotNull
    private SalesOrderDO validateOrderExists(Long id) {
        // 校验订单是否存在
        SalesOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.MEMBER_RECEIVE)
    public void receiveOrderByPartner(Long userId, Long id) {
        // 校验并获得交易订单（可收货）
        SalesOrderDO order = validateOrderReceivable(userId, id);

        // 收货订单
        receiveOrder0(order);
    }

    @Override
    public int receiveOrderBySystem() {
        // 1. 查询过期的待支付订单
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getReceiveExpireTime());
        List<SalesOrderDO> orders = tradeOrderMapper.selectListByStatusAndDeliveryTimeLt(
                SalesOrderStatusEnum.DELIVERED.getStatus(), expireTime);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行，逐个取消
        int count = 0;
        for (SalesOrderDO order : orders) {
            try {
                getSelf().receiveOrderBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[receiveOrderBySystem][order({}) 自动收货订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    /**
     * 自动收货单个订单
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.SYSTEM_RECEIVE)
    public void receiveOrderBySystem(SalesOrderDO order) {
        receiveOrder0(order);
    }

    /**
     * 收货订单的核心实现
     *
     * @param order 订单
     */
    private void receiveOrder0(SalesOrderDO order) {
        // 1. 更新 SalesOrderDO 状态为已完成
        LocalDateTime receiveTime = LocalDateTime.now();
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(),
                new SalesOrderDO().setStatus(SalesOrderStatusEnum.COMPLETED.getStatus()).setReceiveTime(receiveTime));
        if (updateCount == 0) {
            throw exception(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }

        // 2. 插入订单日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), SalesOrderStatusEnum.COMPLETED.getStatus());

        // 3. 执行 SalesOrderHandler 后置处理
        order.setStatus(SalesOrderStatusEnum.COMPLETED.getStatus()).setReceiveTime(receiveTime);
        tradeOrderHandlers.forEach(handler -> handler.afterReceiveOrder(order));
    }

    /**
     * 校验交易订单满足可售货的条件
     * <p>
     * 1. 交易订单待收货
     *
     * @param userId 用户编号
     * @param id     交易订单编号
     * @return 交易订单
     */
    private SalesOrderDO validateOrderReceivable(Long userId, Long id) {
        // 校验订单是否存在
        SalesOrderDO order = tradeOrderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 校验订单是否是待收货状态
        if (!SalesOrderStatusEnum.isDelivered(order.getStatus())) {
            throw exception(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.MEMBER_CANCEL)
    public void cancelOrderByPartner(Long userId, Long id) {
        // 1.1 校验存在
        SalesOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 1.2 校验状态
        if (ObjectUtil.notEqual(order.getStatus(), SalesOrderStatusEnum.UNPAID.getStatus())) {
            throw exception(ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID);
        }
        // 1.3 校验是否支持延迟（不允许取消）
        if (SalesOrderStatusEnum.isUnpaid(order.getStatus())) {
            PayOrderRespDTO payOrder = payOrderApi.getOrder(order.getPayOrderId());
            if (payOrder != null && PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
                log.warn("[cancelOrderByPartner][order({}) 支付单已支付（支付回调延迟），不支持取消]", order.getId());
                throw exception(ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID);
            }
        }

        // 2. 取消订单
        cancelOrder0(order, SalesOrderCancelTypeEnum.MEMBER_CANCEL);
    }

    @Override
    public int cancelOrderBySystem() {
        // 1. 查询过期的待支付订单
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getPayExpireTime());
        List<SalesOrderDO> orders = tradeOrderMapper.selectListByStatusAndCreateTimeLt(
                SalesOrderStatusEnum.UNPAID.getStatus(), expireTime);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行，逐个取消
        int count = 0;
        for (SalesOrderDO order : orders) {
            try {
                getSelf().cancelOrderBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[cancelOrderBySystem][order({}) 过期订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    /**
     * 自动取消单个订单
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.SYSTEM_CANCEL)
    public void cancelOrderBySystem(SalesOrderDO order) {
        // 校验是否支持延迟（不允许取消）
        if (SalesOrderStatusEnum.isUnpaid(order.getStatus())) {
            PayOrderRespDTO payOrder = payOrderApi.getOrder(order.getPayOrderId());
            if (payOrder != null && PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
                log.warn("[cancelOrderBySystem][order({}) 支付单已支付（支付回调延迟），不支持取消]", order.getId());
                return;
            }
        }

        cancelOrder0(order, SalesOrderCancelTypeEnum.PAY_TIMEOUT);
    }

    /**
     * 取消订单的核心实现
     *
     * @param order      订单
     * @param cancelType 取消类型
     */
    private void cancelOrder0(SalesOrderDO order, SalesOrderCancelTypeEnum cancelType) {
        // 1. 更新 SalesOrderDO 状态为已取消
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(),
                new SalesOrderDO().setStatus(SalesOrderStatusEnum.CANCELED.getStatus())
                        .setCancelType(cancelType.getType()).setCancelTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID);
        }

        // 2. 执行 SalesOrderHandler 的后置处理
        List<SalesOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        tradeOrderHandlers.forEach(handler -> handler.afterCancelOrder(order, orderItems));

        // 3. 增加订单日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), SalesOrderStatusEnum.CANCELED.getStatus());
    }

    /**
     * 如果金额全部被退款，则取消订单
     * 如果还有未被退款的金额，则无需取消订单
     *
     * @param order       订单
     * @param refundPrice 退款金额
     */
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.ADMIN_CANCEL_AFTER_SALE)
    public void cancelOrderByAfterSale(SalesOrderDO order, Integer refundPrice) {
        // 1. 更新订单
        if (refundPrice < order.getPayPrice()) {
            return;
        }
        tradeOrderMapper.updateById(new SalesOrderDO().setId(order.getId())
                .setStatus(SalesOrderStatusEnum.CANCELED.getStatus())
                .setCancelType(SalesOrderCancelTypeEnum.AFTER_SALE_CLOSE.getType()).setCancelTime(LocalDateTime.now()));

        // 2. 执行 SalesOrderHandler 的后置处理
        List<SalesOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        tradeOrderHandlers.forEach(handler -> handler.afterCancelOrder(order, orderItems));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.MEMBER_DELETE)
    public void deleteOrder(Long userId, Long id) {
        // 1.1 校验存在
        SalesOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 1.2 校验状态
        if (ObjectUtil.notEqual(order.getStatus(), SalesOrderStatusEnum.CANCELED.getStatus())) {
            throw exception(ORDER_DELETE_FAIL_STATUS_NOT_CANCEL);
        }
        // 2. 删除订单
        tradeOrderMapper.deleteById(id);

        // 3. 记录日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
    }

    @Override
    public void updateOrderRemark(SalesOrderRemarkReqVO reqVO) {
        // 校验并获得交易订单
        validateOrderExists(reqVO.getId());

        // 更新
        SalesOrderDO order = SalesOrderConvert.INSTANCE.convert(reqVO);
        tradeOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.ADMIN_UPDATE_PRICE)
    public void updateOrderPrice(SalesOrderUpdatePriceReqVO reqVO) {
        // 1.1 校验交易订单
        SalesOrderDO order = validateOrderExists(reqVO.getId());
        if (order.getPayStatus()) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_PAID);
        }
        // 1.2 校验调价金额是否变化
        if (order.getAdjustPrice() > 0) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_ALREADY);
        }
        // 1.3 支付价格不能为 0
        int newPayPrice = order.getPayPrice() + reqVO.getAdjustPrice();
        if (newPayPrice <= 0) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_PRICE_ERROR);
        }

        // 2. 更新订单
        tradeOrderMapper.updateById(new SalesOrderDO().setId(order.getId())
                .setAdjustPrice(reqVO.getAdjustPrice() + order.getAdjustPrice()).setPayPrice(newPayPrice));

        // 3. 更新 SalesOrderItem，需要做 adjustPrice 的分摊
        List<SalesOrderItemDO> orderOrderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        List<Integer> dividePrices = SalesPriceCalculatorHelper.dividePrice2(orderOrderItems, reqVO.getAdjustPrice());
        List<SalesOrderItemDO> updateItems = new ArrayList<>();
        for (int i = 0; i < orderOrderItems.size(); i++) {
            SalesOrderItemDO item = orderOrderItems.get(i);
            updateItems.add(new SalesOrderItemDO().setId(item.getId())
                    .setAdjustPrice(item.getAdjustPrice() + dividePrices.get(i))
                    .setPayPrice(item.getPayPrice() + dividePrices.get(i)));
        }
        tradeOrderItemMapper.updateBatch(updateItems);

        // 4. 更新支付订单
        payOrderApi.updatePayOrderPrice(order.getPayOrderId(), newPayPrice);

        // 5. 记录订单日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus(),
                MapUtil.<String, Object>builder().put("oldPayPrice", MoneyUtils.fenToYuanStr(order.getPayPrice()))
                        .put("adjustPrice", MoneyUtils.fenToYuanStr(reqVO.getAdjustPrice()))
                        .put("newPayPrice", MoneyUtils.fenToYuanStr(newPayPrice)).build());
    }

    @Override
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.ADMIN_UPDATE_ADDRESS)
    public void updateOrderAddress(SalesOrderUpdateAddressReqVO reqVO) {
        // 校验交易订单
        SalesOrderDO order = validateOrderExists(reqVO.getId());
        // 只有待发货状态，才可以修改订单收货地址；
        if (!SalesOrderStatusEnum.isUndelivered(order.getStatus())) {
            throw exception(ORDER_UPDATE_ADDRESS_FAIL_STATUS_NOT_DELIVERED);
        }

        // 更新
        tradeOrderMapper.updateById(SalesOrderConvert.INSTANCE.convert(reqVO));

        // 记录订单日志
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
    }

    @Override
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.ADMIN_PICK_UP_RECEIVE)
    public void pickUpOrderByAdmin(Long userId, Long id) {
        getSelf().pickUpOrder(userId, tradeOrderMapper.selectById(id));
    }

    @Override
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.ADMIN_PICK_UP_RECEIVE)
    public void pickUpOrderByAdmin(Long userId, String pickUpVerifyCode) {
        getSelf().pickUpOrder(userId, tradeOrderMapper.selectOneByPickUpVerifyCode(pickUpVerifyCode));
    }

    @Override
    public SalesOrderDO getByPickUpVerifyCode(String pickUpVerifyCode) {
        return tradeOrderMapper.selectOneByPickUpVerifyCode(pickUpVerifyCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void pickUpOrder(Long userId, SalesOrderDO order) {
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (ObjUtil.notEqual(SalesDeliveryTypeEnum.PICK_UP.getType(), order.getDeliveryType())) {
            throw exception(ORDER_RECEIVE_FAIL_DELIVERY_TYPE_NOT_PICK_UP);
        }
        if (!SalesOrderStatusEnum.isUndelivered(order.getStatus())) {
            throw exception(ORDER_PICK_UP_FAIL_STATUS_NOT_UNDELIVERED);
        }
        // 情况一：如果是拼团订单，则校验拼团是否成功
        if (SalesOrderTypeEnum.isCombination(order.getType())) {
            SalesCombinationRecordRespDTO combinationRecord = combinationRecordApi.getCombinationRecordByOrderId(
                    order.getUserId(), order.getId());
            if (!SalesCombinationRecordStatusEnum.isSuccess(combinationRecord.getStatus())) {
                throw exception(ORDER_PICK_UP_FAIL_COMBINATION_NOT_SUCCESS);
            }
        }
        SalesDeliveryPickUpStoreDO deliveryPickUpStore = pickUpStoreService.getDeliveryPickUpStore(order.getPickUpStoreId());
        if (deliveryPickUpStore == null
                || !CollUtil.contains(deliveryPickUpStore.getVerifyUserIds(), userId)) {
            throw exception(ORDER_PICK_UP_FAIL_NOT_VERIFY_USER);
        }

        receiveOrder0(order);
    }

    // =================== Order Item ===================

    @Override
    public void updateOrderItemWhenAfterSaleCreate(Long id, Long afterSaleId) {
        // 更新订单项
        updateOrderItemAfterSaleStatus(id, SalesOrderItemAfterSaleStatusEnum.NONE.getStatus(),
                SalesOrderItemAfterSaleStatusEnum.APPLY.getStatus(), afterSaleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderItemWhenAfterSaleSuccess(Long id, Integer refundPrice) {
        // 1.1 更新订单项
        updateOrderItemAfterSaleStatus(id, SalesOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
                SalesOrderItemAfterSaleStatusEnum.SUCCESS.getStatus(), null);
        // 1.2 执行 SalesOrderHandler 的后置处理
        SalesOrderItemDO orderItem = tradeOrderItemMapper.selectById(id);
        SalesOrderDO order = tradeOrderMapper.selectById(orderItem.getOrderId());
        tradeOrderHandlers.forEach(handler -> handler.afterCancelOrderItem(order, orderItem));
        partnerMemberService.revokeMemberByRefundedOrderItem(order.getUserId(), orderItem);

        // 2.1 更新订单的退款金额、积分
        Integer orderRefundPrice = order.getRefundPrice() + refundPrice;
        Integer orderRefundPoint = order.getRefundPoint() + orderItem.getUsePoint();
        Integer refundStatus = isAllOrderItemAfterSaleSuccess(order.getId()) ?
                SalesOrderRefundStatusEnum.ALL.getStatus() // 如果都售后成功，则需要取消订单
                : SalesOrderRefundStatusEnum.PART.getStatus();
        tradeOrderMapper.updateById(new SalesOrderDO().setId(order.getId())
                .setRefundStatus(refundStatus)
                .setRefundPrice(orderRefundPrice).setRefundPoint(orderRefundPoint));
        // 2.2 如果全部退款，则进行取消订单
        getSelf().cancelOrderByAfterSale(order, orderRefundPrice);
    }

    @Override
    public void updateOrderItemWhenAfterSaleCancel(Long id) {
        // 更新订单项
        updateOrderItemAfterSaleStatus(id, SalesOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
                SalesOrderItemAfterSaleStatusEnum.NONE.getStatus(), null);
    }

    private void updateOrderItemAfterSaleStatus(Long id, Integer oldAfterSaleStatus, Integer newAfterSaleStatus,
                                                Long afterSaleId) {
        // 更新订单项
        int updateCount = tradeOrderItemMapper.updateAfterSaleStatus(id, oldAfterSaleStatus, newAfterSaleStatus, afterSaleId);
        if (updateCount <= 0) {
            throw exception(ORDER_ITEM_UPDATE_AFTER_SALE_STATUS_FAIL);
        }

    }

    /**
     * 判断指定订单的所有订单项，是不是都售后成功
     *
     * @param id 订单编号
     * @return 是否都售后成功
     */
    private boolean isAllOrderItemAfterSaleSuccess(Long id) {
        List<SalesOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(id);
        return orderItems.stream().allMatch(orderItem -> Objects.equals(orderItem.getAfterSaleStatus(),
                SalesOrderItemAfterSaleStatusEnum.SUCCESS.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.MEMBER_COMMENT)
    public Long createOrderItemCommentByPartner(Long userId, AppSalesOrderItemCommentCreateReqVO createReqVO) {
        // 1.1 先通过订单项 ID，查询订单项是否存在
        SalesOrderItemDO orderItem = tradeOrderItemMapper.selectByIdAndUserId(createReqVO.getOrderItemId(), userId);
        if (orderItem == null) {
            throw exception(ORDER_ITEM_NOT_FOUND);
        }
        // 1.2 校验订单相关状态
        SalesOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(orderItem.getOrderId(), userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(order.getStatus(), SalesOrderStatusEnum.COMPLETED.getStatus())) {
            throw exception(ORDER_COMMENT_FAIL_STATUS_NOT_COMPLETED);
        }
        if (ObjectUtil.notEqual(order.getCommentStatus(), Boolean.FALSE)) {
            throw exception(ORDER_COMMENT_STATUS_NOT_FALSE);
        }

        // 2. 创建评价
        Long commentId = createOrderItemComment0(orderItem, createReqVO);

        // 3. 如果订单项都评论了，则更新订单评价状态
        List<SalesOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        if (!anyMatch(orderItems, item -> Objects.equals(item.getCommentStatus(), Boolean.FALSE))) {
            tradeOrderMapper.updateById(new SalesOrderDO().setId(order.getId()).setCommentStatus(Boolean.TRUE)
                    .setFinishTime(LocalDateTime.now()));
            // 增加订单日志。注意：只有在所有订单项都评价后，才会增加
            SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
        }
        return commentId;
    }

    @Override
    public int createOrderItemCommentBySystem() {
        // 1. 查询过期的待支付订单
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getCommentExpireTime());
        List<SalesOrderDO> orders = tradeOrderMapper.selectListByStatusAndReceiveTimeLt(
                SalesOrderStatusEnum.COMPLETED.getStatus(), expireTime, false);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行，逐个取消
        int count = 0;
        for (SalesOrderDO order : orders) {
            try {
                getSelf().createOrderItemCommentBySystemBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[createOrderItemCommentBySystem][order({}) 过期订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderCombinationInfo(Long orderId, Long activityId, Long combinationRecordId, Long headId) {
        tradeOrderMapper.updateById(
                new SalesOrderDO().setId(orderId).setCombinationActivityId(activityId)
                        .setCombinationRecordId(combinationRecordId).setCombinationHeadId(headId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPaidOrder(Long userId, Long orderId, Integer cancelType) {
        // 1.1 这里校验下 cancelType 只允许拼团关闭；
        if (ObjUtil.notEqual(SalesOrderCancelTypeEnum.COMBINATION_CLOSE.getType(), cancelType)) {
            return;
        }
        // 1.2 检验订单存在
        SalesOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 1.3 校验订单是否支付
        if (!order.getPayStatus()) {
            throw exception(ORDER_CANCEL_PAID_FAIL, "已支付");
        }
        // 1.4 校验订单是否未退款
        if (ObjUtil.notEqual(SalesOrderRefundStatusEnum.NONE.getStatus(), order.getRefundStatus())) {
            throw exception(ORDER_CANCEL_PAID_FAIL, "未退款");
        }

        // 2.1 取消订单
        cancelOrder0(order, SalesOrderCancelTypeEnum.COMBINATION_CLOSE);
        // 2.2 创建退款单
        payRefundApi.createRefund(new PayRefundCreateReqDTO()
                .setAppKey(tradeOrderProperties.getPayAppKey())  // 支付应用
                .setUserIp(NetUtil.getLocalhostStr()) // 使用本机 IP，因为是服务器发起退款的
                .setUserId(order.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue()) // 用户信息
                .setMerchantOrderId(String.valueOf(order.getId())) // 支付单号
                // 特殊：因为订单支持 AfterSale 单个售后退款，也支持整单退款，所以需要通过 order- 进行下区分
                //      具体可见 SalesAfterSaleController 的 updateAfterSaleRefunded 方法
                .setMerchantRefundId("order-" + order.getId())
                .setReason(SalesOrderCancelTypeEnum.COMBINATION_CLOSE.getName()).setPrice(order.getPayPrice())); // 价格信息
    }

    @Override
    public void updatePaidOrderRefunded(Long id, Long payRefundId) {
        PayRefundRespDTO payRefund = payRefundApi.getRefund(payRefundId);
        if (payRefund == null) {
            throw exception(ORDER_UPDATE_PAID_ORDER_REFUNDED_FAIL_REFUND_NOT_FOUND);
        }
        // 特殊：因为在 cancelPaidOrder 已经进行订单的取消，所以这里必须退款成功！！！
        if (!PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            throw exception(ORDER_UPDATE_PAID_ORDER_REFUNDED_FAIL_REFUND_STATUS_NOT_SUCCESS);
        }
    }

    @Override
    public void updateOrderGiveCouponIds(Long userId, Long orderId, List<Long> giveCouponIds) {
        // 1. 检验订单存在
        SalesOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }

        // 2. 更新订单赠送的优惠券编号列表
        tradeOrderMapper.updateById(new SalesOrderDO().setId(orderId).setGiveCouponIds(giveCouponIds));
    }

    /**
     * 创建单个订单的评论
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    @SalesOrderLog(operateType = SalesOrderOperateTypeEnum.SYSTEM_COMMENT)
    public void createOrderItemCommentBySystemBySystem(SalesOrderDO order) {
        // 1. 查询未评论的订单项
        List<SalesOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderIdAndCommentStatus(
                order.getId(), Boolean.FALSE);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }

        // 2. 逐个评论
        for (SalesOrderItemDO orderItem : orderItems) {
            // 2.1 创建评价
            AppSalesOrderItemCommentCreateReqVO commentCreateReqVO = new AppSalesOrderItemCommentCreateReqVO()
                    .setOrderItemId(orderItem.getId()).setAnonymous(false).setContent("")
                    .setBenefitScores(5).setDescriptionScores(5);
            createOrderItemComment0(orderItem, commentCreateReqVO);

            // 2.2 更新订单项评价状态
            tradeOrderItemMapper.updateById(new SalesOrderItemDO().setId(orderItem.getId()).setCommentStatus(Boolean.TRUE));
        }

        // 3. 所有订单项都评论了，则更新订单评价状态
        tradeOrderMapper.updateById(new SalesOrderDO().setId(order.getId()).setCommentStatus(Boolean.TRUE)
                .setFinishTime(LocalDateTime.now()));
        // 增加订单日志。注意：只有在所有订单项都评价后，才会增加
        SalesOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
    }

    /**
     * 创建订单项的评论的核心实现
     *
     * @param orderItem   订单项
     * @param createReqVO 评论内容
     * @return 评论编号
     */
    private Long createOrderItemComment0(SalesOrderItemDO orderItem, AppSalesOrderItemCommentCreateReqVO createReqVO) {
        // 1. 创建评价
        ProductCommentCreateReqDTO productCommentCreateReqDTO = SalesOrderConvert.INSTANCE.convert04(createReqVO, orderItem);
        Long commentId = productCommentApi.createComment(productCommentCreateReqDTO);

        // 2. 更新订单项评价状态
        tradeOrderItemMapper.updateById(new SalesOrderItemDO().setId(orderItem.getId()).setCommentStatus(Boolean.TRUE));
        return commentId;
    }

    // =================== 营销相关的操作 ===================

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private SalesOrderUpdateServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}

