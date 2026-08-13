import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const mallRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const readMall = (relative) => fs.readFileSync(path.join(mallRoot, relative), 'utf8');

test('background user asset counters do not expose backend diagnostics as page toasts', () => {
  const orderApi = readMall('sheep/api/sales/order.js');
  const couponApi = readMall('sheep/api/promotion/coupon.js');

  assert.match(
    orderApi,
    /getOrderCount:[\s\S]*?showLoading:\s*false,[\s\S]*?showError:\s*false,[\s\S]*?auth:\s*true/,
  );
  assert.match(
    couponApi,
    /getUnusedCouponCount:[\s\S]*?showLoading:\s*false,[\s\S]*?showError:\s*false,[\s\S]*?auth:\s*true/,
  );
});
