package vip.appap.suxin.module.system.controller.admin;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.system.controller.admin.vo.LoginLogPageReqVO;
import vip.appap.suxin.module.system.controller.admin.vo.LoginLogRespVO;
import vip.appap.suxin.module.system.dal.dataobject.LoginLogDO;
import vip.appap.suxin.module.system.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 登录日志")
@RestController
@RequestMapping("/system/login-log")
@Validated
public class LoginLogController {

    @Resource
    private LoginLogService loginLogService;

    @GetMapping("/get")
    @Operation(summary = "获得登录日志")
    @PreAuthorize("@ss.hasPermission('system:login-log:query')")
    public CommonResult<LoginLogRespVO> getLoginLog(Long id) {
        LoginLogDO loginLog = loginLogService.getLoginLog(id);
        return success(BeanUtils.toBean(loginLog, LoginLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得登录日志分页列表")
    @PreAuthorize("@ss.hasPermission('system:login-log:query')")
    public CommonResult<PageResult<LoginLogRespVO>> getLoginLogPage(@Valid LoginLogPageReqVO pageReqVO) {
        PageResult<LoginLogDO> pageResult = loginLogService.getLoginLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, LoginLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出登录日志 Excel")
    @PreAuthorize("@ss.hasPermission('system:login-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLoginLog(HttpServletResponse response, @Valid LoginLogPageReqVO exportReqVO) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<LoginLogDO> list = loginLogService.getLoginLogPage(exportReqVO).getList();
        // 输出
        ExcelUtils.write(response, "登录日志.xls", "数据列表", LoginLogRespVO.class,
                BeanUtils.toBean(list, LoginLogRespVO.class));
    }

}
