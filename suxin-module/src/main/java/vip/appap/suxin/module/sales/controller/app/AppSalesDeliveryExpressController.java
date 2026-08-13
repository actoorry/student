package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesDeliveryyExpressRespVO;
import vip.appap.suxin.module.sales.convert.SalesDeliveryExpressConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.service.SalesDeliveryExpressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 快递公司")
@RestController
@RequestMapping("/sales/delivery/express")
@Validated
public class AppSalesDeliveryExpressController {

    @Resource
    private SalesDeliveryExpressService deliveryExpressService;

    @GetMapping("/list")
    @Operation(summary = "获得快递公司列表")
    @PermitAll
    public CommonResult<List<AppSalesDeliveryyExpressRespVO>> getDeliveryExpressList() {
        List<SalesDeliveryExpressDO> list = deliveryExpressService.getDeliveryExpressListByStatus(CommonStatusEnum.ENABLE.getStatus());
        list.sort(Comparator.comparing(SalesDeliveryExpressDO::getSort));
        return success(SalesDeliveryExpressConvert.INSTANCE.convertList03(list));
    }

}
