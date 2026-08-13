import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { normalizeShareId, isSelfShareId } from '../sheep/helper/share-id.js';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');

test('normalizeShareId accepts positive integers as number or numeric string', () => {
  assert.equal(normalizeShareId(123), 123);
  assert.equal(normalizeShareId('123'), 123);
  assert.equal(normalizeShareId(' 456 '), 456);
  assert.equal(normalizeShareId('1'), 1);
});

test('normalizeShareId rejects zero, negative, decimal, non-numeric and empty values', () => {
  assert.equal(normalizeShareId(0), undefined);
  assert.equal(normalizeShareId('0'), undefined);
  assert.equal(normalizeShareId(-5), undefined);
  assert.equal(normalizeShareId('-5'), undefined);
  assert.equal(normalizeShareId(1.5), undefined);
  assert.equal(normalizeShareId('1.5'), undefined);
  assert.equal(normalizeShareId('abc'), undefined);
  assert.equal(normalizeShareId('12a'), undefined);
  assert.equal(normalizeShareId(''), undefined);
  assert.equal(normalizeShareId('   '), undefined);
  assert.equal(normalizeShareId(null), undefined);
  assert.equal(normalizeShareId(undefined), undefined);
});

test('isSelfShareId detects the current user id and ignores empty current user', () => {
  assert.equal(isSelfShareId(10, 10), true);
  assert.equal(isSelfShareId('10', '10'), true);
  assert.equal(isSelfShareId(10, 11), false);
  assert.equal(isSelfShareId(10, undefined), false);
  assert.equal(isSelfShareId(10, null), false);
  assert.equal(isSelfShareId(10, ''), false);
});

test('share.js captures only valid positive integers and filters self invites', () => {
  const share = read('sheep/platform/share.js');
  // 使用共享纯函数做正整数校验
  assert.match(share, /normalizeShareId/);
  assert.match(share, /import .*normalizeShareId.*from '@\/sheep\/helper\/share-id'/);
  // 自邀请过滤：不保存不调用
  assert.match(share, /isSelfShareId\(shareId, user\.userInfo\?\.id\)/);
  // 无效值不保存：显式清理
  assert.match(share, /uni\.removeStorageSync\('shareId'\)/);
  // 二维码场景统一入口
  assert.match(share, /captureBindUserId/);
  assert.match(share, /captureShareId\(normalizeShareId\(bindUserId\)\)/);
  // 商城分享不依赖婚恋接口或婚恋邀请状态
  assert.doesNotMatch(share, /\/marriage\/|store\('social'\)|marriage-pending-bind-user-id/);
});

test('bindBrokerageUser has request lock, idempotent clearing and network retry semantics', () => {
  const share = read('sheep/platform/share.js');
  // 请求锁：防止登录回调/页面恢复/重复启动并发提交
  assert.match(share, /let bindingLock = false/);
  assert.match(share, /if \(bindingLock\)/);
  assert.match(share, /bindingLock = true/);
  assert.match(share, /bindingLock = false/);
  // 绑定接口仍是标准入口
  assert.match(share, /BrokerageApi\.bindBrokerageUser\(\{ bindUserId: shareId \}\)/);
  // 服务端明确结果后清理上下文
  assert.match(share, /typeof res\.code !== 'undefined'/);
  assert.match(share, /uni\.removeStorageSync\('shareId'\)/);
  // 网络失败保留上下文支持重试
  assert.match(share, /bindBrokerageUser 网络失败，保留待绑定上下文/);
});

test('home index and custom page wire the qrcode bindUserId scene into the shared context', () => {
  const index = read('pages/index/index.vue');
  const page = read('pages/index/page.vue');
  // 首页 scene 解析后消费 bindUserId
  assert.match(index, /options\.bindUserId/);
  assert.match(index, /\$share\.captureBindUserId\(options\.bindUserId\)/);
  // 自定义页 scene 为 bindUserId 时消费，否则仍按 id 解析自定义页
  assert.match(page, /sceneParams\[0\] === 'bindUserId'/);
  assert.match(page, /\$share\.captureBindUserId\(sceneParams\[1\]\)/);
  // 二维码场景不引入婚恋依赖
  assert.doesNotMatch(index, /\/marriage\/|recommend-member/);
  assert.doesNotMatch(page, /\/marriage\/|recommend-member/);
});

test('post-login binding entry still flows through the shared sales bind call', () => {
  const userStore = read('sheep/store/user.js');
  const openPlatform = read('sheep/platform/provider/wechat/openPlatform.js');
  // 登录完成后统一消费待绑定上下文
  assert.match(userStore, /\$share\.bindBrokerageUser\(\)/);
  assert.match(openPlatform, /\$share\.bindBrokerageUser\(\)/);
  assert.doesNotMatch(userStore, /\/marriage\/|marriage-pending-bind-user-id/);
  assert.doesNotMatch(openPlatform, /\/marriage\/|marriage-pending-bind-user-id/);
});
