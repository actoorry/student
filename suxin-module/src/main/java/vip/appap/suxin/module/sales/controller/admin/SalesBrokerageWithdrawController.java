package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.accountant.api.dto.PayTransferNotifyReqDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageWithdrawRejectReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageWithdrawPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageWithdrawRespVO;
import vip.appap.suxin.module.sales.convert.SalesBrokerageWithdrawConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageWithdrawDO;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawStatusEnum;
import vip.appap.suxin.module.sales.service.SalesBrokerageWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;

@Tag(name = "管理后台 - 佣金提现")
@RestController
@RequestMapping("/sales/brokerage-withdraw")
@Validated
@Slf4j
public class SalesBrokerageWithdrawController {

    @Resource
    private SalesBrokerageWithdrawService brokerageWithdrawService;

    @Resource
    private PartnerApi PartnerApi;

    @PutMapping("/approve")
    @Operation(summary = "通过申请")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_withdraw:audit')")
    public CommonResult<Boolean> approveBrokerageWithdraw(@RequestParam("id") Long id) {
        brokerageWithdrawService.auditBrokerageWithdraw(id,
                SalesBrokerageWithdrawStatusEnum.AUDIT_SUCCESS, "", getClientIP());
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "驳回申请")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_withdraw:audit')")
    public CommonResult<Boolean> rejectBrokerageWithdraw(@Valid @RequestBody SalesBrokerageWithdrawRejectReqVO reqVO) {
        brokerageWithdrawService.auditBrokerageWithdraw(reqVO.getId(),
                SalesBrokerageWithdrawStatusEnum.AUDIT_FAIL, reqVO.getAuditReason(), getClientIP());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得佣金提现")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_withdraw:query')")
    public CommonResult<SalesBrokerageWithdrawRespVO> getBrokerageWithdraw(@RequestParam("id") Long id) {
        SalesBrokerageWithdrawDO brokerageWithdraw = brokerageWithdrawService.getBrokerageWithdraw(id);
        return success(SalesBrokerageWithdrawConvert.INSTANCE.convert(brokerageWithdraw));
    }

    @GetMapping("/page")
    @Operation(summary = "获得佣金提现分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_withdraw:query')")
    public CommonResult<PageResult<SalesBrokerageWithdrawRespVO>> getBrokerageWithdrawPage(@Valid SalesBrokerageWithdrawPageReqVO pageVO) {
        // 分页查询
        PageResult<SalesBrokerageWithdrawDO> pageResult = brokerageWithdrawService.getBrokerageWithdrawPage(pageVO);

        // 拼接信息
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(
                convertSet(pageResult.getList(), SalesBrokerageWithdrawDO::getUserId));
        return success(SalesBrokerageWithdrawConvert.INSTANCE.convertPage(pageResult, userMap));
    }

    @PostMapping("/update-transferred")
    @Operation(summary = "更新佣金提现的转账结果") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll // 无需登录，安全由 SalesBrokerageWithdrawService 内部校验实现
    public CommonResult<Boolean> updateBrokerageWithdrawTransferred(@RequestBody PayTransferNotifyReqDTO notifyReqDTO) {
        log.info("[updateAfterRefund][notifyReqDTO({})]", notifyReqDTO);
        brokerageWithdrawService.updateBrokerageWithdrawTransferred(
                Long.parseLong(notifyReqDTO.getMerchantTransferId()), notifyReqDTO.getPayTransferId());
        return success(true);
    }

}

