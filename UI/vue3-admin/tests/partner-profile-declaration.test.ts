import { describe, it } from 'node:test'
import assert from 'node:assert'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const configModule = await import(
  new URL(
    '../src/components/DiyEditor/components/mobile/PartnerProfileDeclaration/config.ts',
    import.meta.url
  ).href
)
const {
  createDefaultPartnerProfileDeclarationProperty,
  DEFAULT_CONTENT_BACKGROUND_COLOR,
  DEFAULT_CONTENT_COLOR,
  DEFAULT_CONTENT_RADIUS,
  DEFAULT_TITLE,
  DEFAULT_TITLE_COLOR,
  normalizeColor,
  normalizeContentRadius,
  normalizeTitle,
  rpxToPreviewPx,
  SAMPLE_DECLARATION,
  TITLE_MAX_LENGTH
} = configModule

const root = dirname(fileURLToPath(import.meta.url))
const read = (relativePath: string) => readFileSync(join(root, relativePath), 'utf-8')

describe('PartnerProfileDeclaration admin contract', () => {
  it('creates complete defaults with a transparent zero-spacing container style', () => {
    const property = createDefaultPartnerProfileDeclarationProperty()
    assert.strictEqual(property.title, DEFAULT_TITLE)
    assert.strictEqual(property.titleColor, DEFAULT_TITLE_COLOR)
    assert.strictEqual(property.contentColor, DEFAULT_CONTENT_COLOR)
    assert.strictEqual(property.contentBackgroundColor, DEFAULT_CONTENT_BACKGROUND_COLOR)
    assert.strictEqual(property.contentRadius, DEFAULT_CONTENT_RADIUS)

    assert.deepStrictEqual(property.style, {
      bgType: 'color',
      bgColor: '',
      bgImg: '',
      margin: 0,
      marginTop: 0,
      marginRight: 0,
      marginBottom: 0,
      marginLeft: 0,
      padding: 0,
      paddingTop: 0,
      paddingRight: 0,
      paddingBottom: 0,
      paddingLeft: 0,
      borderRadius: 0,
      borderTopLeftRadius: 0,
      borderTopRightRadius: 0,
      borderBottomRightRadius: 0,
      borderBottomLeftRadius: 0
    })
  })

  it('normalizes title whitespace, invalid values, and Unicode length', () => {
    assert.strictEqual(normalizeTitle('  真诚交友  '), '真诚交友')
    assert.strictEqual(normalizeTitle(''), DEFAULT_TITLE)
    assert.strictEqual(normalizeTitle('   '), DEFAULT_TITLE)
    assert.strictEqual(normalizeTitle(null), DEFAULT_TITLE)
    assert.strictEqual(normalizeTitle(123), DEFAULT_TITLE)
    assert.strictEqual(
      Array.from(normalizeTitle('一二三四五六七八九十十一十二十三')).length,
      TITLE_MAX_LENGTH
    )
  })

  it('accepts only #RRGGBB colors and normalizes letter case', () => {
    assert.strictEqual(normalizeColor('#abcdef', DEFAULT_TITLE_COLOR), '#ABCDEF')
    assert.strictEqual(normalizeColor('#5E4A46', DEFAULT_CONTENT_COLOR), '#5E4A46')
    assert.strictEqual(normalizeColor('red', DEFAULT_TITLE_COLOR), DEFAULT_TITLE_COLOR)
    assert.strictEqual(normalizeColor('#12345', DEFAULT_TITLE_COLOR), DEFAULT_TITLE_COLOR)
    assert.strictEqual(normalizeColor(null, DEFAULT_TITLE_COLOR), DEFAULT_TITLE_COLOR)
  })

  it('normalizes content radius and preview unit conversion', () => {
    assert.strictEqual(normalizeContentRadius(0), 0)
    assert.strictEqual(normalizeContentRadius(80), 80)
    assert.strictEqual(normalizeContentRadius(39.6), 40)
    assert.strictEqual(normalizeContentRadius(-1), DEFAULT_CONTENT_RADIUS)
    assert.strictEqual(normalizeContentRadius(81), DEFAULT_CONTENT_RADIUS)
    assert.strictEqual(normalizeContentRadius(null), DEFAULT_CONTENT_RADIUS)
    assert.strictEqual(normalizeContentRadius(''), DEFAULT_CONTENT_RADIUS)
    assert.strictEqual(normalizeContentRadius(false), DEFAULT_CONTENT_RADIUS)
    assert.strictEqual(rpxToPreviewPx(40), 20)
    assert.strictEqual(rpxToPreviewPx(41), 21)
  })

  it('keeps the static sample outside persisted property', () => {
    const property = createDefaultPartnerProfileDeclarationProperty() as unknown as Record<
      string,
      unknown
    >
    assert.ok(SAMPLE_DECLARATION.length > 0)
    assert.strictEqual('bio' in property, false)
    assert.strictEqual('sampleDeclaration' in property, false)
    assert.strictEqual('partnerId' in property, false)
    assert.strictEqual('api' in property, false)
    assert.strictEqual('route' in property, false)
  })

  it('round-trips unknown future fields without changing the component envelope', () => {
    const property = {
      ...createDefaultPartnerProfileDeclarationProperty(),
      futureField: { enabled: true }
    }
    const component = { id: 'PartnerProfileDeclaration', property }
    const reloaded = JSON.parse(JSON.stringify(component))
    assert.strictEqual(reloaded.id, 'PartnerProfileDeclaration')
    assert.strictEqual(reloaded.property.title, DEFAULT_TITLE)
    assert.deepStrictEqual(reloaded.property.futureField, { enabled: true })
    assert.deepStrictEqual(Object.keys(reloaded).sort(), ['id', 'property'])
  })

  it('uses automatic discovery, the existing social group, and a v-model property panel', () => {
    const library = read('../src/components/DiyEditor/util.ts')
    const registry = read('../src/components/DiyEditor/components/mobile/index.ts')
    const preview = read(
      '../src/components/DiyEditor/components/mobile/PartnerProfileDeclaration/index.vue'
    )
    const propertyPanel = read(
      '../src/components/DiyEditor/components/mobile/PartnerProfileDeclaration/property.vue'
    )

    assert.match(library, /name:\s*'社交组件'[\s\S]*'PartnerProfileDeclaration'/)
    assert.match(registry, /import\.meta\.glob\('\.\/\*\/config\.ts'/)
    assert.match(registry, /import\.meta\.glob\('\.\/\*\/\*\.vue'/)
    assert.match(propertyPanel, /useVModel/)
    assert.match(propertyPanel, /ComponentContainerProperty/)
    assert.match(propertyPanel, /TITLE_MAX_LENGTH/)
    assert.doesNotMatch(preview, /fetch\(|axios|request\(|PartnerApi|MarriageApi/)
  })
})
