import { describe, it } from 'node:test'
import assert from 'node:assert'
import {
  createDefaultFloatingInteractionButtons,
  createDefaultPartnerRecommendation,
  normalizeFloatingInteractionIconUrl,
  parseFloatingInteractionButtons,
  parsePartnerRecommendation,
  type PartnerRecommendationConfig
} from '../src/components/DiyEditor/components/mobile/PageConfig/partnerRecommendation'

function getFilters(result: PartnerRecommendationConfig) {
  return result.filters!
}

describe('PageConfig partner recommendation pure functions', () => {
  it('parse undefined as disabled with all filters defaulting to true', () => {
    const result = parsePartnerRecommendation(undefined)
    assert.strictEqual(result.enabled, false)
    assert.strictEqual(getFilters(result).realVerifiedOnly, true)
    assert.strictEqual(getFilters(result).backgroundImageRequired, true)
    assert.strictEqual(getFilters(result).oppositeSexOnly, true)
  })

  it('parse missing fields as true safe defaults', () => {
    const result = parsePartnerRecommendation({ enabled: true })
    assert.strictEqual(result.enabled, true)
    assert.strictEqual(getFilters(result).realVerifiedOnly, true)
    assert.strictEqual(getFilters(result).backgroundImageRequired, true)
    assert.strictEqual(getFilters(result).oppositeSexOnly, true)
  })

  it('preserve explicitly saved false values', () => {
    const result = parsePartnerRecommendation({
      enabled: true,
      filters: {
        realVerifiedOnly: false,
        backgroundImageRequired: false,
        oppositeSexOnly: false
      }
    })
    assert.strictEqual(result.enabled, true)
    assert.strictEqual(getFilters(result).realVerifiedOnly, false)
    assert.strictEqual(getFilters(result).backgroundImageRequired, false)
    assert.strictEqual(getFilters(result).oppositeSexOnly, false)
  })

  it('ignore invalid types and use true as default', () => {
    const result = parsePartnerRecommendation({
      enabled: true,
      filters: {
        realVerifiedOnly: 'false' as any,
        backgroundImageRequired: null as any,
        oppositeSexOnly: 1 as any
      }
    })
    assert.strictEqual(result.enabled, true)
    assert.strictEqual(getFilters(result).realVerifiedOnly, true)
    assert.strictEqual(getFilters(result).backgroundImageRequired, true)
    assert.strictEqual(getFilters(result).oppositeSexOnly, true)
  })

  it('treat only strict boolean true as enabled', () => {
    assert.strictEqual(parsePartnerRecommendation({ enabled: true }).enabled, true)
    assert.strictEqual(parsePartnerRecommendation({ enabled: 'true' as any }).enabled, false)
    assert.strictEqual(parsePartnerRecommendation({ enabled: 1 as any }).enabled, false)
    assert.strictEqual(parsePartnerRecommendation({ enabled: false }).enabled, false)
  })

  it('ignore unknown future fields in parse output', () => {
    const raw = {
      enabled: true,
      filters: {
        realVerifiedOnly: true,
        futureField: 'any',
        another: 123
      }
    } as any
    const result = parsePartnerRecommendation(raw)
    assert.strictEqual(result.enabled, true)
    assert.strictEqual(getFilters(result).realVerifiedOnly, true)
    assert.ok(!('futureField' in getFilters(result)))
    assert.ok(!('another' in getFilters(result)))
  })

  it('createDefaultPartnerRecommendation returns enabled with all true filters', () => {
    const result = createDefaultPartnerRecommendation()
    assert.strictEqual(result.enabled, true)
    assert.strictEqual(getFilters(result).realVerifiedOnly, true)
    assert.strictEqual(getFilters(result).backgroundImageRequired, true)
    assert.strictEqual(getFilters(result).oppositeSexOnly, true)
  })

  it('round-trip preserves unknown page fields and known filter values', () => {
    const original: PartnerRecommendationConfig = {
      enabled: true,
      filters: {
        realVerifiedOnly: false,
        backgroundImageRequired: true,
        oppositeSexOnly: false
      }
    }
    const parsed = parsePartnerRecommendation(original)
    assert.strictEqual(parsed.enabled, true)
    assert.strictEqual(getFilters(parsed).realVerifiedOnly, false)
    assert.strictEqual(getFilters(parsed).backgroundImageRequired, true)
    assert.strictEqual(getFilters(parsed).oppositeSexOnly, false)
  })

  it('strictly parses the page-level floating interaction configuration', () => {
    const parsed = parseFloatingInteractionButtons({
      enabled: true,
      shareIconUrl: ' https://cdn.example.com/share.png ',
      greetingIconUrl: '/static/greeting.png',
      followIconUrl: 'javascript:alert(1)',
      futureField: 'preserved by the original JSON, ignored by the parser'
    })
    assert.deepStrictEqual(parsed, {
      enabled: true,
      shareIconUrl: 'https://cdn.example.com/share.png',
      greetingIconUrl: '/static/greeting.png',
      followIconUrl: ''
    })
    assert.strictEqual(normalizeFloatingInteractionIconUrl('ftp://cdn.example.com/a.png'), '')
    assert.strictEqual(normalizeFloatingInteractionIconUrl(`https://x/${'a'.repeat(2048)}`), '')
  })

  it('uses disabled, empty-icon defaults until the administrator enables the entry', () => {
    assert.deepStrictEqual(createDefaultFloatingInteractionButtons(), {
      enabled: false,
      shareIconUrl: '',
      greetingIconUrl: '',
      followIconUrl: ''
    })
  })
})
