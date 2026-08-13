package vip.appap.suxin.module.hr.service.employee;

/**
 * 员工档案岗位与系统用户岗位双向同步
 */
public interface EmployeePostSyncService {

    /**
     * 员工档案岗位变更后，同步到 system_user_post（若已开通登录账号）
     */
    void syncUserPostFromEmployee(Long partnerId, Long postId);

    /**
     * 用户管理岗位变更后，同步到 hr_employee.post_id
     */
    void syncEmployeePostFromUser(Long partnerId, Long postId);

}
