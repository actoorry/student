import { describe, it } from 'node:test'
import assert from 'node:assert'
import {
  createDefaultPartnerProfileHeroProperty,
  DEFAULT_AGE_COLOR,
  DEFAULT_AVATAR_SIZE,
  DEFAULT_HEIGHT,
  DEFAULT_NAME_COLOR,
  normalizeAvatarSize,
  normalizeColor,
  normalizeHeight,
  rpxToPreviewPx,
  SAMPLE_PROFILE,
} from '../src/components/DiyEditor/components/mobile/PartnerProfileHero/config'

describe('PartnerProfileHero admin contract', () => {
  it('default property contains all fields with safe defaults', () => {
    const property = createDefaultPartnerProfileHeroProperty()
    assert.strictEqual(property.height, DEFAULT_HEIGHT)
    assert.strictEqual(property.avatarSize, DEFAULT_AVATAR_SIZE)
    assert.strictEqual(property.nameColor, DEFAULT_NAME_COLOR)
    assert.strictEqual(property.ageColor, DEFAULT_AGE_COLOR)

    assert.strictEqual(property.style.bgType, 'color')
    assert.strictEqual(property.style.bgColor, '')
    assert.strictEqual(property.style.bgImg, '')
    assert.strictEqual(property.style.margin, 0)
    assert.strictEqual(property.style.marginTop, 0)
    assert.strictEqual(property.style.marginRight, 0)
    assert.strictEqual(property.style.marginBottom, 0)
    assert.strictEqual(property.style.marginLeft, 0)
    assert.strictEqual(property.style.padding, 0)
    assert.strictEqual(property.style.paddingTop, 0)
    assert.strictEqual(property.style.paddingRight, 0)
    assert.strictEqual(property.style.paddingBottom, 0)
    assert.strictEqual(property.style.paddingLeft, 0)
    assert.strictEqual(property.style.borderRadius, 0)
    assert.strictEqual(property.style.borderTopLeftRadius, 0)
    assert.strictEqual(property.style.borderTopRightRadius, 0)
    assert.strictEqual(property.style.borderBottomRightRadius, 0)
    assert.strictEqual(property.style.borderBottomLeftRadius, 0)
  })

  it('normalizes height within range', () => {
    assert.strictEqual(normalizeHeight(640), 640)
    assert.strictEqual(normalizeHeight(1400), 1400)
    assert.strictEqual(normalizeHeight(1040), 1040)
    assert.strictEqual(normalizeHeight(500), DEFAULT_HEIGHT)
    assert.strictEqual(normalizeHeight(1500), DEFAULT_HEIGHT)
  })

  it('falls back height default for invalid types', () => {
    assert.strictEqual(normalizeHeight(null), DEFAULT_HEIGHT)
    assert.strictEqual(normalizeHeight(undefined), DEFAULT_HEIGHT)
    assert.strictEqual(normalizeHeight('abc'), DEFAULT_HEIGHT)
    assert.strictEqual(normalizeHeight(NaN), DEFAULT_HEIGHT)
    assert.strictEqual(normalizeHeight(Infinity), DEFAULT_HEIGHT)
  })

  it('normalizes avatar size within range', () => {
    assert.strictEqual(normalizeAvatarSize(64), 64)
    assert.strictEqual(normalizeAvatarSize(160), 160)
    assert.strictEqual(normalizeAvatarSize(92), 92)
    assert.strictEqual(normalizeAvatarSize(50), DEFAULT_AVATAR_SIZE)
    assert.strictEqual(normalizeAvatarSize(200), DEFAULT_AVATAR_SIZE)
  })

  it('falls back avatar size default for invalid types', () => {
    assert.strictEqual(normalizeAvatarSize(null), DEFAULT_AVATAR_SIZE)
    assert.strictEqual(normalizeAvatarSize(undefined), DEFAULT_AVATAR_SIZE)
    assert.strictEqual(normalizeAvatarSize(''), DEFAULT_AVATAR_SIZE)
  })

  it('normalizes #RRGGBB colors only', () => {
    assert.strictEqual(normalizeColor('#201917', DEFAULT_NAME_COLOR), '#201917')
    assert.strictEqual(normalizeColor('#6F5A55', DEFAULT_AGE_COLOR), '#6F5A55')
    assert.strictEqual(normalizeColor('#abcdef', DEFAULT_NAME_COLOR), '#ABCDEF')
  })

  it('falls back color default for invalid colors', () => {
    assert.strictEqual(normalizeColor('#12345', DEFAULT_NAME_COLOR), DEFAULT_NAME_COLOR)
    assert.strictEqual(normalizeColor('#1234567', DEFAULT_NAME_COLOR), DEFAULT_NAME_COLOR)
    assert.strictEqual(normalizeColor('red', DEFAULT_NAME_COLOR), DEFAULT_NAME_COLOR)
    assert.strictEqual(normalizeColor('rgb(0,0,0)', DEFAULT_NAME_COLOR), DEFAULT_NAME_COLOR)
    assert.strictEqual(normalizeColor(null, DEFAULT_NAME_COLOR), DEFAULT_NAME_COLOR)
    assert.strictEqual(normalizeColor(123, DEFAULT_NAME_COLOR), DEFAULT_NAME_COLOR)
  })

  it('converts rpx to preview px by halving', () => {
    assert.strictEqual(rpxToPreviewPx(1040), 520)
    assert.strictEqual(rpxToPreviewPx(92), 46)
    assert.strictEqual(rpxToPreviewPx(641), 321)
  })

  it('sample profile is local and not persisted in default property', () => {
    const property = createDefaultPartnerProfileHeroProperty()
    assert.strictEqual('name' in property, false)
    assert.strictEqual('age' in property, false)
    assert.strictEqual('mainImage' in property, false)
    assert.strictEqual('avatarImage' in property, false)
    assert.strictEqual(SAMPLE_PROFILE.name, '林小鹿')
    assert.strictEqual(SAMPLE_PROFILE.age, 26)
  })

  it('unknown fields round-trip through plain object clone', () => {
    const property = createDefaultPartnerProfileHeroProperty()
    const withExtra = { ...property, futureField: 'any', another: 123 } as any
    const reloaded = JSON.parse(JSON.stringify(withExtra))
    assert.strictEqual(reloaded.height, DEFAULT_HEIGHT)
    assert.strictEqual(reloaded.futureField, 'any')
    assert.strictEqual(reloaded.another, 123)
  })

  it('serialized component keeps id before property', () => {
    const component = {
      id: 'PartnerProfileHero',
      property: createDefaultPartnerProfileHeroProperty(),
    }
    const serialized = JSON.stringify(component)
    const idIndex = serialized.indexOf('"id"')
    const propertyIndex = serialized.indexOf('"property"')
    assert.ok(idIndex >= 0)
    assert.ok(propertyIndex >= 0)
    assert.ok(idIndex < propertyIndex)
  })
})
