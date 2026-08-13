import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');

test('Mall cart API uses the restored sales routes', () => {
  const api = read('sheep/api/sales/cart.js');
  for (const route of [
    '/sales/cart/add',
    '/sales/cart/update-count',
    '/sales/cart/update-selected',
    '/sales/cart/reset',
    '/sales/cart/delete',
    '/sales/cart/list',
    '/sales/cart/get-count',
  ]) {
    assert.match(api, new RegExp(route.replaceAll('/', '\\/')));
  }
  assert.doesNotMatch(api, /\/trade\/cart\//);
});

test('Cart store and page keep checkout limited to valid backend items', () => {
  const store = read('sheep/store/cart.js');
  const page = read('pages/index/cart.vue');
  assert.match(store, /CartApi from '@\/sheep\/api\/sales\/cart'/);
  assert.match(store, /this\.editMode \? this\.list : this\.newList/);
  assert.match(page, /cart\.newList\.filter/);
  assert.match(page, /cartId: item\.id/);
});

test('Order flow serializes cart IDs and refreshes the shared cart after checkout', () => {
  const api = read('sheep/api/sales/order.js');
  const confirm = read('pages/order/confirm.vue');
  assert.match(api, /items\[' \+ i \+ '' \+ '\]\.cartId'/);
  assert.match(confirm, /items\.some\(\(item\) => item\.cartId > 0\)/);
  assert.match(confirm, /sheep\.\$store\('cart'\)\.getList\(\)/);
});

test('Goods detail and user lifecycle share the cart store', () => {
  const goods = read('pages/goods/index.vue');
  const user = read('sheep/store/user.js');
  assert.match(goods, /sheep\.\$store\('cart'\)\.add\(e\)/);
  assert.match(user, /cart\(\)\.getList\(\)/);
  assert.match(user, /cart\(\)\.emptyList\(\)/);
});
