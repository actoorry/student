package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.accountant.api.dto.PayRefundNotifyReqDTO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesAfterSaleConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleLogDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.service.SalesAfterSaleLogService;
import vip.appap.suxin.module.sales.service.SalesAfterSaleService;
import vip.appap.suxin.module.sales.service.SalesOrderQueryService;
import vip.appap.suxin.module.sales.service.SalesOrderUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 售后订单")
@RestController
@RequestMapping("/sales/after-sale")
@Validated
@Slf4j
public class SalesAfterSaleController {

    @Resource
    private SalesAfterSaleService afterSaleService;
    @Resource
    private SalesOrderQueryService tradeOrderQueryService;
    @Resource
    private SalesOrderUpdateService tradeOrderUpdateService;
    @Resource
    private SalesAfterSaleLogService afterSaleLogService;
    @Resource
    private PartnerApi PartnerApi;

    @GetMapping("/page")
    @Operation(summary = "获得售后订单分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_after_sale:query')")
    public CommonResult<PageResult<SalesAfterSaleRespPageItemVO>> getAfterSalePage(@Valid SalesAfterSalePageReqVO pageVO) {
        // 查询售后
        PageResult<SalesAfterSaleDO> pageResult = afterSaleService.getAfterSalePage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 查询会员
        Map<Long, PartnerRespDTO> PartnerUsers = PartnerApi.getUserMap(
                convertSet(pageResult.getList(), SalesAfterSaleDO::getUserId));
        return success(SalesAfterSaleConvert.INSTANCE.convertPage(pageResult, PartnerUsers));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得售后订单详情")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('sales:sales_after_sale:query')")
    public CommonResult<SalesAfterSaleDetailRespVO> getOrderDetail(@RequestParam("id") Long id) {
        // 查询订单
        SalesAfterSaleDO afterSale = afterSaleService.getAfterSale(id);
        if (afterSale == null) {
            return success(null);
        }

        // 查询订单
        SalesOrderDO order = tradeOrderQueryService.getOrder(afterSale.getOrderId());
        // 查询订单项
        SalesOrderItemDO orderItem = tradeOrderQueryService.getOrderItem(afterSale.getOrderItemId());
        // 拼接数据
        PartnerRespDTO user = PartnerApi.getUser(afterSale.getUserId());
        List<SalesAfterSaleLogDO> logs = afterSaleLogService.getAfterSaleLogList(afterSale.getId());
        return success(SalesAfterSaleConvert.INSTANCE.convert(afterSale, order, orderItem, user, logs));
    }

    @PutMapping("/agree")
    @Operation(summary = "同意售后")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('sales:sales_after_sale:agree')")
    public CommonResult<Boolean> agreeAfterSale(@RequestParam("id") Long id) {
        afterSaleService.agreeAfterSale(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/disagree")
    @Operation(summary = "拒绝售后")
    @PreAuthorize("@ss.hasPermission('sales:sales_after_sale:disagree')")
    public CommonResult<Boolean> disagreeAfterSale(@RequestBody SalesAfterSaleDisagreeReqVO confirmReqVO) {
        afterSaleService.disagreeAfterSale(getLoginUserId(), confirmReqVO);
        return success(true);
    }

    @PutMapping("/receive")
    @Operation(summary = "确认收货")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('sales:sales_after_sale:receive')")
    public CommonResult<Boolean> receiveAfterSale(@RequestParam("id") Long id) {
        afterSaleService.receiveAfterSale(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/refuse")
    @Operation(summary = "拒绝收货")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('sales:sales_after_sale:receive')")
    public CommonResult<Boolean> refuseAfterSale(SalesAfterSaleRefuseReqVO refuseReqVO) {
        afterSaleService.refuseAfterSale(getLoginUserId(), refuseReqVO);
        return success(true);
    }

    @PutMapping("/refund")
    @Operation(summary = "确认退款")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('sales:sales_after_sale:refund')")
    public CommonResult<Boolean> refundAfterSale(@RequestParam("id") Long id) {
        afterSaleService.refundAfterSale(getLoginUserId(), getClientIP(), id);
        return success(true);
    }

    @PostMapping("/update-refunded")
    @Operation(summary = "更新售后订单为已退款") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll // 无需登录，安全由 SalesAfterSaleService 内部校验实现
    public CommonResult<Boolean> updateAfterSaleRefunded(@RequestBody PayRefundNotifyReqDTO notifyReqDTO) {
        log.info("[updateAfterRefund][notifyReqDTO({})]", notifyReqDTO);
        if (StrUtil.startWithAny(notifyReqDTO.getMerchantRefundId(), "order-")) {
            Long orderId = Long.parseLong(StrUtil.subAfter(notifyReqDTO.getMerchantRefundId(), "order-", true));
            tradeOrderUpdateService.updatePaidOrderRefunded(orderId, notifyReqDTO.getPayRefundId());
        } else {
            afterSaleService.updateAfterSaleRefunded(
                    Long.parseLong(notifyReqDTO.getMerchantRefundId()),
                    Long.parseLong(notifyReqDTO.getMerchantOrderId()),
                    notifyReqDTO.getPayRefundId());
        }
        return success(true);
    }

}

