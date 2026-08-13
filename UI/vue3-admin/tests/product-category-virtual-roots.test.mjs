import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import assert from 'node:assert'
import { describe, it } from 'node:test'

const root = dirname(fileURLToPath(import.meta.url))
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8')

describe('product category virtual dimensions', () => {
  it('builds immutable sales and warehouse roots in the combined management tree', () => {
    const source = read('../src/views/product/category/categoryDimension.ts')

    assert.match(source, /SALES_CATEGORY_ROOT_ID = 1/)
    assert.match(source, /WAREHOUSE_CATEGORY_ROOT_ID = 2/)
    assert.match(source, /name: '销售分类'/)
    assert.match(source, /name: '仓储分类'/)
    assert.match(source, /immutable: true/)
  })

  it('suppresses mutable management actions for virtual nodes and prevents cyclic parents', () => {
    const index = read('../src/views/product/category/index.vue')
    const form = read('../src/views/product/category/CategoryForm.vue')

    assert.match(index, /!isVirtualCategoryRoot\(scope\.row\)/)
    assert.match(form, /disableCategoryAndDescendants/)
    assert.match(form, /disabled: 'disabled'/)
  })

  it('keeps sales consumers and warehouse MES consumers explicitly scoped', () => {
    const spuForm = read('../src/views/product/spu/form/InfoForm.vue')
    const diyRow = read('../src/components/DiyEditor/components/mobile/ProductRow/property.vue')
    const diyWaterfall = read('../src/components/DiyEditor/components/mobile/ProductWaterfall/property.vue')
    const mesItem = read('../src/views/mes/md/item/index.vue')

    assert.match(spuForm, /parentId: SALES_CATEGORY_ROOT_ID/)
    assert.match(diyRow, /:parent-id="1"/)
    assert.match(diyWaterfall, /:parent-id="1"/)
    assert.match(mesItem, /parentId: 2/)
  })
})
