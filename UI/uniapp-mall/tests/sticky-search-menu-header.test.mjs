import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { describe, it } from 'node:test';
import { fileURLToPath } from 'node:url';

const root = dirname(fileURLToPath(import.meta.url));
const mallRoot = resolve(root, '..');
const repoRoot = resolve(mallRoot, '..', '..');
const readMall = (relativePath) => readFileSync(join(mallRoot, relativePath), 'utf8');
const readRepo = (relativePath) => readFileSync(join(repoRoot, relativePath), 'utf8');

describe('StickySearchMenuHeader mall runtime contract', () => {
  const layout = readMall('sheep/components/s-layout/s-layout.vue');
  const renderer = readMall(
    'sheep/components/s-sticky-search-menu-header/s-sticky-search-menu-header.vue',
  );
  const dispatch = readMall('sheep/components/s-block-item/s-block-item.vue');
  const registry = readMall('sheep/components/s-block-item/shared-diy-renderers.js');
  const home = readMall('pages/index/index.vue');
  const page = readMall('pages/index/page.vue');
  const menu = readMall('sheep/components/s-menu-text-grid/s-menu-text-grid.vue');
  const fixture = JSON.parse(readMall('tests/fixtures/sticky-search-menu-header-template.json'));

  it('hides custom navigation only for an explicit false value', () => {
    assert.match(
      layout,
      /navbarEnabled\s*=\s*computed\(\(\)\s*=>\s*props\.navbarStyle\?\.enabled\s*!==\s*false\)/,
    );
    assert.equal({}.enabled !== false, true);
    assert.equal({ enabled: true }.enabled !== false, true);
    assert.equal({ enabled: false }.enabled !== false, false);
    assert.match(layout, /navbarEnabled\s*&&\s*navbarMode\s*===\s*'normal'/);
    assert.match(layout, /navbarEnabled\s*&&\s*navbarMode\s*===\s*'inner'/);
  });

  it('uses existing sticky, safe-area, search and horizontal-menu building blocks', () => {
    assert.match(renderer, /<su-status-bar\s*\/>/);
    assert.match(renderer, /<s-search-block/);
    assert.match(renderer, /<s-menu-text-grid/);
    assert.match(renderer, /sheep\.\$platform\.navbar/);
    assert.match(renderer, /sheep\.\$platform\.capsule/);
    assert.match(renderer, /scrollable:\s*true/);
    assert.match(renderer, /row:\s*1/);
    assert.doesNotMatch(
      renderer,
      /getMenuButtonBoundingClientRect|createIntersectionObserver|position:\s*fixed/,
    );
  });

  it('anchors content at the bottom with independent saved spacing controls', () => {
    assert.match(renderer, /class="bottom-content"/);
    assert.match(renderer, /searchMenuGap:\s*Math\.min\(Math\.max\(Number\(value\.searchMenuGap\)/);
    assert.match(renderer, /menuBottomGap:\s*Math\.min\(Math\.max\(Number\(value\.menuBottomGap\)/);
    assert.match(
      renderer,
      /height:\s*`\$\{Math\.max\(Number\(background\.value\.height\)[\s\S]*320\)}rpx`/,
    );
    assert.doesNotMatch(renderer, /minHeight|offsetTop|brandSpacerStyle/);
    assert.match(renderer, /\.brand-spacer\s*\{[^}]*flex:\s*1\s+1\s+auto/s);
    assert.match(renderer, /\.bottom-content,[\s\S]*flex:\s*0\s+0\s+auto/);
    assert.match(renderer, /\.\.\.\(search\.style\s*\|\|\s*\{\}\),[\s\S]*paddingBottom:\s*0/);
    assert.match(
      renderer,
      /\.\.\.\(menu\.style\s*\|\|\s*\{\}\),[\s\S]*paddingTop:\s*0,[\s\S]*paddingBottom:\s*0/,
    );
    assert.equal(fixture.components[0].property.spacing.searchMenuGap, 0);
    assert.equal(fixture.components[0].property.spacing.menuBottomGap, 0);
  });

  it('bottom-aligns only the composite navigation content', () => {
    assert.match(renderer, /<s-menu-text-grid[\s\S]*vertical-align="bottom"/);
    assert.match(renderer, /vertical-align="bottom"[\s\S]*compact/);
    assert.match(menu, /verticalAlign[\s\S]*default:\s*'center'/);
    assert.match(menu, /compact:[\s\S]*default:\s*false/);
    assert.match(menu, /props\.compact\s*\?\s*fontSize\s*\*\s*2\.8\s*\+\s*8/);
    assert.match(menu, /menu-text-grid-bottom-align/);
    assert.match(menu, /align-items:\s*flex-end\s*!important/);
  });

  it('registers the persisted component id without changing unknown/retired behavior', () => {
    assert.match(dispatch, /type\s*===\s*'StickySearchMenuHeader'/);
    assert.match(registry, /'StickySearchMenuHeader'/);
    assert.match(registry, /isKnownDiyComponent/);
    assert.match(registry, /isRetiredDiyComponent/);
  });

  it('wraps the whole block with upstream su-sticky at zero hidden-navbar offset', () => {
    // 首页：本功能重构后 v-if 挂在外层 template 上，吸顶内直接渲染专用头部组件
    assert.match(
      home,
      /<template v-if="item\.id === 'StickySearchMenuHeader' && item\.property\?\.sticky !== false">[\s\S]*<su-sticky[\s\S]*:offsetTop="0"[\s\S]*:customNavHeight="0"[\s\S]*<s-block[\s\S]*<s-sticky-search-menu-header/,
    );
    assert.match(
      home,
      /<su-sticky[\s\S]*<s-block[\s\S]*<s-sticky-search-menu-header[\s\S]*<\/su-sticky>/,
    );
    // 独立装修页：仍走通用 s-block-item 分发，v-if 挂在 su-sticky 上
    assert.match(page, /<su-sticky[\s\S]*item\.id\s*===\s*'StickySearchMenuHeader'/);
    assert.match(page, /item\.property\?\.sticky\s*!==\s*false/);
    assert.match(page, /:offsetTop="0"/);
    assert.match(page, /:customNavHeight="0"/);
    assert.match(page, /<su-sticky[\s\S]*<s-block[\s\S]*<s-block-item[\s\S]*<\/su-sticky>/);
    assert.equal(fixture.components[0].property.sticky, true);
    assert.equal({}.sticky !== false, true);
    assert.equal({ sticky: false }.sticky !== false, false);
  });

  it('keeps menu links in the existing native horizontal renderer', () => {
    assert.match(menu, /<scroll-view[\s\S]*scroll-x/);
    assert.match(menu, /sheep\.\$router\.go\(item\.url\)/);
  });

  it('provides a white-page test template with the composite header first', () => {
    assert.equal(fixture.page.backgroundColor, '#ffffff');
    assert.equal(fixture.page.backgroundImage, '');
    assert.equal(fixture.navigationBar.enabled, false);
    assert.equal(fixture.components[0].id, 'StickySearchMenuHeader');
    assert.equal(fixture.components[0].property.menu.scrollable, true);
    assert.equal(fixture.components[0].property.menu.row, 1);
    assert.ok(fixture.components[0].property.menu.list.length > 5);
  });

  it('does not enable the feature in the separate marriage client', () => {
    const marriageLayout = readRepo('UI/uniapp-marriage/sheep/components/s-layout/s-layout.vue');
    const marriageHome = readRepo('UI/uniapp-marriage/pages/index/index.vue');
    const marriageRegistry = readRepo(
      'UI/uniapp-marriage/sheep/components/s-block-item/shared-diy-renderers.js',
    );
    for (const source of [marriageLayout, marriageHome, marriageRegistry]) {
      assert.doesNotMatch(source, /StickySearchMenuHeader|navbarEnabled/);
    }
  });
});
