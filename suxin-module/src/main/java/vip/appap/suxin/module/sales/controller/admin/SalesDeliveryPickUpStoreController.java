package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import vip.appap.suxin.module.sales.controller.admin.SalesUserSimpleBaseVO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesDeliveryPickUpStoreConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryPickUpStoreDO;
import vip.appap.suxin.module.sales.service.SalesDeliveryPickUpStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 自提门店")
@RestController
@RequestMapping("/sales/delivery/pick-up-store")
@Validated
public class SalesDeliveryPickUpStoreController {

    @Resource
    private SalesDeliveryPickUpStoreService deliveryPickUpStoreService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建自提门店")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_pick_up_store:create')")
    public CommonResult<Long> createDeliveryPickUpStore(@Valid @RequestBody SalesDeliveryPickUpStoreCreateReqVO createReqVO) {
        return success(deliveryPickUpStoreService.createDeliveryPickUpStore(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新自提门店")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_pick_up_store:update')")
    public CommonResult<Boolean> updateDeliveryPickUpStore(@Valid @RequestBody SalesDeliveryPickUpStoreUpdateReqVO updateReqVO) {
        deliveryPickUpStoreService.updateDeliveryPickUpStore(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除自提门店")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_pick_up_store:delete')")
    public CommonResult<Boolean> deleteDeliveryPickUpStore(@RequestParam("id") Long id) {
        deliveryPickUpStoreService.deleteDeliveryPickUpStore(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得自提门店")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_pick_up_store:query')")
    public CommonResult<SalesDeliveryPickUpStoreRespVO> getDeliveryPickUpStore(@RequestParam("id") Long id) {
        SalesDeliveryPickUpStoreDO deliveryPickUpStore = deliveryPickUpStoreService.getDeliveryPickUpStore(id);
        if (deliveryPickUpStore == null) {
            return success(null);
        }
        List<AdminUserRespDTO> verifyUsers = CollUtil.isNotEmpty(deliveryPickUpStore.getVerifyUserIds()) ?
                adminUserApi.getUserList(deliveryPickUpStore.getVerifyUserIds()) : null;
        return success(BeanUtils.toBean(deliveryPickUpStore, SalesDeliveryPickUpStoreRespVO.class)
                .setVerifyUsers(BeanUtils.toBean(verifyUsers, SalesUserSimpleBaseVO.class)));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得自提门店精简信息列表")
    public CommonResult<List<SalesDeliveryPickUpStoreSimpleRespVO>> getSimpleDeliveryPickUpStoreList() {
        List<SalesDeliveryPickUpStoreDO> list = deliveryPickUpStoreService.getDeliveryPickUpStoreListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(SalesDeliveryPickUpStoreConvert.INSTANCE.convertList1(list));
    }

    @GetMapping("/list")
    @Operation(summary = "获得自提门店列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_pick_up_store:query')")
    public CommonResult<List<SalesDeliveryPickUpStoreRespVO>> getDeliveryPickUpStoreList(@RequestParam("ids") Collection<Long> ids) {
        List<SalesDeliveryPickUpStoreDO> list = deliveryPickUpStoreService.getDeliveryPickUpStoreList(ids);
        return success(SalesDeliveryPickUpStoreConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得自提门店分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_pick_up_store:query')")
    public CommonResult<PageResult<SalesDeliveryPickUpStoreRespVO>> getDeliveryPickUpStorePage(@Valid SalesDeliveryPickUpStorePageReqVO pageVO) {
        PageResult<SalesDeliveryPickUpStoreDO> pageResult = deliveryPickUpStoreService.getDeliveryPickUpStorePage(pageVO);
        return success(SalesDeliveryPickUpStoreConvert.INSTANCE.convertPage(pageResult));
    }

    @PostMapping("/bind")
    @Operation(summary = "绑定自提店员")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_pick_up_store:create')")
    public CommonResult<Boolean> bindDeliveryPickUpStore(@Valid @RequestBody SalesDeliveryPickUpBindReqVO bindReqVO) {
        deliveryPickUpStoreService.bindDeliveryPickUpStore(bindReqVO);
        return success(true);
    }

}
