package vip.appap.suxin.module.hr.controller.admin.my;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.hr.controller.admin.employee.vo.EmployeeRespVO;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;
import vip.appap.suxin.module.hr.framework.my.HrMyScopeSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

/**
 * 职工「我的」公用只读接口
 *
 * 供各填报表单展示本人姓名/工号/科室，避免调用管理端 /hr/employee/page。
 *
 * @author suxin
 */
@Tag(name = "管理后台 - 职工我的公用")
@RestController
@RequestMapping("/hr/my/self")
@Validated
public class HrMySelfController {

    @Resource
    private HrMyScopeSupport myScope;
    @Resource
    private EmployeeMapper employeeMapper;

    @GetMapping("/employee")
    @Operation(summary = "获得当前登录职工基本信息（表单只读展示）")
    @PreAuthorize("@ss.hasAnyPermissions('hr:my:profile:query','hr:my:contract:query','hr:my:resume:query','hr:my:outbound:query','hr:my:certificate:query','hr:my:overtime:query')")
    public CommonResult<EmployeeRespVO> getMyEmployee() {
        Long partnerId = myScope.requireBoundPartnerId();
        return success(employeeMapper.selectByPartnerIdJoin(partnerId));
    }

}
