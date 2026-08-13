package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.module.partner.api.PartnerAddressApi;
import vip.appap.suxin.module.partner.api.dto.PartnerAddressRespDTO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryPickUpStoreDO;
import vip.appap.suxin.module.sales.enums.SalesDeliveryExpressChargeModeEnum;
import vip.appap.suxin.module.sales.enums.SalesDeliveryTypeEnum;
import vip.appap.suxin.module.sales.service.SalesConfigService;
import vip.appap.suxin.module.sales.service.SalesDeliveryExpressTemplateService;
import vip.appap.suxin.module.sales.service.SalesDeliveryPickUpStoreService;
import vip.appap.suxin.module.sales.service.bo.SalesDeliveryExpressTemplateRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO.OrderItem;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.*;

/**
 * 运费的 {@link SalesPriceCalculator} 实现类
 *
 * @author jason
 */
@Component
@Order(SalesPriceCalculator.ORDER_DELIVERY)
@Slf4j
public class SalesDeliveryPriceCalculator implements SalesPriceCalculator {

    @Resource
    private PartnerAddressApi addressApi;

    @Resource
    private SalesDeliveryPickUpStoreService deliveryPickUpStoreService;
    @Resource
    private SalesDeliveryExpressTemplateService deliveryExpressTemplateService;
    @Resource
    private SalesConfigService tradeConfigService;

    @Override
    public void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        if (param.getDeliveryType() == null) {
            return;
        }
        // 校验是不是存在商品不能门店自提，或者不能快递发货的情况。就是说，配送方式不匹配哈
        if (CollectionUtils.anyMatch(result.getItems(), item -> !item.getDeliveryTypes().contains(param.getDeliveryType()))) {
            throw exception(PRICE_CALCULATE_DELIVERY_PRICE_TYPE_ILLEGAL);
        }

        if (SalesDeliveryTypeEnum.ONLINE.getType().equals(param.getDeliveryType())
                || SalesDeliveryTypeEnum.AUTO.getType().equals(param.getDeliveryType())) {
            return;
        } else if (SalesDeliveryTypeEnum.PICK_UP.getType().equals(param.getDeliveryType())) {
            calculateByPickUp(param);
        } else if (SalesDeliveryTypeEnum.EXPRESS.getType().equals(param.getDeliveryType())) {
            calculateExpress(param, result);
        }
    }

    private void calculateByPickUp(SalesPriceCalculateReqBO param) {
        if (param.getPickUpStoreId() == null) {
            // 价格计算时，如果为空就不算~最终下单，会校验该字段不允许空
            return;
        }
        SalesDeliveryPickUpStoreDO pickUpStore = deliveryPickUpStoreService.getDeliveryPickUpStore(param.getPickUpStoreId());
        if (pickUpStore == null || CommonStatusEnum.DISABLE.getStatus().equals(pickUpStore.getStatus())) {
            throw exception(PICK_UP_STORE_NOT_EXISTS);
        }
    }

    // ========= 快递发货 ==========

    private void calculateExpress(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        // 0. 得到收件地址区域
        if (param.getAddressId() == null) {
            // 价格计算时，如果为空就不算~最终下单，会校验该字段不允许空
            return;
        }
        PartnerAddressRespDTO address = addressApi.getAddress(param.getAddressId(), param.getUserId());
        Assert.notNull(address, "收件人({})的地址，不能为空", param.getUserId());

        // 情况一：全局包邮
        if (isGlobalExpressFree(result)) {
            return;
        }

        // 情况二：活动包邮
        if (Boolean.TRUE.equals(result.getFreeDelivery())) {
            return;
        }

        // 情况三：快递模版
        // 2.1 过滤出已选中的商品 SKU
        List<OrderItem> selectedItem = filterList(result.getItems(), OrderItem::getSelected);
        Set<Long> deliveryTemplateIds = convertSet(selectedItem, OrderItem::getDeliveryTemplateId);
        Map<Long, SalesDeliveryExpressTemplateRespBO> expressTemplateMap =
                deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(deliveryTemplateIds, address.getAreaId());
        // 2.2 计算配送费用
        if (CollUtil.isEmpty(expressTemplateMap)) {
            log.error("[calculate][找不到商品 templateIds {} areaId{} 对应的运费模板]", deliveryTemplateIds, address.getAreaId());
            throw exception(PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND);
        }
        calculateDeliveryPrice(selectedItem, expressTemplateMap, result);
    }

    /**
     * 是否全局包邮
     *
     * @param result 计算结果
     * @return 是否包邮
     */
    private boolean isGlobalExpressFree(SalesPriceCalculateRespBO result) {
        SalesConfigDO config = tradeConfigService.getTradeConfig();
        // 情况一：交易中心配置不存在默认不包邮
        if (config == null) {
            return false;
        }
        // 情况二：开启了全局包邮 && 满足包邮金额
        return Boolean.TRUE.equals(config.getDeliveryExpressFreeEnabled()) &&
                result.getPrice().getPayPrice() >= config.getDeliveryExpressFreePrice();
    }

    private void calculateDeliveryPrice(List<OrderItem> selectedSkus,
                                        Map<Long, SalesDeliveryExpressTemplateRespBO> expressTemplateMap,
                                        SalesPriceCalculateRespBO result) {
        // 按商品运费模板来计算商品的运费：相同的运费模板可能对应多条订单商品 SKU
        Map<Long, List<OrderItem>> template2ItemMap = convertMultiMap(selectedSkus, OrderItem::getDeliveryTemplateId);
        // 依次计算快递运费
        for (Map.Entry<Long, List<OrderItem>> entry : template2ItemMap.entrySet()) {
            Long templateId = entry.getKey();
            List<OrderItem> orderItems = entry.getValue();
            SalesDeliveryExpressTemplateRespBO templateBO = expressTemplateMap.get(templateId);
            if (templateBO == null) {
                // 失败关闭：单个模板缺失不得按零运费继续
                throw exception(EXPRESS_DELIVERY_TEMPLATE_MISSING, templateId);
            }
            // 1. 优先判断是否包邮。如果包邮不计算快递运费
            if (isExpressTemplateFree(orderItems, templateBO.getChargeMode(), templateBO.getFree())) {
                continue;
            }
            // 2. 计算快递运费
            calculateExpressFeeByChargeMode(orderItems, templateBO.getChargeMode(), templateBO.getCharge());
        }
        SalesPriceCalculatorHelper.recountAllPrice(result);
    }

    /**
     * 按配送方式来计算运费
     *
     * @param orderItems     SKU 商品项目
     * @param chargeMode     配送计费方式
     * @param templateCharge 快递运费配置
     */
    private void calculateExpressFeeByChargeMode(List<OrderItem> orderItems, Integer chargeMode,
                                                 SalesDeliveryExpressTemplateRespBO.Charge templateCharge) {
        if (templateCharge == null) {
            // 失败关闭：区域计费规则缺失不得按零运费继续
            throw exception(EXPRESS_DELIVERY_CHARGE_RULE_NOT_FOUND);
        }
        // 失败关闭：非法计费参数（首量/续量/费用非正）不得除零或产生错误结果
        if (templateCharge.getStartCount() == null || templateCharge.getStartCount() <= 0
                || templateCharge.getExtraCount() == null || templateCharge.getExtraCount() <= 0
                || templateCharge.getStartPrice() == null || templateCharge.getStartPrice() <= 0
                || templateCharge.getExtraPrice() == null || templateCharge.getExtraPrice() <= 0) {
            throw exception(EXPRESS_DELIVERY_CHARGE_PARAM_NOT_POSITIVE);
        }
        double totalChargeValue = getTotalChargeValue(orderItems, chargeMode);
        // 失败关闭：总计费量非正不得继续
        if (totalChargeValue <= 0) {
            throw exception(EXPRESS_DELIVERY_CHARGE_VALUE_NOT_POSITIVE);
        }
        // 1. 计算 SKU 商品快递费用
        int deliveryPrice;
        if (totalChargeValue <= templateCharge.getStartCount()) {
            deliveryPrice = templateCharge.getStartPrice();
        } else {
            double remainWeight = totalChargeValue - templateCharge.getStartCount();
            // 剩余重量/ 续件 = 续件的次数. 向上取整
            int extraNum = (int) Math.ceil(remainWeight / templateCharge.getExtraCount());
            int extraPrice = templateCharge.getExtraPrice() * extraNum;
            deliveryPrice = templateCharge.getStartPrice() + extraPrice;
        }

        // 2. 分摊快递费用到 SKU. 退费的时候，可能按照 SKU 考虑退费金额
        int remainPrice = deliveryPrice;
        for (int i = 0; i < orderItems.size(); i++) {
            SalesPriceCalculateRespBO.OrderItem item = orderItems.get(i);
            int partPrice;
            double chargeValue = getChargeValue(item, chargeMode);
            if (i < orderItems.size() - 1) { // 减一的原因，是因为拆分时，如果按照比例，可能会出现.所以最后一个，使用反减
                partPrice = (int) (deliveryPrice * (chargeValue / totalChargeValue));
                remainPrice -= partPrice;
            } else {
                partPrice = remainPrice;
            }
            Assert.isTrue(partPrice >= 0, "分摊金额必须大于等于 0");
            // 更新快递运费
            item.setDeliveryPrice(partPrice);
            SalesPriceCalculatorHelper.recountPayPrice(item);
        }
    }

    /**
     * 检查是否包邮
     *
     * @param chargeMode   配送计费方式
     * @param templateFree 包邮配置
     */
    private boolean isExpressTemplateFree(List<OrderItem> orderItems, Integer chargeMode,
                                          SalesDeliveryExpressTemplateRespBO.Free templateFree) {
        if (templateFree == null) {
            return false;
        }
        double totalChargeValue = getTotalChargeValue(orderItems, chargeMode);
        double totalPrice = SalesPriceCalculatorHelper.calculateTotalPayPrice(orderItems);
        // 包邮条件：总计费量 >= 包邮数量阈值，且应付金额 >= 包邮金额阈值
        return totalChargeValue >= templateFree.getFreeCount() && totalPrice >= templateFree.getFreePrice();
    }

    private double getTotalChargeValue(List<OrderItem> orderItems, Integer chargeMode) {
        double total = 0;
        for (OrderItem orderItem : orderItems) {
            total += getChargeValue(orderItem, chargeMode);
        }
        return total;
    }

    private double getChargeValue(OrderItem orderItem, Integer chargeMode) {
        SalesDeliveryExpressChargeModeEnum chargeModeEnum = SalesDeliveryExpressChargeModeEnum.valueOf(chargeMode);
        if (chargeModeEnum == null) {
            throw exception(EXPRESS_DELIVERY_CHARGE_MODE_ILLEGAL, chargeMode);
        }
        switch (chargeModeEnum) {
            case COUNT:
                return orderItem.getCount();
            case WEIGHT:
                if (orderItem.getWeight() == null) {
                    throw exception(EXPRESS_DELIVERY_METRIC_MISSING);
                }
                return orderItem.getWeight() * orderItem.getCount();
            case VOLUME:
                if (orderItem.getVolume() == null) {
                    throw exception(EXPRESS_DELIVERY_METRIC_MISSING);
                }
                return orderItem.getVolume() * orderItem.getCount();
            default:
                throw exception(EXPRESS_DELIVERY_CHARGE_MODE_ILLEGAL, chargeMode);
        }
    }

}
