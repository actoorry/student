import assert from 'node:assert/strict';
import test from 'node:test';

const importProductionModule = async (relativePath) => {
  try {
    return await import(new URL(relativePath, import.meta.url));
  } catch (error) {
    assert.fail(`production interaction contract is unavailable: ${error.message}`);
  }
};

test('the dynamic tabbar ignores a click that resolves to the current page', async () => {
  const { shouldNavigateTabbar } = await importProductionModule(
    '../sheep/helper/tabbar-navigation.mjs',
  );

  assert.equal(shouldNavigateTabbar('/pages/index/cart', '/pages/index/cart'), false);
  assert.equal(shouldNavigateTabbar('/pages/index/cart?from=home', '/pages/index/cart'), false);
  assert.equal(shouldNavigateTabbar('/pages/index/cart', '/pages/messages/index'), true);
});

test('a fitted layout gives the message scroller one bounded viewport', async () => {
  const { getViewportLayoutStyles } = await importProductionModule(
    '../sheep/helper/viewport-layout.mjs',
  );

  assert.deepEqual(getViewportLayoutStyles(false), {
    main: {},
    body: {},
    scroll: {},
  });
  assert.deepEqual(getViewportLayoutStyles(true), {
    main: { height: '100%', minHeight: 0, overflow: 'hidden' },
    body: {
      display: 'flex',
      flexDirection: 'column',
      minHeight: 0,
      overflow: 'hidden',
    },
    scroll: { flex: 1, minHeight: 0 },
  });
});
