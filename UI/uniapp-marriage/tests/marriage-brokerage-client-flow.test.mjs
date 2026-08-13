import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const mallRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const repoRoot = path.resolve(mallRoot, '..', '..');
const readMall = (relativePath) => fs.readFileSync(path.join(mallRoot, relativePath), 'utf8');
const readAdmin = (relativePath) => fs.readFileSync(path.join(repoRoot, 'UI/vue3-admin', relativePath), 'utf8');

const commissionRoutes = [
  '/pages/commission/index',
  '/pages/commission/wallet',
  '/pages/commission/withdraw',
  '/pages/commission/team',
  '/pages/commission/order',
  '/pages/commission/goods',
  '/pages/commission/promoter',
  '/pages/commission/commission-ranking',
];

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

const commissionRouteTitles = new Map([
  ['/pages/commission/index', '分销中心'],
  ['/pages/commission/wallet', '佣金明细'],
  ['/pages/commission/withdraw', '申请提现'],
  ['/pages/commission/team', '我的团队'],
  ['/pages/commission/order', '分销订单'],
  ['/pages/commission/goods', '推广商品'],
  ['/pages/commission/promoter', '推广排行'],
  ['/pages/commission/commission-ranking', '佣金排行'],
]);

test('all marriage brokerage routes are compiled under /pages/commission and require auth', () => {
  const pages = JSON.parse(readMall('pages.json'));
  const compiled = new Map([
    ...(pages.pages || []).map((page) => [`/${page.path}`, page]),
    ...(pages.subPackages || []).flatMap((subpackage) =>
      (subpackage.pages || []).map((page) => [`/${subpackage.root}/${page.path}`, page]),
    ),
  ]);
  commissionRoutes.forEach((route) => {
    assert.ok(compiled.has(route), `${route} must be compiled`);
    assert.strictEqual(compiled.get(route).meta?.auth, true, `${route} must require auth`);
  });
  commissionRoutes.forEach((route) => {
    assert.strictEqual(compiled.get(route).meta?.group, '处佳缘', `${route} must be in 处佳缘 group`);
    assert.strictEqual(
      compiled.get(route).meta?.title,
      commissionRouteTitles.get(route),
      `${route} must use the same title as the marriage link catalog`,
    );
  });
});

test('target brokerage pages do not reference legacy distributor routes or trade endpoints', () => {
  const source = commissionPages.map(readMall).join('\n');
  assert.doesNotMatch(source, /\/pages\/distributor\//);
  assert.doesNotMatch(source, /\/trade\/brokerage-/);
  assert.doesNotMatch(source, /\/trade\/brokerage_/);
});

test('brokerage API contract uses /sales namespace and exposes all required endpoints', () => {
  const api = readMall('sheep/api/sales/brokerage.js');
  const config = readMall('sheep/api/sales/config.js');
  const required = [
    '/sales/brokerage-user/get',
    '/sales/brokerage-user/get-summary',
    '/sales/brokerage-user/bind',
    '/sales/brokerage-record/page',
    '/sales/brokerage-record/get-product-brokerage-price',
    '/sales/brokerage-user/child-summary-page',
    '/sales/brokerage-user/rank-page-by-price',
    '/sales/brokerage-user/rank-page-by-user-count',
    '/sales/brokerage-user/get-rank-by-price',
    '/sales/brokerage-withdraw/create',
    '/sales/brokerage-withdraw/page',
    '/sales/brokerage-withdraw/get',
  ];
  required.forEach((url) => assert.match(api, new RegExp(`url: ['\`\"]?${url.replace(/\//g, '\\/')}`)));
  assert.match(config, /\/sales\/config\/get/);
  assert.doesNotMatch(api, /\/trade\/brokerage-/);
  assert.doesNotMatch(api, /\/trade\/brokerage_/);
});

test('commission center uses marriage visual language and share modal, not hardcoded demo data', () => {
  const center = readMall('pages/commission/index.vue');
  const components = [
    'commission-info',
    'account-info',
    'commission-menu',
    'commission-log',
    'commission-auth',
  ];
  components.forEach((name) => assert.match(center, new RegExp(`<${name}`)));
  assert.match(center, /showShareModal|getShareInfo/);
  assert.doesNotMatch(center, /#F7D598|暖金|background:\s*#F7D598/);
  assert.doesNotMatch(center, /demoUser|demoNickname|演示用户/);
  commissionPages.forEach((page) => {
    const source = readMall(page);
    assert.doesNotMatch(source, /fen2yuan\([^)]+\)\s*\|\|\s*['\"]\d/);
  });
});

test('commission center packages every referenced feature icon as a non-empty local asset', () => {
  const menu = readMall('pages/commission/components/commission-menu.vue');
  const iconPaths = Array.from(
    menu.matchAll(/img:\s*['"](\/static\/img\/shop\/commission\/commission_icon\d+\.png)['"]/g),
    (match) => match[1],
  );

  assert.equal(iconPaths.length, 8, 'all eight feature entries must reference packaged icons');
  iconPaths.forEach((iconPath) => {
    const absolutePath = path.join(mallRoot, iconPath.slice(1));
    assert.ok(fs.existsSync(absolutePath), `${iconPath} must exist in the marriage client package`);
    assert.ok(fs.statSync(absolutePath).size > 0, `${iconPath} must not be empty`);
  });
});

test('commission center entries route to compiled pages with debounced tap and onShow refresh', () => {
  const menu = readMall('pages/commission/components/commission-menu.vue');
  const center = readMall('pages/commission/index.vue');
  commissionRoutes.forEach((route) => {
    if (route === '/pages/commission/index') return;
    assert.match(menu, new RegExp(`['\"]${route.replace(/\//g, '\\/')}['\"]`));
  });
  assert.match(menu, /locked|disabled/);
  assert.match(center, /onShow|refresh/);
});

test('commission center isolates stale responses and exposes partial retry states', () => {
  const center = readMall('pages/commission/index.vue');
  assert.match(center, /refreshGeneration/);
  assert.match(center, /generation !== refreshGeneration\.value/);
  assert.match(center, /Promise\.allSettled/);
  assert.match(center, /sectionErrors/);
  assert.match(center, /applyBrokerage/);
  assert.match(center, /重新加载|重试/);
});

test('commission center separates unopened application from pending approval', () => {
  const center = readMall('pages/commission/index.vue');
  const access = readMall('sheep/helper/brokerage-contract.js');
  assert.match(access, /title:\s*'没有申请资格'/);
  assert.match(access, /actionLabel:\s*'申请开通分销'/);
  assert.match(access, /title:\s*'分销资格审核中'/);
  assert.match(access, /canApply:\s*false/);
  assert.match(center, /state\.access\.actionLabel/);
});

test('AppLinkInput groups all brokerage pages under 处佳缘 and removes duplicate 分销商城 group', () => {
  const data = readAdmin('src/components/AppLinkInput/data.ts');
  const groups = Array.from(data.matchAll(/\{[\s\S]*?name:\s*['\"]([^'\"]+)['\"][\s\S]*?links:/g)).map((m) => m[1]);
  const marriageStart = data.indexOf("name: '处佳缘'");
  const marriageEnd = data.indexOf('\n  },\n  {', marriageStart);
  const marriageGroup = data.slice(marriageStart, marriageEnd === -1 ? data.length : marriageEnd);
  commissionRoutes.forEach((route) => {
    const pageName = route.replace('/pages/commission/', '');
    assert.match(
      marriageGroup,
      new RegExp(`path:\\s*['\"]${route.replace(/\//g, '\\/')}['\"]`),
      `${route} must be registered in 处佳缘 group`,
    );
  });
  assert.ok(!groups.includes('分销商城'), '分销商城 static group should be removed');
});

test('share binding still uses SPM params and existing brokerage bind endpoint', () => {
  const share = readMall('sheep/platform/share.js');
  const api = readMall('sheep/api/sales/brokerage.js');
  const center = readMall('pages/commission/index.vue');
  assert.match(api, /\/sales\/brokerage-user\/bind/);
  assert.match(share, /bindBrokerageUser\s*\(/);
  assert.match(share, /SharePageEnum\.HOME|shareId|spm/);
  assert.match(center, /SharePageEnum\.HOME|showShareModal|getShareInfo/);
});

test('withdraw page validates amount, enabled types, required fields and uses backend status', () => {
  const withdraw = readMall('pages/commission/withdraw.vue');
  const wallet = readMall('pages/commission/wallet.vue');
  assert.match(withdraw, /brokerageWithdrawMinPrice|minPrice/);
  assert.match(withdraw, /brokerageWithdrawTypes|withdrawTypes/);
  assert.match(withdraw, /brokeragePrice|maxPrice/);
  assert.match(`${withdraw}\n${wallet}`, /typeName|statusName/);
  assert.match(withdraw, /loading/);
  assert.match(wallet, /requestMerchantTransfer|transfer/);
});

test('shared brokerage pages avoid DOM, SVG and H5-only browser APIs', () => {
  const source = commissionPages.map(readMall).join('\n');
  assert.doesNotMatch(source, /<svg\b|document\.|window\.|innerHTML/);
});

test('DIY object and string links retain the existing controlled router fallback', () => {
  const router = readMall('sheep/router/index.js');
  const pages = JSON.parse(readMall('pages.json'));
  const compiled = new Set([
    ...(pages.pages || []).map((page) => `/${page.path}`),
    ...(pages.subPackages || []).flatMap((subpackage) =>
      (subpackage.pages || []).map((page) => `/${subpackage.root}/${page.path}`),
    ),
  ]);
  const fixtures = [
    '/pages/commission/index',
    { url: '/pages/commission/wallet', params: { type: 2 } },
  ];
  fixtures.forEach((fixture) => {
    const path = typeof fixture === 'string' ? fixture : fixture.url;
    assert.ok(compiled.has(path), `${path} fixture must remain compiled`);
  });
  assert.match(router, /if \(isObject\(path\)\)/);
  assert.match(router, /page = path\.url/);
  assert.match(router, /if \(!nextRoute\)/);
  assert.match(router, /return;/);
  assert.match(router, /nextRoute\.meta\?\.auth/);
});
