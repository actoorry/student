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

test('commission pages use only sales brokerage and config APIs without trade or marriage paths', () => {
  for (const file of commissionPages) {
    const source = read(file);
    // 页面族依赖的 API 封装来自 /sales/brokerage-* 与 /sales/config/get
    assert.doesNotMatch(source, /\/trade\/brokerage-|\/trade\//);
    // 不调用婚恋实名认证接口或婚恋人物接口
    assert.doesNotMatch(source, /\/marriage\/auth\/|get-real-verified-status|recommend-member|marriage-check|name-check/);
  }
});

test('sales api and config api expose the full commission contract without trade paths', () => {
  const brokerage = read('sheep/api/sales/brokerage.js');
  const config = read('sheep/api/sales/config.js');
  // 分销中心所需接口齐备
  assert.match(brokerage, /\/sales\/brokerage-user\/get/);
  assert.match(brokerage, /\/sales\/brokerage-user\/get-summary/);
  assert.match(brokerage, /\/sales\/brokerage-user\/bind/);
  assert.match(brokerage, /\/sales\/brokerage-user\/child-summary-page/);
  assert.match(brokerage, /\/sales\/brokerage-user\/rank-page-by-price/);
  assert.match(brokerage, /\/sales\/brokerage-user\/rank-page-by-user-count/);
  assert.match(brokerage, /\/sales\/brokerage-record\/page/);
  assert.match(brokerage, /\/sales\/brokerage-record\/get-product-brokerage-price/);
  assert.match(brokerage, /\/sales\/brokerage-withdraw\/create/);
  assert.match(brokerage, /\/sales\/brokerage-withdraw\/page/);
  assert.match(brokerage, /\/sales\/brokerage-withdraw\/get/);
  assert.match(config, /\/sales\/config\/get/);
  assert.doesNotMatch(brokerage, /\/trade\//);
  assert.doesNotMatch(brokerage, /\/marriage\//);
});

test('commission pages keep unauthenticated-free access per sales rules without real-name guards', () => {
  for (const file of commissionPages) {
    const source = read(file);
    // 未实名认证仍按 Sales 分销规则访问：不调用实名接口、不出现实名守卫弹窗
    assert.doesNotMatch(source, /getRealVerifiedStatus|real-verified|realVerified/);
    assert.doesNotMatch(source, /实名/);
    assert.doesNotMatch(source, /\/marriage\/auth\//);
  }
});

test('commission routes stay compiled in pages.json under the distribution group', () => {
  const pageJson = read('pages.json');
  assert.match(pageJson, /"root": "pages\/commission"/);
  const expectedPaths = ['index', 'wallet', 'withdraw', 'team', 'order', 'goods', 'promoter', 'commission-ranking'];
  for (const leaf of expectedPaths) {
    assert.match(pageJson, new RegExp(`"path": "${leaf}"`));
  }
  // 通用分销入口登记在「分销商城」分组（与 baseline 一致）
  assert.match(pageJson, /"group": "分销商城"/);
});

test('admin AppLinkInput keeps distribution links under 商城分销 and not under marriage groups', () => {
  const data = read('../vue3-admin/src/components/AppLinkInput/data.ts');
  // 「商城分销」分类包含全部 8 个分销原生页
  const mallGroup = data.match(/name: '商城分销'[\s\S]*?\]\s*\}/);
  assert.ok(mallGroup, 'AppLinkInput 应包含「商城分销」分类');
  assert.match(mallGroup[0], /\/pages\/commission\/index/);
  assert.match(mallGroup[0], /\/pages\/commission\/wallet/);
  assert.match(mallGroup[0], /\/pages\/commission\/withdraw/);
  assert.match(mallGroup[0], /\/pages\/commission\/team/);
  assert.match(mallGroup[0], /\/pages\/commission\/order/);
  assert.match(mallGroup[0], /\/pages\/commission\/goods/);
  assert.match(mallGroup[0], /\/pages\/commission\/promoter/);
  assert.match(mallGroup[0], /\/pages\/commission\/commission-ranking/);
  // 婚恋分组不得再提供商城分销入口
  const marriageGroup = data.match(/name: '处佳缘'[\s\S]*?\]\s*\}/);
  if (marriageGroup) {
    assert.doesNotMatch(marriageGroup[0], /\/pages\/commission\//);
  }
  // 不新增 DIY 组件或业务 JSON 字段：分销页面以链接目录方式可达
  assert.doesNotMatch(data, /commission.*property|brokerage.*property|佣金.*JSON|brokeragePercent/i);
});
