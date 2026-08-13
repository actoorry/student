package vip.appap.suxin.module.accountant.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPayCheckoutProfileRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPayResultRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPaySubmitReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPaySubmitRespVO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayChannelDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderExtensionDO;
import vip.appap.suxin.module.accountant.dal.mysql.PayOrderExtensionMapper;
import vip.appap.suxin.module.accountant.dal.redis.no.PayNoRedisDAO;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.enums.PayOrderStatusEnum;
import vip.appap.suxin.module.accountant.enums.PayOrderWechatVirtualDeliverStatusEnum;
import vip.appap.suxin.module.accountant.framework.pay.config.PayProperties;
import vip.appap.suxin.module.accountant.framework.pay.core.client.dto.order.PayOrderRespDTO;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxVirtualPayAccessTokenUtils;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxVirtualPayClientConfig;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxVirtualPayHttpClient;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxVirtualPaySignatureUtils;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.mysql.ProductSkuMapper;
import vip.appap.suxin.module.product.dal.mysql.ProductSpuMapper;
import vip.appap.suxin.module.product.enums.ProductSkuWechatVirtualStatusEnum;
import vip.appap.suxin.module.product.enums.ProductSpuStatusEnum;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderItemMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderMapper;
import vip.appap.suxin.module.system.dal.dataobject.SocialUserDO;
import vip.appap.suxin.module.system.enums.SocialTypeEnum;
import vip.appap.suxin.module.system.service.SocialClientService;
import vip.appap.suxin.module.system.service.SocialUserService;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.json.JsonUtils.toJsonString;
import static vip.appap.suxin.module.accountant.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class WechatVirtualPayServiceImpl implements WechatVirtualPayService {

    private static final String EXTRA_WECHAT_VIRTUAL_BUY_QUANTITY = "wechatVirtualBuyQuantity";
    private static final String EXTRA_WECHAT_VIRTUAL_GOODS_PRICE = "wechatVirtualGoodsPrice";
    private static final String EXTRA_WECHAT_VIRTUAL_OPENID = "wechatVirtualOpenid";
    private static final String EXTRA_WECHAT_VIRTUAL_QUERY_RETRY_COUNT = "wechatVirtualQueryRetryCount";
    private static final int MAX_WECHAT_VIRTUAL_QUERY_RETRY_COUNT = 12;
    /**
     * notify_provide_goods 是微信发货推送失败后的兜底接口，不能在首次查到支付成功时立即调用。
     */
    private static final int WECHAT_VIRTUAL_MANUAL_DELIVER_RETRY_THRESHOLD = 3;
    private static final String DEFAULT_CURRENCY_TYPE = "CNY";

    @Resource
    private PayOrderService payOrderService;
    @Resource
    private PayChannelService payChannelService;
    @Resource
    private PayOrderExtensionMapper payOrderExtensionMapper;
    @Resource
    private PayNoRedisDAO payNoRedisDAO;
    @Resource
    private PayProperties payProperties;
    @Resource
    private SalesOrderMapper salesOrderMapper;
    @Resource
    private SalesOrderItemMapper salesOrderItemMapper;
    @Resource
    private ProductSkuMapper productSkuMapper;
    @Resource
    private ProductSpuMapper productSpuMapper;
    @Resource
    private SocialUserService socialUserService;
    @Resource
    private SocialClientService socialClientService;

    /**
     * 允许测试替换 HTTP 客户端，避免协议测试访问真实微信接口。
     */
    protected WxVirtualPayHttpClient createWxVirtualPayHttpClient() {
        return new WxVirtualPayHttpClient();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppWechatVirtualPaySubmitRespVO submitWechatVirtualPay(Long userId, AppWechatVirtualPaySubmitReqVO reqVO,
                                                                  String userIp) {
        PayOrderDO payOrder = validatePayOrder(userId, reqVO.getPayOrderId());
        SalesOrderDO salesOrder = validateSalesOrder(userId, payOrder);
        List<SalesOrderItemDO> orderItems = salesOrderItemMapper.selectListByOrderId(salesOrder.getId());
        VirtualCheckout virtualCheckout = validateAndGetVirtualCheckout(salesOrder, orderItems);
        WxVirtualPayClientConfig config = getVirtualPayConfig(payOrder.getAppId());
        String sessionKey = getSessionKey(userId, reqVO.getOpenid());
        PayOrderExtensionDO extension = createVirtualExtensionIfAbsent(payOrder, virtualCheckout, userIp,
                reqVO.getOpenid());
        String signData = buildRequestVirtualPaymentSignData(config, virtualCheckout.productId(),
                virtualCheckout.buyQuantity(), virtualCheckout.goodsPrice(), extension.getNo(), extension.getNo());

        return new AppWechatVirtualPaySubmitRespVO()
                .setPayOrderId(payOrder.getId())
                .setPayExtensionNo(extension.getNo())
                .setMode(AppWechatVirtualPaySubmitRespVO.MODE_SHORT_SERIES_GOODS)
                .setSignData(signData)
                .setPaySig(WxVirtualPaySignatureUtils.calculateRequestVirtualPaymentPaySig(signData, config.getAppKey()))
                .setSignature(WxVirtualPaySignatureUtils.calculateSignature(signData, sessionKey));
    }

    @Override
    public AppWechatVirtualPayResultRespVO getWechatVirtualPayResult(Long userId, Long payOrderId) {
        PayOrderDO payOrder = validatePayOrderOwner(userId, payOrderId);
        PayOrderExtensionDO extension = CollUtil.findOne(payOrderExtensionMapper.selectListByOrderId(payOrderId),
                item -> Objects.equals(item.getChannelCode(), PayChannelEnum.WX_VIRTUAL_LITE.getCode()));
        if (extension != null && Objects.equals(extension.getWechatVirtualDeliverStatus(),
                PayOrderWechatVirtualDeliverStatusEnum.FAILED.getStatus())) {
            // 兼容旧版本把主动兜底接口的瞬时错误误判为永久失败的订单。
            payOrderExtensionMapper.updateWechatVirtualDeliveryById(extension.getId(), new PayOrderExtensionDO()
                    .setWechatVirtualDeliverStatus(PayOrderWechatVirtualDeliverStatusEnum.WAITING.getStatus()));
            extension.setWechatVirtualDeliverStatus(PayOrderWechatVirtualDeliverStatusEnum.WAITING.getStatus());
        }
        if (extension != null && !Objects.equals(extension.getWechatVirtualDeliverStatus(),
                PayOrderWechatVirtualDeliverStatusEnum.DELIVERED.getStatus())) {
            try {
                queryAndConfirmWechatVirtualPay(extension);
            } catch (Exception ex) {
                log.warn("[getWechatVirtualPayResult][extension({}) 主动查询微信支付结果失败]",
                        extension.getId(), ex);
            }
            // 主动查询可能已经确认支付与发货，返回前重新读取权威状态。
            payOrder = validatePayOrderOwner(userId, payOrderId);
            extension = CollUtil.findOne(payOrderExtensionMapper.selectListByOrderId(payOrderId),
                    item -> Objects.equals(item.getChannelCode(), PayChannelEnum.WX_VIRTUAL_LITE.getCode()));
        }
        return new AppWechatVirtualPayResultRespVO()
                .setPayStatus(payOrder.getStatus())
                .setDeliverStatus(extension != null ? extension.getWechatVirtualDeliverStatus() : null)
                .setPayExtensionNo(extension != null ? extension.getNo() : null);
    }

    @Override
    public AppWechatVirtualPayCheckoutProfileRespVO getWechatVirtualPayCheckoutProfile(Long userId, Long payOrderId) {
        PayOrderDO payOrder = validatePayOrder(userId, payOrderId);
        SalesOrderDO salesOrder = validateSalesOrder(userId, payOrder);
        List<SalesOrderItemDO> orderItems = salesOrderItemMapper.selectListByOrderId(salesOrder.getId());
        CheckoutProductType checkoutProductType = getCheckoutProductType(orderItems);
        if (checkoutProductType == CheckoutProductType.MERCHANT) {
            return new AppWechatVirtualPayCheckoutProfileRespVO().setPayOrderId(payOrderId)
                    .setMode(AppWechatVirtualPayCheckoutProfileRespVO.MODE_WECHAT_MERCHANT)
                    .setRequiredChannelCode(PayChannelEnum.WX_LITE.getCode());
        }
        if (checkoutProductType == CheckoutProductType.MIXED) {
            return virtualNotReadyProfile(payOrderId, "订单同时包含虚拟商品和其他商品，请拆单后支付");
        }
        if (checkoutProductType == CheckoutProductType.INVALID) {
            return virtualNotReadyProfile(payOrderId, "订单商品不存在或不可用");
        }
        try {
            validateAndGetVirtualCheckout(salesOrder, orderItems);
            getVirtualPayConfig(payOrder.getAppId());
            return new AppWechatVirtualPayCheckoutProfileRespVO().setPayOrderId(payOrderId)
                    .setMode(AppWechatVirtualPayCheckoutProfileRespVO.MODE_WECHAT_VIRTUAL)
                    .setRequiredChannelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode());
        } catch (ServiceException ex) {
            return virtualNotReadyProfile(payOrderId, ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> notifyGoodsDeliver(Map<String, Object> body) {
        String outTradeNo = readString(body, "OutTradeNo", "out_trade_no");
        Map<String, Object> weChatPayInfo = readMap(body, "WeChatPayInfo", "we_chat_pay_info");
        Map<String, Object> goodsInfo = readMap(body, "GoodsInfo", "goods_info");
        String transactionId = readString(weChatPayInfo, "TransactionId", "transaction_id");
        String mchOrderNo = readString(weChatPayInfo, "MchOrderNo", "mch_order_no");
        String openid = readString(body, "OpenId");
        PayOrderExtensionDO extension = payOrderExtensionMapper.selectByNo(outTradeNo);
        if (extension == null) {
            throw exception(PAY_ORDER_EXTENSION_NOT_FOUND);
        }
        if (!Objects.equals(extension.getChannelCode(), PayChannelEnum.WX_VIRTUAL_LITE.getCode())) {
            throw exception(WECHAT_VIRTUAL_PAY_GOODS_DELIVER_MISMATCH);
        }
        log.info("[notifyGoodsDeliver][收到微信虚拟支付发货推送，extensionId={}, outTradeNo={}]",
                extension.getId(), outTradeNo);
        validateGoodsDeliver(extension, goodsInfo);
        confirmWechatVirtualPaySuccess(extension, firstNotBlank(transactionId, mchOrderNo), openid,
                readInteger(goodsInfo, "ActualPrice", "actual_price"), body);
        log.info("[notifyGoodsDeliver][微信虚拟支付发货确认完成，extensionId={}, outTradeNo={}]",
                extension.getId(), outTradeNo);
        return successNotify();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> notifyGoodsDeliver(String rawBody, boolean xml) {
        return notifyGoodsDeliver(parseGoodsDeliverBody(rawBody, xml));
    }

    @Override
    public Map<String, Object> notifyComplaint(Map<String, Object> body) {
        log.warn("[notifyComplaint][收到微信虚拟支付投诉回调]");
        return successNotify();
    }

    @Override
    public int syncPendingWechatVirtualPayOrders() {
        List<PayOrderExtensionDO> extensions = payOrderExtensionMapper.selectListByChannelCodeAndWechatVirtualDeliverStatus(
                PayChannelEnum.WX_VIRTUAL_LITE.getCode(), PayOrderWechatVirtualDeliverStatusEnum.WAITING.getStatus());
        int count = 0;
        for (PayOrderExtensionDO extension : extensions) {
            if (!tryRecordWechatVirtualQueryRetry(extension)) {
                continue;
            }
            try {
                if (queryAndConfirmWechatVirtualPay(extension)) {
                    count++;
                }
            } catch (Exception ex) {
                log.warn("[syncPendingWechatVirtualPayOrders][extension({}) 兜底查询失败]", extension.getId(), ex);
            }
        }
        return count;
    }

    private boolean tryRecordWechatVirtualQueryRetry(PayOrderExtensionDO extension) {
        int retryCount = readExtensionInteger(extension, EXTRA_WECHAT_VIRTUAL_QUERY_RETRY_COUNT, 0);
        if (retryCount >= MAX_WECHAT_VIRTUAL_QUERY_RETRY_COUNT) {
            PayOrderDO payOrder = payOrderService.getOrder(extension.getOrderId());
            if (payOrder != null && PayOrderStatusEnum.SUCCESS.getStatus().equals(payOrder.getStatus())) {
                // 本地已经确认收款的订单不能因为达到普通查询上限而永久停留在待发货状态。
                // 保持计数封顶并继续查询，直到微信返回已发货或主动发货接口成功。
                return true;
            }
            log.error("[syncPendingWechatVirtualPayOrders][extension({}) 已达到补偿查询上限({})，需要人工处理]",
                    extension.getId(), MAX_WECHAT_VIRTUAL_QUERY_RETRY_COUNT);
            return false;
        }
        Map<String, String> extras = new LinkedHashMap<>();
        if (extension.getChannelExtras() != null) {
            extras.putAll(extension.getChannelExtras());
        }
        extras.put(EXTRA_WECHAT_VIRTUAL_QUERY_RETRY_COUNT, String.valueOf(retryCount + 1));
        int updated = payOrderExtensionMapper.updateById(
                new PayOrderExtensionDO().setId(extension.getId()).setChannelExtras(extras));
        if (updated <= 0) {
            log.warn("[syncPendingWechatVirtualPayOrders][extension({}) 记录补偿查询次数失败]", extension.getId());
            return false;
        }
        // 当前轮询继续使用同一个 extension 对象，必须同步内存值；否则达到阈值后还会多等待一个调度周期。
        extension.setChannelExtras(extras);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmWechatVirtualPaySuccess(PayOrderExtensionDO extension, String channelOrderNo, String openid,
                                               Integer paidAmount, Object rawData) {
        confirmWechatVirtualPayOrder(extension, channelOrderNo, openid, paidAmount, rawData);
        payOrderExtensionMapper.updateWechatVirtualDeliveryById(extension.getId(), new PayOrderExtensionDO()
                .setWechatVirtualDeliverStatus(PayOrderWechatVirtualDeliverStatusEnum.DELIVERED.getStatus())
                .setWechatVirtualDeliverNotifyData(toJsonString(rawData))
                .setWechatVirtualDeliverConfirmTime(LocalDateTime.now()));
    }

    private void confirmWechatVirtualPayOrder(PayOrderExtensionDO extension, String channelOrderNo, String openid,
                                              Integer paidAmount, Object rawData) {
        PayOrderDO payOrder = payOrderService.getOrder(extension.getOrderId());
        if (payOrder == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (paidAmount != null && !Objects.equals(payOrder.getPrice(), paidAmount)) {
            log.error("[confirmWechatVirtualPaySuccess][extension({}) 金额不匹配，local({}) wx({})]",
                    extension.getId(), payOrder.getPrice(), paidAmount);
            throw exception(WECHAT_VIRTUAL_PAY_AMOUNT_MISMATCH);
        }
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            payOrderService.notifyOrder(extension.getChannelId(), PayOrderRespDTO.successOf(channelOrderNo, openid,
                    LocalDateTime.now(), extension.getNo(), rawData));
        }
    }

    private PayOrderDO validatePayOrder(Long userId, Long payOrderId) {
        PayOrderDO payOrder = validatePayOrderOwner(userId, payOrderId);
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(payOrder.getStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        return payOrder;
    }

    private PayOrderDO validatePayOrderOwner(Long userId, Long payOrderId) {
        PayOrderDO payOrder = payOrderService.getOrder(payOrderId);
        if (payOrder == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (ObjUtil.notEqual(payOrder.getUserId(), userId)) {
            throw exception(WECHAT_VIRTUAL_PAY_ORDER_NOT_SELF);
        }
        return payOrder;
    }

    private SalesOrderDO validateSalesOrder(Long userId, PayOrderDO payOrder) {
        SalesOrderDO salesOrder = salesOrderMapper.selectById(Long.valueOf(payOrder.getMerchantOrderId()));
        if (salesOrder == null || ObjUtil.notEqual(salesOrder.getUserId(), userId)) {
            throw exception(WECHAT_VIRTUAL_PAY_TRADE_ORDER_NOT_FOUND);
        }
        if (!Objects.equals(salesOrder.getPayOrderId(), payOrder.getId())
                || !Objects.equals(salesOrder.getPayPrice(), payOrder.getPrice())) {
            throw exception(WECHAT_VIRTUAL_PAY_AMOUNT_MISMATCH);
        }
        if (Boolean.TRUE.equals(salesOrder.getPayStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_SUCCESS);
        }
        return salesOrder;
    }

    private VirtualCheckout validateAndGetVirtualCheckout(SalesOrderDO salesOrder,
                                                          List<SalesOrderItemDO> orderItems) {
        if (CollUtil.isEmpty(orderItems) || orderItems.size() != 1) {
            throw exception(WECHAT_VIRTUAL_PAY_UNSUPPORTED_PRODUCT_TYPE);
        }
        SalesOrderItemDO orderItem = orderItems.get(0);
        if (!Objects.equals(orderItem.getOrderId(), salesOrder.getId())
                || !Objects.equals(orderItem.getUserId(), salesOrder.getUserId())) {
            throw exception(WECHAT_VIRTUAL_PAY_UNSUPPORTED_PRODUCT_TYPE);
        }
        ProductSkuDO sku = validateAndGetWechatSku(orderItem);
        Integer buyQuantity = orderItem.getCount();
        if (buyQuantity == null || buyQuantity <= 0) {
            throw exception(WECHAT_VIRTUAL_PAY_BUY_QUANTITY_INVALID);
        }
        if (orderItem.getPrice() == null || !Objects.equals(orderItem.getPrice(), sku.getPrice())) {
            throw exception(WECHAT_VIRTUAL_PAY_AMOUNT_MISMATCH);
        }
        int expectedAmount;
        try {
            expectedAmount = Math.multiplyExact(orderItem.getPrice(), buyQuantity);
        } catch (ArithmeticException ex) {
            throw exception(WECHAT_VIRTUAL_PAY_AMOUNT_MISMATCH);
        }
        validateUnmodifiedPrice(salesOrder, orderItem, expectedAmount);
        return new VirtualCheckout(sku.getWechatVirtualProductId(), buyQuantity, orderItem.getPrice());
    }

    private ProductSkuDO validateAndGetWechatSku(SalesOrderItemDO orderItem) {
        ProductSpuDO spu = productSpuMapper.selectById(orderItem.getSpuId());
        if (spu == null || !Boolean.TRUE.equals(spu.getIsWechatMiniappVirtualGoods())) {
            throw exception(WECHAT_VIRTUAL_PAY_UNSUPPORTED_PRODUCT_TYPE);
        }
        if (!ProductSpuStatusEnum.isEnable(spu.getStatus()) || !Boolean.TRUE.equals(spu.getIsSale())) {
            throw exception(WECHAT_VIRTUAL_PAY_GOODS_NOT_PUBLISHED);
        }
        ProductSkuDO sku = productSkuMapper.selectById(orderItem.getSkuId());
        if (sku == null || !Objects.equals(sku.getSpuId(), orderItem.getSpuId())
                || StrUtil.isBlank(sku.getWechatVirtualProductId())
                || !Objects.equals(sku.getWechatVirtualUploadStatus(),
                        ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                || !Objects.equals(sku.getWechatVirtualPublishStatus(),
                        ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                || !Objects.equals(sku.getWechatVirtualReviewStatus(),
                        ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())) {
            throw exception(WECHAT_VIRTUAL_PAY_GOODS_NOT_PUBLISHED);
        }
        return sku;
    }

    private void validateUnmodifiedPrice(SalesOrderDO salesOrder, SalesOrderItemDO orderItem, int expectedAmount) {
        if (!Objects.equals(orderItem.getPayPrice(), expectedAmount)
                || !Objects.equals(salesOrder.getTotalPrice(), expectedAmount)
                || !Objects.equals(salesOrder.getPayPrice(), expectedAmount)
                || !isZero(orderItem.getDiscountPrice())
                || !isZero(orderItem.getDeliveryPrice())
                || !isZero(orderItem.getAdjustPrice())
                || !isZero(orderItem.getCouponPrice())
                || !isZero(orderItem.getPointPrice())
                || !isZero(orderItem.getUsePoint())
                || !isZero(orderItem.getVipPrice())
                || !isZero(salesOrder.getDiscountPrice())
                || !isZero(salesOrder.getDeliveryPrice())
                || !isZero(salesOrder.getAdjustPrice())
                || !isZero(salesOrder.getCouponPrice())
                || !isZero(salesOrder.getPointPrice())
                || !isZero(salesOrder.getUsePoint())
                || !isZero(salesOrder.getVipPrice())
                || salesOrder.getCouponId() != null
                || salesOrder.getSeckillActivityId() != null
                || salesOrder.getBargainActivityId() != null
                || salesOrder.getBargainRecordId() != null
                || salesOrder.getCombinationActivityId() != null
                || salesOrder.getCombinationHeadId() != null
                || salesOrder.getCombinationRecordId() != null
                || salesOrder.getPointActivityId() != null) {
            throw exception(WECHAT_VIRTUAL_PAY_AMOUNT_MISMATCH);
        }
    }

    private boolean isZero(Integer value) {
        return value != null && value == 0;
    }

    private WxVirtualPayClientConfig getVirtualPayConfig(Long appId) {
        PayChannelDO channel = payChannelService.validPayChannel(appId, PayChannelEnum.WX_VIRTUAL_LITE.getCode());
        if (!(channel.getConfig() instanceof WxVirtualPayClientConfig config)) {
            throw exception(WECHAT_VIRTUAL_PAY_CHANNEL_CONFIG_ERROR);
        }
        if (StrUtil.isBlank(config.getAppid()) || StrUtil.isBlank(config.getOfferId())
                || StrUtil.isBlank(config.getAppKey()) || config.getEnv() == null) {
            throw exception(WECHAT_VIRTUAL_PAY_CHANNEL_CONFIG_ERROR);
        }
        return config;
    }

    private PayOrderExtensionDO createVirtualExtensionIfAbsent(PayOrderDO payOrder, VirtualCheckout virtualCheckout,
                                                              String userIp, String openid) {
        PayOrderExtensionDO existing = CollUtil.findOne(payOrderExtensionMapper.selectListByOrderId(payOrder.getId()),
                item -> Objects.equals(item.getChannelCode(), PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                        && PayOrderStatusEnum.WAITING.getStatus().equals(item.getStatus()));
        if (existing != null) {
            validateExistingVirtualExtension(existing, virtualCheckout);
            persistWechatVirtualOpenidIfAbsent(existing, openid);
            return existing;
        }
        PayChannelDO channel = payChannelService.validPayChannel(payOrder.getAppId(), PayChannelEnum.WX_VIRTUAL_LITE.getCode());
        PayOrderExtensionDO extension = new PayOrderExtensionDO()
                .setNo(payNoRedisDAO.generate(payProperties.getOrderNoPrefix()))
                .setOrderId(payOrder.getId())
                .setChannelId(channel.getId())
                .setChannelCode(channel.getCode())
                .setUserIp(userIp)
                .setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setChannelExtras(Map.of(
                        EXTRA_WECHAT_VIRTUAL_BUY_QUANTITY, String.valueOf(virtualCheckout.buyQuantity()),
                        EXTRA_WECHAT_VIRTUAL_GOODS_PRICE, String.valueOf(virtualCheckout.goodsPrice()),
                        EXTRA_WECHAT_VIRTUAL_OPENID, openid))
                .setWechatVirtualProductId(virtualCheckout.productId())
                .setWechatVirtualDeliverStatus(PayOrderWechatVirtualDeliverStatusEnum.WAITING.getStatus());
        payOrderExtensionMapper.insert(extension);
        return extension;
    }

    private void persistWechatVirtualOpenidIfAbsent(PayOrderExtensionDO extension, String openid) {
        if (StrUtil.isBlank(openid) || (extension.getChannelExtras() != null
                && StrUtil.isNotBlank(extension.getChannelExtras().get(EXTRA_WECHAT_VIRTUAL_OPENID)))) {
            return;
        }
        Map<String, String> extras = new LinkedHashMap<>();
        if (extension.getChannelExtras() != null) {
            extras.putAll(extension.getChannelExtras());
        }
        extras.put(EXTRA_WECHAT_VIRTUAL_OPENID, openid);
        payOrderExtensionMapper.updateById(new PayOrderExtensionDO().setId(extension.getId()).setChannelExtras(extras));
        extension.setChannelExtras(extras);
    }

    private String buildRequestVirtualPaymentSignData(WxVirtualPayClientConfig config, String productId,
                                                      Integer buyQuantity, Integer goodsPrice,
                                                      String outTradeNo, String attach) {
        Map<String, Object> signData = new LinkedHashMap<>();
        signData.put("offerId", config.getOfferId());
        signData.put("buyQuantity", buyQuantity);
        signData.put("env", config.getEnv());
        signData.put("currencyType", DEFAULT_CURRENCY_TYPE);
        signData.put("productId", productId);
        signData.put("goodsPrice", goodsPrice);
        signData.put("outTradeNo", outTradeNo);
        signData.put("attach", attach);
        return JsonUtils.toJsonString(signData);
    }

    private void validateExistingVirtualExtension(PayOrderExtensionDO extension, VirtualCheckout virtualCheckout) {
        if (!Objects.equals(extension.getWechatVirtualProductId(), virtualCheckout.productId())) {
            throw exception(WECHAT_VIRTUAL_PAY_EXTENSION_CONFLICT);
        }
        String storedQuantity = extension.getChannelExtras() == null ? null
                : extension.getChannelExtras().get(EXTRA_WECHAT_VIRTUAL_BUY_QUANTITY);
        if (storedQuantity != null && !Objects.equals(storedQuantity, String.valueOf(virtualCheckout.buyQuantity()))) {
            throw exception(WECHAT_VIRTUAL_PAY_EXTENSION_CONFLICT);
        }
        String storedGoodsPrice = extension.getChannelExtras() == null ? null
                : extension.getChannelExtras().get(EXTRA_WECHAT_VIRTUAL_GOODS_PRICE);
        if (storedGoodsPrice != null
                && !Objects.equals(storedGoodsPrice, String.valueOf(virtualCheckout.goodsPrice()))) {
            throw exception(WECHAT_VIRTUAL_PAY_EXTENSION_CONFLICT);
        }
    }

    private AppWechatVirtualPayCheckoutProfileRespVO virtualNotReadyProfile(Long payOrderId, String reason) {
        return new AppWechatVirtualPayCheckoutProfileRespVO().setPayOrderId(payOrderId)
                .setMode(AppWechatVirtualPayCheckoutProfileRespVO.MODE_VIRTUAL_NOT_READY)
                .setRequiredChannelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .setUnavailableReason(reason);
    }

    private CheckoutProductType getCheckoutProductType(List<SalesOrderItemDO> orderItems) {
        if (CollUtil.isEmpty(orderItems)) {
            return CheckoutProductType.INVALID;
        }
        boolean hasVirtualProduct = false;
        boolean hasOtherProduct = false;
        for (SalesOrderItemDO orderItem : orderItems) {
            ProductSpuDO spu = productSpuMapper.selectById(orderItem.getSpuId());
            if (spu == null) {
                return CheckoutProductType.INVALID;
            }
            if (Boolean.TRUE.equals(spu.getIsWechatMiniappVirtualGoods())) {
                hasVirtualProduct = true;
            } else {
                hasOtherProduct = true;
            }
        }
        if (hasVirtualProduct && hasOtherProduct) {
            return CheckoutProductType.MIXED;
        }
        return hasVirtualProduct ? CheckoutProductType.VIRTUAL : CheckoutProductType.MERCHANT;
    }

    private boolean queryAndConfirmWechatVirtualPay(PayOrderExtensionDO extension) {
        PayOrderDO payOrder = payOrderService.getOrder(extension.getOrderId());
        if (payOrder == null) {
            return false;
        }
        WxVirtualPayClientConfig config = getVirtualPayConfig(payOrder.getAppId());
        String openid = getWechatVirtualOpenid(extension, payOrder.getUserId());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("openid", openid);
        body.put("env", config.getEnv());
        body.put("order_id", extension.getNo());
        String bodyJson = toJsonString(body);
        String responseText = WxVirtualPayAccessTokenUtils.execute(socialClientService,
                accessToken -> createWxVirtualPayHttpClient().queryOrder(accessToken, config, bodyJson, null));
        Map<String, Object> response = JsonUtils.parseMap(responseText);
        Integer errCode = readInteger(response, "errcode", "ErrCode", "error_code", "ErrorCode");
        if (errCode != null && errCode != 0) {
            String errMsg = readString(response, "errmsg", "ErrMsg");
            if (Objects.equals(errCode, 268490002)
                    && StrUtil.contains(errMsg, "数据不存在")
                    && PayOrderStatusEnum.WAITING.getStatus().equals(payOrder.getStatus())) {
                // 用户提交支付参数后可能退出或取消，微信不会为这类本地扩展单保留可查询订单。
                // 它不是系统故障，保留有限次数补偿即可，避免历史未支付单每分钟打印异常栈。
                log.debug("[queryAndConfirmWechatVirtualPay][extension({}) 微信订单不存在，跳过本轮未支付单查询]",
                        extension.getId());
                return false;
            }
            throw new IllegalStateException("微信虚拟支付订单查询失败，errcode=" + errCode
                    + ", errmsg=" + errMsg);
        }
        Map<String, Object> order = readMap(response, "order", "Order");
        if (!isWechatQueryPaid(order)) {
            return false;
        }
        Integer status = readInteger(order, "status", "Status");
        log.info("[queryAndConfirmWechatVirtualPay][extension({}) 微信订单状态={}, retryCount={}]",
                extension.getId(), status,
                readExtensionInteger(extension, EXTRA_WECHAT_VIRTUAL_QUERY_RETRY_COUNT, 0));
        String channelOrderNo = firstNotBlank(readString(order, "wxpay_order_id", "transaction_id"),
                readString(order, "channel_order_id", "wx_order_id", "WxOrderId", "wxOrderId"),
                extension.getNo());
        confirmWechatVirtualPayOrder(extension, channelOrderNo, openid,
                readInteger(order, "paid_fee", "order_fee"), response);
        if (Objects.equals(status, 4)) {
            markWechatVirtualGoodsDelivered(extension, responseText);
        } else if (Objects.equals(status, 2) && shouldFallbackNotifyProvideGoods(extension)) {
            // 正常链路由 xpay_goods_deliver_notify 推送完成发货；持续查询仍停留在待发货时才调用兜底接口。
            notifyProvideGoods(extension, config);
        }
        return true;
    }

    private boolean shouldFallbackNotifyProvideGoods(PayOrderExtensionDO extension) {
        return readExtensionInteger(extension, EXTRA_WECHAT_VIRTUAL_QUERY_RETRY_COUNT, 0)
                >= WECHAT_VIRTUAL_MANUAL_DELIVER_RETRY_THRESHOLD;
    }

    private void notifyProvideGoods(PayOrderExtensionDO extension, WxVirtualPayClientConfig config) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("order_id", extension.getNo());
        body.put("env", config.getEnv());
        String bodyJson = toJsonString(body);
        String responseText = WxVirtualPayAccessTokenUtils.execute(socialClientService,
                accessToken -> createWxVirtualPayHttpClient().notifyProvideGoods(
                        accessToken, config, bodyJson, null));
        if (StrUtil.isNotBlank(responseText)) {
            Map<String, Object> response = JsonUtils.parseMap(responseText);
            Integer errCode = readInteger(response, "errcode", "ErrCode", "error_code", "ErrorCode");
            if (errCode != null && errCode != 0) {
                // 微信推送与主动兜底可能并发，非零返回不能直接判定永久发货失败，后续查询 order.status 对账。
                log.warn("[notifyProvideGoods][extension({}) 微信虚拟支付兜底发货失败，errcode={}, errmsg={}]",
                        extension.getId(), errCode, readString(response, "errmsg", "ErrMsg"));
                payOrderExtensionMapper.updateWechatVirtualDeliveryById(extension.getId(),
                        new PayOrderExtensionDO().setWechatVirtualDeliverNotifyData(responseText));
                return;
            }
        }
        markWechatVirtualGoodsDelivered(extension, responseText);
    }

    private void markWechatVirtualGoodsDelivered(PayOrderExtensionDO extension, String responseText) {
        // 允许把旧版本误标为 FAILED 的记录通过微信权威状态自动修复。
        payOrderExtensionMapper.updateWechatVirtualDeliveryById(extension.getId(), new PayOrderExtensionDO()
                .setWechatVirtualDeliverStatus(PayOrderWechatVirtualDeliverStatusEnum.DELIVERED.getStatus())
                .setWechatVirtualDeliverNotifyData(responseText)
                .setWechatVirtualDeliverConfirmTime(LocalDateTime.now()));
    }

    private boolean isWechatQueryPaid(Map<String, Object> order) {
        Integer status = readInteger(order, "status", "Status");
        // 微信官方状态：2 已支付待发货，3 发货中，4 已发货。
        return status != null && status >= 2 && status <= 4;
    }

    private String getWechatVirtualOpenid(PayOrderExtensionDO extension, Long userId) {
        String storedOpenid = extension.getChannelExtras() == null ? null
                : extension.getChannelExtras().get(EXTRA_WECHAT_VIRTUAL_OPENID);
        if (StrUtil.isNotBlank(storedOpenid)) {
            return storedOpenid;
        }
        SocialUserDO socialUser = CollUtil.findOne(
                socialUserService.getSocialUserList(userId, UserTypeEnum.MEMBER.getValue()),
                item -> Objects.equals(item.getType(), SocialTypeEnum.WECHAT_MINI_PROGRAM.getType())
                        && StrUtil.isNotBlank(item.getOpenid()));
        if (socialUser == null) {
            throw exception(WECHAT_VIRTUAL_PAY_SESSION_KEY_NOT_FOUND);
        }
        persistWechatVirtualOpenidIfAbsent(extension, socialUser.getOpenid());
        return socialUser.getOpenid();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGoodsDeliverBody(String rawBody, boolean xml) {
        if (StrUtil.isBlank(rawBody)) {
            throw exception(WECHAT_VIRTUAL_PAY_GOODS_DELIVER_MISMATCH);
        }
        if (!xml) {
            return JsonUtils.parseMap(rawBody);
        }
        Map<String, Object> parsed = XmlUtil.xmlToMap(rawBody);
        Object root = parsed.get("xml");
        if (root instanceof Map<?, ?> rootMap) {
            return (Map<String, Object>) rootMap;
        }
        return parsed;
    }

    private void validateGoodsDeliver(PayOrderExtensionDO extension, Map<String, Object> goodsInfo) {
        String productId = readString(goodsInfo, "ProductId", "product_id");
        Integer quantity = readInteger(goodsInfo, "Quantity", "quantity");
        Integer actualPrice = readInteger(goodsInfo, "ActualPrice", "actual_price");
        Integer expectedQuantity = readExtensionBuyQuantity(extension);
        if (!Objects.equals(extension.getWechatVirtualProductId(), productId)
                || !Objects.equals(expectedQuantity, quantity)) {
            throw exception(WECHAT_VIRTUAL_PAY_GOODS_DELIVER_MISMATCH);
        }
        if (actualPrice == null) {
            throw exception(WECHAT_VIRTUAL_PAY_AMOUNT_MISMATCH);
        }
    }

    private Integer readExtensionBuyQuantity(PayOrderExtensionDO extension) {
        String quantity = extension.getChannelExtras() == null ? null
                : extension.getChannelExtras().get(EXTRA_WECHAT_VIRTUAL_BUY_QUANTITY);
        if (StrUtil.isBlank(quantity)) {
            // 历史扩展单由旧版固定传递 1；新扩展单会在 channelExtras 中持久化精确数量。
            return 1;
        }
        try {
            int result = Integer.parseInt(quantity);
            if (result <= 0) {
                throw exception(WECHAT_VIRTUAL_PAY_BUY_QUANTITY_INVALID);
            }
            return result;
        } catch (NumberFormatException ex) {
            throw exception(WECHAT_VIRTUAL_PAY_BUY_QUANTITY_INVALID);
        }
    }

    private int readExtensionInteger(PayOrderExtensionDO extension, String key, int defaultValue) {
        String value = extension.getChannelExtras() == null ? null : extension.getChannelExtras().get(key);
        if (StrUtil.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            log.warn("[readExtensionInteger][extension({}) 的 {} 非法，按 {} 处理]", extension.getId(), key, defaultValue);
            return defaultValue;
        }
    }

    private String getSessionKey(Long userId, String openid) {
        List<SocialUserDO> socialUsers = socialUserService.getSocialUserList(userId, UserTypeEnum.MEMBER.getValue());
        SocialUserDO socialUser = CollUtil.findOne(socialUsers, item ->
                Objects.equals(item.getType(), SocialTypeEnum.WECHAT_MINI_PROGRAM.getType())
                        && Objects.equals(item.getOpenid(), openid));
        if (socialUser == null || StrUtil.isBlank(socialUser.getRawTokenInfo())) {
            throw exception(WECHAT_VIRTUAL_PAY_SESSION_KEY_NOT_FOUND);
        }
        Map<String, Object> tokenInfo;
        try {
            tokenInfo = JsonUtils.parseObject(socialUser.getRawTokenInfo(), Map.class);
        } catch (RuntimeException ex) {
            log.warn("[getSessionKey][userId({}) 的微信登录态数据格式错误]", userId);
            throw exception(WECHAT_VIRTUAL_PAY_SESSION_KEY_NOT_FOUND);
        }
        String sessionKey = readString(tokenInfo, "session_key", "sessionKey");
        if (StrUtil.isBlank(sessionKey)) {
            throw exception(WECHAT_VIRTUAL_PAY_SESSION_KEY_NOT_FOUND);
        }
        return sessionKey;
    }

    private String readString(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            Object value = body.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private Integer readInteger(Map<String, Object> body, String... keys) {
        String value = readString(body, keys);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readMap(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            Object value = body.get(key);
            if (value instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
        }
        return Map.of();
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private Map<String, Object> successNotify() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ErrCode", 0);
        result.put("ErrMsg", "success");
        return result;
    }

    private record VirtualCheckout(String productId, Integer buyQuantity, Integer goodsPrice) {
    }

    private enum CheckoutProductType {
        MERCHANT, VIRTUAL, MIXED, INVALID
    }

}
