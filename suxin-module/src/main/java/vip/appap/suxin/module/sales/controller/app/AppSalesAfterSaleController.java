package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSaleCreateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSaleDeliveryReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSalePageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSaleRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleDO;
import vip.appap.suxin.module.sales.service.SalesAfterSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 交易售后")
@RestController
@RequestMapping("/sales/after-sale")
@Validated
@Slf4j
public class AppSalesAfterSaleController {

    @Resource
    private SalesAfterSaleService afterSaleService;

    @GetMapping(value = "/page")
    @Operation(summary = "获得售后分页")
    public CommonResult<PageResult<AppSalesAfterSaleRespVO>> getAfterSalePage(AppSalesAfterSalePageReqVO pageReqVO) {
        PageResult<SalesAfterSaleDO> pageResult = afterSaleService.getAfterSalePage(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppSalesAfterSaleRespVO.class));
    }

    @GetMapping(value = "/get")
    @Operation(summary = "获得售后订单")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    public CommonResult<AppSalesAfterSaleRespVO> getAfterSale(@RequestParam("id") Long id) {
        SalesAfterSaleDO afterSale = afterSaleService.getAfterSale(getLoginUserId(), id);
        return success(BeanUtils.toBean(afterSale, AppSalesAfterSaleRespVO.class));
    }

    @PostMapping(value = "/create")
    @Operation(summary = "申请售后")
    public CommonResult<Long> createAfterSale(@RequestBody AppSalesAfterSaleCreateReqVO createReqVO) {
        return success(afterSaleService.createAfterSale(getLoginUserId(), createReqVO));
    }

    @PutMapping(value = "/delivery")
    @Operation(summary = "退回货物")
    public CommonResult<Boolean> deliveryAfterSale(@RequestBody AppSalesAfterSaleDeliveryReqVO deliveryReqVO) {
        afterSaleService.deliveryAfterSale(getLoginUserId(), deliveryReqVO);
        return success(true);
    }

    @DeleteMapping(value = "/cancel")
    @Operation(summary = "取消售后")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    public CommonResult<Boolean> cancelAfterSale(@RequestParam("id") Long id) {
        afterSaleService.cancelAfterSale(getLoginUserId(), id);
        return success(true);
    }

}
