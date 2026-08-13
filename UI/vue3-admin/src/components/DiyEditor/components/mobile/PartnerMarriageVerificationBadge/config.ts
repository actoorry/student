import type { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 婚恋认证标志属性 */
export interface PartnerMarriageVerificationBadgeProperty {
  // 卡片背景色
  badgeBackgroundColor: string
  // 强调色（图标/对勾）
  accentColor: string
  // 标题颜色
  titleColor: string
  // 说明颜色
  descriptionColor: string
  // 通用容器样式
  style: ComponentStyle
}

/** 默认卡片背景色 */
export const DEFAULT_BADGE_BACKGROUND_COLOR = '#FFF9F0'
/** 默认强调色 */
export const DEFAULT_ACCENT_COLOR = '#D8A33E'
/** 默认标题颜色 */
export const DEFAULT_TITLE_COLOR = '#4F3433'
/** 默认说明颜色 */
export const DEFAULT_DESCRIPTION_COLOR = '#776361'

/** 创建组件默认属性 */
export function createDefaultPartnerMarriageVerificationBadgeProperty(): PartnerMarriageVerificationBadgeProperty {
  return {
    badgeBackgroundColor: DEFAULT_BADGE_BACKGROUND_COLOR,
    accentColor: DEFAULT_ACCENT_COLOR,
    titleColor: DEFAULT_TITLE_COLOR,
    descriptionColor: DEFAULT_DESCRIPTION_COLOR,
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
      borderBottomLeftRadius: 0,
    } as ComponentStyle,
  }
}

/** 将任意值规范化为 #RRGGBB 颜色，非法值回退到 defaultColor */
export function normalizeColor(value: unknown, defaultColor: string): string {
  if (typeof value !== 'string') {
    return defaultColor
  }
  const trimmed = value.trim()
  if (/^#[0-9A-Fa-f]{6}$/.test(trimmed)) {
    return trimmed.toUpperCase()
  }
  return defaultColor
}

/** 管理端 375px 画布按 1px = 2rpx 换算为预览像素 */
export function rpxToPreviewPx(rpx: number): number {
  return Math.round(rpx / 2)
}

// 定义组件
export const component = {
  id: 'PartnerMarriageVerificationBadge',
  name: '婚恋认证标志',
  icon: 'fluent-emoji:ring',
  property: createDefaultPartnerMarriageVerificationBadgeProperty(),
} as DiyComponent<PartnerMarriageVerificationBadgeProperty>
