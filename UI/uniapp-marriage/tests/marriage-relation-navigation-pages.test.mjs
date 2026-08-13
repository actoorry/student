import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const mallRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const repoRoot = path.resolve(mallRoot, '..', '..');
const readMall = (relativePath) => fs.readFileSync(path.join(mallRoot, relativePath), 'utf8');
const readAdmin = (relativePath) => fs.readFileSync(path.join(repoRoot, 'UI/vue3-admin', relativePath), 'utf8');

test('four relationship destinations are compiled with safe fixed mode defaults', () => {
  const pages = JSON.parse(readMall('pages.json'));
  const routes = new Set([
    ...(pages.pages || []).map((page) => `/${page.path}`),
    ...(pages.subPackages || []).flatMap((subpackage) =>
      (subpackage.pages || []).map((page) => `/${subpackage.root}/${page.path}`),
    ),
  ]);
  ['/pages/mine-view/index', '/pages/mine-follow/index', '/pages/member-detail/index'].forEach((route) => {
    assert.ok(routes.has(route), `${route} must be compiled`);
  });
  const mineView = readMall('pages/mine-view/index.vue');
  const mineFollow = readMall('pages/mine-follow/index.vue');
  assert.match(mineView, /options\?\.mode === MY_VIEW \? MY_VIEW : VIEW_ME/);
  assert.match(mineFollow, /options\?\.type === FOLLOWING \? FOLLOWING : FOLLOWERS/);
  assert.match(mineView, /title="pageTitle"/);
  assert.match(mineFollow, /title="pageTitle"/);
});

test('relationship lists gate authentication and preserve list state across refresh, pagination and races', () => {
  ['pages/mine-view/index.vue', 'pages/mine-follow/index.vue'].forEach((page) => {
    const source = readMall(page);
    assert.match(source, /showAuthModal/);
    assert.match(source, /requestId !== requestSequence/);
    assert.match(source, /dedupeBy/);
    assert.match(source, /errorText/);
    assert.match(source, /@tap\.stop/);
    assert.match(source, /parsePositiveId\(item\?\.partnerId\)/);
    assert.match(source, /members\.value\.concat\(page\.list\)/);
  });
});

test('follow-back and unfollow update only after their existing server contracts succeed', () => {
  const source = readMall('pages/mine-follow/index.vue');
  const adapter = readMall('sheep/api/marriage/interaction.js');
  assert.match(adapter, /unfollow: \(relPartnerId\)/);
  assert.match(adapter, /url: '\/marriage\/interaction\/unfollow'/);
  assert.match(adapter, /method: 'DELETE'/);
  assert.match(source, /getResponseData\(await InteractionApi\.follow/);
  assert.match(source, /getResponseData\(await InteractionApi\.unfollow/);
  assert.match(source, /members\.value = members\.value\.filter/);
  assert.match(source, /if \(!confirm\) return/);
});

test('member detail uses the current profile contract and protects self interactions', () => {
  const source = readMall('pages/member-detail/index.vue');
  assert.match(source, /parsePositiveId\(options\?\.id\)/);
  assert.match(source, /MarriageProfileApi\.get\(memberId\.value\)/);
  assert.match(source, /marriageVerified/);
  assert.doesNotMatch(source, /singleVerified/);
  assert.match(source, /const isSelf = computed/);
  assert.match(source, /recordViewIfEligible/);
  assert.match(source, /showInteractionActions/);
  assert.match(source, /previewAlbum/);
  assert.match(source, /safeBack/);
});

test('AppLink selection distinguishes fixed query variants and only commits a positive member detail id', () => {
  const data = readAdmin('src/components/AppLinkInput/data.ts');
  const dialog = readAdmin('src/components/AppLinkInput/AppLinkSelectDialog.vue');
  assert.match(data, /MARRIAGE_MEMBER_DETAIL/);
  assert.match(data, /exactQuery\?: boolean/);
  assert.match(dialog, /isSameLink\(appLink\.path, activeAppLink\.path, appLink\.exactQuery\)/);
  assert.match(dialog, /import PartnerSelect/);
  assert.match(dialog, /APP_LINK_TYPE_ENUM\.MARRIAGE_MEMBER_DETAIL/);
  assert.match(dialog, /handleMarriageMemberSelected/);
  assert.match(dialog, /if \(!id \|\| id <= 0\)/);
  assert.match(dialog, /path: `\/pages\/member-detail\/index\?id=\$\{id\}`/);
});

test('menu grid restores the four relationship links and never routes an empty value', () => {
  const menuGrid = readMall('sheep/components/s-menu-grid/s-menu-grid.vue');
  const menuGridConfig = readAdmin('src/components/DiyEditor/components/mobile/MenuGrid/config.ts');
  const appLinkInput = readAdmin('src/components/AppLinkInput/index.vue');

  [
    ['我看过的', '/pages/mine-view/index?mode=my-view'],
    ['看过我的', '/pages/mine-view/index?mode=view-me'],
    ['我关注的', '/pages/mine-follow/index?type=following'],
    ['关注我的', '/pages/mine-follow/index?type=followers'],
  ].forEach(([title, route]) => {
    assert.match(menuGrid, new RegExp(`'${title}': '${route.replaceAll('?', '\\?')}'`));
  });
  assert.match(menuGrid, /const url = resolveMenuUrl\(item\);[\s\S]*if \(url\) sheep\.\$router\.go\(url\)/);
  assert.match(menuGridConfig, /iconUrl: '',[\s\S]*url: '',[\s\S]*text: ''/);
  assert.match(appLinkInput, /const appLink = computed\(\{[\s\S]*emit\('update:modelValue', link\)/);
  assert.doesNotMatch(appLinkInput, /watch\(/);
});

test('this change adds no relationship database migration or DIY component', () => {
  const changeDir = path.join(repoRoot, 'openspec/changes/migrate-marriage-relation-navigation-pages');
  const sql = fs.readdirSync(changeDir, { recursive: true }).filter((entry) => String(entry).endsWith('.sql'));
  assert.deepEqual(sql, []);
  assert.equal(fs.existsSync(path.join(mallRoot, 'sheep/components/s-mine-follow')), false);
  assert.equal(fs.existsSync(path.join(repoRoot, 'UI/vue3-admin/src/components/DiyEditor/components/mobile/MineFollow')), false);
});
