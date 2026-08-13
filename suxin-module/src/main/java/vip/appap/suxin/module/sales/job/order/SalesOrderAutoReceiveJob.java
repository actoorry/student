package vip.appap.suxin.module.sales.job.order;

import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import vip.appap.suxin.module.sales.service.SalesOrderUpdateService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 交易订单的自动收货 Job
 *
 * @author 书心软件
 */
@Component
public class SalesOrderAutoReceiveJob implements JobHandler {

    @Resource
    private SalesOrderUpdateService tradeOrderUpdateService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = tradeOrderUpdateService.receiveOrderBySystem();
        return String.format("自动收货 %s 个", count);
    }

}
