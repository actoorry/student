import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';
import { buildWebSocketUrl, createKeyedSubscribers, getOldestCursor, mergeByMessageId, parseRealtimeFrame, unwrapResponse } from '../sheep/helper/realtime-messaging.js';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (file) => fs.readFileSync(path.join(root, file), 'utf8');

test('realtime transport uses access-token URLs, parses frames, and isolates keyed subscribers', () => {
  assert.equal(buildWebSocketUrl('https://api.example.com/', '/infra/ws', 'a b'), 'wss://api.example.com/infra/ws?token=a%20b');
  assert.deepEqual(parseRealtimeFrame('{"type":"im-private-message","content":"{\\"id\\":1}"}'), { type: 'im-private-message', content: { id: 1 } });
  assert.equal(parseRealtimeFrame('pong'), null);
  const failures = []; const hub = createKeyedSubscribers((type, key) => failures.push(`${type}:${key}`)); const received = [];
  hub.subscribe('message', 'same', () => received.push('old')); hub.subscribe('message', 'same', () => received.push('new'));
  hub.subscribe('message', 'bad', () => { throw new Error('bad'); }); hub.subscribe('message', 'good', () => received.push('good'));
  hub.dispatch('message', 1); hub.unsubscribe('message', 'same'); hub.dispatch('message', 2);
  assert.deepEqual(received, ['new', 'good', 'good']); assert.deepEqual(failures, ['message:bad', 'message:bad']);
});

test('message contracts reject failures and merge HTTP/WebSocket histories without duplicates', () => {
  assert.throws(() => unwrapResponse({ code: 500, msg: '失败' }), /失败/);
  const messages = mergeByMessageId([{ id: 2, createTime: '2026-01-02' }], [{ id: 1, createTime: '2026-01-01' }, { id: 2, createTime: '2026-01-02' }], { prepend: true });
  assert.deepEqual(messages.map((item) => item.id), [1, 2]); assert.equal(getOldestCursor(messages, 'createTime'), '2026-01-01');
});

test('Mall integrations keep one transport and preserve backend contracts', () => {
  const realtime = read('sheep/api/infra/realtime.js'); const kefuApi = read('sheep/api/promotion/kefu.js'); const kefuPage = read('pages/chat/index.vue'); const list = read('pages/chat/components/messageList.vue');
  assert.match(realtime, /getAccessToken/); assert.doesNotMatch(realtime, /getRefreshToken/); assert.match(realtime, /reconnectTimer/);
  assert.match(realtime, /onError[\s\S]*markSocketDisconnected[\s\S]*scheduleReconnect/);
  assert.match(kefuApi, /\/sales\/promotion\/kefu-message\/update-read-status/); assert.match(kefuPage, /subscribeRealtime/); assert.doesNotMatch(kefuPage, /useWebSocket/);
  assert.match(list, /createTime/); assert.doesNotMatch(list, /queryParams\.no|\bno:\s*1/);
});
