package vip.appap.suxin.module.sales.job.order;

import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import vip.appap.suxin.module.sales.service.SalesOrderUpdateService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 交易订单的自动过期 Job
 *
 * @author 书心软件
 */
@Component
public class SalesOrderAutoCancelJob implements JobHandler {

    @Resource
    private SalesOrderUpdateService tradeOrderUpdateService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = tradeOrderUpdateService.cancelOrderBySystem();
        return String.format("过期订单 %s 个", count);
    }

}
