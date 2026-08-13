package vip.appap.suxin.module.product.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.product.controller.app.vo.AppProductDisplayConfigRespVO;
import vip.appap.suxin.module.product.service.ProductDisplayConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 商品展示配置")
@RestController
@RequestMapping("/product/display-config")
@Validated
public class AppProductDisplayConfigController {

    @Resource
    private ProductDisplayConfigService productDisplayConfigService;

    @GetMapping("/get")
    @Operation(summary = "获得商品展示配置")
    @Parameter(name = "sceneCode", description = "展示场景编码", required = true)
    @PermitAll
    public CommonResult<AppProductDisplayConfigRespVO> getDisplayConfig(
            @RequestParam("sceneCode") @NotBlank(message = "展示场景编码不能为空") String sceneCode) {
        return success(new AppProductDisplayConfigRespVO()
                .setSceneCode(sceneCode)
                .setCategoryIds(productDisplayConfigService.getEnabledCategoryIdsBySceneCode(sceneCode)));
    }

}
