package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesElectronicWaybillAccountConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;
import vip.appap.suxin.module.sales.service.SalesElectronicWaybillAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 快递100电子面单账户")
@RestController
@RequestMapping("/sales/electronic-waybill/account")
@Validated
public class SalesElectronicWaybillAccountController {

    @Resource
    private SalesElectronicWaybillAccountService waybillAccountService;

    @PostMapping("/create")
    @Operation(summary = "创建电子面单账户")
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill_account:create')")
    public CommonResult<Long> createWaybillAccount(@Valid @RequestBody SalesElectronicWaybillAccountCreateReqVO createReqVO) {
        return success(waybillAccountService.createWaybillAccount(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新电子面单账户")
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill_account:update')")
    public CommonResult<Boolean> updateWaybillAccount(@Valid @RequestBody SalesElectronicWaybillAccountUpdateReqVO updateReqVO) {
        waybillAccountService.updateWaybillAccount(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除电子面单账户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill_account:delete')")
    public CommonResult<Boolean> deleteWaybillAccount(@RequestParam("id") Long id) {
        waybillAccountService.deleteWaybillAccount(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得电子面单账户")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill_account:query')")
    public CommonResult<SalesElectronicWaybillAccountRespVO> getWaybillAccount(@RequestParam("id") Long id) {
        SalesElectronicWaybillAccountDO waybillAccount = waybillAccountService.getWaybillAccount(id);
        return success(SalesElectronicWaybillAccountConvert.INSTANCE.convert(waybillAccount));
    }

    @GetMapping("/page")
    @Operation(summary = "获得电子面单账户分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill_account:query')")
    public CommonResult<PageResult<SalesElectronicWaybillAccountRespVO>> getWaybillAccountPage(
            @Valid SalesElectronicWaybillAccountPageReqVO pageReqVO) {
        PageResult<SalesElectronicWaybillAccountDO> pageResult = waybillAccountService.getWaybillAccountPage(pageReqVO);
        return success(SalesElectronicWaybillAccountConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取启用的电子面单账户精简列表", description = "用于订单发货弹窗电子面单模式的下拉选项")
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill:create')")
    public CommonResult<List<SalesElectronicWaybillAccountRespVO>> getSimpleWaybillAccountList() {
        List<SalesElectronicWaybillAccountDO> list =
                waybillAccountService.getWaybillAccountListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(SalesElectronicWaybillAccountConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-by-express")
    @Operation(summary = "获取指定快递公司启用的电子面单账户列表", description = "用于订单发货弹窗电子面单模式按快递公司匹配加载")
    @Parameter(name = "expressId", description = "快递公司编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill:create')")
    public CommonResult<List<SalesElectronicWaybillAccountRespVO>> getWaybillAccountListByExpress(
            @RequestParam("expressId") Long expressId) {
        List<SalesElectronicWaybillAccountDO> list =
                waybillAccountService.getWaybillAccountListByExpressIdAndStatus(expressId,
                        CommonStatusEnum.ENABLE.getStatus());
        return success(SalesElectronicWaybillAccountConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-sender-address")
    @Operation(summary = "获取当前租户商户寄件地址列表", description = "用于电子面单账户表单与订单发货弹窗选择寄件地址")
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill_account:query') "
            + "or @ss.hasPermission('sales:sales_electronic_waybill:create')")
    public CommonResult<List<SalesElectronicWaybillAddressRespVO>> getSenderAddressList() {
        return success(waybillAccountService.getSenderAddressList());
    }

}
