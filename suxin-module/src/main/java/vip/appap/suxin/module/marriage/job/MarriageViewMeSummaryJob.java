package vip.appap.suxin.module.marriage.job;

import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import vip.appap.suxin.module.marriage.service.MarriageInteractionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class MarriageViewMeSummaryJob implements JobHandler {

    @Resource
    private MarriageInteractionService marriageInteractionService;

    @Override
    @TenantJob
    public String execute(String param) {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.minusDays(1).atStartOfDay();
        LocalDateTime endTime = today.atStartOfDay();
        int count = marriageInteractionService.generateViewMeSummaryNotifications(startTime, endTime);
        return String.format("生成谁看过我汇总通知 %s 条", count);
    }

}
