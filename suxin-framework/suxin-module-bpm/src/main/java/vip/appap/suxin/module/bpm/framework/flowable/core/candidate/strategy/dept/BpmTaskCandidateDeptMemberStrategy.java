package vip.appap.suxin.module.bpm.framework.flowable.core.candidate.strategy.dept;

import vip.appap.suxin.framework.common.util.string.StrUtils;
import vip.appap.suxin.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import vip.appap.suxin.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import vip.appap.suxin.module.system.api.DeptApi;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 部门的成员 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidateDeptMemberStrategy implements BpmTaskCandidateStrategy {

    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.DEPT_MEMBER;
    }

    @Override
    public void validateParam(String param) {
        Set<Long> deptIds = StrUtils.splitToLongSet(param);
        deptApi.validateDeptList(deptIds);
    }

    @Override
    public Set<Long> calculateUsers(String param) {
        Set<Long> deptIds = StrUtils.splitToLongSet(param);
        List<AdminUserRespDTO> users = adminUserApi.getUserListByDeptIds(deptIds);
        return convertSet(users, AdminUserRespDTO::getId);
    }

}