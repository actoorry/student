import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const mallRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const repoRoot = path.resolve(mallRoot, '..', '..');
const readMall = (relative) => fs.readFileSync(path.join(mallRoot, relative), 'utf8');
const readRepo = (relative) => fs.readFileSync(path.join(repoRoot, relative), 'utf8');
const signInHelperSource = readMall('pages/mine-checkin/helper.js');
const pointHelperSource = readMall('pages/mine-points/helper.js');
const signInHelper = await import(`data:text/javascript;base64,${Buffer.from(signInHelperSource).toString('base64')}`);
const pointHelper = await import(`data:text/javascript;base64,${Buffer.from(pointHelperSource).toString('base64')}`);
const helper = { ...signInHelper, ...pointHelper };

test('mine check-in and point routes are compiled and authenticated', () => {
  const pages = JSON.parse(readMall('pages.json'));
  const packages = new Map((pages.subPackages || []).map((item) => [item.root, item]));
  assert.equal(packages.get('pages/mine-checkin')?.pages[0]?.path, 'index');
  assert.equal(packages.get('pages/mine-checkin')?.pages[0]?.meta?.auth, true);
  assert.equal(packages.get('pages/mine-points')?.pages[0]?.path, 'index');
  assert.equal(packages.get('pages/mine-points')?.pages[0]?.meta?.auth, true);
  const paths = new Set((pages.subPackages || []).flatMap((pkg) => pkg.pages.map((item) => `/${pkg.root}/${item.path}`)));
  ['/pages/mine-checkin/index', '/pages/mine-points/index', '/pages/app/sign', '/pages/user/wallet/score'].forEach((route) => assert.ok(paths.has(route), `${route} must remain compiled`));
});

test('sign-in summary normalization and action lock are deterministic', () => {
  assert.deepEqual(helper.normalizeSignInSummary(null), helper.emptySignInSummary());
  const summary = helper.normalizeSignInSummary({ signedToday: 1, currentDay: '2', todayRecord: { id: 3, day: 2, point: 20, experience: 6 }, nextRewardPoint: 30, configs: [{ id: 1, day: 1, point: 10, experience: 5, status: 0 }] });
  assert.equal(summary.signedToday, true);
  assert.equal(summary.currentDay, 2);
  assert.equal(summary.todayRecord.point, 20);
  assert.equal(summary.configs[0].experience, 5);
  assert.deepEqual(helper.normalizeSignInSummary({ configs: null }).configs, []);
  const longSummary = helper.normalizeSignInSummary({ currentDay: '900719925474099', nextRewardPoint: '999999999', nextRewardExperience: '888888888', configs: [{ day: '900719925474099', point: '999999999', experience: '888888888' }] });
  assert.equal(longSummary.currentDay, 900719925474099);
  assert.equal(longSummary.configs[0].point, 999999999);
  assert.equal(helper.normalizeSignInResult(null), null);
  assert.deepEqual(helper.normalizeSignInResult({ id: 4, day: '7', point: '70', experience: '100' }), { id: 4, day: 7, point: 70, experience: 100, createTime: null });
  assert.equal(helper.canCreateSignIn({ isLoggedIn: true, loading: false, submitting: false, signedToday: false, hasConfigs: true }), true);
  assert.equal(helper.canCreateSignIn({ isLoggedIn: true, loading: false, submitting: true, signedToday: false, hasConfigs: true }), false);
  assert.equal(helper.canCreateSignIn({ isLoggedIn: false, loading: false, submitting: false, signedToday: false, hasConfigs: true }), false);
});

test('point response parsing, pagination de-duplication and stale-request guards are deterministic', () => {
  assert.deepEqual(helper.normalizePointPage(null), { totalPoint: 0, records: { list: [], total: 0 } });
  assert.deepEqual(helper.normalizePointPage({ totalPoint: 'bad', records: { list: 'bad', total: -2 } }), { totalPoint: 0, records: { list: [], total: 0 } });
  const page = helper.normalizePointPage({ totalPoint: '88', records: { total: '3', list: [{ id: 1, point: 10, totalPoint: 88 }, { id: 2, point: -2, totalPoint: 86 }, { id: 3, point: 0, totalPoint: 86 }] } });
  assert.equal(page.totalPoint, 88);
  assert.deepEqual(page.records.list.map((item) => item.point), [10, -2, 0]);
  assert.deepEqual(helper.appendUniquePointRecords(page.records.list.slice(0, 2), page.records.list).map((item) => item.id), [1, 2, 3]);
  assert.equal(helper.isActiveMineRequest(2, 2, true), true);
  assert.equal(helper.isActiveMineRequest(2, 3, true), false);
  assert.equal(helper.isActiveMineRequest(2, 2, false), false);
  assert.equal(helper.isActiveMineRequest(3, 2, true), false);
  assert.equal(helper.canLoadMorePointRecords({ isLoggedIn: true, loading: false, hasMore: true }), true);
  assert.equal(helper.canLoadMorePointRecords({ isLoggedIn: true, loading: true, hasMore: true }), false);
  assert.equal(helper.canLoadMorePointRecords({ isLoggedIn: false, loading: false, hasMore: true }), false);
  assert.equal(helper.nextPointPage({ confirmedPage: 1, failedPage: 3 }), 3);
  assert.equal(helper.nextPointPage({ confirmedPage: 1, failedPage: 0 }), 2);
});

test('pages use reviewed App contracts and keep route/data boundaries closed', () => {
  const signInPage = readMall('pages/mine-checkin/index.vue');
  const pointsPage = readMall('pages/mine-points/index.vue');
  const signInApi = readMall('sheep/api/partner/signin.js');
  const pointApi = readMall('sheep/api/partner/point.js');
  const appLinkData = readRepo('UI/vue3-admin/src/components/AppLinkInput/data.ts');
  const dialog = readRepo('UI/vue3-admin/src/components/AppLinkInput/AppLinkSelectDialog.vue');
  assert.match(signInPage, /getSignInRecordSummary\(quietAuthRequest\)/);
  assert.match(signInPage, /from '\.\/helper'/);
  assert.match(signInPage, /createSignInRecord\(quietAuthRequest\)/);
  assert.match(signInPage, /isActiveMineRequest/);
  assert.match(signInPage, /function markUnauthorized\(\)/);
  assert.match(signInPage, /sheep\.\$store\('user'\)\.isLogin !== true/);
  assert.match(signInPage, /actionId !== state\.actionId/);
  assert.match(pointsPage, /normalizePointPage/);
  assert.match(pointsPage, /from '\.\/helper'/);
  assert.match(pointsPage, /appendUniquePointRecords/);
  assert.match(pointsPage, /onReachBottom\(loadMore\)/);
  assert.match(pointsPage, /function markUnauthorized\(\)/);
  assert.match(pointsPage, /nextPointPage\(state\)/);
  assert.doesNotMatch(`${signInPage}\n${pointsPage}`, /setStorageSync|localStorage|userId\s*[:=]|window\.|document\./);
  assert.match(signInApi, /url: '\/partner\/sign-in\/record\/get-summary'/);
  assert.match(signInApi, /url: '\/partner\/sign-in\/record\/create'/);
  assert.match(pointApi, /url: `\/partner\/point\/record\/page\?\$\{queryString\}`/);
  assert.match(appLinkData, /\{ name: '我的签到', path: '\/pages\/mine-checkin\/index' \}/);
  assert.match(appLinkData, /\{ name: '我的积分', path: '\/pages\/mine-points\/index' \}/);
  assert.match(appLinkData, /\{ name: '我的认证', path: '\/pages\/mine-certifications\/index' \}/);
  assert.match(dialog, /v-for="\(group, groupIndex\) in APP_LINK_GROUP_LIST"/);
  assert.match(dialog, /emit\('change', activeAppLink\.value\.path\)/);
  assert.match(dialog, /emit\('appLinkChange', activeAppLink\.value\)/);
});
