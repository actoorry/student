import { baseUrl, websocketPath } from '@/sheep/config';
import { getAccessToken } from '@/sheep/request';
import { buildWebSocketUrl, createKeyedSubscribers, parseRealtimeFrame } from '@/sheep/helper/realtime-messaging';

const subscribers = createKeyedSubscribers((type, key, error) => console.error(`[mall-realtime] listener ${type}:${key} failed`, error));
const RECONNECT_INTERVAL = 5000;
const HEARTBEAT_INTERVAL = 30000;
const CONNECTION_EVENT = '__mall-realtime-connected__';
let socketTask = null;
let activeToken = '';
let reconnectTimer = null;
let heartbeatTimer = null;
let connecting = false;
let connected = false;
let allowReconnect = true;

function clearTimers() {
  if (reconnectTimer) clearTimeout(reconnectTimer);
  if (heartbeatTimer) clearInterval(heartbeatTimer);
  reconnectTimer = null;
  heartbeatTimer = null;
}

function markSocketDisconnected(task) {
  if (socketTask !== task) return false;
  socketTask = null;
  connecting = false;
  connected = false;
  if (heartbeatTimer) clearInterval(heartbeatTimer);
  heartbeatTimer = null;
  return true;
}

function scheduleReconnect() {
  if (!allowReconnect || reconnectTimer || !getAccessToken()) return;
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    ensureRealtimeConnection();
  }, RECONNECT_INTERVAL);
}

function closeSocket(disableReconnect) {
  if (disableReconnect) allowReconnect = false;
  clearTimers();
  const task = socketTask;
  socketTask = null;
  connecting = false;
  connected = false;
  if (task) task.close({ code: 1000, reason: 'Normal closure' });
}

export function subscribeRealtime(type, key, listener) {
  subscribers.subscribe(type, key, listener);
}

export function unsubscribeRealtime(type, key) {
  subscribers.unsubscribe(type, key);
}

export const subscribeRealtimeConnection = (key, listener) => subscribeRealtime(CONNECTION_EVENT, key, listener);
export const unsubscribeRealtimeConnection = (key) => unsubscribeRealtime(CONNECTION_EVENT, key);

export function ensureRealtimeConnection() {
  const token = getAccessToken();
  if (!token) return;
  if (socketTask && activeToken === token && (connected || connecting)) return;
  closeSocket(false);
  activeToken = token;
  allowReconnect = true;
  connecting = true;
  let task;
  try {
    task = uni.connectSocket({ url: buildWebSocketUrl(baseUrl, websocketPath, token) });
  } catch (error) {
    connecting = false;
    console.warn('[mall-realtime] websocket creation failed', error);
    scheduleReconnect();
    return;
  }
  socketTask = task;
  task.onOpen(() => {
    if (socketTask !== task) return;
    connecting = false;
    connected = true;
    subscribers.dispatch(CONNECTION_EVENT);
    heartbeatTimer = setInterval(() => {
      if (connected && socketTask === task) task.send({ data: 'ping' });
    }, HEARTBEAT_INTERVAL);
  });
  task.onMessage((event) => {
    if (socketTask !== task) return;
    try {
      const frame = parseRealtimeFrame(event.data);
      if (frame) subscribers.dispatch(frame.type, frame.content);
    } catch (error) {
      console.warn('[mall-realtime] ignored invalid websocket frame', error);
    }
  });
  task.onClose(() => {
    if (!markSocketDisconnected(task)) return;
    scheduleReconnect();
  });
  task.onError((error) => {
    if (!markSocketDisconnected(task)) return;
    // Some uni-app targets do not emit onClose after a failed connection. Resetting
    // the task here is therefore required to make the single reconnect timer effective.
    console.warn('[mall-realtime] websocket error, scheduling reconnect', error);
    try { task.close({ code: 1000, reason: 'Connection error' }); } catch (closeError) { /* ignore */ }
    scheduleReconnect();
  });
}

export function disconnectRealtime({ clearListeners = false } = {}) {
  activeToken = '';
  closeSocket(true);
  if (clearListeners) subscribers.clear();
}

export function isRealtimeConnected() {
  return connected;
}
