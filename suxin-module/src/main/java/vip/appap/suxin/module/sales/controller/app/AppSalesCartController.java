package vip.appap.suxin.module.sales.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartAddReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartListRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartResetReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartUpdateCountReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartUpdateSelectedReqVO;
import vip.appap.suxin.module.sales.service.SalesCartService;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 购物车")
@RestController
@RequestMapping("/sales/cart")
@Validated
public class AppSalesCartController {

    @Resource
    private SalesCartService salesCartService;

    @PostMapping("/add")
    @Operation(summary = "添加购物车商品")
    public CommonResult<Long> addCart(@Valid @RequestBody AppSalesCartAddReqVO addReqVO) {
        return success(salesCartService.addCart(getLoginUserId(), addReqVO));
    }

    @PutMapping("/update-count")
    @Operation(summary = "更新购物车商品数量")
    public CommonResult<Boolean> updateCartCount(
            @Valid @RequestBody AppSalesCartUpdateCountReqVO updateReqVO) {
        salesCartService.updateCartCount(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @PutMapping("/update-selected")
    @Operation(summary = "更新购物车商品选中状态")
    public CommonResult<Boolean> updateCartSelected(
            @Valid @RequestBody AppSalesCartUpdateSelectedReqVO updateReqVO) {
        salesCartService.updateCartSelected(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @PutMapping("/reset")
    @Operation(summary = "重置购物车商品")
    public CommonResult<Boolean> resetCart(@Valid @RequestBody AppSalesCartResetReqVO resetReqVO) {
        salesCartService.resetCart(getLoginUserId(), resetReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除购物车商品")
    @Parameter(name = "ids", description = "购物车商品编号", required = true, example = "1024,2048")
    public CommonResult<Boolean> deleteCart(@RequestParam("ids") List<Long> ids) {
        salesCartService.deleteCart(getLoginUserId(), ids);
        return success(true);
    }

    @GetMapping("/get-count")
    @Operation(summary = "查询用户在购物车中的商品数量")
    public CommonResult<Integer> getCartCount() {
        return success(salesCartService.getCartCount(getLoginUserId()));
    }

    @GetMapping("/list")
    @Operation(summary = "查询用户的购物车列表")
    public CommonResult<AppSalesCartListRespVO> getCartList() {
        return success(salesCartService.getCartList(getLoginUserId()));
    }

}
