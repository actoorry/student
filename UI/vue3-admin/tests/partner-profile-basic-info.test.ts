import { describe, it } from 'node:test'
import assert from 'node:assert'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import {
  BASIC_INFO_FIELDS,
  buildPreviewBasicInfoItems,
  component,
  createDefaultPartnerProfileBasicInfoProperty,
  isBasicInfoFieldVisible,
  SAMPLE_BASIC_INFO_VALUES
} from '../src/components/DiyEditor/components/mobile/PartnerProfileBasicInfo/config.ts'

const currentDir = dirname(fileURLToPath(import.meta.url))
const read = (relativePath: string) => readFileSync(join(currentDir, relativePath), 'utf-8')

describe('PartnerProfileBasicInfo admin contract', () => {
  it('declares the stable component identity and eight fields in fixed order', () => {
    assert.strictEqual(component.id, 'PartnerProfileBasicInfo')
    assert.strictEqual(component.name, '基本资料')
    assert.deepStrictEqual(
      BASIC_INFO_FIELDS.map((field) => field.label),
      ['现居地区', '工作', '身高', '体重', '月收入', '学历', '住房', '车辆']
    )
  })

  it('defaults all eight visibility switches to true', () => {
    const property = createDefaultPartnerProfileBasicInfoProperty()
    for (const field of BASIC_INFO_FIELDS) {
      assert.strictEqual(property[field.toggleKey], true)
    }
  })

  it('contains a complete transparent zero-spacing common style', () => {
    const { style } = createDefaultPartnerProfileBasicInfoProperty()
    assert.strictEqual(style.bgType, 'color')
    assert.strictEqual(style.bgColor, '')
    assert.strictEqual(style.bgImg, '')
    assert.strictEqual(style.margin, 0)
    assert.strictEqual(style.marginTop, 0)
    assert.strictEqual(style.marginRight, 0)
    assert.strictEqual(style.marginBottom, 0)
    assert.strictEqual(style.marginLeft, 0)
    assert.strictEqual(style.padding, 0)
    assert.strictEqual(style.paddingTop, 0)
    assert.strictEqual(style.paddingRight, 0)
    assert.strictEqual(style.paddingBottom, 0)
    assert.strictEqual(style.paddingLeft, 0)
    assert.strictEqual(style.borderRadius, 0)
    assert.strictEqual(style.borderTopLeftRadius, 0)
    assert.strictEqual(style.borderTopRightRadius, 0)
    assert.strictEqual(style.borderBottomRightRadius, 0)
    assert.strictEqual(style.borderBottomLeftRadius, 0)
  })

  it('only strict false hides a field', () => {
    assert.strictEqual(isBasicInfoFieldVisible(false), false)
    assert.strictEqual(isBasicInfoFieldVisible(true), true)
    assert.strictEqual(isBasicInfoFieldVisible(undefined), true)
    assert.strictEqual(isBasicInfoFieldVisible(null), true)
    assert.strictEqual(isBasicInfoFieldVisible(0), true)
    assert.strictEqual(isBasicInfoFieldVisible('false'), true)
    assert.strictEqual(isBasicInfoFieldVisible({}), true)
  })

  it('applies independent switches without changing the remaining order', () => {
    const property = createDefaultPartnerProfileBasicInfoProperty()
    property.showJob = false
    property.showIncome = false
    property.showCarStatus = false

    const items = buildPreviewBasicInfoItems(property)
    assert.deepStrictEqual(
      items.map((item) => item.label),
      ['现居地区', '身高', '体重', '学历', '住房']
    )
  })

  it('uses safe defaults for missing or polluted visibility values', () => {
    const items = buildPreviewBasicInfoItems({
      showCity: undefined,
      showJob: null as unknown as boolean,
      showHeight: 'false' as unknown as boolean,
      showWeight: false
    })
    assert.deepStrictEqual(
      items.map((item) => item.label),
      ['现居地区', '工作', '身高', '月收入', '学历', '住房', '车辆']
    )
  })

  it('returns no preview items when all fields are disabled', () => {
    const property = createDefaultPartnerProfileBasicInfoProperty()
    for (const field of BASIC_INFO_FIELDS) {
      property[field.toggleKey] = false
    }
    assert.deepStrictEqual(buildPreviewBasicInfoItems(property), [])
  })

  it('keeps anonymous sample values out of persisted property', () => {
    const property = createDefaultPartnerProfileBasicInfoProperty()
    for (const profileKey of [
      'city',
      'job',
      'height',
      'weight',
      'income',
      'education',
      'houseStatus',
      'carStatus',
      'maritalStatus',
      'partnerId'
    ]) {
      assert.strictEqual(profileKey in property, false)
    }
    assert.strictEqual(SAMPLE_BASIC_INFO_VALUES.showCity, '成都市')
  })

  it('preserves unknown future fields through component JSON round-trip', () => {
    const componentValue = {
      id: component.id,
      property: {
        ...createDefaultPartnerProfileBasicInfoProperty(),
        futureField: '保留'
      }
    }
    const reloaded = JSON.parse(JSON.stringify(componentValue))
    assert.strictEqual(reloaded.id, 'PartnerProfileBasicInfo')
    assert.strictEqual(reloaded.property.showCity, true)
    assert.strictEqual(reloaded.property.futureField, '保留')
    assert.deepStrictEqual(Object.keys(reloaded), ['id', 'property'])
  })
})

describe('PartnerProfileBasicInfo admin integration guards', () => {
  const preview = read(
    '../src/components/DiyEditor/components/mobile/PartnerProfileBasicInfo/index.vue'
  )
  const propertyPanel = read(
    '../src/components/DiyEditor/components/mobile/PartnerProfileBasicInfo/property.vue'
  )
  const libraries = read('../src/components/DiyEditor/util.ts')

  it('registers in the existing social component library', () => {
    assert.match(libraries, /name:\s*'社交组件'[\s\S]*'PartnerProfileBasicInfo'/)
  })

  it('renders a non-persisted editor hint when every switch is off', () => {
    assert.match(preview, /v-else class="partner-profile-basic-info__empty"/)
    assert.match(preview, /已关闭全部资料项/)
  })

  it('offers one right-panel switch per fixed field', () => {
    assert.match(propertyPanel, /v-for="field in BASIC_INFO_FIELDS"/)
    assert.match(propertyPanel, /<el-switch/)
    assert.match(propertyPanel, /setFieldVisibility/)
  })

  it('does not persist profile values, data sources or actions', () => {
    const config = read(
      '../src/components/DiyEditor/components/mobile/PartnerProfileBasicInfo/config.ts'
    )
    const defaultFactory = config.slice(
      config.indexOf('export function createDefaultPartnerProfileBasicInfoProperty'),
      config.indexOf('export const component')
    )
    assert.doesNotMatch(defaultFactory, /partnerId|maritalStatus|api|route|action|script/)
  })
})
