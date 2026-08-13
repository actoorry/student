import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';

const mallRoot = path.resolve(import.meta.dirname, '..');

test('category links open the hierarchical product page without redirecting', () => {
  const categoryPage = fs.readFileSync(path.join(mallRoot, 'pages/index/category.vue'), 'utf8');
  const legacyPage = fs.readFileSync(
    path.join(mallRoot, 'pages/index/category-legacy.vue'),
    'utf8',
  );
  const router = fs.readFileSync(path.join(mallRoot, 'sheep/router/index.js'), 'utf8');
  const pages = fs.readFileSync(path.join(mallRoot, 'pages.json'), 'utf8');

  assert.match(categoryPage, /CategoryApi\.getCategoryList/);
  assert.match(categoryPage, /s-goods-waterfall/);
  assert.match(categoryPage, /showLeftButton/);
  assert.match(categoryPage, /item\.picUrl/);
  assert.match(categoryPage, /sheep\.\$url\.cdn\(item\.picUrl\)/);
  assert.match(categoryPage, /category-menu-item-primary/);
  assert.match(categoryPage, /width: calc\(\(100% - 80rpx\) \/ 5\)/);
  assert.match(categoryPage, /height: calc\(\(100vw - 120rpx\) \/ 5\)/);
  assert.match(categoryPage, /category-menu-item-primary:nth-child\(5n\)/);
  assert.match(categoryPage, /category-menu-item-secondary/);
  assert.match(categoryPage, /\.category-menu-item-secondary\s*\{/);
  assert.match(
    categoryPage,
    /category-menu-item-secondary\.category-menu-item-active[\s\S]*background: transparent/,
  );
  assert.match(categoryPage, /category-menu-text/);
  assert.match(categoryPage, /sheep\.\$router\.go\('\/pages\/index\/category'/);
  assert.match(router, /uni\.navigateTo\(/);
  assert.match(categoryPage, /state\.mode === 'primary'/);
  assert.match(categoryPage, /isAll: true/);
  assert.doesNotMatch(categoryPage, /sheep\.\$router\.redirect\('\/pages\/goods\/list'/);
  assert.match(legacyPage, /商品分类列表/);
  assert.match(pages, /"path": "pages\/index\/category"/);
  assert.doesNotMatch(pages, /category-legacy/);
});

test('home page renders pdd panel and hides decoration components in pdd mode', () => {
  const home = fs.readFileSync(path.join(mallRoot, 'pages/index/index.vue'), 'utf8');

  // 拼多多面板：二级分类五列图 + 商品瀑布流
  assert.match(home, /pdd-secondary-grid/);
  assert.match(home, /onSecondaryCategoryTap/);
  assert.match(home, /:category-id="pddCategoryId"/);
  // 头部开启拦截并联动高亮
  assert.match(home, /@menuClickItem="onHeaderMenuTap"/);
  assert.match(home, /:active-menu-index="activeMenuIndex"/);
  // 拼多多态隐藏其余装修组件
  assert.match(home, /v-else-if="!pddMode"/);
  // 面板锚定到 header 后第一个有效组件，并覆盖 header 为最后组件的兜底
  assert.match(home, /firstComponentAfterHeaderIndex/);
  assert.match(home, /shouldRenderPanelAfter\(\)/);
  assert.match(home, /!hasComponentAfterHeader\.value/);
  // 已移除草稿中的返回按钮（退出仅通过“推荐”）
  assert.doesNotMatch(home, /pdd-back-button/);
});

test('menu text grid supports tap interception and active highlight', () => {
  const menuGrid = fs.readFileSync(
    path.join(mallRoot, 'sheep/components/s-menu-text-grid/s-menu-text-grid.vue'),
    'utf8',
  );

  // 拦截开关：为 true 时不执行默认跳转
  assert.match(menuGrid, /interceptTap:\s*\{\s*type: Boolean/s);
  assert.match(menuGrid, /interceptTap[\s\S]{0,200}default: false/);
  // 选中高亮索引
  assert.match(menuGrid, /activeIndex:\s*\{\s*type: Number/s);
  assert.match(menuGrid, /activeIndex[\s\S]{0,200}default: -1/);
  // 点击事件携带 item 与原始索引
  assert.match(menuGrid, /emit\('clickItem',\s*\{\s*item,\s*index\s*\}\)/);
  // 拦截时不跳转：跳转语句受 interceptTap 守卫
  assert.match(menuGrid, /if\s*\(!props\.interceptTap\s*&&\s*item\.url\)/);
});

test('waterfall supports controlled category changes and stale request protection', () => {
  const waterfall = fs.readFileSync(
    path.join(mallRoot, 'sheep/components/s-goods-waterfall/s-goods-waterfall.vue'),
    'utf8',
  );

  assert.match(waterfall, /categoryId:\s*\{\s*type: \[Number, String\]/s);
  assert.match(waterfall, /const selectedCategoryId = computed/);
  assert.match(waterfall, /props\.data\?\.rule\?\.categorySales/);
  assert.match(waterfall, /categorySales: selectedCategoryId\.value/);
  assert.match(waterfall, /let requestGeneration = 0/);
  assert.match(waterfall, /currentGeneration !== requestGeneration/);
  assert.match(waterfall, /watch\(selectedCategoryId/);
});

test('sticky search menu header forwards interception and highlight props', () => {
  const header = fs.readFileSync(
    path.join(mallRoot, 'sheep/components/s-sticky-search-menu-header/s-sticky-search-menu-header.vue'),
    'utf8',
  );

  assert.match(header, /interceptMenuTap:\s*\{\s*type: Boolean/s);
  assert.match(header, /activeMenuIndex:\s*\{\s*type: Number/s);
  // 透传给内部菜单组件
  assert.match(header, /:intercept-tap="interceptMenuTap"/);
  assert.match(header, /:active-index="activeMenuIndex"/);
  // 事件透传携带 item 与 index
  assert.match(header, /emit\('menuClickItem',\s*payload\)/);
});

test('home page owns the pdd category state machine and dispatch', () => {
  const home = fs.readFileSync(path.join(mallRoot, 'pages/index/index.vue'), 'utf8');

  // 状态机三要素（页面实例局部，不写全局 Store）
  assert.match(home, /const pddMode = ref\(false\)/);
  assert.match(home, /const pddCategoryId = ref\(0\)/);
  assert.match(home, /const pddMenuIndex = ref\(-1\)/);
  // 拦截分发：category 链接原地切换，首页链接退出，其余走默认跳转
  assert.match(home, /pages\/index\/category/);
  assert.match(home, /pages\/index\/index/);
  assert.match(home, /function onHeaderMenuTap/);
  // 分类树懒加载缓存
  assert.match(home, /CategoryApi\.getCategoryList/);
  assert.match(home, /handleTree/);
  // 开启了头部菜单拦截
  assert.match(home, /:intercept-menu-tap="true"/);
});
