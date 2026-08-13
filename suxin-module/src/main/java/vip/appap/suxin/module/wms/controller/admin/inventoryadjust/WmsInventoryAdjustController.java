package vip.appap.suxin.module.wms.controller.admin.inventoryadjust;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;
import java.util.*;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

import vip.appap.suxin.module.wms.controller.admin.inventoryadjust.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.inventoryadjust.WmsInventoryAdjustDO;
import vip.appap.suxin.module.wms.service.inventoryadjust.WmsInventoryAdjustService;

@Tag(name = "管理后台 - 盘点调整记录")
@RestController
@RequestMapping("/wms/inventory-adjust")
@Validated
public class WmsInventoryAdjustController {

    @Resource
    private WmsInventoryAdjustService inventoryAdjustService;

    @PostMapping("/create")
    @Operation(summary = "创建盘点调整（草稿），自动读取当前账面库存")
    @PreAuthorize("@ss.hasPermission('wms:inventory-adjust:create')")
    public CommonResult<Long> createInventoryAdjust(
            @Validated(WmsInventoryAdjustSaveReqVO.Create.class) @RequestBody WmsInventoryAdjustSaveReqVO createReqVO) {
        return success(inventoryAdjustService.createInventoryAdjust(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新盘点调整草稿")
    @PreAuthorize("@ss.hasPermission('wms:inventory-adjust:update')")
    public CommonResult<Boolean> updateInventoryAdjust(@Valid @RequestBody WmsInventoryAdjustSaveReqVO updateReqVO) {
        inventoryAdjustService.updateInventoryAdjust(updateReqVO);
        return success(true);
    }

    @PutMapping("/approve")
    @Operation(summary = "审核盘点调整（草稿→已审核），自动更新库存并写入流水")
    @PreAuthorize("@ss.hasPermission('wms:inventory-adjust:finish')")
    public CommonResult<Boolean> approveInventoryAdjust(@RequestParam("id") Long id) {
        inventoryAdjustService.approveInventoryAdjust(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘点调整草稿")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inventory-adjust:delete')")
    public CommonResult<Boolean> deleteInventoryAdjust(@RequestParam("id") Long id) {
        inventoryAdjustService.deleteInventoryAdjust(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得盘点调整记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inventory-adjust:query')")
    public CommonResult<WmsInventoryAdjustRespVO> getInventoryAdjust(@RequestParam("id") Long id) {
        WmsInventoryAdjustDO adjust = inventoryAdjustService.getInventoryAdjust(id);
        return success(BeanUtils.toBean(adjust, WmsInventoryAdjustRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得盘点调整记录分页")
    @PreAuthorize("@ss.hasPermission('wms:inventory-adjust:query')")
    public CommonResult<PageResult<WmsInventoryAdjustRespVO>> getInventoryAdjustPage(
            @Valid WmsInventoryAdjustPageReqVO pageReqVO) {
        PageResult<WmsInventoryAdjustDO> pageResult = inventoryAdjustService.getInventoryAdjustPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsInventoryAdjustRespVO.class));
    }
}