import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { describe, it } from 'node:test'
import { fileURLToPath } from 'node:url'

const root = dirname(fileURLToPath(import.meta.url))
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8')

describe('MenuGrid navigation title contract', () => {
  const config = read('../src/components/DiyEditor/components/mobile/MenuGrid/config.ts')
  const preview = read('../src/components/DiyEditor/components/mobile/MenuGrid/index.vue')
  const propertyPanel = read(
    '../src/components/DiyEditor/components/mobile/MenuGrid/property.vue'
  )
  const runtime = read('../../uniapp-mall/sheep/components/s-menu-grid/s-menu-grid.vue')

  it('provides and edits a default navigation title', () => {
    assert.match(config, /title:\s*'导航标题'/)
    assert.match(propertyPanel, /label="导航标题"\s+prop="title"/)
    assert.match(propertyPanel, /v-model="formData\.title"/)
  })

  it('renders the same optional title in admin preview and uni-app runtime', () => {
    assert.match(preview, /v-if="property\.title"[^>]*>\{\{\s*property\.title\s*\}\}/)
    assert.match(runtime, /v-if="data\.title"[^>]*>\{\{\s*data\.title\s*\}\}/)
  })

  it('keeps legacy properties without a title and new properties with a title JSON-safe', () => {
    const legacyProperty = {
      column: 3,
      list: [],
      style: { bgType: 'color', bgColor: '#fff' }
    }
    const titledProperty = { ...legacyProperty, title: '常用服务' }

    assert.equal(JSON.parse(JSON.stringify(legacyProperty)).title, undefined)
    assert.equal(JSON.parse(JSON.stringify(titledProperty)).title, '常用服务')
  })
})
