package vip.appap.suxin.module.accountant.job.order;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import vip.appap.suxin.module.accountant.service.WechatVirtualPayService;

@Component
public class WechatVirtualPayOrderSyncJob implements JobHandler {

    @Resource
    private WechatVirtualPayService wechatVirtualPayService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = wechatVirtualPayService.syncPendingWechatVirtualPayOrders();
        return StrUtil.format("同步微信虚拟支付订单 {} 个", count);
    }

}
