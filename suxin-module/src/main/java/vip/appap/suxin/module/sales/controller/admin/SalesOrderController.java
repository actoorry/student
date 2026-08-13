package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesElectronicWaybillConvert;
import vip.appap.suxin.module.sales.convert.SalesOrderConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderLogDO;
import vip.appap.suxin.module.sales.service.SalesDeliveryExpressService;
import vip.appap.suxin.module.sales.service.SalesElectronicWaybillService;
import vip.appap.suxin.module.sales.service.SalesOrderLogService;
import vip.appap.suxin.module.sales.service.SalesOrderQueryService;
import vip.appap.suxin.module.sales.service.SalesOrderUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 交易订单")
@RestController
@RequestMapping("/sales/order")
@Validated
@Slf4j
public class SalesOrderController {

    @Resource
    private SalesOrderUpdateService tradeOrderUpdateService;
    @Resource
    private SalesOrderQueryService tradeOrderQueryService;
    @Resource
    private SalesOrderLogService tradeOrderLogService;
    @Resource
    private SalesElectronicWaybillService waybillService;
    @Resource
    private SalesDeliveryExpressService deliveryExpressService;

    @Resource
    private PartnerApi PartnerApi;

    @GetMapping("/page")
    @Operation(summary = "获得交易订单分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:query')")
    public CommonResult<PageResult<SalesOrderPageItemRespVO>> getOrderPage(SalesOrderPageReqVO reqVO) {
        // 查询订单
        PageResult<SalesOrderDO> pageResult = tradeOrderQueryService.getOrderPage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 查询用户信息
        Set<Long> userIds = CollUtil.unionDistinct(convertList(pageResult.getList(), SalesOrderDO::getUserId),
                convertList(pageResult.getList(), SalesOrderDO::getBrokerageUserId, Objects::nonNull));
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(userIds);
        // 查询订单项
        List<SalesOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(
                convertSet(pageResult.getList(), SalesOrderDO::getId));
        // 最终组合
        PageResult<SalesOrderPageItemRespVO> pageVO = SalesOrderConvert.INSTANCE.convertPage(pageResult, orderItems, userMap);
        // 补充电子面单信息
        fillWaybillInfo(pageVO.getList(), convertSet(pageResult.getList(), SalesOrderDO::getId));
        return success(pageVO);
    }

    @GetMapping("/summary")
    @Operation(summary = "获得交易订单统计")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:query')")
    public CommonResult<SalesOrderSummaryRespVO> getOrderSummary(SalesOrderPageReqVO reqVO) {
        return success(tradeOrderQueryService.getOrderSummary(reqVO));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得交易订单详情")
    @Parameter(name = "id", description = "订单编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:query')")
    public CommonResult<SalesOrderDetailRespVO> getOrderDetail(@RequestParam("id") Long id) {
        // 查询订单
        SalesOrderDO order = tradeOrderQueryService.getOrder(id);
        if (order == null) {
            return success(null);
        }
        // 查询订单项
        List<SalesOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(id);

        // 拼接数据
        PartnerRespDTO user = PartnerApi.getUser(order.getUserId());
        PartnerRespDTO brokerageUser = order.getBrokerageUserId() != null ?
                PartnerApi.getUser(order.getBrokerageUserId()) : null;
        List<SalesOrderLogDO> orderLogs = tradeOrderLogService.getOrderLogListByOrderId(id);
        SalesOrderDetailRespVO detailVO = SalesOrderConvert.INSTANCE.convert(order, orderItems, orderLogs, user, brokerageUser);
        // 补充电子面单信息（按订单查询权限提供状态与可打印内容）
        SalesElectronicWaybillDO waybill = waybillService.getValidByOrderId(id);
        if (waybill != null) {
            SalesElectronicWaybillRespVO waybillVO = SalesElectronicWaybillConvert.INSTANCE.convert(waybill);
            SalesDeliveryExpressDO express = deliveryExpressService.getDeliveryExpress(waybill.getExpressId());
            waybillVO.setExpressName(express != null ? express.getName() : null);
            detailVO.setWaybill(waybillVO);
        }
        return success(detailVO);
    }

    @GetMapping("/get-express-track-list")
    @Operation(summary = "获得交易订单的物流轨迹")
    @Parameter(name = "id", description = "交易订单编号")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:query')")
    public CommonResult<List<?>> getOrderExpressTrackList(@RequestParam("id") Long id) {
        return success(SalesOrderConvert.INSTANCE.convertList02(
                tradeOrderQueryService.getExpressTrackList(id)));
    }

    @PutMapping("/delivery")
    @Operation(summary = "订单发货")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:update')")
    public CommonResult<Boolean> deliveryOrder(@RequestBody SalesOrderDeliveryReqVO deliveryReqVO) {
        tradeOrderUpdateService.deliveryOrder(deliveryReqVO);
        return success(true);
    }

    @PutMapping("/delivery-by-waybill")
    @Operation(summary = "订单电子面单发货")
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill:create')")
    public CommonResult<Boolean> deliveryOrderByElectronicWaybill(
            @Valid @RequestBody SalesOrderElectronicWaybillDeliveryReqVO reqVO) {
        tradeOrderUpdateService.deliveryOrderByElectronicWaybill(reqVO);
        return success(true);
    }

    @PutMapping("/waybill-reprint")
    @Operation(summary = "电子面单复打")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill:reprint')")
    public CommonResult<SalesElectronicWaybillRespVO> reprintElectronicWaybill(@RequestParam("id") Long id) {
        return success(tradeOrderUpdateService.reprintElectronicWaybill(id));
    }

    @PutMapping("/waybill-cancel")
    @Operation(summary = "电子面单取消")
    @PreAuthorize("@ss.hasPermission('sales:sales_electronic_waybill:cancel')")
    public CommonResult<Boolean> cancelElectronicWaybill(@Valid @RequestBody SalesOrderElectronicWaybillCancelReqVO reqVO) {
        tradeOrderUpdateService.cancelElectronicWaybill(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/update-remark")
    @Operation(summary = "订单备注")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:update')")
    public CommonResult<Boolean> updateOrderRemark(@RequestBody SalesOrderRemarkReqVO reqVO) {
        tradeOrderUpdateService.updateOrderRemark(reqVO);
        return success(true);
    }

    @PutMapping("/update-price")
    @Operation(summary = "订单调价")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:update')")
    public CommonResult<Boolean> updateOrderPrice(@RequestBody SalesOrderUpdatePriceReqVO reqVO) {
        tradeOrderUpdateService.updateOrderPrice(reqVO);
        return success(true);
    }

    @PutMapping("/update-address")
    @Operation(summary = "修改订单收货地址")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:update')")
    public CommonResult<Boolean> updateOrderAddress(@RequestBody SalesOrderUpdateAddressReqVO reqVO) {
        tradeOrderUpdateService.updateOrderAddress(reqVO);
        return success(true);
    }

    @PutMapping("/pick-up-by-id")
    @Operation(summary = "订单核销")
    @Parameter(name = "id", description = "交易订单编号")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:pick-up')")
    public CommonResult<Boolean> pickUpOrderById(@RequestParam("id") Long id) {
        tradeOrderUpdateService.pickUpOrderByAdmin(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/pick-up-by-verify-code")
    @Operation(summary = "订单核销")
    @Parameter(name = "pickUpVerifyCode", description = "自提核销码")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:pick-up')")
    public CommonResult<Boolean> pickUpOrderByVerifyCode(@RequestParam("pickUpVerifyCode") String pickUpVerifyCode) {
        tradeOrderUpdateService.pickUpOrderByAdmin(getLoginUserId(), pickUpVerifyCode);
        return success(true);
    }

    @GetMapping("/get-by-pick-up-verify-code")
    @Operation(summary = "查询核销码对应的订单")
    @Parameter(name = "pickUpVerifyCode", description = "自提核销码")
    @PreAuthorize("@ss.hasPermission('sales:sales_order:query')")
    public CommonResult<SalesOrderDetailRespVO> getByPickUpVerifyCode(@RequestParam("pickUpVerifyCode") String pickUpVerifyCode) {
        SalesOrderDO tradeOrder = tradeOrderUpdateService.getByPickUpVerifyCode(pickUpVerifyCode);
        return success(SalesOrderConvert.INSTANCE.convert2(tradeOrder, null));
    }

    /**
     * 为订单分页项补充有效电子面单信息
     */
    private void fillWaybillInfo(List<SalesOrderPageItemRespVO> orderVOs, Set<Long> orderIds) {
        if (CollUtil.isEmpty(orderVOs) || CollUtil.isEmpty(orderIds)) {
            return;
        }
        List<SalesElectronicWaybillDO> waybills = waybillService.getValidListByOrderIds(orderIds);
        if (CollUtil.isEmpty(waybills)) {
            return;
        }
        Map<Long, SalesElectronicWaybillDO> waybillMap = convertMap(waybills, SalesElectronicWaybillDO::getOrderId);
        Map<Long, String> expressNameMap = getExpressNameMap(convertSet(waybills, SalesElectronicWaybillDO::getExpressId));
        orderVOs.forEach(orderVO -> {
            SalesElectronicWaybillDO waybill = waybillMap.get(orderVO.getId());
            if (waybill != null) {
                SalesElectronicWaybillRespVO waybillVO = SalesElectronicWaybillConvert.INSTANCE.convert(waybill);
                waybillVO.setExpressName(expressNameMap.get(waybill.getExpressId()));
                orderVO.setWaybill(waybillVO);
            }
        });
    }

    private Map<Long, String> getExpressNameMap(Set<Long> expressIds) {
        Map<Long, String> map = new java.util.HashMap<>();
        if (CollUtil.isEmpty(expressIds)) {
            return map;
        }
        expressIds.forEach(expressId -> {
            SalesDeliveryExpressDO express = deliveryExpressService.getDeliveryExpress(expressId);
            if (express != null) {
                map.put(expressId, express.getName());
            }
        });
        return map;
    }

}
