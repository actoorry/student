package vip.appap.suxin.module.iot.gateway.protocol.tcp.handler.downstream;

import vip.appap.suxin.module.iot.core.messagebus.core.IotMessageBus;
import vip.appap.suxin.module.iot.core.mq.message.IotDeviceMessage;
import vip.appap.suxin.module.iot.gateway.protocol.IotProtocol;
import vip.appap.suxin.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 下游订阅者：接收下行给设备的消息
 *
 * @author 书心软件
 */
@Slf4j
public class IotTcpDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    private final IotTcpDownstreamHandler downstreamHandler;

    public IotTcpDownstreamSubscriber(IotProtocol protocol,
                                      IotTcpDownstreamHandler downstreamHandler,
                                      IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

}
