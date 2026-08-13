package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.system.api.NotifyMessageSendApi;
import vip.appap.suxin.module.system.api.dto.NotifySendSingleToUserReqDTO;
import vip.appap.suxin.module.sales.enums.MessageTemplateConstants;
import vip.appap.suxin.module.sales.service.bo.SalesOrderMessageWhenDeliveryOrderReqBO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Trade 消息 service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesMessageServiceImpl implements SalesMessageService {

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    public void sendMessageWhenDeliveryOrder(SalesOrderMessageWhenDeliveryOrderReqBO reqBO) {
        if (true) {
            return;
        }
        // 1、构造消息
        Map<String, Object> msgMap = new HashMap<>(2);
        msgMap.put("orderId", reqBO.getOrderId());
        msgMap.put("deliveryMessage", reqBO.getMessage());
        // TODO 芋艿：看下模版
        // 2、发送站内信
        notifyMessageSendApi.sendSingleMessageToMember(
                new NotifySendSingleToUserReqDTO()
                        .setUserId(reqBO.getUserId())
                        .setTemplateCode(MessageTemplateConstants.SMS_ORDER_DELIVERY)
                        .setTemplateParams(msgMap));
    }

}
