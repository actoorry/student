import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const mallRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const repoRoot = path.resolve(mallRoot, '..', '..');
const readMall = (relativePath) => fs.readFileSync(path.join(mallRoot, relativePath), 'utf8');
const readRepo = (relativePath) => fs.readFileSync(path.join(repoRoot, relativePath), 'utf8');

const nativePagePaths = [
  'pages/dynamics/index.vue',
  'pages/messages/index.vue',
  'pages/member-detail/index.vue',
  'pages/messages/follow/index.vue',
  'pages/messages/mutual/index.vue',
  'pages/mine-view/index.vue',
  'pages/mine-follow/index.vue',
  'pages/messages/chat/index.vue',
];

test('all native social routes are compiled and the four static tab targets match the template contract', () => {
  const pages = JSON.parse(readMall('pages.json'));
  const compiledPaths = new Set([
    ...(pages.pages || []).map((page) => `/${page.path}`),
    ...(pages.subPackages || []).flatMap((subpackage) =>
      (subpackage.pages || []).map((page) => `/${subpackage.root}/${page.path}`),
    ),
  ]);
  const required = [
    '/pages/dynamics/index',
    '/pages/messages/index',
    '/pages/member-detail/index',
    '/pages/messages/follow/index',
    '/pages/messages/mutual/index',
    '/pages/mine-view/index',
    '/pages/mine-follow/index',
    '/pages/messages/chat/index',
  ];
  required.forEach((route) => assert.ok(compiledPaths.has(route), `${route} must be compiled`));
  assert.deepEqual(
    pages.tabBar.list.map((item) => `/${item.pagePath}`),
    ['/pages/index/index', '/pages/dynamics/index', '/pages/messages/index', '/pages/index/user'],
  );
});

test('top-level social tabs hide the shared back button while child pages receive it automatically', () => {
  const layout = readMall('sheep/components/s-layout/s-layout.vue');
  const navbar = readMall('sheep/ui/su-navbar/su-navbar.vue');
  const dynamics = readMall('pages/dynamics/index.vue');
  const messages = readMall('pages/messages/index.vue');
  const childPages = [
    'pages/member-detail/index.vue',
    'pages/messages/follow/index.vue',
    'pages/messages/mutual/index.vue',
    'pages/mine-view/index.vue',
    'pages/mine-follow/index.vue',
    'pages/messages/chat/index.vue',
  ].map(readMall);

  assert.match(layout, /:leftIcon="shouldShowBackButton \? 'left' : ''"/);
  assert.match(layout, /props\.showLeftButton \|\| !Boolean\(props\.tabbar\)/);
  assert.match(navbar, /<text class="sicon-back" \/>/);
  assert.doesNotMatch(navbar, /sicon-more|showMenuTools|icon-button-right/);
  assert.doesNotMatch(dynamics, /showLeftButton/);
  assert.doesNotMatch(messages, /showLeftButton/);
  childPages.forEach((source) => assert.match(source, /showLeftButton/));
});

test('native social pages cannot fall back to DIY or legacy marriage runtime state', () => {
  const source = nativePagePaths.map(readMall).join('\n');
  const forbidden = [
    /MarriageDecorationRenderer/,
    /getPageComponents/,
    /member_access_token/,
    /tenant.?303/i,
    /\.uts(?:['"]|\b)/,
    /\.uvue(?:['"]|\b)/,
    /apiRoot/,
    /options\?\.(?:tenant|tenantId|api)/,
    /template\.components/,
  ];
  forbidden.forEach((pattern) => assert.doesNotMatch(source, pattern));
  assert.match(readMall('pages/index/index.vue'), /template\.components/);
  assert.match(readMall('pages/index/user.vue'), /template\.components/);
});

test('moment and member pages preserve filters, request races, upload and route validation', () => {
  const dynamics = readMall('pages/dynamics/index.vue');
  const member = readMall('pages/member-detail/index.vue');
  assert.match(dynamics, /filters = \['推荐', '关注'\]/);
  assert.match(dynamics, /following: requestedFilter === 1/);
  assert.match(dynamics, /requestId !== feedRequestId/);
  assert.match(dynamics, /dedupeBy/);
  assert.match(dynamics, /FileApi\.uploadFile/);
  assert.match(dynamics, /MomentApi\.(create|delete|toggleLike|getCommentPage|createComment)/);
  assert.match(member, /parsePositiveId\(options\?\.id\)/);
  assert.match(member, /MarriageProfileApi\.get/);
  assert.match(member, /safeBack/);
});

test('API adapters use only the approved existing social contracts', () => {
  const moment = readMall('sheep/api/marriage/moment.js');
  const interaction = readMall('sheep/api/marriage/interaction.js');
  const message = readMall('sheep/api/marriage/message.js');
  const profile = readMall('sheep/api/marriage/profile.js');
  [
    '/marriage/moment/page',
    '/marriage/moment/my-page',
    '/marriage/moment/create',
    '/marriage/moment/delete',
    '/marriage/moment/like',
    '/marriage/moment/comment/page',
    '/marriage/moment/comment/create',
  ].forEach((url) => assert.match(moment, new RegExp(url.replaceAll('/', '\\/'))));
  [
    '/marriage/interaction/follow',
    '/marriage/interaction-notification/page',
    '/marriage/interaction-notification/read-scene',
  ].forEach((url) => assert.match(interaction, new RegExp(url.replaceAll('/', '\\/'))));
  [
    '/im/message/private/list',
    '/im/message/private/send',
    '/im/message/private/read',
    '/marriage/chat/unread-count',
    '/marriage/chat/conversation-list',
  ].forEach((url) => assert.match(message, new RegExp(url.replaceAll('/', '\\/'))));
  assert.match(profile, /\/marriage\/recommend-member\/get/);
  assert.match(profile, /\/marriage\/auth\/get-real-verified-status/);
});

test('messages protect anonymous privacy and reconcile store, conversations and websocket events', () => {
  const messages = readMall('pages/messages/index.vue');
  const chat = readMall('pages/messages/chat/index.vue');
  const socket = readMall('sheep/api/marriage/imSocket.js');
  const store = readMall('sheep/store/social.js');
  assert.match(messages, /if \(!isLoggedIn\.value\)/);
  assert.match(messages, /Promise\.all/);
  assert.match(messages, /MessageApi\.getConversationList/);
  assert.match(chat, /parsePositiveId\(options\?\.peerId\)/);
  assert.match(chat, /clientMessageId/);
  assert.match(chat, /maxId/);
  assert.match(chat, /IM_MESSAGE_TYPES\.(RECALL|RECEIPT|READ)/);
  assert.match(socket, /subscribeRealtime\(IM_TYPE, key, listener\)/);
  assert.match(socket, /ensureRealtimeConnection/);
  assert.match(readMall('sheep/helper/realtime-messaging.js'), /\/infra\/ws/);
  assert.match(store, /marriage-social-store/);
  assert.match(store, /messageItem\.badge/);
});

test('notification routes use scene read-back and keep parameterized children out of management data', () => {
  const follow = readMall('pages/messages/follow/index.vue');
  const mutual = readMall('pages/messages/mutual/index.vue');
  const mineView = readMall('pages/mine-view/index.vue');
  assert.match(follow, /readScene\(INTERACTION_SCENES\.FOLLOW\)/);
  assert.match(mutual, /readScene\(INTERACTION_SCENES\.MUTUAL_FOLLOW\)/);
  assert.match(mineView, /readScene\(INTERACTION_SCENES\.VIEW_ME_SUMMARY\)/);
  [follow, mutual, mineView].forEach((source) => assert.match(source, /errorText/));
});

test('management ChujiaYuan category keeps existing links and lets this change add exact relationship variants', () => {
  const data = readRepo('UI/vue3-admin/src/components/AppLinkInput/data.ts');
  const category = data.match(/name: '处佳缘',[\s\S]*?\r?\n  \},\r?\n  \{\r?\n    name: '商品'/)?.[0];
  assert.ok(category, 'ChujiaYuan category must exist before product category');
  [
    ['动态', '/pages/dynamics/index'],
    ['消息', '/pages/messages/index'],
    ['我看过的', '/pages/mine-view/index?mode=my-view'],
    ['看过我的', '/pages/mine-view/index?mode=view-me'],
    ['我关注的', '/pages/mine-follow/index?type=following'],
    ['关注我的', '/pages/mine-follow/index?type=followers'],
  ].forEach(([name, route]) => {
    assert.match(category, new RegExp(`name: '${name}', path: '${route.replaceAll('?', '\\?')}'`));
  });
  assert.match(category, /name: '资料详情页',[\s\S]*?type: APP_LINK_TYPE_ENUM\.MARRIAGE_MEMBER_DETAIL/);
  assert.equal((category.match(/exactQuery: true/g) || []).length, 4);
});

test('historical link values are not rewritten merely by opening the selector', () => {
  const input = readRepo('UI/vue3-admin/src/components/AppLinkInput/index.vue');
  const dialog = readRepo('UI/vue3-admin/src/components/AppLinkInput/AppLinkSelectDialog.vue');
  assert.match(input, /get: \(\) => props\.modelValue/);
  const openBody = dialog.match(/const open = \(link: string\) => \{[\s\S]*?\n\}/)?.[0] || '';
  assert.doesNotMatch(openBody, /emit\(/);
  assert.match(dialog, /const handleSubmit = \(\) => \{/);
  assert.match(dialog, /emit\('change', activeAppLink\.value\.path\)/);
});

test('the change introduces no SQL artifact or message schema migration', () => {
  const changeDir = path.join(repoRoot, 'openspec/changes/add-marriage-native-messages-dynamics-pages');
  const stack = [changeDir];
  const sqlFiles = [];
  while (stack.length) {
    const current = stack.pop();
    for (const entry of fs.readdirSync(current, { withFileTypes: true })) {
      const target = path.join(current, entry.name);
      if (entry.isDirectory()) stack.push(target);
      else if (entry.name.endsWith('.sql')) sqlFiles.push(target);
    }
  }
  assert.deepEqual(sqlFiles, []);
});
