import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (relative) => fs.readFileSync(path.join(root, relative), 'utf8');

test('mine check-in and points AppLinks use exact compiled paths', () => {
  const data = read('src/components/AppLinkInput/data.ts');
  const dialog = read('src/components/AppLinkInput/AppLinkSelectDialog.vue');
  assert.match(data, /\{ name: '我的签到', path: '\/pages\/mine-checkin\/index' \}/);
  assert.match(data, /\{ name: '我的积分', path: '\/pages\/mine-points\/index' \}/);
  assert.match(data, /\{ name: '我的认证', path: '\/pages\/mine-certifications\/index' \}/);
  assert.doesNotMatch(data, /mine-(?:checkin|points)\/index\?/);
  assert.match(dialog, /:content="appLink\.path"/);
  assert.match(dialog, /nextTick\(\(\) => handleGroupSelected\(group\.name\)\)/);
  assert.match(dialog, /linkScrollbar\.value\?\.setScrollTop\(titleRef\.offsetTop\)/);
  assert.match(dialog, /emit\('change', activeAppLink\.value\.path\)/);
  assert.match(dialog, /emit\('appLinkChange', activeAppLink\.value\)/);
});
