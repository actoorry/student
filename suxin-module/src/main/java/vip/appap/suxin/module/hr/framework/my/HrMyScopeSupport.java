package vip.appap.suxin.module.hr.framework.my;

import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.HR_MY_DATA_NOT_YOURS;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.HR_MY_EMPLOYEE_NOT_BOUND;

/**
 * HR 职工自助「我的」数据范围校验支持类
 *
 * 约定：system_users.id = partner.id（见 hr_import_system_users.sql）。
 * 所有 /hr/my/* 接口必须通过本类取得 partnerId 并校验记录归属，
 * 不得直接使用前端传入的 partnerId。
 *
 * @author suxin
 */
@Component
public class HrMyScopeSupport {

    @Resource
    private EmployeeMapper employeeMapper;

    /**
     * 取得当前登录用户对应的 partnerId（= loginUserId），并校验已绑定员工档案。
     * 未绑定档案时抛 HR_MY_EMPLOYEE_NOT_BOUND，禁止静默返回空数据。
     */
    public Long requireBoundPartnerId() {
        Long userId = getLoginUserId();
        if (userId == null) {
            throw exception(HR_MY_EMPLOYEE_NOT_BOUND);
        }
        Long employeeId = employeeMapper.selectIdByPartnerId(userId);
        if (employeeId == null) {
            throw exception(HR_MY_EMPLOYEE_NOT_BOUND);
        }
        return userId;
    }

    /**
     * 校验某条记录的 partnerId 是否属于当前登录用户。
     * 用于 get / update / delete 前的归属校验。
     */
    public void validateBelongsToMe(Long recordPartnerId) {
        Long me = requireBoundPartnerId();
        if (recordPartnerId == null || !recordPartnerId.equals(me)) {
            throw exception(HR_MY_DATA_NOT_YOURS);
        }
    }

}
