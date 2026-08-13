import {
  disconnectRealtime,
  ensureRealtimeConnection,
  isRealtimeConnected,
  subscribeRealtime,
  unsubscribeRealtime,
} from '@/sheep/api/infra/realtime';
export { parseRealtimeFrame as parseSocketFrame } from '@/sheep/helper/realtime-messaging';

const IM_TYPE = 'im-private-message';
const INTERACTION_TYPE = 'app-interaction-notification';

export const addImMessageListener = (key, listener) => subscribeRealtime(IM_TYPE, key, listener);
export const removeImMessageListener = (key) => unsubscribeRealtime(IM_TYPE, key);
export const addInteractionNotificationListener = (key, listener) => subscribeRealtime(INTERACTION_TYPE, key, listener);
export const removeInteractionNotificationListener = (key) => unsubscribeRealtime(INTERACTION_TYPE, key);
export const connectImWebSocket = ensureRealtimeConnection;
export const disconnectImWebSocket = () => disconnectRealtime({ clearListeners: true });
export const isImWebSocketConnected = isRealtimeConnected;
