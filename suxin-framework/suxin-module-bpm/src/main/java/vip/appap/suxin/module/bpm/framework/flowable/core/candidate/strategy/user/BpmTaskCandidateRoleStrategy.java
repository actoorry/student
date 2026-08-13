package vip.appap.suxin.module.bpm.framework.flowable.core.candidate.strategy.user;

import vip.appap.suxin.framework.common.util.string.StrUtils;
import vip.appap.suxin.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import vip.appap.suxin.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import vip.appap.suxin.module.system.api.PermissionApi;
import vip.appap.suxin.module.system.api.RoleApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 角色 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidateRoleStrategy implements BpmTaskCandidateStrategy {

    @Resource
    private RoleApi roleApi;
    @Resource
    private PermissionApi permissionApi;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.ROLE;
    }

    @Override
    public void validateParam(String param) {
        Set<Long> roleIds = StrUtils.splitToLongSet(param);
        roleApi.validRoleList(roleIds);
    }

    @Override
    public Set<Long> calculateUsers(String param) {
        Set<Long> roleIds = StrUtils.splitToLongSet(param);
        return permissionApi.getUserRoleIdListByRoleIds(roleIds);
    }

}