package vip.appap.suxin.module.product.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigListRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigUpdateReqVO;
import vip.appap.suxin.module.product.service.ProductDisplayConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 商品页面展示配置")
@RestController
@RequestMapping("/product/display-config")
@Validated
public class ProductDisplayConfigController {

    @Resource
    private ProductDisplayConfigService productDisplayConfigService;

    @GetMapping("/list")
    @Operation(summary = "获得商品页面展示配置列表")
    @PreAuthorize("@ss.hasPermission('product:display-config:query')")
    public CommonResult<List<ProductDisplayConfigListRespVO>> getDisplayConfigList() {
        return success(productDisplayConfigService.getDisplayConfigList());
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品页面展示配置")
    @PreAuthorize("@ss.hasPermission('product:display-config:update')")
    public CommonResult<ProductDisplayConfigListRespVO> updateDisplayConfig(
            @Valid @RequestBody ProductDisplayConfigUpdateReqVO updateReqVO) {
        return success(productDisplayConfigService.updateDisplayConfig(updateReqVO));
    }

}
