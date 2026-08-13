import { describe, it } from 'node:test'
import assert from 'node:assert'
import { readFileSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import {
  createDefaultPartnerRealNameVerificationBadgeProperty,
  DEFAULT_ACCENT_COLOR as REAL_NAME_ACCENT,
  DEFAULT_BADGE_BACKGROUND_COLOR as REAL_NAME_BG,
  DEFAULT_DESCRIPTION_COLOR as REAL_NAME_DESC,
  DEFAULT_TITLE_COLOR as REAL_NAME_TITLE,
  extractIdCardPrefix,
  normalizeColor,
  rpxToPreviewPx,
  SAMPLE_MASKED_ID_CARD_PREFIX,
  component as realNameComponent,
} from '../src/components/DiyEditor/components/mobile/PartnerRealNameVerificationBadge/config'
import {
  createDefaultPartnerMarriageVerificationBadgeProperty,
  DEFAULT_ACCENT_COLOR as MARRIAGE_ACCENT,
  DEFAULT_BADGE_BACKGROUND_COLOR as MARRIAGE_BG,
  DEFAULT_DESCRIPTION_COLOR as MARRIAGE_DESC,
  DEFAULT_TITLE_COLOR as MARRIAGE_TITLE,
  component as marriageComponent,
} from '../src/components/DiyEditor/components/mobile/PartnerMarriageVerificationBadge/config'
import { PAGE_LIBS } from '../src/components/DiyEditor/util'

const __filename = fileURLToPath(import.meta.url)
const root = dirname(__filename)
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8')

function createRealNameBlock() {
  return {
    id: realNameComponent.id,
    property: createDefaultPartnerRealNameVerificationBadgeProperty(),
  }
}

function createMarriageBlock() {
  return {
    id: marriageComponent.id,
    property: createDefaultPartnerMarriageVerificationBadgeProperty(),
  }
}

describe('PartnerRealNameVerificationBadge admin contract', () => {
  it('default property contains all fields with safe defaults', () => {
    const property = createDefaultPartnerRealNameVerificationBadgeProperty()
    assert.strictEqual(property.badgeBackgroundColor, REAL_NAME_BG)
    assert.strictEqual(property.accentColor, REAL_NAME_ACCENT)
    assert.strictEqual(property.titleColor, REAL_NAME_TITLE)
    assert.strictEqual(property.descriptionColor, REAL_NAME_DESC)

    assert.strictEqual(property.style.bgType, 'color')
    assert.strictEqual(property.style.bgColor, '')
    assert.strictEqual(property.style.margin, 0)
    assert.strictEqual(property.style.padding, 0)
    assert.strictEqual(property.style.borderRadius, 0)
  })

  it('normalizes #RRGGBB colors only', () => {
    assert.strictEqual(normalizeColor('#C84449', REAL_NAME_ACCENT), '#C84449')
    assert.strictEqual(normalizeColor('#c84449', REAL_NAME_ACCENT), '#C84449')
  })

  it('falls back color defaults for invalid colors', () => {
    assert.strictEqual(normalizeColor('#12345', REAL_NAME_ACCENT), REAL_NAME_ACCENT)
    assert.strictEqual(normalizeColor('red', REAL_NAME_ACCENT), REAL_NAME_ACCENT)
    assert.strictEqual(normalizeColor(null, REAL_NAME_ACCENT), REAL_NAME_ACCENT)
    assert.strictEqual(normalizeColor(123, REAL_NAME_ACCENT), REAL_NAME_ACCENT)
  })

  it('converts fixed rpx sizes to preview px by halving', () => {
    assert.strictEqual(rpxToPreviewPx(72), 36)
    assert.strictEqual(rpxToPreviewPx(40), 20)
    assert.strictEqual(rpxToPreviewPx(28), 14)
    assert.strictEqual(rpxToPreviewPx(32), 16)
    assert.strictEqual(rpxToPreviewPx(20), 10)
  })

  it('extracts exactly four leading digits from masked id card', () => {
    assert.strictEqual(extractIdCardPrefix('4403**************'), '4403')
    assert.strictEqual(extractIdCardPrefix('  4403**************'), '4403')
    assert.strictEqual(extractIdCardPrefix('440'), '')
    assert.strictEqual(extractIdCardPrefix('abcd**************'), '')
    assert.strictEqual(extractIdCardPrefix(null), '')
    assert.strictEqual(extractIdCardPrefix(4403), '')
  })

  it('sample masked id card prefix is not persisted in default property', () => {
    const property = createDefaultPartnerRealNameVerificationBadgeProperty()
    assert.strictEqual('maskedIdCard' in property, false)
    assert.strictEqual(SAMPLE_MASKED_ID_CARD_PREFIX, '4403')
  })

  it('is registered in the 社交组件 group', () => {
    const group = PAGE_LIBS.find((lib) => lib.name === '社交组件')
    assert.ok(group)
    assert.ok(group!.components.includes('PartnerRealNameVerificationBadge'))
  })

  it('serialized component keeps id before property', () => {
    const component = {
      id: realNameComponent.id,
      property: createDefaultPartnerRealNameVerificationBadgeProperty(),
    }
    const serialized = JSON.stringify(component)
    const idIndex = serialized.indexOf('"id"')
    const propertyIndex = serialized.indexOf('"property"')
    assert.ok(idIndex >= 0)
    assert.ok(propertyIndex >= 0)
    assert.ok(idIndex < propertyIndex)
  })

  it('preview component does not request network or persist sample', () => {
    const index = read('../src/components/DiyEditor/components/mobile/PartnerRealNameVerificationBadge/index.vue')
    assert.doesNotMatch(index, /axios|fetch|XMLHttpRequest|http:\/\/|https:\/\//)
    assert.doesNotMatch(index, /SAMPLE_MASKED_ID_CARD_PREFIX\s*in\s*property|property\.maskedIdCard/)
  })

  it('property panel uses ComponentContainerProperty and useVModel', () => {
    const property = read('../src/components/DiyEditor/components/mobile/PartnerRealNameVerificationBadge/property.vue')
    assert.match(property, /<ComponentContainerProperty/)
    assert.match(property, /useVModel/)
  })
})

describe('PartnerMarriageVerificationBadge admin contract', () => {
  it('default property contains all fields with marriage defaults', () => {
    const property = createDefaultPartnerMarriageVerificationBadgeProperty()
    assert.strictEqual(property.badgeBackgroundColor, MARRIAGE_BG)
    assert.strictEqual(property.accentColor, MARRIAGE_ACCENT)
    assert.strictEqual(property.titleColor, MARRIAGE_TITLE)
    assert.strictEqual(property.descriptionColor, MARRIAGE_DESC)
  })

  it('is registered in the 社交组件 group', () => {
    const group = PAGE_LIBS.find((lib) => lib.name === '社交组件')
    assert.ok(group)
    assert.ok(group!.components.includes('PartnerMarriageVerificationBadge'))
  })

  it('serialized component keeps id before property', () => {
    const component = {
      id: marriageComponent.id,
      property: createDefaultPartnerMarriageVerificationBadgeProperty(),
    }
    const serialized = JSON.stringify(component)
    const idIndex = serialized.indexOf('"id"')
    const propertyIndex = serialized.indexOf('"property"')
    assert.ok(idIndex >= 0)
    assert.ok(propertyIndex >= 0)
    assert.ok(idIndex < propertyIndex)
  })

  it('preview component does not request network', () => {
    const index = read('../src/components/DiyEditor/components/mobile/PartnerMarriageVerificationBadge/index.vue')
    assert.doesNotMatch(index, /axios|fetch|XMLHttpRequest|http:\/\/|https:\/\//)
  })

  it('property panel uses ComponentContainerProperty and useVModel', () => {
    const property = read('../src/components/DiyEditor/components/mobile/PartnerMarriageVerificationBadge/property.vue')
    assert.match(property, /<ComponentContainerProperty/)
    assert.match(property, /useVModel/)
  })
})

describe('Verification badge editor integration', () => {
  it('supports drag, copy, sort, delete and preserves id before property', () => {
    let page = [createRealNameBlock()]

    // 拖入婚恋认证标志
    page.push(createMarriageBlock())
    assert.strictEqual(page.length, 2)
    assert.strictEqual(page[0].id, 'PartnerRealNameVerificationBadge')
    assert.strictEqual(page[1].id, 'PartnerMarriageVerificationBadge')

    // 复制第一个
    const copy = JSON.parse(JSON.stringify(page[0]))
    copy.uid = 2
    page.splice(1, 0, copy)
    assert.strictEqual(page.length, 3)

    // 排序：把婚恋认证标志移到最前
    page = [page[2], page[0], page[1]]
    assert.strictEqual(page[0].id, 'PartnerMarriageVerificationBadge')

    // 删除复制的实名标志（现在在索引 2）
    page.splice(2, 1)
    assert.strictEqual(page.length, 2)
    assert.strictEqual(page[1].id, 'PartnerRealNameVerificationBadge')

    // 最终序列化保持 { id, property } 顺序
    const serialized = JSON.stringify(page)
    const idIndex = serialized.indexOf('"id"')
    const propertyIndex = serialized.indexOf('"property"')
    assert.ok(idIndex < propertyIndex)
  })

  it('edits allowed colors and reloads from JSON string', () => {
    const block = createRealNameBlock()
    block.property.accentColor = '#000000'
    const reloaded = JSON.parse(JSON.stringify(block))
    assert.strictEqual(reloaded.property.accentColor, '#000000')
    assert.strictEqual(reloaded.id, 'PartnerRealNameVerificationBadge')
  })

  it('ignores unknown fields in runtime but preserves them in JSON', () => {
    const block = createRealNameBlock()
    const withExtra = { ...block, property: { ...block.property, forgedStatus: 1, fakePrefix: '1234' } }
    const reloaded = JSON.parse(JSON.stringify(withExtra))
    assert.strictEqual(reloaded.id, 'PartnerRealNameVerificationBadge')
    assert.strictEqual(reloaded.property.forgedStatus, 1)
    assert.strictEqual(reloaded.property.fakePrefix, '1234')
  })
})
