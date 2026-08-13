package vip.appap.suxin.module.hr.service.employee;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.appap.suxin.module.hr.dal.dataobject.employee.EmployeeDO;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;
import vip.appap.suxin.module.system.dal.dataobject.PostDO;
import vip.appap.suxin.module.system.dal.dataobject.UserPostDO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import vip.appap.suxin.module.system.dal.mysql.UserPostMapper;
import vip.appap.suxin.module.system.dal.mysql.AdminUserMapper;
import vip.appap.suxin.module.system.service.PostService;

/**
 * 员工档案岗位与系统用户岗位双向同步
 */
@Service
public class EmployeePostSyncServiceImpl implements EmployeePostSyncService {

    @Resource
    private AdminUserMapper userMapper;
    @Resource
    private UserPostMapper userPostMapper;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private PostService postService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUserPostFromEmployee(Long partnerId, Long postId) {
        if (partnerId == null) {
            return;
        }
        AdminUserDO user = userMapper.selectById(partnerId);
        if (user == null) {
            return;
        }
        userPostMapper.deleteByUserId(partnerId);
        if (postId != null) {
            userPostMapper.insert(new UserPostDO().setUserId(partnerId).setPostId(postId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncEmployeePostFromUser(Long partnerId, Long postId) {
        if (partnerId == null) {
            return;
        }
        Long employeeId = employeeMapper.selectIdByPartnerId(partnerId);
        if (employeeId == null) {
            return;
        }
        EmployeeDO update = new EmployeeDO();
        update.setId(employeeId);
        update.setPostId(postId);
        if (postId != null) {
            PostDO post = postService.getPost(postId);
            if (post != null) {
                update.setPosition(post.getName());
            }
        } else {
            update.setPosition(null);
        }
        employeeMapper.updateById(update);
    }

}
