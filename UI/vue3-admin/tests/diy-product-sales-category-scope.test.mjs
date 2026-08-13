import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import assert from 'node:assert'
import { describe, it } from 'node:test'

const root = dirname(fileURLToPath(import.meta.url))
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8')

const scopedSalesCategorySelector =
  /<ProductCategorySelect\s+v-model="formData\.rule\.categorySales"\s+:parent-id="1"\s*\/>/

describe('DIY product sales-category selector scope', () => {
  it('scopes ProductRow rule categories to the sales root', () => {
    const propertyPanel = read(
      '../src/components/DiyEditor/components/mobile/ProductRow/property.vue'
    )

    assert.match(propertyPanel, scopedSalesCategorySelector)
  })

  it('scopes ProductWaterfall rule categories to the sales root', () => {
    const propertyPanel = read(
      '../src/components/DiyEditor/components/mobile/ProductWaterfall/property.vue'
    )

    assert.match(propertyPanel, scopedSalesCategorySelector)
  })
})
