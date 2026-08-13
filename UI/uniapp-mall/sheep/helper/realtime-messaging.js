/** Pure contracts shared by the Mall realtime transport and focused Node tests. */
export function buildWebSocketUrl(baseUrl, websocketPath, token) {
  const base = String(baseUrl || '').trim().replace(/^http:/i, 'ws:').replace(/^https:/i, 'wss:').replace(/\/+$/, '');
  const path = `/${String(websocketPath || '/infra/ws').replace(/^\/+/, '')}`;
  return `${base}${path}?token=${encodeURIComponent(token || '')}`;
}

export function parseRealtimeFrame(raw) {
  if (!raw || raw === 'pong') return null;
  const frame = typeof raw === 'string' ? JSON.parse(raw) : raw;
  if (!frame || !frame.type) return null;
  return {
    type: frame.type,
    content: typeof frame.content === 'string' ? JSON.parse(frame.content) : frame.content,
  };
}

export function createKeyedSubscribers(onListenerError = () => {}) {
  const types = new Map();
  return {
    subscribe(type, key, listener) {
      if (!types.has(type)) types.set(type, new Map());
      types.get(type).set(key, listener);
    },
    unsubscribe(type, key) {
      const typed = types.get(type);
      typed?.delete(key);
      if (typed?.size === 0) types.delete(type);
    },
    clear() { types.clear(); },
    dispatch(type, payload) {
      types.get(type)?.forEach((listener, key) => {
        try { listener(payload); } catch (error) { onListenerError(type, key, error); }
      });
    },
  };
}

export function unwrapResponse(response, fallback = '请求失败') {
  if (!response || Number(response.code) !== 0) {
    throw new Error(response?.msg || fallback);
  }
  return response.data;
}

export function mergeByMessageId(current, incoming, { prepend = false } = {}) {
  const map = new Map();
  (prepend ? [...incoming, ...current] : [...current, ...incoming]).forEach((item) => {
    const key = Number(item?.id) > 0 ? `id-${item.id}` : item?.clientMessageId || item?.localId;
    if (key) map.set(key, item);
  });
  return [...map.values()].sort((left, right) => {
    const leftTime = new Date(left.createTime || left.sendTime || 0).getTime();
    const rightTime = new Date(right.createTime || right.sendTime || 0).getTime();
    return leftTime - rightTime || Number(left.id || 0) - Number(right.id || 0);
  });
}

export function getOldestCursor(messages, field) {
  const values = messages.map((item) => item?.[field]).filter(Boolean);
  return values.sort()[0];
}
