package vip.appap.suxin.framework.websocket.core.sender.local;

import vip.appap.suxin.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import vip.appap.suxin.framework.websocket.core.sender.WebSocketMessageSender;
import vip.appap.suxin.framework.websocket.core.session.WebSocketSessionManager;

/**
 * 本地的 {@link WebSocketMessageSender} 实现类
 *
 * 注意：仅仅适合单机场景！！！
 *
 * @author 书心软件
 */
public class LocalWebSocketMessageSender extends AbstractWebSocketMessageSender {

    public LocalWebSocketMessageSender(WebSocketSessionManager sessionManager) {
        super(sessionManager);
    }

}
