package vip.appap.suxin.module.sales.service;

import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.api.PartnerAddressApi;
import vip.appap.suxin.module.partner.api.dto.PartnerAddressRespDTO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryPickUpStoreDO;
import vip.appap.suxin.module.sales.enums.SalesDeliveryExpressChargeModeEnum;
import vip.appap.suxin.module.sales.enums.SalesDeliveryTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesDeliveryExpressTemplateRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static vip.appap.suxin.framework.common.util.collection.SetUtils.asSet;
import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_DELIVERY_CHARGE_MODE_ILLEGAL;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_DELIVERY_CHARGE_PARAM_NOT_POSITIVE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_DELIVERY_CHARGE_RULE_NOT_FOUND;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_DELIVERY_CHARGE_VALUE_NOT_POSITIVE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_DELIVERY_METRIC_MISSING;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_DELIVERY_TEMPLATE_MISSING;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PICK_UP_STORE_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PRICE_CALCULATE_DELIVERY_PRICE_TYPE_ILLEGAL;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link SalesDeliveryPriceCalculator} 的单元测试
 *
 * 按原版 {@code TradeDeliveryPriceCalculatorTest} 场景，按当前 BO、partner 地址 API、Jakarta 与 sales 错误码重建，
 * 并增加“总计费量小于包邮阈值不得包邮”与失败关闭场景。
 */
public class SalesDeliveryPriceCalculatorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesDeliveryPriceCalculator calculator;

    @Mock
    private PartnerAddressApi addressApi;
    @Mock
    private SalesDeliveryPickUpStoreService deliveryPickUpStoreService;
    @Mock
    private SalesDeliveryExpressTemplateService deliveryExpressTemplateService;
    @Mock
    private SalesConfigService tradeConfigService;

    private SalesPriceCalculateReqBO reqBO;
    private SalesPriceCalculateRespBO resultBO;

    private SalesDeliveryExpressTemplateRespBO templateRespBO;
    private SalesDeliveryExpressTemplateRespBO.Charge chargeBO;
    private SalesDeliveryExpressTemplateRespBO.Free freeBO;

    @BeforeEach
    public void init() {
        // 准备参数：2 个选中 + 1 个未选中
        reqBO = new SalesPriceCalculateReqBO()
                .setDeliveryType(SalesDeliveryTypeEnum.EXPRESS.getType())
                .setAddressId(10L)
                .setUserId(1L)
                .setItems(asList(
                        new SalesPriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true),
                        new SalesPriceCalculateReqBO.Item().setSkuId(20L).setCount(10).setSelected(true),
                        new SalesPriceCalculateReqBO.Item().setSkuId(30L).setCount(4).setSelected(false)
                ));
        resultBO = new SalesPriceCalculateRespBO()
                .setPrice(new SalesPriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(asList(
                        new SalesPriceCalculateRespBO.OrderItem().setDeliveryTemplateId(1L).setSkuId(10L).setCount(2).setSelected(true)
                                .setWeight(10d).setVolume(10d).setPrice(100)
                                .setDeliveryTypes(asList(SalesDeliveryTypeEnum.EXPRESS.getType(), SalesDeliveryTypeEnum.PICK_UP.getType())),
                        new SalesPriceCalculateRespBO.OrderItem().setDeliveryTemplateId(1L).setSkuId(20L).setCount(10).setSelected(true)
                                .setWeight(10d).setVolume(10d).setPrice(200)
                                .setDeliveryTypes(asList(SalesDeliveryTypeEnum.EXPRESS.getType(), SalesDeliveryTypeEnum.PICK_UP.getType())),
                        new SalesPriceCalculateRespBO.OrderItem().setDeliveryTemplateId(1L).setSkuId(30L).setCount(1).setSelected(false)
                                .setWeight(10d).setVolume(10d).setPrice(300)
                                .setDeliveryTypes(asList(SalesDeliveryTypeEnum.EXPRESS.getType(), SalesDeliveryTypeEnum.PICK_UP.getType()))
                ));
        // 初始化价格
        SalesPriceCalculatorHelper.recountPayPrice(resultBO.getItems());
        SalesPriceCalculatorHelper.recountAllPrice(resultBO);

        // 起步 10 件 1000 分，续 10 件 2000 分
        chargeBO = new SalesDeliveryExpressTemplateRespBO.Charge()
                .setStartCount(10d).setStartPrice(1000).setExtraCount(10d).setExtraPrice(2000);
        // 默认不包邮：订单总件数 12 < 包邮件数 20
        freeBO = new SalesDeliveryExpressTemplateRespBO.Free().setFreeCount(20).setFreePrice(100);
        templateRespBO = new SalesDeliveryExpressTemplateRespBO()
                .setChargeMode(SalesDeliveryExpressChargeModeEnum.COUNT.getType())
                .setCharge(chargeBO).setFree(freeBO);
    }

    // ========== 配送方式校验 ==========

    @Test
    @DisplayName("配送方式为空：直接返回，不计算运费")
    public void testCalculate_deliveryTypeNull() {
        reqBO.setDeliveryType(null);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("配送方式与商品不匹配：抛出异常")
    public void testCalculate_deliveryTypeMismatch() {
        resultBO.getItems().forEach(item -> item.setDeliveryTypes(singletonList(SalesDeliveryTypeEnum.PICK_UP.getType())));
        assertServiceException(() -> calculator.calculate(reqBO, resultBO),
                PRICE_CALCULATE_DELIVERY_PRICE_TYPE_ILLEGAL);
    }

    @Test
    @DisplayName("线上配送：直接返回，不计算运费")
    public void testCalculate_onlineDelivery_keepsFreightZero() {
        reqBO.setDeliveryType(SalesDeliveryTypeEnum.ONLINE.getType());
        resultBO.getItems().forEach(item -> item.setDeliveryTypes(singletonList(SalesDeliveryTypeEnum.ONLINE.getType())));
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("自动配送：直接返回，不计算运费")
    public void testCalculate_autoDelivery_keepsFreightZero() {
        reqBO.setDeliveryType(SalesDeliveryTypeEnum.AUTO.getType());
        resultBO.getItems().forEach(item -> item.setDeliveryTypes(singletonList(SalesDeliveryTypeEnum.AUTO.getType())));
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    // ========== 自提模式 ==========

    @Test
    @DisplayName("自提：未选择门店时直接返回")
    public void testCalculate_pickUp_storeIdNull() {
        reqBO.setDeliveryType(SalesDeliveryTypeEnum.PICK_UP.getType()).setPickUpStoreId(null);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("自提：门店不存在时抛出异常")
    public void testCalculate_pickUp_storeNotFound() {
        reqBO.setDeliveryType(SalesDeliveryTypeEnum.PICK_UP.getType()).setPickUpStoreId(99L);
        when(deliveryPickUpStoreService.getDeliveryPickUpStore(eq(99L))).thenReturn(null);
        assertServiceException(() -> calculator.calculate(reqBO, resultBO), PICK_UP_STORE_NOT_EXISTS);
    }

    @Test
    @DisplayName("自提：门店被禁用时抛出异常")
    public void testCalculate_pickUp_storeDisabled() {
        reqBO.setDeliveryType(SalesDeliveryTypeEnum.PICK_UP.getType()).setPickUpStoreId(99L);
        when(deliveryPickUpStoreService.getDeliveryPickUpStore(eq(99L))).thenReturn(
                new SalesDeliveryPickUpStoreDO().setStatus(CommonStatusEnum.DISABLE.getStatus()));
        assertServiceException(() -> calculator.calculate(reqBO, resultBO), PICK_UP_STORE_NOT_EXISTS);
    }

    @Test
    @DisplayName("自提：门店正常时不计算运费")
    public void testCalculate_pickUp_ok() {
        reqBO.setDeliveryType(SalesDeliveryTypeEnum.PICK_UP.getType()).setPickUpStoreId(99L);
        when(deliveryPickUpStoreService.getDeliveryPickUpStore(eq(99L))).thenReturn(
                new SalesDeliveryPickUpStoreDO().setStatus(CommonStatusEnum.ENABLE.getStatus()));
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    // ========== 快递模式 ==========

    @Test
    @DisplayName("快递：收件地址为空时直接返回")
    public void testCalculate_express_addressIdNull() {
        reqBO.setAddressId(null);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("快递：全场包邮（开关开启 && 总金额满足全场包邮金额）")
    public void testCalculate_globalFree() {
        mockAddress();
        when(tradeConfigService.getTradeConfig()).thenReturn(new SalesConfigDO()
                .setDeliveryExpressFreeEnabled(true).setDeliveryExpressFreePrice(2200));
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("快递：活动包邮（freeDelivery=true）时不计算运费")
    public void testCalculate_activityFree() {
        resultBO.setFreeDelivery(true);
        mockAddress();
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("快递：找不到运费模板（Map 全空）时抛出异常")
    public void testCalculate_express_templateNotFound() {
        mockAddress();
        when(deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(eq(asSet(1L)), eq(10)))
                .thenReturn(Collections.emptyMap());
        assertServiceException(() -> calculator.calculate(reqBO, resultBO),
                PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND);
    }

    @Test
    @DisplayName("按件计算运费：不包邮的情况（总计费量 12 < 包邮件数 20）")
    public void testCalculate_expressTemplateCharge() {
        // 运费：首件 1000 + 续件 ceil((12-10)/10)=1 * 2000 = 3000
        mockAddress();
        mockTemplate(templateRespBO);
        calculator.calculate(reqBO, resultBO);

        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(3000);
        // SKU1 分摊 (int)(3000 * 2/12)=500；SKU2 反减 2500；SKU3 未选中 0
        assertThat(resultBO.getItems().get(0).getDeliveryPrice()).isEqualTo(500);
        assertThat(resultBO.getItems().get(1).getDeliveryPrice()).isEqualTo(2500);
        assertThat(resultBO.getItems().get(2).getDeliveryPrice()).isEqualTo(0);
        // 分摊和守恒
        assertThat(resultBO.getItems().get(0).getDeliveryPrice() + resultBO.getItems().get(1).getDeliveryPrice())
                .isEqualTo(resultBO.getPrice().getDeliveryPrice());
    }

    @Test
    @DisplayName("按件计算运费：包邮（总件数 >= 包邮件数 && 总金额 >= 包邮金额）")
    public void testCalculate_expressTemplateFree() {
        freeBO.setFreeCount(10).setFreePrice(1000);
        mockAddress();
        mockTemplate(templateRespBO);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("按件计算运费：边界场景（总件数 = 包邮件数 → 包邮）")
    public void testCalculate_expressTemplateFree_countBoundary() {
        freeBO.setFreeCount(12).setFreePrice(1000);
        mockAddress();
        mockTemplate(templateRespBO);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("按件计算运费：总金额未达到包邮金额 → 不包邮")
    public void testCalculate_expressTemplateFree_priceNotMatch() {
        freeBO.setFreeCount(10).setFreePrice(5000);
        mockAddress();
        mockTemplate(templateRespBO);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(3000);
    }

    @Test
    @DisplayName("按件计算运费：边界场景（总金额 = 包邮金额 → 包邮）")
    public void testCalculate_expressTemplateFree_priceBoundary() {
        freeBO.setFreeCount(10).setFreePrice(2200);
        mockAddress();
        mockTemplate(templateRespBO);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("按件计算运费：总计费量小于包邮阈值（数量未达标）不得包邮")
    public void testCalculate_countBelowFreeThreshold_notFree() {
        // 金额 2200 >= freePrice 100，但总件数 12 < freeCount 20，不得包邮
        mockAddress();
        mockTemplate(templateRespBO);
        calculator.calculate(reqBO, resultBO);
        // 当前实现误用 <= 会导致这里错误包邮（返回 0），修复后应返回 3000
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(3000);
    }

    @Test
    @DisplayName("按重量计算运费")
    public void testCalculate_expressTemplate_byWeight() {
        // 总重量 120kg：首件 1000 + 续件 ceil((120-10)/10)=11 * 2000 = 23000
        templateRespBO.setChargeMode(SalesDeliveryExpressChargeModeEnum.WEIGHT.getType());
        freeBO.setFreeCount(200).setFreePrice(100);
        mockAddress();
        mockTemplate(templateRespBO);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(23000);
    }

    @Test
    @DisplayName("按体积计算运费")
    public void testCalculate_expressTemplate_byVolume() {
        // 总体积 120m³：与按重量同
        templateRespBO.setChargeMode(SalesDeliveryExpressChargeModeEnum.VOLUME.getType());
        freeBO.setFreeCount(200).setFreePrice(100);
        mockAddress();
        mockTemplate(templateRespBO);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(23000);
    }

    @Test
    @DisplayName("首量等值边界：总计费量 = 首量 → 只收首费")
    public void testCalculate_expressTemplate_startCountBoundary() {
        // 总件数 12 > 首量 10：1000 + ceil(2/10)=1 * 2000 = 3000
        freeBO.setFreeCount(200).setFreePrice(100);
        mockAddress();
        mockTemplate(templateRespBO);
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(3000);

        // 总件数 10 = 首量 10：只收首费 1000
        resultBO.getItems().get(1).setCount(8);
        SalesPriceCalculatorHelper.recountPayPrice(resultBO.getItems());
        calculator.calculate(reqBO, resultBO);
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(1000);
    }

    // ========== 失败关闭（迁移加固） ==========

    @Test
    @DisplayName("单个模板缺失：从日志后继续改为明确错误")
    public void testCalculate_singleTemplateMissing_throws() {
        // 两个商品分属模板 1 和 2，只返回模板 1
        resultBO.getItems().get(1).setDeliveryTemplateId(2L);
        mockAddress();
        when(deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(eq(asSet(1L, 2L)), eq(10)))
                .thenReturn(MapUtil.of(1L, templateRespBO));
        assertServiceException(() -> calculator.calculate(reqBO, resultBO), EXPRESS_DELIVERY_TEMPLATE_MISSING, 2L);
    }

    @Test
    @DisplayName("区域计费规则缺失（charge 为空）：改为明确错误")
    public void testCalculate_chargeRuleMissing_throws() {
        SalesDeliveryExpressTemplateRespBO onlyFree = new SalesDeliveryExpressTemplateRespBO()
                .setChargeMode(SalesDeliveryExpressChargeModeEnum.COUNT.getType())
                .setCharge(null).setFree(freeBO);
        mockAddress();
        mockTemplate(onlyFree);
        assertServiceException(() -> calculator.calculate(reqBO, resultBO), EXPRESS_DELIVERY_CHARGE_RULE_NOT_FOUND);
    }

    @Test
    @DisplayName("按重量缺少商品重量：改为明确错误")
    public void testCalculate_weightMissing_throws() {
        templateRespBO.setChargeMode(SalesDeliveryExpressChargeModeEnum.WEIGHT.getType());
        freeBO.setFreeCount(200).setFreePrice(100);
        resultBO.getItems().get(0).setWeight(null);
        mockAddress();
        mockTemplate(templateRespBO);
        assertServiceException(() -> calculator.calculate(reqBO, resultBO), EXPRESS_DELIVERY_METRIC_MISSING);
    }

    @Test
    @DisplayName("按体积缺少商品体积：改为明确错误")
    public void testCalculate_volumeMissing_throws() {
        templateRespBO.setChargeMode(SalesDeliveryExpressChargeModeEnum.VOLUME.getType());
        freeBO.setFreeCount(200).setFreePrice(100);
        resultBO.getItems().get(1).setVolume(null);
        mockAddress();
        mockTemplate(templateRespBO);
        assertServiceException(() -> calculator.calculate(reqBO, resultBO), EXPRESS_DELIVERY_METRIC_MISSING);
    }

    @Test
    @DisplayName("非法计费模式：改为明确错误")
    public void testCalculate_illegalChargeMode_throws() {
        templateRespBO.setChargeMode(99);
        mockAddress();
        mockTemplate(templateRespBO);
        assertServiceException(() -> calculator.calculate(reqBO, resultBO), EXPRESS_DELIVERY_CHARGE_MODE_ILLEGAL, 99);
    }

    @Test
    @DisplayName("总计费量非正：改为明确错误")
    public void testCalculate_nonPositiveChargeValue_throws() {
        // 选中商品数量都为 0，总计费量 = 0
        resultBO.getItems().get(0).setCount(0);
        resultBO.getItems().get(1).setCount(0);
        SalesPriceCalculatorHelper.recountPayPrice(resultBO.getItems());
        mockAddress();
        mockTemplate(templateRespBO);
        assertServiceException(() -> calculator.calculate(reqBO, resultBO), EXPRESS_DELIVERY_CHARGE_VALUE_NOT_POSITIVE);
    }

    @Test
    @DisplayName("首量/续量/费用非正：改为明确错误")
    public void testCalculate_nonPositiveChargeParams_throws() {
        chargeBO.setStartCount(0d);
        mockAddress();
        mockTemplate(templateRespBO);
        assertServiceException(() -> calculator.calculate(reqBO, resultBO), EXPRESS_DELIVERY_CHARGE_PARAM_NOT_POSITIVE);
    }

    // ========== 工具方法 ==========

    private void mockAddress() {
        PartnerAddressRespDTO addressResp = new PartnerAddressRespDTO().setAreaId(10);
        when(addressApi.getAddress(eq(10L), eq(1L))).thenReturn(addressResp);
    }

    private void mockTemplate(SalesDeliveryExpressTemplateRespBO template) {
        when(deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(eq(asSet(1L)), eq(10)))
                .thenReturn(MapUtil.of(1L, template));
    }

}
