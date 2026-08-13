import { describe, it } from 'node:test'
import assert from 'node:assert'
import { readFileSync } from 'node:fs'

const config = readFileSync(
  new URL('../src/components/DiyEditor/components/mobile/PartnerUserHeader/config.ts', import.meta.url),
  'utf-8'
)
const preview = readFileSync(
  new URL('../src/components/DiyEditor/components/mobile/PartnerUserHeader/index.vue', import.meta.url),
  'utf-8'
)

describe('PartnerUserHeader admin contract', () => {
  it('uses the declared component ID and an exhaustive, safe style default', () => {
    assert.match(config, /id:\s*'PartnerUserHeader'/)
    assert.match(config, /bgType:\s*'color'/)
    assert.match(config, /marginTop:\s*0/)
    assert.match(config, /paddingBottom:\s*0/)
    assert.match(config, /borderBottomLeftRadius:\s*0/)
  })

  it('keeps the anonymous preview fixture out of persisted properties', () => {
    const propertyBlock = config.match(/return\s*\{([\s\S]*?)\n\s*}\n}/)?.[1] ?? ''
    assert.ok(!propertyBlock.includes('林小鹿'))
    assert.ok(!propertyBlock.includes('编辑资料'))
    assert.match(preview, /PARTNER_USER_HEADER_PREVIEW/)
  })

  it('is appended to the existing social component library', () => {
    const util = readFileSync(new URL('../src/components/DiyEditor/util.ts', import.meta.url), 'utf-8')
    assert.match(util, /'PartnerProfileMoment',\s*'PartnerUserHeader'/)
  })
})
