package vip.appap.suxin.module.sales.convert;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.string.StrUtils;
import vip.appap.suxin.framework.dict.core.DictFrameworkUtils;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.module.partner.api.dto.PartnerAddressRespDTO;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.accountant.api.dto.PayOrderCreateReqDTO;
import vip.appap.suxin.module.accountant.enums.DictTypeConstants;
import vip.appap.suxin.module.partner.dal.dataobject.SalesCartDO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.api.dto.ProductCommentCreateReqDTO;
import vip.appap.suxin.module.product.api.dto.ProductPropertyValueDetailRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductSkuUpdateStockReqDTO;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationRecordCreateReqDTO;
import vip.appap.suxin.module.sales.api.dto.SalesOrderRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderPartnerRespVO;
import vip.appap.suxin.module.sales.controller.admin.SalesProductPropertyValueDetailRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.controller.app.AppSalesProductPropertyValueDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.*;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderItemCommentCreateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderItemRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderLogDO;
import vip.appap.suxin.module.sales.enums.SalesOrderItemAfterSaleStatusEnum;
import vip.appap.suxin.module.sales.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import vip.appap.suxin.module.sales.framework.order.config.SalesOrderProperties;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageAddReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils.addTime;

@Mapper
public interface SalesOrderConvert {

    SalesOrderConvert INSTANCE = Mappers.getMapper(SalesOrderConvert.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "userId", target = "userId"),
            @Mapping(source = "createReqVO.couponId", target = "couponId"),
            @Mapping(target = "remark", ignore = true),
            @Mapping(source = "createReqVO.remark", target = "userRemark"),
            @Mapping(source = "calculateRespBO.price.totalPrice", target = "totalPrice"),
            @Mapping(source = "calculateRespBO.price.discountPrice", target = "discountPrice"),
            @Mapping(source = "calculateRespBO.price.deliveryPrice", target = "deliveryPrice"),
            @Mapping(source = "calculateRespBO.price.couponPrice", target = "couponPrice"),
            @Mapping(source = "calculateRespBO.price.pointPrice", target = "pointPrice"),
            @Mapping(source = "calculateRespBO.price.vipPrice", target = "vipPrice"),
            @Mapping(source = "calculateRespBO.price.payPrice", target = "payPrice")
    })
    SalesOrderDO convert(Long userId, AppSalesOrderCreateReqVO createReqVO, SalesPriceCalculateRespBO calculateRespBO);

    SalesOrderRespDTO convert(SalesOrderDO orderDO);

    default List<SalesOrderItemDO> convertList(SalesOrderDO tradeOrderDO, SalesPriceCalculateRespBO calculateRespBO) {
        return CollectionUtils.convertList(calculateRespBO.getItems(), item -> {
            SalesOrderItemDO orderItem = convert(item);
            orderItem.setOrderId(tradeOrderDO.getId());
            orderItem.setUserId(tradeOrderDO.getUserId());
            orderItem.setAfterSaleStatus(SalesOrderItemAfterSaleStatusEnum.NONE.getStatus());
            orderItem.setCommentStatus(false);
            return orderItem;
        });
    }

    SalesOrderItemDO convert(SalesPriceCalculateRespBO.OrderItem item);

    default ProductSkuUpdateStockReqDTO convert(List<SalesOrderItemDO> list) {
        List<ProductSkuUpdateStockReqDTO.Item> items = CollectionUtils.convertList(list, item ->
                new ProductSkuUpdateStockReqDTO.Item().setId(item.getSkuId()).setIncrCount(item.getCount()));
        return new ProductSkuUpdateStockReqDTO(items);
    }

    default ProductSkuUpdateStockReqDTO convertNegative(List<SalesOrderItemDO> list) {
        List<ProductSkuUpdateStockReqDTO.Item> items = CollectionUtils.convertList(list, item ->
                new ProductSkuUpdateStockReqDTO.Item().setId(item.getSkuId()).setIncrCount(-item.getCount()));
        return new ProductSkuUpdateStockReqDTO(items);
    }

    default PayOrderCreateReqDTO convert(SalesOrderDO order, List<SalesOrderItemDO> orderItems,
                                         SalesOrderProperties orderProperties) {
        PayOrderCreateReqDTO createReqDTO = new PayOrderCreateReqDTO()
                .setAppKey(orderProperties.getPayAppKey()).setUserIp(order.getUserIp())
                .setUserId(order.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue());
        // 商户相关字段
        createReqDTO.setMerchantOrderId(String.valueOf(order.getId()));
        String subject = orderItems.get(0).getSpuName();
        subject = StrUtils.maxLength(subject, PayOrderCreateReqDTO.SUBJECT_MAX_LENGTH); // 避免超过 32 位
        createReqDTO.setSubject(subject);
        createReqDTO.setBody(subject); // TODO 芋艿：临时写死
        // 订单相关字段
        createReqDTO.setPrice(order.getPayPrice()).setExpireTime(addTime(orderProperties.getPayExpireTime()));
        return createReqDTO;
    }

    default PageResult<SalesOrderPageItemRespVO> convertPage(PageResult<SalesOrderDO> pageResult,
                                                             List<SalesOrderItemDO> orderItems,
                                                             Map<Long, PartnerRespDTO> PartnerUserMap) {
        Map<Long, List<SalesOrderItemDO>> orderItemMap = convertMultiMap(orderItems, SalesOrderItemDO::getOrderId);
        // 转化 List
        List<SalesOrderPageItemRespVO> orderVOs = CollectionUtils.convertList(pageResult.getList(), order -> {
            List<SalesOrderItemDO> xOrderItems = orderItemMap.get(order.getId());
            SalesOrderPageItemRespVO orderVO = convert(order, xOrderItems);
            // 处理收货地址
            orderVO.setReceiverAreaName(AreaUtils.format(order.getReceiverAreaId()));
            // 增加用户信息
            orderVO.setUser(convertUser(PartnerUserMap.get(orderVO.getUserId())));
            // 增加推广人信息
            orderVO.setBrokerageUser(convertUser(PartnerUserMap.get(orderVO.getBrokerageUserId())));
            return orderVO;
        });
        return new PageResult<>(orderVOs, pageResult.getTotal());
    }

    SalesOrderPartnerRespVO convertUser(PartnerRespDTO PartnerRespDTO);

    SalesOrderPageItemRespVO convert(SalesOrderDO order, List<SalesOrderItemDO> items);

    SalesProductPropertyValueDetailRespVO convert(ProductPropertyValueDetailRespDTO bean);

    default SalesOrderDetailRespVO convert(SalesOrderDO order, List<SalesOrderItemDO> orderItems,
                                           List<SalesOrderLogDO> orderLogs,
                                           PartnerRespDTO user, PartnerRespDTO brokerageUser) {
        SalesOrderDetailRespVO orderVO = convert2(order, orderItems);
        // 处理收货地址
        orderVO.setReceiverAreaName(AreaUtils.format(order.getReceiverAreaId()));
        // 处理用户信息
        orderVO.setUser(convert(user));
        orderVO.setBrokerageUser(convert(brokerageUser));
        // 处理日志
        orderVO.setLogs(convertList03(orderLogs));
        return orderVO;
    }
    List<SalesOrderDetailRespVO.OrderLog> convertList03(List<SalesOrderLogDO> orderLogs);

    SalesOrderDetailRespVO convert2(SalesOrderDO order, List<SalesOrderItemDO> items);

    SalesOrderPartnerRespVO convert(PartnerRespDTO bean);

    default PageResult<AppSalesOrderPageItemRespVO> convertPage02(PageResult<SalesOrderDO> pageResult,
                                                                  List<SalesOrderItemDO> orderItems) {
        Map<Long, List<SalesOrderItemDO>> orderItemMap = convertMultiMap(orderItems, SalesOrderItemDO::getOrderId);
        // 转化 List
        List<AppSalesOrderPageItemRespVO> orderVOs = CollectionUtils.convertList(pageResult.getList(), order -> {
            List<SalesOrderItemDO> xOrderItems = orderItemMap.get(order.getId());
            return convert02(order, xOrderItems);
        });
        return new PageResult<>(orderVOs, pageResult.getTotal());
    }

    AppSalesOrderPageItemRespVO convert02(SalesOrderDO order, List<SalesOrderItemDO> items);

    AppSalesProductPropertyValueDetailRespVO convert02(ProductPropertyValueDetailRespDTO bean);

    default AppSalesOrderDetailRespVO convert02(SalesOrderDO order, List<SalesOrderItemDO> orderItems,
                                                SalesOrderProperties tradeOrderProperties,
                                                SalesDeliveryExpressDO express) {
        AppSalesOrderDetailRespVO orderVO = convert3(order, orderItems);
        orderVO.setPayExpireTime(order.getCreateTime().plus(tradeOrderProperties.getPayExpireTime()));
        if (StrUtil.isNotEmpty(order.getPayChannelCode())) {
            orderVO.setPayChannelName(DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.CHANNEL_CODE, order.getPayChannelCode()));
        }
        // 处理收货地址
        orderVO.setReceiverAreaName(AreaUtils.format(order.getReceiverAreaId()));
        if (express != null) {
            orderVO.setLogisticsId(express.getId()).setLogisticsName(express.getName());
        }
        return orderVO;
    }

    AppSalesOrderDetailRespVO convert3(SalesOrderDO order, List<SalesOrderItemDO> items);

    AppSalesOrderItemRespVO convert03(SalesOrderItemDO bean);

    @Mappings({
            @Mapping(target = "skuId", source = "tradeOrderItemDO.skuId"),
            @Mapping(target = "orderId", source = "tradeOrderItemDO.orderId"),
            @Mapping(target = "orderItemId", source = "tradeOrderItemDO.id"),
            @Mapping(target = "descriptionScores", source = "createReqVO.descriptionScores"),
            @Mapping(target = "benefitScores", source = "createReqVO.benefitScores"),
            @Mapping(target = "content", source = "createReqVO.content"),
            @Mapping(target = "picUrls", source = "createReqVO.picUrls"),
            @Mapping(target = "anonymous", source = "createReqVO.anonymous"),
            @Mapping(target = "userId", source = "tradeOrderItemDO.userId")
    })
    ProductCommentCreateReqDTO convert04(AppSalesOrderItemCommentCreateReqVO createReqVO, SalesOrderItemDO tradeOrderItemDO);

    SalesPriceCalculateReqBO convert(AppSalesOrderSettlementReqVO settlementReqVO);

    default SalesPriceCalculateReqBO convert(Long userId, AppSalesOrderSettlementReqVO settlementReqVO,
                                             List<SalesCartDO> cartList) {
        SalesPriceCalculateReqBO reqBO = new SalesPriceCalculateReqBO().setUserId(userId)
                .setItems(new ArrayList<>(settlementReqVO.getItems().size()))
                .setCouponId(settlementReqVO.getCouponId()).setPointStatus(settlementReqVO.getPointStatus())
                // 物流信息
                .setDeliveryType(settlementReqVO.getDeliveryType()).setAddressId(settlementReqVO.getAddressId())
                .setPickUpStoreId(settlementReqVO.getPickUpStoreId())
                // 各种活动
                .setSeckillActivityId(settlementReqVO.getSeckillActivityId())
                .setBargainRecordId(settlementReqVO.getBargainRecordId())
                .setCombinationActivityId(settlementReqVO.getCombinationActivityId())
                .setCombinationHeadId(settlementReqVO.getCombinationHeadId())
                .setPointActivityId(settlementReqVO.getPointActivityId());
        Map<Long, SalesCartDO> cartMap = convertMap(cartList, SalesCartDO::getId);
        for (AppSalesOrderSettlementReqVO.Item item : settlementReqVO.getItems()) {
            if (item.getCartId() != null) {
                SalesCartDO cart = cartMap.get(item.getCartId());
                if (cart != null) {
                    reqBO.getItems().add(new SalesPriceCalculateReqBO.Item()
                            .setSkuId(cart.getSkuId())
                            .setCount(cart.getCount())
                            .setCartId(cart.getId())
                            .setSelected(true));
                }
                continue;
            }
            if (item.getSkuId() != null) {
                reqBO.getItems().add(new SalesPriceCalculateReqBO.Item()
                        .setSkuId(item.getSkuId())
                        .setCount(item.getCount())
                        .setSelected(true));
            }
        }
        return reqBO;
    }

    default AppSalesOrderSettlementRespVO convert(SalesPriceCalculateRespBO calculate, PartnerAddressRespDTO address) {
        AppSalesOrderSettlementRespVO respVO = convert0(calculate, address);
        if (address != null) {
            respVO.getAddress().setAreaName(AreaUtils.format(address.getAreaId()));
        }
        return respVO;
    }

    @Mapping(source = "calculate.type", target = "type") // 订单类型来源为计价结果；PartnerAddressRespDTO.type 为地址类型，避免歧义
    AppSalesOrderSettlementRespVO convert0(SalesPriceCalculateRespBO calculate, PartnerAddressRespDTO address);

    List<AppSalesOrderExpressTrackRespDTO> convertList02(List<ExpressTrackRespDTO> list);

    SalesOrderDO convert(SalesOrderUpdateAddressReqVO reqVO);

    SalesOrderDO convert(SalesOrderUpdatePriceReqVO reqVO);

    SalesOrderDO convert(SalesOrderRemarkReqVO reqVO);

    default SalesBrokerageAddReqBO convert(PartnerRespDTO user, SalesOrderItemDO item,
                                      ProductSpuRespDTO spu, ProductSkuRespDTO sku) {
        SalesBrokerageAddReqBO bo = new SalesBrokerageAddReqBO().setBizId(String.valueOf(item.getId())).setSourceUserId(item.getUserId())
                .setBasePrice(item.getPayPrice())
                .setTitle(StrUtil.format("{}成功购买{}", user.getNickname(), item.getSpuName()));
        if (BooleanUtil.isTrue(spu.getSubCommissionType())) {
            // 特殊：单独设置的佣金需要乘以购买数量。关联 https://gitee.com/yudaocode/yudao-mall-uniapp/issues/ICY7SJ
            bo.setFirstFixedPrice(sku.getFirstBrokeragePrice() * item.getCount())
                    .setSecondFixedPrice(sku.getSecondBrokeragePrice() * item.getCount());
        }
        return bo;
    }

    @Named("convertList04")
    List<SalesOrderRespDTO> convertList04(List<SalesOrderDO> list);

    @Mappings({
            @Mapping(target = "activityId", source = "order.combinationActivityId"),
            @Mapping(target = "spuId", source = "item.spuId"),
            @Mapping(target = "skuId", source = "item.skuId"),
            @Mapping(target = "count", source = "item.count"),
            @Mapping(target = "orderId", source = "order.id"),
            @Mapping(target = "userId", source = "order.userId"),
            @Mapping(target = "headId", source = "order.combinationHeadId"),
            @Mapping(target = "combinationPrice", source = "item.payPrice"),
    })
    SalesCombinationRecordCreateReqDTO convert(SalesOrderDO order, SalesOrderItemDO item);

}

