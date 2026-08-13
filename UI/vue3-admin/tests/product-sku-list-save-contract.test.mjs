import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import assert from 'node:assert'
import { describe, it } from 'node:test'

const root = dirname(fileURLToPath(import.meta.url))
const productSpuListPage = readFileSync(
  join(root, '../src/views/product/spu/index.vue'),
  'utf-8'
)

describe('product SKU list save contract', () => {
  it('fills every SKU name from the SPU before updating', () => {
    const saveHandlerStart = productSpuListPage.indexOf('const onSkuSave = async')
    const saveHandlerEnd = productSpuListPage.indexOf('/** 导出按钮操作 */', saveHandlerStart)
    const saveHandler = productSpuListPage.slice(saveHandlerStart, saveHandlerEnd)

    assert.ok(saveHandlerStart >= 0, 'onSkuSave handler should exist')
    assert.ok(saveHandlerEnd > saveHandlerStart, 'onSkuSave handler should have a stable boundary')
    assert.match(
      saveHandler,
      /deepCopyFormData\.skus!\.forEach\(\(item\) => \{[\s\S]*?item\.name = deepCopyFormData\.name[\s\S]*?ProductSpuApi\.updateSpu\(deepCopyFormData\)/
    )
  })
})
