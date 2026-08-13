package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.api.dto.PartnerLevelRespDTO;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.api.SalesDiscountActivityApi;
import vip.appap.suxin.module.sales.api.dto.SalesDiscountProductRespDTO;
import vip.appap.suxin.module.sales.api.SalesRewardActivityApi;
import vip.appap.suxin.module.sales.api.dto.SalesRewardActivityMatchRespDTO;
import vip.appap.suxin.module.sales.enums.SalesTypeEnum;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesProductSettlementRespVO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import vip.appap.suxin.module.sales.service.SalesDiscountActivityPriceCalculator;
import vip.appap.suxin.module.sales.service.SalesPriceCalculator;
import vip.appap.suxin.module.sales.service.SalesPriceCalculatorHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SKU_STOCK_NOT_ENOUGH;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PRICE_CALCULATE_PAY_PRICE_ILLEGAL;

/**
 * 价格计算 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
@Slf4j
public class SalesPriceServiceImpl implements SalesPriceService {

    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private SalesDiscountActivityApi discountActivityApi;
    @Resource
    private SalesRewardActivityApi rewardActivityApi;

    @Resource
    private List<SalesPriceCalculator> priceCalculators;

    @Resource
    private SalesDiscountActivityPriceCalculator discountActivityPriceCalculator;

    @Override
    public SalesPriceCalculateRespBO calculateOrderPrice(SalesPriceCalculateReqBO calculateReqBO) {
        // 1.1 获得商品 SKU 数组
        List<ProductSkuRespDTO> skuList = checkSkuList(calculateReqBO);
        // 1.2 获得商品 SPU 数组
        List<ProductSpuRespDTO> spuList = checkSpuList(skuList);

        // 2.1 计算价格
        SalesPriceCalculateRespBO calculateRespBO = SalesPriceCalculatorHelper
                .buildCalculateResp(calculateReqBO, spuList, skuList);
        priceCalculators.forEach(calculator -> calculator.calculate(calculateReqBO, calculateRespBO));
        // 2.2  如果最终支付金额小于等于 0，则抛出业务异常
        if (calculateReqBO.getPointActivityId() == null // 积分订单，允许支付金额为 0
                && calculateRespBO.getPrice().getPayPrice() <= 0) {
            log.error("[calculatePrice][价格计算不正确，请求 calculateReqDTO({})，结果 priceCalculate({})]",
                    calculateReqBO, calculateRespBO);
            throw exception(PRICE_CALCULATE_PAY_PRICE_ILLEGAL);
        }
        return calculateRespBO;
    }

    private List<ProductSkuRespDTO> checkSkuList(SalesPriceCalculateReqBO reqBO) {
        // 获得商品 SKU 数组
        Map<Long, Integer> skuIdCountMap = convertMap(reqBO.getItems(),
                SalesPriceCalculateReqBO.Item::getSkuId, SalesPriceCalculateReqBO.Item::getCount);
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(skuIdCountMap.keySet());

        // 校验商品 SKU
        skus.forEach(sku -> {
            Integer count = skuIdCountMap.get(sku.getId());
            if (count == null) {
                throw exception(SKU_NOT_EXISTS);
            }
            if (count > sku.getStock()) {
                throw exception(SKU_STOCK_NOT_ENOUGH);
            }
        });
        return skus;
    }

    private List<ProductSpuRespDTO> checkSpuList(List<ProductSkuRespDTO> skuList) {
        return productSpuApi.validateSpuList(convertSet(skuList, ProductSkuRespDTO::getSpuId));
    }

    @Override
    public List<AppSalesProductSettlementRespVO> calculateProductPrice(Long userId, List<Long> spuIds) {
        // 1.1 获得 SPU 与 SKU 的映射
        List<ProductSkuRespDTO> allSkuList = productSkuApi.getSkuListBySpuId(spuIds);
        Map<Long, List<ProductSkuRespDTO>> spuIdAndSkuListMap = convertMultiMap(allSkuList, ProductSkuRespDTO::getSpuId);
        // 1.2 获得会员等级
        PartnerLevelRespDTO level = discountActivityPriceCalculator.getMemberLevel(userId);
        // 1.3 获得限时折扣活动
        Map<Long, SalesDiscountProductRespDTO> skuIdAndDiscountMap = convertMap(
                discountActivityApi.getMatchDiscountProductListBySkuIds(convertSet(allSkuList, ProductSkuRespDTO::getId)),
                SalesDiscountProductRespDTO::getSkuId);
        // 1.4 获得满减送活动
       List<SalesRewardActivityMatchRespDTO> rewardActivityMap = rewardActivityApi.getMatchRewardActivityListBySpuIds(spuIds);

        // 2. 价格计算
        return convertList(spuIds, spuId -> {
            AppSalesProductSettlementRespVO spuVO = new AppSalesProductSettlementRespVO().setSpuId(spuId);
            // 2.1 优惠价格
            List<ProductSkuRespDTO> skuList = spuIdAndSkuListMap.get(spuId);
            List<AppSalesProductSettlementRespVO.Sku> skuVOList = convertList(skuList, sku -> {
                AppSalesProductSettlementRespVO.Sku skuVO = new AppSalesProductSettlementRespVO.Sku()
                        .setId(sku.getId());
                SalesPriceCalculateRespBO.OrderItem orderItem = new SalesPriceCalculateRespBO.OrderItem()
                        .setPayPrice(sku.getPrice()).setCount(1);
                // 计算限时折扣的优惠价格
                SalesDiscountProductRespDTO discountProduct = skuIdAndDiscountMap.get(sku.getId());
                Integer discountPrice = discountActivityPriceCalculator.calculateActivityPrice(discountProduct, orderItem);
                // 计算 VIP 优惠金额
                Integer vipPrice = discountActivityPriceCalculator.calculateVipPrice(level, orderItem);
                if (discountPrice <= 0 && vipPrice <= 0) {
                    return skuVO;
                }
                // 选择一个大的优惠
                if (discountPrice > vipPrice) {
                    return skuVO.setPromotionPrice(sku.getPrice() - discountPrice)
                            .setPromotionType(SalesTypeEnum.DISCOUNT_ACTIVITY.getType())
                            .setPromotionId(discountProduct.getId()).setPromotionEndTime(discountProduct.getActivityEndTime());
                } else {
                    return skuVO.setPromotionPrice(sku.getPrice() - vipPrice)
                            .setPromotionType(SalesTypeEnum.MEMBER_LEVEL.getType());
                }
            });
            spuVO.setSkus(skuVOList);
            // 2.2 满减送活动
            SalesRewardActivityMatchRespDTO rewardActivity = CollUtil.findOne(rewardActivityMap,
                    activity -> CollUtil.contains(activity.getSpuIds(), spuId));
            spuVO.setRewardActivity(BeanUtils.toBean(rewardActivity, AppSalesProductSettlementRespVO.SalesRewardActivity.class));
            return spuVO;
        });
    }

}
