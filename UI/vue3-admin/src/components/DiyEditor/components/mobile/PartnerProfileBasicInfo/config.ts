import type { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 人物基本资料属性 */
export interface PartnerProfileBasicInfoProperty {
  showCity: boolean
  showJob: boolean
  showHeight: boolean
  showWeight: boolean
  showIncome: boolean
  showEducation: boolean
  showHouseStatus: boolean
  showCarStatus: boolean
  style: ComponentStyle
}

/** 基本资料字段定义：顺序、标签和开关键均由代码固定持有 */
export const BASIC_INFO_FIELDS = [
  { toggleKey: 'showCity', label: '现居地区' },
  { toggleKey: 'showJob', label: '工作' },
  { toggleKey: 'showHeight', label: '身高' },
  { toggleKey: 'showWeight', label: '体重' },
  { toggleKey: 'showIncome', label: '月收入' },
  { toggleKey: 'showEducation', label: '学历' },
  { toggleKey: 'showHouseStatus', label: '住房' },
  { toggleKey: 'showCarStatus', label: '车辆' }
] as const

export type PartnerProfileBasicInfoToggleKey = (typeof BASIC_INFO_FIELDS)[number]['toggleKey']

export interface PartnerProfileBasicInfoPreviewItem {
  key: PartnerProfileBasicInfoToggleKey
  label: string
  value: string
}

/** 匿名静态样例：仅用于管理端预览，不进入 property 或保存 JSON */
export const SAMPLE_BASIC_INFO_VALUES: Record<PartnerProfileBasicInfoToggleKey, string> = {
  showCity: '成都市',
  showJob: '产品设计师',
  showHeight: '165cm',
  showWeight: '52kg',
  showIncome: '15k-20k',
  showEducation: '本科',
  showHouseStatus: '已购房',
  showCarStatus: '已购车'
}

/** 仅严格布尔值 false 关闭；缺失、null 或错误类型使用安全默认 true */
export function isBasicInfoFieldVisible(value: unknown): boolean {
  return value !== false
}

/** 根据开关构造管理端匿名预览列表 */
export function buildPreviewBasicInfoItems(
  property: Partial<PartnerProfileBasicInfoProperty> | null | undefined
): PartnerProfileBasicInfoPreviewItem[] {
  const source = property ?? {}
  return BASIC_INFO_FIELDS.filter((field) => isBasicInfoFieldVisible(source[field.toggleKey])).map(
    (field) => ({
      key: field.toggleKey,
      label: field.label,
      value: SAMPLE_BASIC_INFO_VALUES[field.toggleKey]
    })
  )
}

/** 创建组件默认属性 */
export function createDefaultPartnerProfileBasicInfoProperty(): PartnerProfileBasicInfoProperty {
  return {
    showCity: true,
    showJob: true,
    showHeight: true,
    showWeight: true,
    showIncome: true,
    showEducation: true,
    showHouseStatus: true,
    showCarStatus: true,
    style: {
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
    } as ComponentStyle
  }
}

export const component = {
  id: 'PartnerProfileBasicInfo',
  name: '基本资料',
  icon: 'ep:document',
  property: createDefaultPartnerProfileBasicInfoProperty()
} as DiyComponent<PartnerProfileBasicInfoProperty>
