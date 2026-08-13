import type { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 择偶宣言属性 */
export interface PartnerProfileDeclarationProperty {
  // 区块标题
  title: string
  // 标题颜色
  titleColor: string
  // 正文颜色
  contentColor: string
  // 正文背景色
  contentBackgroundColor: string
  // 正文圆角（rpx）
  contentRadius: number
  // 通用容器样式
  style: ComponentStyle
}

/** 默认区块标题 */
export const DEFAULT_TITLE = '择偶宣言'
/** 标题最大 Unicode 字符数 */
export const TITLE_MAX_LENGTH = 12
/** 默认标题颜色 */
export const DEFAULT_TITLE_COLOR = '#8B7670'
/** 默认正文颜色 */
export const DEFAULT_CONTENT_COLOR = '#5E4A46'
/** 默认正文背景色 */
export const DEFAULT_CONTENT_BACKGROUND_COLOR = '#FFF9F6'
/** 默认正文圆角（rpx） */
export const DEFAULT_CONTENT_RADIUS = 40
/** 正文圆角有效范围（rpx） */
export const CONTENT_RADIUS_RANGE = { min: 0, max: 80 } as const

/** 创建组件默认属性 */
export function createDefaultPartnerProfileDeclarationProperty(): PartnerProfileDeclarationProperty {
  return {
    title: DEFAULT_TITLE,
    titleColor: DEFAULT_TITLE_COLOR,
    contentColor: DEFAULT_CONTENT_COLOR,
    contentBackgroundColor: DEFAULT_CONTENT_BACKGROUND_COLOR,
    contentRadius: DEFAULT_CONTENT_RADIUS,
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

/** 将任意值规范化为有效正文圆角（rpx） */
export function normalizeContentRadius(value: unknown): number {
  if (value === null || value === '' || typeof value === 'boolean') {
    return DEFAULT_CONTENT_RADIUS
  }
  const num = Number(value)
  if (
    !Number.isFinite(num) ||
    num < CONTENT_RADIUS_RANGE.min ||
    num > CONTENT_RADIUS_RANGE.max
  ) {
    return DEFAULT_CONTENT_RADIUS
  }
  return Math.round(num)
}

/** 管理端 375px 画布按 1px = 2rpx 换算为预览像素 */
export function rpxToPreviewPx(rpx: number): number {
  return Math.round(rpx / 2)
}

/** 静态样例：只用于管理端预览，不进入 property 或保存 JSON */
export const SAMPLE_DECLARATION = '热爱生活，也期待遇见愿意认真了解彼此、共同成长的你。'

// 定义组件
export const component = {
  id: 'PartnerProfileDeclaration',
  name: '择偶宣言',
  icon: 'fluent-emoji:love-letter',
  property: createDefaultPartnerProfileDeclarationProperty()
} as DiyComponent<PartnerProfileDeclarationProperty>
