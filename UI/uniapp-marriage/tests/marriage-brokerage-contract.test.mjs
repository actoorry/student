import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import path from 'node:path';
import test from 'node:test';

const helperSource = await fs.readFile(
  path.resolve(import.meta.dirname, '../sheep/helper/brokerage-contract.js'),
  'utf8',
);
const helper = await import(`data:text/javascript,${encodeURIComponent(helperSource)}`);

test('normalizes only positive integer share identifiers', () => {
  assert.equal(helper.toPositiveInteger('42'), 42);
  assert.equal(helper.toPositiveInteger(42), 42);
  assert.equal(helper.toPositiveInteger('0'), 0);
  assert.equal(helper.toPositiveInteger('-1'), 0);
  assert.equal(helper.toPositiveInteger('42.5'), 0);
  assert.equal(helper.toPositiveInteger('not-a-number'), 0);
});

test('builds encoded sales query strings without mutating caller params', () => {
  const params = { pageNo: 1, pageSize: 20, status: undefined, times: ['a b', 'c'] };
  assert.equal(helper.buildSalesQuery(params), 'pageNo=1&pageSize=20&times=a%20b&times=c');
  assert.deepEqual(params, { pageNo: 1, pageSize: 20, status: undefined, times: ['a b', 'c'] });
});

test('validates configured withdrawal methods and amount boundaries', () => {
  const base = {
    priceYuan: '40',
    withdrawablePrice: 5000,
    minPrice: 4000,
    type: '2',
    withdrawTypes: '2,3,4',
    accountInfo: { userAccount: '6222', userName: '张三', bankName: 'ICBC', bankAddress: '北京' },
  };
  assert.deepEqual(helper.validateBrokerageWithdraw(base), { valid: true, message: '' });
  assert.equal(helper.validateBrokerageWithdraw({ ...base, priceYuan: '39.99' }).valid, false);
  assert.equal(helper.validateBrokerageWithdraw({ ...base, priceYuan: '50.01' }).valid, false);
  assert.equal(helper.validateBrokerageWithdraw({ ...base, type: '5' }).valid, false);
  assert.equal(
    helper.validateBrokerageWithdraw({ ...base, type: '3', accountInfo: {} }).message,
    '请上传收款码',
  );
  assert.deepEqual(helper.normalizeWithdrawTypes('2|3|4'), [2, 3, 4]);
  assert.deepEqual(
    helper.validateBrokerageWithdraw({
      ...base,
      type: '3',
      accountInfo: { qrCodeUrl: '/uploads/wechat.png' },
    }),
    { valid: true, message: '' },
  );
  assert.deepEqual(
    helper.validateBrokerageWithdraw({
      ...base,
      type: '4',
      accountInfo: { qrCodeUrl: '/uploads/alipay.png' },
    }),
    { valid: true, message: '' },
  );
  assert.deepEqual(
    helper.validateBrokerageWithdraw({
      ...base,
      type: '5',
      withdrawTypes: '5',
      accountInfo: { userName: '张三' },
    }),
    { valid: true, message: '' },
  );
  assert.deepEqual(
    helper.validateBrokerageWithdraw({
      ...base,
      type: '6',
      withdrawTypes: [6],
      accountInfo: { userAccount: 'alipay-user', userName: '张三' },
    }),
    { valid: true, message: '' },
  );
});

test('clears a pending share identifier only after a successful bind', () => {
  assert.equal(helper.shouldClearPendingShareId({ code: 0, data: true }), true);
  assert.equal(helper.shouldClearPendingShareId({ data: false }), false);
  assert.equal(helper.shouldClearPendingShareId({ code: 1, data: true }), false);
});

test('resolves brokerage center access states without exposing account data', () => {
  const unopened = helper.resolveBrokerageAccess(null, { brokerageEnabled: true });
  assert.equal(unopened.title, '没有申请资格');
  assert.equal(unopened.message, '当前账号还没有申请个人分销资格');
  assert.equal(unopened.actionLabel, '申请开通分销');
  assert.equal(unopened.canApply, true);

  assert.equal(helper.resolveBrokerageAccess(null, { brokerageEnabled: false }).canApply, false);
  const pending = helper.resolveBrokerageAccess(
    { brokerageUserExists: true, brokerageEnabled: false },
    { brokerageEnabled: true },
  );
  assert.equal(pending.available, false);
  assert.equal(pending.canApply, false);
  assert.equal(pending.title, '分销资格审核中');
  assert.equal(pending.message, '你的分销申请已提交，正在审核中，请耐心等待');
  assert.equal(
    helper.resolveBrokerageAccess(
      { brokerageUserExists: true, brokerageEnabled: true },
      { brokerageEnabled: true },
    ).available,
    true,
  );
});

test('keeps application available when the app config response omits brokerageEnabled', () => {
  const access = helper.resolveBrokerageAccess(
    { brokerageUserExists: false, brokerageEnabled: false },
    { brokeragePosterUrls: [], brokerageWithdrawTypes: [] },
  );

  assert.equal(access.canApply, true);
  assert.equal(access.actionLabel, '申请开通分销');
});

test('keeps current, withdrawable and cumulative withdrawn brokerage amounts distinct', () => {
  assert.equal(typeof helper.resolveBrokerageWalletSummary, 'function');
  assert.deepEqual(
    helper.resolveBrokerageWalletSummary({
      brokeragePrice: 0,
      frozenPrice: 0,
      withdrawPrice: 170,
    }),
    {
      currentPrice: 0,
      withdrawablePrice: 0,
      frozenPrice: 0,
      withdrawnPrice: 170,
    },
  );
});

test('loads all brokerage history by default and adds a date range only when selected', () => {
  assert.equal(typeof helper.buildBrokerageListParams, 'function');
  assert.deepEqual(helper.buildBrokerageListParams({ pageNo: 1, pageSize: 8 }), {
    pageNo: 1,
    pageSize: 8,
  });
  assert.deepEqual(
    helper.buildBrokerageListParams({
      pageNo: 2,
      pageSize: 8,
      dateRange: ['2026-07-01', '2026-08-05'],
    }),
    {
      pageNo: 2,
      pageSize: 8,
      'createTime[0]': '2026-07-01 00:00:00',
      'createTime[1]': '2026-08-05 23:59:59',
    },
  );
});
