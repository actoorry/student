import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (relative) => fs.readFileSync(path.join(root, relative), 'utf8');

test('my certification is the only certification destination exposed by AppLinkInput', () => {
  const data = read('src/components/AppLinkInput/data.ts');
  const dialog = read('src/components/AppLinkInput/AppLinkSelectDialog.vue');
  assert.match(data, /\{ name: '我的认证', path: '\/pages\/mine-certifications\/index' \}/);
  assert.doesNotMatch(data, /mine-profile\/(?:nameCheck|marriageCheck)/);
  assert.match(dialog, /v-for="\(group, groupIndex\) in appLinkGroups"/);
  assert.match(dialog, /\.\.\.APP_LINK_GROUP_LIST/);
  assert.match(dialog, /emit\('change', activeAppLink\.value\.path\)/);
  assert.match(dialog, /emit\('appLinkChange', activeAppLink\.value\)/);
});
