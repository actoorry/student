package vip.appap.suxin.module.hr.controller.admin.positionchange;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.hr.controller.admin.positionchange.vo.*;
import vip.appap.suxin.module.hr.service.positionchange.PositionChangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HR 岗位异动")
@RestController
@RequestMapping("/hr/position-change")
@Validated
public class PositionChangeController {

    @Resource
    private PositionChangeService positionChangeService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('hr:position-change:create')")
    public CommonResult<Long> createPositionChange(@Valid @RequestBody PositionChangeSaveReqVO createReqVO) {
        return success(positionChangeService.createPositionChange(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('hr:position-change:update')")
    public CommonResult<Boolean> updatePositionChange(@Valid @RequestBody PositionChangeSaveReqVO updateReqVO) {
        positionChangeService.updatePositionChange(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('hr:position-change:delete')")
    public CommonResult<Boolean> deletePositionChange(@RequestParam("id") Long id) {
        positionChangeService.deletePositionChange(id);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('hr:position-change:query')")
    public CommonResult<PositionChangeRespVO> getPositionChange(@RequestParam("id") Long id) {
        return success(positionChangeService.getPositionChange(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('hr:position-change:query')")
    public CommonResult<PageResult<PositionChangeRespVO>> getPositionChangePage(
            @Valid PositionChangePageReqVO pageReqVO) {
        return success(positionChangeService.getPositionChangePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('hr:position-change:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPositionChangeExcel(@Valid PositionChangePageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PositionChangeRespVO> list = positionChangeService.getPositionChangePage(pageReqVO).getList();
        ExcelUtils.write(response, "岗位异动.xls", "数据", PositionChangeRespVO.class, list);
    }

    @GetMapping("/timeline")
    @Operation(summary = "岗位变动时间轴（含外出记录）")
    @PreAuthorize("@ss.hasPermission('hr:position-change:query')")
    public CommonResult<List<PositionTimelineRespVO>> getPositionTimeline(
            @RequestParam("partnerId") Long partnerId) {
        return success(positionChangeService.getPositionTimeline(partnerId));
    }

}
