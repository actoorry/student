import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');

test('search page keeps local history and routes keyword to the goods list', () => {
  const search = read('pages/index/search.vue');
  assert.match(search, /saveSearchHistory\(keyword\)/);
  assert.match(search, /uni\.setStorageSync\('searchHistory'/);
  assert.match(search, /\/pages\/goods\/list/);
  assert.match(search, /state\.historyList/);
});

test('goods detail reads the current product detail and favorite/history contracts', () => {
  const goods = read('pages/goods/index.vue');
  assert.match(goods, /SpuApi from '@\/sheep\/api\/product\/spu'/);
  assert.match(goods, /FavoriteApi from '@\/sheep\/api\/product\/favorite'/);
  assert.match(goods, /getSpuDetail/);
  assert.match(goods, /state\.goodsInfo\.skus/);
  assert.match(goods, /state\.selectedSku/);
  assert.doesNotMatch(goods, /\/trade\/|\/member\/spu|\/pay\//);
});

test('user center, address, points and sign pages use current partner contracts', () => {
  const info = read('pages/user/info.vue');
  const addressList = read('pages/user/address/list.vue');
  const score = read('pages/user/wallet/score.vue');
  const sign = read('pages/app/sign.vue');
  assert.match(info, /UserApi from '@\/sheep\/api\/partner\/user'/);
  assert.match(addressList, /AddressApi from '@\/sheep\/api\/partner\/address'/);
  assert.match(addressList, /AreaApi from '@\/sheep\/api\/system\/area'/);
  assert.match(score, /PointApi from '@\/sheep\/api\/partner\/point'/);
  assert.match(sign, /SignInApi from '@\/sheep\/api\/partner\/signin'/);
});

test('mall pages carry no marriage routes, ids or social runtime references', () => {
  const pages = [
    'pages/index/index.vue',
    'pages/index/user.vue',
    'pages/index/cart.vue',
    'pages/index/category.vue',
    'pages/index/search.vue',
    'pages/goods/index.vue',
    'pages/goods/list.vue',
    'pages/order/list.vue',
    'pages/order/confirm.vue',
    'pages/accountant/index.vue',
  ];
  for (const file of pages) {
    const source = read(file);
    assert.doesNotMatch(
      source,
      /pages\/mine-view|pages\/mine-follow|pages\/member-detail|pages\/dynamics|pages\/messages|pages\/mine-profile|pages\/mine-certifications|pages\/mine-checkin|pages\/mine-points|s-partner-|store\('social'\)|recommendation/,
      `${file} must not reference marriage routes or runtimes`,
    );
  }
});
