import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (relativePath) => fs.readFileSync(path.join(root, relativePath), 'utf8');

test('shared layout shows one back button outside bottom navigation pages', () => {
  const layout = read('sheep/components/s-layout/s-layout.vue');
  const navbar = read('sheep/ui/su-navbar/su-navbar.vue');

  assert.match(layout, /props\.showLeftButton \|\| !Boolean\(props\.tabbar\)/);
  assert.match(navbar, /<text class="sicon-back" \/>/);
  assert.doesNotMatch(navbar, /sicon-more|sicon-home|showMenuTools|icon-button-right/);
});

test('standalone marriage and public pages use the shared navbar', () => {
  [
    'pages/mine-points/index.vue',
    'pages/mine-checkin/index.vue',
    'pages/mine-certifications/index.vue',
    'pages/mine-profile/basic/index.vue',
    'pages/mine-profile/nameCheck/index.vue',
    'pages/mine-profile/marriageCheck/index.vue',
    'pages/public/privacy-agreement.vue',
    'pages/public/error.vue',
    'pages/public/webview.vue',
  ].forEach((page) => assert.match(read(page), /<su-navbar(?:\s|>)/, page));

  assert.doesNotMatch(read('pages/public/privacy-agreement.vue'), /privacy-back|@tap="goBack"/);
});
