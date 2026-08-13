package vip.appap.suxin.module.bpm.framework.flowable.core.candidate.strategy.user;

import vip.appap.suxin.framework.common.util.string.StrUtils;
import vip.appap.suxin.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import vip.appap.suxin.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import vip.appap.suxin.module.system.api.PostApi;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 岗位 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidatePostStrategy implements BpmTaskCandidateStrategy {

    @Resource
    private PostApi postApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.POST;
    }

    @Override
    public void validateParam(String param) {
        Set<Long> postIds = StrUtils.splitToLongSet(param);
        postApi.validPostList(postIds);
    }

    @Override
    public Set<Long> calculateUsers(String param) {
        Set<Long> postIds = StrUtils.splitToLongSet(param);
        List<AdminUserRespDTO> users = adminUserApi.getUserListByPostIds(postIds);
        return convertSet(users, AdminUserRespDTO::getId);
    }

}