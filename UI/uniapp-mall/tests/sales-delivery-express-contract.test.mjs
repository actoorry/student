import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');

test('sales 配送/订单/售后 API 只使用 /sales/**，无 /trade/** 残留', () => {
  for (const file of ['sheep/api/sales/delivery.js', 'sheep/api/sales/order.js', 'sheep/api/sales/afterSale.js']) {
    const content = read(file);
    assert.doesNotMatch(content, /\/trade\//, `${file} 不应包含 /trade/`);
  }
  assert.match(read('sheep/api/sales/delivery.js'), /\/sales\/delivery\/express\/list/);
  assert.match(read('sheep/api/sales/order.js'), /\/sales\/order\/settlement/);
  assert.match(read('sheep/api/sales/afterSale.js'), /\/sales\/after-sale\/delivery/);
});

test('订单确认页保留四种配送方式全局交集，不回退原版二选一', () => {
  const confirm = read('pages/order/confirm.vue');
  // 全局交集：所有订单项共同配送方式
  assert.match(confirm, /availableDeliveryTypes/);
  // 四种合法配送方式 0/1/2/3
  assert.match(confirm, /\[0, 1, 2, 3\]/);
  // 只在快递要求地址
  assert.match(confirm, /deliveryType === 1/);
  assert.match(confirm, /addressInfo\.id/);
  // 只在自提要求门店与联系人
  assert.match(confirm, /deliveryType === 2/);
  assert.match(confirm, /pickUpInfo\.id/);
  assert.match(confirm, /receiverName/);
  assert.match(confirm, /receiverMobile/);
  // 展示服务端 deliveryPrice，不在客户端复算模板
  assert.match(confirm, /price\.deliveryPrice/);
  // 地址或配送方式变化后重新结算
  assert.match(confirm, /watch\(addressState/);
});

test('售后退货页使用 sales API 提交公司与单号，不新增第二套请求层', () => {
  const returnDelivery = read('pages/order/aftersale/return-delivery.vue');
  assert.match(returnDelivery, /@\/sheep\/api\/sales\/afterSale/);
  assert.match(returnDelivery, /@\/sheep\/api\/sales\/delivery/);
  assert.match(returnDelivery, /logisticsId:/);
  assert.match(returnDelivery, /logisticsNo:/);
  assert.match(returnDelivery, /deliveryAfterSale\(/);
  // 复用同一快递公司主数据（sales delivery API）
  assert.match(returnDelivery, /getDeliveryExpressList\(\)/);
});

test('物流轨迹页读取订单物流信息（sales 订单 API）', () => {
  const log = read('pages/order/express/log.vue');
  assert.match(log, /logisticsNo/);
  assert.match(log, /logisticsName/);
  assert.match(log, /getOrderExpressTrackList|getOrderDetail/);
});
