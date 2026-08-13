package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesDeliveryyPickUpStoreRespVO;
import vip.appap.suxin.module.sales.convert.SalesDeliveryPickUpStoreConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryPickUpStoreDO;
import vip.appap.suxin.module.sales.service.SalesDeliveryPickUpStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 自提门店")
@RestController
@RequestMapping("/sales/delivery/pick-up-store")
@Validated
public class AppSalesDeliveryPickUpStoreController {

    @Resource
    private SalesDeliveryPickUpStoreService deliveryPickUpStoreService;

    @GetMapping("/list")
    @Operation(summary = "获得自提门店列表")
    @Parameters({
            @Parameter(name = "latitude", description = "精度", example = "110"),
            @Parameter(name = "longitude", description = "纬度", example = "120")
    })
    @PermitAll
    public CommonResult<List<AppSalesDeliveryyPickUpStoreRespVO>> getDeliveryPickUpStoreList(
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude) {
        List<SalesDeliveryPickUpStoreDO> list = deliveryPickUpStoreService.getDeliveryPickUpStoreListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(SalesDeliveryPickUpStoreConvert.INSTANCE.convertList(list, latitude, longitude));
    }

    @GetMapping("/get")
    @Operation(summary = "获得自提门店")
    @Parameter(name = "id", description = "门店编号")
    @PermitAll
    public CommonResult<AppSalesDeliveryyPickUpStoreRespVO> getOrder(@RequestParam("id") Long id) {
        SalesDeliveryPickUpStoreDO store = deliveryPickUpStoreService.getDeliveryPickUpStore(id);
        return success(SalesDeliveryPickUpStoreConvert.INSTANCE.convert03(store));
    }

}
