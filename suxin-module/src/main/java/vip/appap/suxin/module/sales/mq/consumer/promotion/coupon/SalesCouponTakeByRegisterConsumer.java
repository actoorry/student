package vip.appap.suxin.module.sales.mq.consumer.promotion.coupon;

import vip.appap.suxin.module.partner.api.PartnerUserCreateMessage;
import vip.appap.suxin.module.sales.service.SalesCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 用户注册时，发送优惠劵的消费者，基 {@link PartnerUserCreateMessage} 消息
 *
 * @author owen
 */
@Component
@Slf4j
public class SalesCouponTakeByRegisterConsumer {

    @Resource
    private SalesCouponService couponService;

    @EventListener
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步
    public void onMessage(PartnerUserCreateMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        couponService.takeCouponByRegister(message.getUserId());
    }

}
