package vip.appap.suxin.module.partner.job;

import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import vip.appap.suxin.module.partner.service.PartnerService;

/**
 * 客户自动掉入公海 Job
 *
 * @author 书心软件
 */
@Component
public class PartnerAutoPutPoolJob implements JobHandler {

    @Resource
    private PartnerService partnerService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = partnerService.autoPutPartnerPool();
        return String.format("掉入公海客户 %s 个", count);
    }

}
