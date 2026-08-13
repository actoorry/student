import type { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 择偶条件属性 */
export interface PartnerProfilePreferenceProperty {
  // 区块标题
  title: string
  // 标题颜色
  titleColor: string
  // 强调线颜色
  accentColor: string
  // 标签文字颜色
  tagTextColor: string
  // 标签背景颜色
  tagBackgroundColor: string
  // 标签圆角（rpx）
  tagRadius: number
  // 通用容器样式
  style: ComponentStyle
}

/** 默认区块标题 */
export const DEFAULT_TITLE = '择偶条件'
/** 标题最大 Unicode 字符数 */
export const TITLE_MAX_LENGTH = 12
/** 默认标题颜色 */
export const DEFAULT_TITLE_COLOR = '#2D2324'
/** 默认强调线颜色 */
export const DEFAULT_ACCENT_COLOR = '#C84449'
/** 默认标签文字颜色 */
export const DEFAULT_TAG_TEXT_COLOR = '#8F675D'
/** 默认标签背景颜色 */
export const DEFAULT_TAG_BACKGROUND_COLOR = '#FFF2E7'
/** 默认标签圆角（rpx） */
export const DEFAULT_TAG_RADIUS = 32
/** 标签圆角有效范围（rpx） */
export const TAG_RADIUS_RANGE = { min: 0, max: 64 } as const

/** 创建组件默认属性 */
export function createDefaultPartnerProfilePreferenceProperty(): PartnerProfilePreferenceProperty {
  return {
    title: DEFAULT_TITLE,
    titleColor: DEFAULT_TITLE_COLOR,
    accentColor: DEFAULT_ACCENT_COLOR,
    tagTextColor: DEFAULT_TAG_TEXT_COLOR,
    tagBackgroundColor: DEFAULT_TAG_BACKGROUND_COLOR,
    tagRadius: DEFAULT_TAG_RADIUS,
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

/** 规范化区块标题，按 Unicode 字符限制长度 */
export function normalizeTitle(value: unknown): string {
  if (typeof value !== 'string') {
    return DEFAULT_TITLE
  }
  const trimmed = value.trim()
  if (!trimmed) {
    return DEFAULT_TITLE
  }
  return Array.from(trimmed).slice(0, TITLE_MAX_LENGTH).join('')
}

/** 将任意值规范化为 #RRGGBB 颜色 */
export function normalizeColor(value: unknown, defaultColor: string): string {
  if (typeof value !== 'string') {
    return defaultColor
  }
  const trimmed = value.trim()
  return /^#[0-9A-Fa-f]{6}$/.test(trimmed) ? trimmed.toUpperCase() : defaultColor
}

/** 将任意值规范化为有效标签圆角（rpx） */
export function normalizeTagRadius(value: unknown): number {
  if (value === null || value === '' || typeof value === 'boolean') {
    return DEFAULT_TAG_RADIUS
  }
  const num = Number(value)
  if (
    !Number.isFinite(num) ||
    num < TAG_RADIUS_RANGE.min ||
    num > TAG_RADIUS_RANGE.max
  ) {
    return DEFAULT_TAG_RADIUS
  }
  return Math.round(num)
}

/** 管理端 375px 画布按 1px = 2rpx 换算为预览像素 */
export function rpxToPreviewPx(rpx: number): number {
  return Math.round(rpx / 2)
}

/** 匿名静态样例标签：仅用于管理端预览，不进入 property 或保存 JSON */
export const SAMPLE_TAGS: readonly string[] = Object.freeze([
  '未婚',
  '160-175cm',
  '本科',
  '成都',
  '15k-20k',
  '已购房'
])

// 定义组件
export const component = {
  id: 'PartnerProfilePreference',
  name: '择偶条件',
  icon: 'ep:star',
  property: createDefaultPartnerProfilePreferenceProperty()
} as DiyComponent<PartnerProfilePreferenceProperty>
