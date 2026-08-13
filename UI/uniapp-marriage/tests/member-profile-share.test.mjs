import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  buildMemberShareInfo,
  buildMemberSharePath,
  buildMemberShareQuery,
  buildMemberShareTitle,
  capturePendingBindUserId,
  clearPendingBindUserId,
  getPendingBindUserId,
  resolveMemberShareImage,
} from '../sheep/helper/member-profile-share.js';

const root = dirname(fileURLToPath(import.meta.url));
const read = (path) => readFileSync(join(root, path), 'utf8');

describe('member profile share rules', () => {
  const female = {
    id: 42,
    sex: 2,
    age: 28,
    city: '杭州',
    job: '设计师',
    mainImage: 'main.jpg',
    avatarImage: 'avatar.jpg',
    albumImages: ['album.jpg'],
  };

  it('reproduces the original title and skips missing display fields', () => {
    assert.equal(buildMemberShareTitle(female), '28岁 · 杭州 · 设计师｜来看看她的个人主页');
    assert.equal(buildMemberShareTitle({ sex: 1, age: 31 }), '31岁｜来看看他的个人主页');
    assert.equal(buildMemberShareTitle({ sex: 0 }), '来看看她的个人主页');
  });

  it('uses main image, album and avatar in that order', () => {
    assert.equal(resolveMemberShareImage(female), 'main.jpg');
    assert.equal(resolveMemberShareImage({ albumImages: ['', 'album.jpg'], avatarImage: 'avatar.jpg' }), 'album.jpg');
    assert.equal(resolveMemberShareImage({ albumImages: [], avatarImage: 'avatar.jpg' }), 'avatar.jpg');
  });

  it('keeps member id and inviter id as separate landing parameters', () => {
    assert.equal(buildMemberShareQuery(7, 42), 'id=42&bindUserId=7');
    assert.equal(buildMemberSharePath(7, 42), '/pages/member-detail/index?id=42&bindUserId=7');
    assert.deepEqual(buildMemberShareInfo(female, 7), {
      title: '28岁 · 杭州 · 设计师｜来看看她的个人主页',
      desc: '28岁 · 杭州 · 设计师｜来看看她的个人主页',
      image: 'main.jpg',
      query: 'id=42&bindUserId=7',
      forward: { path: '/pages/member-detail/index?id=42&bindUserId=7' },
      poster: { type: 'user' },
    });
  });

  it('captures an inviter only for an anonymous registration flow', () => {
    const storage = new Map();
    globalThis.uni = {
      setStorageSync: (key, value) => storage.set(key, value),
      getStorageSync: (key) => storage.get(key),
      removeStorageSync: (key) => storage.delete(key),
    };
    assert.equal(capturePendingBindUserId({ bindUserId: '7' }, false), 7);
    assert.equal(getPendingBindUserId(), 7);
    assert.equal(capturePendingBindUserId({ bindUserId: '9' }, true), 0);
    assert.equal(getPendingBindUserId(), 0);
    clearPendingBindUserId();
    assert.equal(getPendingBindUserId(), 0);
  });
});

describe('member profile share integrations', () => {
  it('shares the same builder from home interactions and member detail', () => {
    const detail = read('../pages/member-detail/index.vue');
    const floating = read('../sheep/components/s-partner-floating-interaction-buttons/s-partner-floating-interaction-buttons.vue');
    const floatingHelper = read('../sheep/components/s-partner-floating-interaction-buttons/partnerFloatingInteractionButtons.js');
    assert.match(detail, /buildMemberShareInfo\(member\.value/);
    assert.match(detail, /capturePendingBindUserId\(options/);
    assert.match(floatingHelper, /buildMemberShareInfo as buildPublicShareInfo/);
    assert.match(floating, /@click\.stop="prepareProfileShare"/);
    assert.match(floating, /setPageShareInfo\(shareInfo\)/);
  });

  it('uses query for timeline and forwards the captured inviter during registration', () => {
    const layout = read('../sheep/components/s-layout/s-layout.vue');
    const login = read('../sheep/components/s-wechat-login/s-wechat-login.vue');
    assert.match(layout, /query: shareInfo\.value\.query \|\| ''/);
    assert.match(login, /bindUserId: getPendingBindUserId\(\)/);
    assert.match(login, /clearPendingBindUserId\(\)/);
  });
});
