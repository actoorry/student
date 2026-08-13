import { readFileSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, it } from 'node:test'
import assert from 'node:assert'

const __filename = fileURLToPath(import.meta.url)
const root = dirname(__filename)

const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8')

describe('PageConfig partner recommendation panel and editor contract', () => {
  const editor = read('../src/components/DiyEditor/index.vue')
  const propertyPanel = read(
    '../src/components/DiyEditor/components/mobile/PageConfig/property.vue'
  )
  const pageConfig = read('../src/components/DiyEditor/components/mobile/PageConfig/config.ts')
  const partnerRec = read(
    '../src/components/DiyEditor/components/mobile/PageConfig/partnerRecommendation.ts'
  )
  const templateDecorate = read('../src/views/sales/promotion/diy/template/decorate.vue')
  const pageDecorate = read('../src/views/sales/promotion/diy/page/decorate.vue')

  it('DiyEditor exposes a pageContext prop for home / user / standalone distinction', () => {
    assert.match(editor, /pageContext.*propTypes\.string\.def\('standalone'\)/)
  })

  it('DiyEditor passes pageContext to the selected property panel', () => {
    assert.match(editor, /:page-context="props\.pageContext"/)
  })

  it('PageConfigProperty type includes optional partnerRecommendation', () => {
    assert.match(pageConfig, /partnerRecommendation\?:\s*PartnerRecommendationConfig/)
  })

  it('PageConfig property panel only shows recommendation switch on home context', () => {
    assert.match(propertyPanel, /props\.pageContext\s*===\s*'home'/)
    assert.match(propertyPanel, /v-if="isHomePage"/)
  })

  it('PageConfig property panel uses safe parser and default creator', () => {
    assert.match(propertyPanel, /parsePartnerRecommendation/)
    assert.match(propertyPanel, /createDefaultPartnerRecommendation/)
    assert.match(propertyPanel, /parseFloatingInteractionButtons/)
    assert.match(propertyPanel, /createDefaultFloatingInteractionButtons/)
  })

  it('PageConfig explains that recommendation mode switches the whole decorated partner card', () => {
    assert.match(propertyPanel, /整页人物卡片模式/)
    assert.match(propertyPanel, /左右滑动切换人物的整套装修组件/)
    assert.match(propertyPanel, /不会自动添加人物展示组件/)
  })

  it('partnerRecommendation parser has strict boolean check and safe defaults', () => {
    assert.match(partnerRec, /value\s*===\s*true/)
    assert.match(partnerRec, /typeof\s+value\s*===\s*'boolean'/)
    assert.match(partnerRec, /:\s*true\s*}/)
  })

  it('template decorate passes pageContext based on selected item', () => {
    assert.match(templateDecorate, /:page-context="currentPageContext"/)
    assert.match(
      templateDecorate,
      /if\s*\(selectedTemplateItem\.value\s*===\s*1\)\s*return\s*'home'/
    )
    assert.match(
      templateDecorate,
      /if\s*\(selectedTemplateItem\.value\s*===\s*2\)\s*return\s*'user'/
    )
  })

  it('standalone page decorate passes pageContext standalone', () => {
    assert.match(pageDecorate, /page-context="standalone"/)
  })

  it('DiyEditor serialization keeps only id and property for components', () => {
    assert.match(
      editor,
      /return\s*\{\s*id:\s*component\.id,\s*property:\s*component\.property\s*\}/
    )
  })

  it('keeps floating interaction settings on the home recommendation panel only', () => {
    assert.match(propertyPanel, /floatingInteractionButtonsEnabled/)
    assert.match(propertyPanel, /分享图标/)
    assert.match(propertyPanel, /打招呼图标/)
    assert.match(propertyPanel, /关注图标/)
    assert.match(partnerRec, /FLOATING_INTERACTION_ICON_URL_MAX_LENGTH\s*=\s*2048/)
  })

  it('keeps the retired interaction ID editable only as retained historical data', () => {
    assert.match(editor, /RETIRED_EDITOR_COMPONENT_IDS/)
    assert.match(editor, /'PartnerFloatingInteractionButtons'/)
    assert.match(editor, /createRetiredDiyComponent/)
  })
})
