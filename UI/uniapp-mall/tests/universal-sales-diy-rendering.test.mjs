import assert from 'node:assert/strict';
import { existsSync } from 'node:fs';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');
const readSource = (relativePath) => readFile(path.join(repoRoot, relativePath), 'utf8');

test('the mall client uses the ordinary tenant and active-template flow', async () => {
  const [appStore, packageJson] = await Promise.all([
    readSource('UI/uniapp-mall/sheep/store/app.js'),
    readSource('UI/uniapp-mall/package.json'),
  ]);

  assert.match(appStore, /getTenantByWebsite\(window\.location\.host\)/);
  assert.match(appStore, /getTenantByWebsite\(appId\)/);
  assert.match(appStore, /DiyApi\.getUsedDiyTemplate\(\)/);
  assert.doesNotMatch(
    appStore,
    /resolveProductStartup|validateActiveTemplate|productCode|productPolicy/,
  );
  assert.doesNotMatch(
    packageJson,
    /verify:product-profiles|mp-weixin-social|mp-weixin-mall|product-foundation/,
  );
});

test('the standard renderer retires custom Marriage IDs and isolates unknown IDs', async () => {
  const [blockItem, rendererRegistry, homePage, userPage, standalonePage] = await Promise.all([
    readSource('UI/uniapp-mall/sheep/components/s-block-item/s-block-item.vue'),
    readSource('UI/uniapp-mall/sheep/components/s-block-item/shared-diy-renderers.js'),
    readSource('UI/uniapp-mall/pages/index/index.vue'),
    readSource('UI/uniapp-mall/pages/index/user.vue'),
    readSource('UI/uniapp-mall/pages/index/page.vue'),
  ]);

  for (const componentId of [
    'MarriageLoginPrompt',
    'MarriageRecommendProfileDeck',
    'MarriageIdentityTrustBanner',
  ]) {
    assert.match(rendererRegistry, new RegExp(componentId));
  }
  assert.doesNotMatch(blockItem, /<component\b|sharedDiyRenderer|getSharedDiyRenderer/);
  assert.match(blockItem, /<s-home-background-swipe v-if="type === 'HomeBackgroundSwipe'"/);
  assert.match(blockItem, /unknown-diy-component/);
  assert.match(rendererRegistry, /return null/);
  for (const source of [homePage, userPage, standalonePage]) {
    assert.match(source, /<s-block-item\b/);
    assert.match(source, /:type="item\.id"/);
    assert.match(source, /:data="item\.property \|\| \{\}"/);
    assert.match(source, /:styles="item\.property\?\.style \|\| \{\}"/);
  }
});

test('all standard DIY surfaces isolate missing component properties', async () => {
  const pages = await Promise.all([
    readSource('UI/uniapp-mall/pages/index/index.vue'),
    readSource('UI/uniapp-mall/pages/index/user.vue'),
    readSource('UI/uniapp-mall/pages/index/page.vue'),
  ]);

  for (const source of pages) {
    assert.match(source, /v-if="item && item\.id"/);
    assert.match(source, /:data="item\.property \|\| \{\}"/);
    assert.match(source, /:styles="item\.property\?\.style \|\| \{\}"/);
  }
});

test('the management editor exposes the reference mall library without custom component filtering', async () => {
  const [editor, library, decorate, componentLibraries] = await Promise.all([
    readSource('UI/vue3-admin/src/components/DiyEditor/index.vue'),
    readSource('UI/vue3-admin/src/components/DiyEditor/components/ComponentLibrary.vue'),
    readSource('UI/vue3-admin/src/views/sales/promotion/diy/template/decorate.vue'),
    readSource('UI/vue3-admin/src/components/DiyEditor/util.ts'),
  ]);

  assert.doesNotMatch(editor, /allowedComponentIds/);
  assert.doesNotMatch(library, /allowedComponentIds/);
  assert.doesNotMatch(
    decorate,
    /TenantApi|productCode|productPolicy|getSocialComponentLibraries|marriageDiyContract/,
  );
  assert.doesNotMatch(componentLibraries, /特色组件/);
  for (const componentId of [
    'MarriageLoginPrompt',
    'MarriageRecommendProfileDeck',
    'MarriageIdentityTrustBanner',
  ]) {
    assert.doesNotMatch(componentLibraries, new RegExp(componentId));
  }
});

test('HomeBackgroundSwipe is hidden from new authoring but retained for historical rendering', async () => {
  const [componentLibraries, componentConfig, blockItem, rendererRegistry] = await Promise.all([
    readSource('UI/vue3-admin/src/components/DiyEditor/util.ts'),
    readSource(
      'UI/vue3-admin/src/components/DiyEditor/components/mobile/HomeBackgroundSwipe/config.ts',
    ),
    readSource('UI/uniapp-mall/sheep/components/s-block-item/s-block-item.vue'),
    readSource('UI/uniapp-mall/sheep/components/s-block-item/shared-diy-renderers.js'),
  ]);

  assert.doesNotMatch(componentLibraries, /['"]HomeBackgroundSwipe['"]/);
  assert.match(componentConfig, /id:\s*['"]HomeBackgroundSwipe['"]/);
  assert.match(componentConfig, /静态场景轮播（历史兼容）/);
  assert.match(blockItem, /<s-home-background-swipe v-if="type === 'HomeBackgroundSwipe'"/);
  assert.match(rendererRegistry, /['"]HomeBackgroundSwipe['"]/);
});

test('retired custom component data is silently skipped without changing retained templates', async () => {
  const [blockItem, rendererRegistry, componentLibraries] = await Promise.all([
    readSource('UI/uniapp-mall/sheep/components/s-block-item/s-block-item.vue'),
    readSource('UI/uniapp-mall/sheep/components/s-block-item/shared-diy-renderers.js'),
    readSource('UI/vue3-admin/src/components/DiyEditor/util.ts'),
  ]);

  assert.match(rendererRegistry, /RETIRED_DIY_COMPONENT_IDS/);
  assert.match(blockItem, /isRetiredDiyComponent/);
  assert.match(blockItem, /!isRetiredDiyComponent/);
  for (const [componentId, directory] of [
    ['MarriageLoginPrompt', 'marriage-login-prompt'],
    ['MarriageRecommendProfileDeck', 'marriage-recommend-profile-deck'],
    ['MarriageIdentityTrustBanner', 'marriage-identity-trust-banner'],
  ]) {
    assert.match(rendererRegistry, new RegExp(componentId));
    assert.doesNotMatch(componentLibraries, new RegExp(componentId));
    assert.equal(
      existsSync(path.join(repoRoot, `UI/uniapp-mall/sheep/components/${directory}`)),
      false,
    );
    assert.equal(
      existsSync(
        path.join(
          repoRoot,
          `UI/vue3-admin/src/components/DiyEditor/components/mobile/${componentId}`,
        ),
      ),
      false,
    );
  }
});

test('superseded product runtimes and server contracts are absent', async () => {
  assert.equal(existsSync(path.join(repoRoot, 'UI/uniapp-mall/product-foundation')), false);
  assert.equal(existsSync(path.join(repoRoot, 'UI/uniapp-mall/product-scopes')), false);

  const sources = await Promise.all([
    readSource(
      'suxin-module/src/main/java/vip/appap/suxin/module/system/dal/dataobject/TenantDO.java',
    ),
    readSource(
      'suxin-module/src/main/java/vip/appap/suxin/module/system/controller/app/AppTenantController.java',
    ),
    readSource(
      'suxin-module/src/main/java/vip/appap/suxin/module/sales/service/SalesPromotionDiyPageServiceImpl.java',
    ),
  ]);
  assert.doesNotMatch(
    sources.join('\n'),
    /TenantProduct|productCode|productPolicy|product_code|product_policy/,
  );
});
