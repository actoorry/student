import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = dirname(fileURLToPath(import.meta.url));
const clientRoot = join(root, '..');
const modalPath = join(clientRoot, 'sheep', 'components', 's-share-modal', 's-share-modal.vue');
const shareAssets = [
  'static/img/shop/share/share_friend.svg',
  'static/img/shop/share/share_poster.svg',
  'static/img/shop/share/share_link.svg',
];

describe('share modal assets', () => {
  it('renders each common share action with a packaged cross-platform icon', () => {
    const modal = readFileSync(modalPath, 'utf8');

    shareAssets.forEach((asset) => {
      assert.match(modal, new RegExp(asset));
      assert.equal(existsSync(join(clientRoot, asset)), true, `${asset} must exist`);
    });
  });
});
