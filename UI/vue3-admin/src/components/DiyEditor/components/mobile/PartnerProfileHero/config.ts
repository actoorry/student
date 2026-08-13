import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 人物主图属性 */
export interface PartnerProfileHeroProperty {
  // 主图高度（rpx）
  height: number
  // 头像尺寸（rpx）
  avatarSize: number
  // 姓名颜色
  nameColor: string
  // 年龄颜色
  ageColor: string
  // 通用容器样式
  style: ComponentStyle
}

/** 默认高度（rpx） */
export const DEFAULT_HEIGHT = 1040
/** 默认头像尺寸（rpx） */
export const DEFAULT_AVATAR_SIZE = 92
/** 默认姓名颜色 */
export const DEFAULT_NAME_COLOR = '#201917'
/** 默认年龄颜色 */
export const DEFAULT_AGE_COLOR = '#6F5A55'

/** 高度有效范围 */
export const HEIGHT_RANGE = { min: 640, max: 1400 } as const
/** 头像尺寸有效范围 */
export const AVATAR_SIZE_RANGE = { min: 64, max: 160 } as const

/** 创建组件默认属性 */
export function createDefaultPartnerProfileHeroProperty(): PartnerProfileHeroProperty {
  return {
    height: DEFAULT_HEIGHT,
    avatarSize: DEFAULT_AVATAR_SIZE,
    nameColor: DEFAULT_NAME_COLOR,
    ageColor: DEFAULT_AGE_COLOR,
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

/** 将任意值规范化为有效高度（rpx） */
export function normalizeHeight(value: unknown): number {
  const num = Number(value)
  if (!Number.isFinite(num) || num < HEIGHT_RANGE.min || num > HEIGHT_RANGE.max) {
    return DEFAULT_HEIGHT
  }
  return Math.round(num)
}

/** 将任意值规范化为有效头像尺寸（rpx） */
export function normalizeAvatarSize(value: unknown): number {
  const num = Number(value)
  if (!Number.isFinite(num) || num < AVATAR_SIZE_RANGE.min || num > AVATAR_SIZE_RANGE.max) {
    return DEFAULT_AVATAR_SIZE
  }
  return Math.round(num)
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

/** 静态样例：仅用于管理端预览，不进入 property 或保存 JSON，也不请求网络真实人物 */
export const SAMPLE_PROFILE = {
  name: '林小鹿',
  age: 26,
  // 管理端预览使用 CSS 渐变和占位头像，避免真实网络请求
  avatarPlaceholder: true,
  mainPlaceholder: true,
} as const

// 定义组件
export const component = {
  id: 'PartnerProfileHero',
  name: '人物主图',
  icon: 'fluent-emoji:woman-and-man-holding-hands-light',
  property: createDefaultPartnerProfileHeroProperty(),
} as DiyComponent<PartnerProfileHeroProperty>
