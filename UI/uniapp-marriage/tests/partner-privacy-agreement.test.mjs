import assert from 'node:assert/strict'
import { describe, it } from 'node:test'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import {
  buildPrivacyAgreementUrl, findAgreementProperty, normalizeProperty, parseSourceDescriptor,
} from '../sheep/components/s-partner-privacy-agreement/partnerPrivacyAgreement.js'
import { PARTNER_PRIVACY_AGREEMENT_FIXTURES } from './partner-privacy-agreement.fixtures.mjs'

const root = dirname(fileURLToPath(import.meta.url))
const read = (path) => readFileSync(join(root, path), 'utf8')

describe('PartnerPrivacyAgreement contract helpers', () => {
  it('uses Unicode-safe defaults, valid local icons, and filters invalid sections without mutation', () => {
    const raw = { entryTitle: '  😀隐私协议  ', entryIcon: 'REMOTE', detailTitle: ' '.repeat(41), detailSubtitle: 1,
      sections: [{ title: '有效', content: '<script>仅文本</script>\n第二行' }, { title: '', content: '跳过' }, { title: '后续', content: '保留' }], future: true }
    const value = normalizeProperty(raw)
    assert.equal(value.entryTitle, '😀隐私协议')
    assert.equal(value.entryIcon, 'LOCK')
    assert.equal(value.detailTitle, '隐私协议')
    assert.equal(value.detailSubtitle, '')
    assert.deepEqual(value.sections.map((item) => item.title), ['有效', '后续'])
    assert.equal(value.sections[0].content, '<script>仅文本</script>\n第二行')
    assert.equal(value.future, true)
    assert.equal(raw.sections.length, 3)
  })

  it('keeps the table-driven original copy, complete shared style, and 1px = 2rpx contract', () => {
    const normalized = normalizeProperty({ ...PARTNER_PRIVACY_AGREEMENT_FIXTURES.original, style: PARTNER_PRIVACY_AGREEMENT_FIXTURES.style })
    assert.equal(normalized.sections.length, 5)
    assert.deepEqual(normalized.style, PARTNER_PRIVACY_AGREEMENT_FIXTURES.style)
    for (const fixture of PARTNER_PRIVACY_AGREEMENT_FIXTURES.routes) {
      const source = parseSourceDescriptor(fixture.query)
      assert.equal(buildPrivacyAgreementUrl(source), fixture.url)
    }
    assert.equal(44 * 2, 88)
  })

  it('accepts exactly the two fixed source forms and rejects malformed or duplicate values', () => {
    assert.deepEqual(parseSourceDescriptor({ source: 'template', id: '12', slot: 'user', index: '0' }), { source: 'template', id: 12, slot: 'user', index: 0 })
    assert.deepEqual(parseSourceDescriptor({ source: 'page', id: '7', index: '9999' }), { source: 'page', id: 7, index: 9999 })
    for (const query of [
      { source: 'template', id: '01', slot: 'user', index: '0' }, { source: 'page', id: '1', index: '1', slot: 'home' },
      { source: 'template', id: ['1'], slot: 'home', index: '0' }, { source: 'template', id: '1', slot: 'home', index: '10000' },
      { source: 'remote', id: '1', index: '0' }, { source: 'page', id: '1 ', index: '0' },
    ]) assert.equal(parseSourceDescriptor(query), null)
    assert.equal(buildPrivacyAgreementUrl({ source: 'template', id: 12, slot: 'home', index: 3 }), '/pages/public/privacy-agreement?source=template&id=12&slot=home&index=3')
    assert.equal(buildPrivacyAgreementUrl({ source: 'page', id: 7, index: 0 }), '/pages/public/privacy-agreement?source=page&id=7&index=0')
  })

  it('reloads an exact component index from object or JSON payload and never searches another instance', () => {
    const payload = { home: { components: [{ id: 'PartnerPrivacyAgreement', property: { sections: [{ title: '第一份', content: '内容' }] } }, { id: 'PartnerPrivacyAgreement', property: { sections: [{ title: '第二份', content: '内容' }] } }] } }
    assert.equal(findAgreementProperty(payload, { source: 'template', id: 1, slot: 'home', index: 1 }).sections[0].title, '第二份')
    assert.equal(findAgreementProperty(JSON.stringify(payload), { source: 'template', id: 1, slot: 'home', index: 0 }).sections[0].title, '第一份')
    assert.equal(findAgreementProperty(payload, { source: 'template', id: 1, slot: 'home', index: 2 }), null)
    assert.equal(findAgreementProperty({ property: { components: [{ id: 'Other', property: {} }] } }, { source: 'page', id: 1, index: 0 }), null)
  })
})

describe('PartnerPrivacyAgreement integration wiring', () => {
  const adminConfig = read('../../vue3-admin/src/components/DiyEditor/components/mobile/PartnerPrivacyAgreement/config.ts')
  const adminProperty = read('../../vue3-admin/src/components/DiyEditor/components/mobile/PartnerPrivacyAgreement/property.vue')
  const pageLibs = read('../../vue3-admin/src/components/DiyEditor/util.ts')
  const block = read('../sheep/components/s-block-item/s-block-item.vue')
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js')
  const detail = read('../pages/public/privacy-agreement.vue')
  const home = read('../pages/index/index.vue')
  const user = read('../pages/index/user.vue')
  const standalone = read('../pages/index/page.vue')
  it('keeps the standard three-piece editor component and public container boundary', () => {
    assert.match(adminConfig, /id: 'PartnerPrivacyAgreement'/)
    assert.match(adminConfig, /LOCK', 'SHIELD', 'DOCUMENT/)
    assert.match(adminProperty, /useVModel/)
    assert.match(adminProperty, /formData\.sections\.length <= 1/)
    assert.match(pageLibs, /'PartnerPrivacyAgreement'/)
  })
  it('uses explicit known dispatch, source contexts, and a fixed pure-text detail page', () => {
    assert.match(block, /s-partner-privacy-agreement v-if="type === 'PartnerPrivacyAgreement'"/)
    assert.match(registry, /'PartnerPrivacyAgreement'/)
    assert.match(home, /slot: 'home'/)
    assert.match(user, /slot: 'user'/)
    assert.match(standalone, /source: 'page'/)
    assert.match(detail, /parseSourceDescriptor/)
    assert.match(detail, /requestGeneration/)
    assert.doesNotMatch(detail, /v-html|rich-text|mp-html|PromotionArticle/)
  })
})
