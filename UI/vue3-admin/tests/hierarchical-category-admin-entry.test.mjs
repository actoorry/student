import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import test from 'node:test'

const adminRoot = path.resolve(import.meta.dirname, '..')

test('hides the new product-category shortcut while preserving legacy handlers', () => {
  const data = fs.readFileSync(path.join(adminRoot, 'src/components/AppLinkInput/data.ts'), 'utf8')
  const dialog = fs.readFileSync(
    path.join(adminRoot, 'src/components/AppLinkInput/AppLinkSelectDialog.vue'),
    'utf8'
  )
  const activeSource = data
    .split('\n')
    .filter((line) => !line.trim().startsWith('//'))
    .join('\n')

  assert.doesNotMatch(activeSource, /name:\s*'商品分类'/)
  assert.match(data, /暂时隐藏商品分类快捷入口/)
  assert.match(data, /PRODUCT_CATEGORY_LIST/)
  assert.match(dialog, /APP_LINK_TYPE_ENUM\.PRODUCT_CATEGORY_LIST/)
  assert.match(dialog, /getUrlNumberValue\('id'/)
  assert.match(dialog, /ProductCategorySelect/)
})
