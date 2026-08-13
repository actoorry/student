import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');

const commissionPages = [
  'pages/commission/index.vue',
  'pages/commission/wallet.vue',
  'pages/commission/withdraw.vue',
  'pages/commission/team.vue',
  'pages/commission/order.vue',
  'pages/commission/goods.vue',
  'pages/commission/promoter.vue',
  'pages/commission/commission-ranking.vue',
];

test('all eight commission pages stay compiled and use sales brokerage contracts', () => {
  const pageJson = read('pages.json');
  assert.match(pageJson, /"root": "pages\/commission"/);
  for (const file of commissionPages) {
    // 读取页面文件：不存在即抛错；再校验其子包内注册
    const source = read(file);
    const leaf = file.replace(/\.vue$/, '').replace(/^pages\/commission\//, '');
    assert.match(pageJson, new RegExp(`"path": "${leaf}"`));
    // 不依赖婚恋 API / 实名守卫 / 人物推荐
    assert.doesNotMatch(
      source,
      /api\/marriage|store\('social'\)|s-partner-|marriage-check|name-check|recommendation/,
    );
  }
});

test('brokerage API maps to current sales endpoints without trade or marriage paths', () => {
  const api = read('sheep/api/sales/brokerage.js');
  assert.match(api, /\/sales\/brokerage-user\/get/);
  assert.match(api, /\/sales\/brokerage-user\/get-summary/);
  assert.match(api, /\/sales\/brokerage-user\/bind/);
  assert.match(api, /\/sales\/brokerage-record\/page/);
  assert.match(api, /\/sales\/brokerage-withdraw\/create/);
  assert.doesNotMatch(api, /\/trade\/brokerage-|\/marriage\//);
});

test('share binding uses the sales brokerage bind endpoint for the mall', () => {
  const share = read('sheep/platform/share.js');
  assert.match(share, /BrokerageApi\.bindBrokerageUser/);
  assert.match(share, /bindUserId/);
  assert.doesNotMatch(share, /\/marriage\/|store\('social'\)/);
});
