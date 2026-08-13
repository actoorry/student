import assert from 'node:assert/strict'
import { describe, it } from 'node:test'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import {
  DEFAULTS, formatRelativeTime, identityKey, normalizeMoment, normalizeProperty,
  truncateUnicode, uniqueImages,
} from '../sheep/components/s-partner-profile-moment/partnerProfileMoment.js'

const root = dirname(fileURLToPath(import.meta.url))
const read = (path) => readFileSync(join(root, path), 'utf8')

describe('PartnerProfileMoment contract helpers', () => {
  it('normalizes all persisted visual fields without dropping future fields', () => {
    const property = normalizeProperty({ title: '  动态  ', contentMaxLength: 999, maxImages: -1, titleColor: '#abcd', future: true })
    assert.equal(property.title, '动态')
    assert.equal(property.contentMaxLength, DEFAULTS.contentMaxLength)
    assert.equal(property.maxImages, DEFAULTS.maxImages)
    assert.equal(property.titleColor, DEFAULTS.titleColor)
    assert.equal(property.future, true)
  })
  it('uses Unicode-safe truncation and stable valid image candidates', () => {
    assert.equal(truncateUnicode('😀😀😀', 2), '😀😀...')
    assert.deepEqual(uniqueImages([' a ', '', 'a', 'b', 1], 3, (url) => `cdn:${url}`), ['cdn:a', 'cdn:b'])
  })
  it('renders only a valid text or image latest public moment without mutating input', () => {
    const source = { id: 8, content: '文字', imageUrls: ['x'], likeCount: -1, commentCount: '2' }
    const moment = normalizeMoment(source, normalizeProperty({ maxImages: 0 }), (url) => url)
    assert.equal(moment.content, '文字')
    assert.deepEqual(moment.images, [])
    assert.equal(moment.likeCount, 0)
    assert.equal(moment.commentCount, 2)
    assert.deepEqual(source.imageUrls, ['x'])
    assert.equal(normalizeMoment({ id: 0, content: 'x' }, normalizeProperty({}), (url) => url), null)
  })
  it('uses an unambiguous identity and deterministic relative time', () => {
    assert.notEqual(identityKey(1, { id: 2, images: ['a|b'] }), identityKey(1, { id: 2, images: ['a', 'b'] }))
    assert.equal(formatRelativeTime('2026-01-01T00:00:00.000Z', Date.parse('2026-01-01T02:00:00.000Z')), '2小时前')
  })
})

describe('PartnerProfileMoment integration wiring', () => {
  const component = read('../sheep/components/s-partner-profile-moment/s-partner-profile-moment.vue')
  const block = read('../sheep/components/s-block-item/s-block-item.vue')
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js')
  const adminConfig = read('../../vue3-admin/src/components/DiyEditor/components/mobile/PartnerProfileMoment/config.ts')
  const adminProperty = read('../../vue3-admin/src/components/DiyEditor/components/mobile/PartnerProfileMoment/property.vue')
  const pageLibs = read('../../vue3-admin/src/components/DiyEditor/util.ts')
  const partnerPage = read('../pages/partner-dynamics/index.vue')
  const minePage = read('../pages/mine-dynamics/index.vue')
  it('uses context only, generation-bound media failure and fixed navigation', () => {
    assert.match(component, /inject\('partnerRecommendationContext', null\)/)
    assert.match(component, /mediaGeneration/)
    assert.match(component, /partner-dynamics\/index/)
    assert.doesNotMatch(component, /MomentApi|InteractionApi|paramsForTabbar/)
  })
  it('is explicitly dispatched and known while retired IDs remain unchanged', () => {
    assert.match(block, /<s-partner-profile-moment v-if="type === 'PartnerProfileMoment'" :data="data"/)
    assert.match(registry, /'PartnerProfileMoment'/)
    assert.match(registry, /'MarriageRecommendProfileDeck'/)
  })
  it('provides the standard admin three-piece component and social catalogue entry', () => {
    assert.match(adminConfig, /id: 'PartnerProfileMoment'/)
    assert.match(adminConfig, /name: 'TA 的动态'/)
    assert.match(adminProperty, /useVModel/)
    assert.match(pageLibs, /'PartnerProfileMoment'/)
    assert.doesNotMatch(adminConfig.match(/createDefaultPartnerProfileMomentProperty[\s\S]*?\n}/)?.[0] || '', /SAMPLE_MOMENT/)
  })
  it('declares non-TabBar public and authenticated owner pages with separate APIs', () => {
    assert.match(partnerPage, /parsePositiveId\(query\?\.partnerId\)/)
    assert.match(partnerPage, /MomentApi\.getPage/)
    assert.match(partnerPage, /InteractionApi\.recordView/)
    assert.match(minePage, /MomentApi\.getMyPage/)
    assert.match(minePage, /MomentApi\.delete/)
    // 我的动态页不读取 partnerId 路由参数（本人分页）；条目自带的 partnerId 仅用于成员入口
    assert.doesNotMatch(minePage, /parsePositiveId\(query\?\.partnerId\)/)
  })
  it('uses the marriage development tenant that owns the verified moment data', () => {
    const environment = read('../.env')
    assert.match(environment, /^SHOPRO_TENANT_ID=303$/m)
  })
})
