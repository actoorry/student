package vip.appap.suxin.module.partner.job;

import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import vip.appap.suxin.module.partner.service.PartnerMemberService;

@Component
public class PartnerMemberExpireJob implements JobHandler {

    @Resource
    private PartnerMemberService partnerMemberService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = partnerMemberService.expireMembers();
        return String.format("过期会员 %s 条", count);
    }

}
