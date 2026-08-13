package vip.appap.suxin.module.rongjh.job;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import vip.appap.suxin.module.rongjh.service.LoveValueService;
import vip.appap.suxin.module.rongjh.service.dto.LoveValueBackfillReport;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 戎爱心历史数据回填任务
 * param 格式：startId,batchSize,dryRun；示例：0,1000,true
 */
@Component
public class LoveValueBackfillJob implements JobHandler {

    @Resource
    private LoveValueService loveValueService;

    @Override
    @TenantJob
    public String execute(String param) {
        String[] arr = StrUtil.splitToArray(StrUtil.blankToDefault(param, "0,1000,true"), ',');
        Long startId = arr.length > 0 ? Long.parseLong(arr[0].trim()) : 0L;
        Integer batchSize = arr.length > 1 ? Integer.parseInt(arr[1].trim()) : 1000;
        boolean dryRun = arr.length <= 2 || Boolean.parseBoolean(arr[2].trim());
        LoveValueBackfillReport report = loveValueService.backfillCompletedOrders(startId, batchSize, dryRun);
        return StrUtil.format("回填完成 dryRun={} scanned={} applied={} skipped={} maxOrderId={}",
                report.isDryRun(), report.getScannedOrders(), report.getAppliedOrders(),
                report.getSkippedOrders(), report.getMaxOrderId());
    }
}
