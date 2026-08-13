package vip.appap.suxin.module.accountant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.accountant.service.ProductWechatVirtualGoodsService;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsRefreshReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsSyncReqVO;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 微信虚拟支付道具")
@RestController
@RequestMapping("/product/wechat-virtual-goods")
@Validated
public class ProductWechatVirtualGoodsController {

    @Resource
    private ProductWechatVirtualGoodsService productWechatVirtualGoodsService;

    @GetMapping("/list")
    @Operation(summary = "查询微信虚拟支付道具状态")
    @PreAuthorize("@ss.hasPermission('product:wechat-virtual-goods:query')")
    public CommonResult<List<ProductWechatVirtualGoodsRespVO>> getWechatVirtualGoodsList(
            @RequestParam("spuId") Long spuId) {
        return success(productWechatVirtualGoodsService.getWechatVirtualGoodsList(spuId));
    }

    @PostMapping("/sync")
    @Operation(summary = "同步微信虚拟支付道具")
    @PreAuthorize("@ss.hasPermission('product:wechat-virtual-goods:sync')")
    public CommonResult<List<ProductWechatVirtualGoodsRespVO>> syncWechatVirtualGoods(
            @Valid @RequestBody ProductWechatVirtualGoodsSyncReqVO reqVO) {
        return success(productWechatVirtualGoodsService.syncWechatVirtualGoods(reqVO));
    }

    @PostMapping("/publish")
    @Operation(summary = "发布微信虚拟支付道具")
    @PreAuthorize("@ss.hasPermission('product:wechat-virtual-goods:publish')")
    public CommonResult<List<ProductWechatVirtualGoodsRespVO>> publishWechatVirtualGoods(
            @Valid @RequestBody ProductWechatVirtualGoodsSyncReqVO reqVO) {
        return success(productWechatVirtualGoodsService.publishWechatVirtualGoods(reqVO));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新微信虚拟支付道具状态")
    @PreAuthorize("@ss.hasPermission('product:wechat-virtual-goods:query')")
    public CommonResult<List<ProductWechatVirtualGoodsRespVO>> refreshWechatVirtualGoods(
            @Valid @RequestBody ProductWechatVirtualGoodsRefreshReqVO reqVO) {
        return success(productWechatVirtualGoodsService.refreshWechatVirtualGoods(reqVO));
    }

}
