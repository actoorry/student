import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');

test('Order confirmation page exposes the custom navbar back button', () => {
  const confirm = read('pages/order/confirm.vue');
  assert.match(confirm, /<s-layout\s+title="确认订单"\s+showLeftButton>/);
});
