package vip.appap.suxin.module.product.controller.app;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.controller.app.vo.AppProductSpuDetailRespVO;
import vip.appap.suxin.module.product.controller.app.vo.AppProductSpuPageReqVO;
import vip.appap.suxin.module.product.controller.app.vo.AppProductSpuRespVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.enums.ProductSpuStatusEnum;
import vip.appap.suxin.module.product.enums.ProductSkuWechatVirtualStatusEnum;
import vip.appap.suxin.module.product.service.ProductSkuService;
import vip.appap.suxin.module.product.service.ProductSpuService;
import vip.appap.suxin.module.product.service.ProductUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SPU_NOT_ENABLE;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;

@Tag(name = "用户 APP - 商品 SPU")
@RestController
@RequestMapping("/product/spu")
@Validated
public class AppProductSpuController {

    @Resource
    private ProductSpuService productSpuService;
    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductUnitService productUnitService;

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得商品 SPU 列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PermitAll
    public CommonResult<List<AppProductSpuRespVO>> getSpuList(@RequestParam("ids") Set<Long> ids) {
        List<ProductSpuDO> list = productSpuService.getSpuList(ids);
        if (CollUtil.isEmpty(list)) {
            return success(Collections.emptyList());
        }

        // 拼接返回
        list.forEach(spu -> spu.setSalesCount(spu.getSalesCount() + spu.getVirtualSalesCount()));
        List<AppProductSpuRespVO> voList = BeanUtils.toBean(list, AppProductSpuRespVO.class);
        return success(voList);
    }

    @GetMapping("/page")
    @Operation(summary = "获得商品 SPU 分页")
    @PermitAll
    public CommonResult<PageResult<AppProductSpuRespVO>> getSpuPage(@Valid AppProductSpuPageReqVO pageVO) {
        PageResult<ProductSpuDO> pageResult = productSpuService.getSpuPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接返回
        pageResult.getList().forEach(spu -> spu.setSalesCount(spu.getSalesCount() + spu.getVirtualSalesCount()));
        PageResult<AppProductSpuRespVO> voPageResult = BeanUtils.toBean(pageResult, AppProductSpuRespVO.class);
        return success(voPageResult);
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得商品 SPU 明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PermitAll
    public CommonResult<AppProductSpuDetailRespVO> getSpuDetail(@RequestParam("id") Long id) {
        // 获得商品 SPU
        ProductSpuDO spu = productSpuService.getSpu(id);
        if (spu == null) {
            throw exception(SPU_NOT_EXISTS);
        }
        if (!ProductSpuStatusEnum.isEnable(spu.getStatus())) {
            throw exception(SPU_NOT_ENABLE, spu.getName());
        }
        // 获得商品 SKU
        List<ProductSkuDO> skus = productSkuService.getSkuListBySpuId(spu.getId());

        // 增加浏览量
        productSpuService.updateBrowseCount(id, 1);

        // 拼接返回
        spu.setSalesCount(spu.getSalesCount() + spu.getVirtualSalesCount());
        AppProductSpuDetailRespVO spuVO = BeanUtils.toBean(spu, AppProductSpuDetailRespVO.class)
                .setSkus(BeanUtils.toBean(skus, AppProductSpuDetailRespVO.Sku.class));
        populateVirtualPaymentReadiness(spu, skus, spuVO);
        spuVO.setDeliveryTypes(spu.getDeliveryTypes());
        if (spu.getUnitId() != null) {
            ProductUnitDO unit = productUnitService.getUnit(spu.getUnitId());
            if (unit != null) {
                spuVO.setUnitName(unit.getName());
            }
        }
        return success(spuVO);
    }

    private void populateVirtualPaymentReadiness(ProductSpuDO spu, List<ProductSkuDO> skus,
                                                 AppProductSpuDetailRespVO spuVO) {
        boolean virtualPaymentRequired = Boolean.TRUE.equals(spu.getIsWechatMiniappVirtualGoods());
        spuVO.setVirtualPaymentRequired(virtualPaymentRequired);
        if (!virtualPaymentRequired) {
            spuVO.setVirtualPaymentReady(true);
            return;
        }
        String spuUnavailableReason = Boolean.TRUE.equals(spu.getIsSale()) ? null : "商品当前未开售";
        Map<Long, AppProductSpuDetailRespVO.Sku> skuVOMap = spuVO.getSkus().stream()
                .collect(Collectors.toMap(AppProductSpuDetailRespVO.Sku::getId, Function.identity()));
        boolean hasReadySku = false;
        for (ProductSkuDO sku : skus) {
            AppProductSpuDetailRespVO.Sku skuVO = skuVOMap.get(sku.getId());
            boolean skuReady = spuUnavailableReason == null && sku.getWechatVirtualProductId() != null
                    && ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus().equals(sku.getWechatVirtualPublishStatus());
            if (skuVO != null) {
                skuVO.setVirtualPaymentReady(skuReady)
                        .setVirtualPaymentUnavailableReason(skuReady ? null : (spuUnavailableReason != null
                                ? spuUnavailableReason : "微信虚拟支付道具尚未发布"));
            }
            hasReadySku = hasReadySku || skuReady;
        }
        spuVO.setVirtualPaymentReady(hasReadySku)
                .setVirtualPaymentUnavailableReason(hasReadySku ? null : (spuUnavailableReason != null
                        ? spuUnavailableReason : "微信虚拟支付道具尚未发布"));
    }

}
