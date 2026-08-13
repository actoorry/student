package vip.appap.suxin.module.wms.job;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import vip.appap.suxin.module.wms.service.stock.WmsStockService;

/**
 * 清理零库存记录 Job
 * <p>
 * 定期清理 wms_stock 中库存数量与预占数量均为 0 的快照行，避免无意义零库存记录堆积。
 * 处理器 Bean 名称：{@code wmsStockCleanupJob}
 *
 * @author admin
 */
@Component
public class WmsStockCleanupJob implements JobHandler {

    @Resource
    private WmsStockService stockService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = stockService.cleanZeroStock();
        return StrUtil.format("清理零库存记录 {} 条", count);
    }

}
