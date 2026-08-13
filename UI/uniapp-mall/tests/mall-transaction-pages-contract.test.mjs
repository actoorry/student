import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');

test('order confirmation reads current settlement/create and delivery contracts', () => {
  const confirm = read('pages/order/confirm.vue');
  const orderApi = read('sheep/api/sales/order.js');
  assert.match(confirm, /OrderApi from '@\/sheep\/api\/sales\/order'/);
  assert.match(confirm, /SalesConfigApi from '@\/sheep\/api\/sales\/config'/);
  assert.match(confirm, /OrderApi\.settlementOrder/);
  assert.match(confirm, /OrderApi\.createOrder/);
  assert.match(orderApi, /\/sales\/order\/settlement/);
  assert.match(orderApi, /\/sales\/order\/create/);
  assert.match(confirm, /deliveryType/);
  assert.doesNotMatch(confirm, /\/trade\/order\/|\/member\/address/);
});

test('payment pages target the accountant module with current submit/get endpoints', () => {
  const cashier = read('pages/accountant/index.vue');
  const result = read('pages/accountant/result.vue');
  const pay = read('sheep/platform/pay.js');
  const payOrderApi = read('sheep/api/accountant/order.js');
  assert.match(cashier, /PayOrderApi from '@\/sheep\/api\/accountant\/order'/);
  assert.match(cashier, /PayChannelApi from '@\/sheep\/api\/accountant\/channel'/);
  assert.match(cashier, /sheep\.\$platform\.pay/);
  assert.match(pay, /PayOrderApi\.submitOrder/);
  assert.match(payOrderApi, /\/accountant\/order\/submit/);
  assert.match(payOrderApi, /\/accountant\/order\/get/);
  assert.match(result, /PayOrderApi from '@\/sheep\/api\/accountant\/order'/);
  assert.doesNotMatch(payOrderApi, /\/pay\/order\/submit/);
});

test('order list/detail, logistics and after-sale use sales endpoints and status-aware actions', () => {
  const orderList = read('pages/order/list.vue');
  const orderDetail = read('pages/order/detail.vue');
  const orderApi = read('sheep/api/sales/order.js');
  const afterSale = read('pages/order/aftersale/apply.vue');
  const afterSaleApi = read('sheep/api/sales/afterSale.js');
  assert.match(orderList, /OrderApi from '@\/sheep\/api\/sales\/order'/);
  assert.match(orderDetail, /\/pages\/order\/express\/log/);
  assert.match(orderApi, /\/sales\/order\/get-express-track-list/);
  assert.match(afterSale, /AfterSaleApi from '@\/sheep\/api\/sales\/afterSale'/);
  assert.match(afterSale, /createAfterSale/);
  assert.match(afterSaleApi, /\/sales\/after-sale\/create/);
  assert.doesNotMatch(orderList, /\/trade\/order\//);
  assert.doesNotMatch(afterSale, /\/trade\/after-sale\//);
});

test('promotion pages use current promotion contracts and keep customer service on kefu+realtime', () => {
  const coupon = read('pages/coupon/list.vue');
  const groupon = read('pages/activity/groupon/list.vue');
  const seckill = read('pages/activity/seckill/list.vue');
  const point = read('pages/activity/point/list.vue');
  const chat = read('pages/chat/index.vue');
  assert.match(coupon, /CouponApi from '@\/sheep\/api\/promotion\/coupon'/);
  assert.match(groupon, /CombinationApi from '@\/sheep\/api\/promotion\/combination'/);
  assert.match(seckill, /SeckillApi from '@\/sheep\/api\/promotion\/seckill'/);
  assert.match(point, /PointApi from '@\/sheep\/api\/promotion\/point'/);
  assert.match(chat, /KeFuApi from '@\/sheep\/api\/promotion\/kefu'/);
  assert.match(chat, /subscribeRealtime/);
  assert.doesNotMatch(chat, /\/im\/message\/private/);
  assert.doesNotMatch(chat, /\/trade\/promotion\//);
});
