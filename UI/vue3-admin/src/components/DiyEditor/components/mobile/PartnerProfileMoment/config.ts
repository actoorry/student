import type { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

export interface PartnerProfileMomentProperty {
  title: string
  showAllMoments: boolean
  contentMaxLength: number
  maxImages: number
  titleColor: string
  allMomentsColor: string
  contentColor: string
  metaColor: string
  cardBackgroundColor: string
  cardRadius: number
  style: ComponentStyle
}

export const DEFAULT_TITLE = 'TA 的动态'
export const DEFAULT_TITLE_COLOR = '#2D2324'
export const DEFAULT_ALL_MOMENTS_COLOR = '#C84449'
export const DEFAULT_CONTENT_COLOR = '#4A3937'
export const DEFAULT_META_COLOR = '#7C6660'
export const DEFAULT_CARD_BACKGROUND_COLOR = '#FFF6F2'
export const DEFAULT_CONTENT_MAX_LENGTH = 80
export const DEFAULT_MAX_IMAGES = 3
export const DEFAULT_CARD_RADIUS = 40

export const TITLE_MAX_LENGTH = 12
export const CONTENT_LENGTH_RANGE = { min: 20, max: 200 } as const
export const IMAGE_COUNT_RANGE = { min: 0, max: 3 } as const
export const CARD_RADIUS_RANGE = { min: 0, max: 80 } as const

export function createDefaultPartnerProfileMomentProperty(): PartnerProfileMomentProperty {
  return {
    title: DEFAULT_TITLE,
    showAllMoments: true,
    contentMaxLength: DEFAULT_CONTENT_MAX_LENGTH,
    maxImages: DEFAULT_MAX_IMAGES,
    titleColor: DEFAULT_TITLE_COLOR,
    allMomentsColor: DEFAULT_ALL_MOMENTS_COLOR,
    contentColor: DEFAULT_CONTENT_COLOR,
    metaColor: DEFAULT_META_COLOR,
    cardBackgroundColor: DEFAULT_CARD_BACKGROUND_COLOR,
    cardRadius: DEFAULT_CARD_RADIUS,
    style: {
      bgType: 'color', bgColor: '', bgImg: '',
      margin: 0, marginTop: 0, marginRight: 0, marginBottom: 0, marginLeft: 0,
      padding: 0, paddingTop: 0, paddingRight: 0, paddingBottom: 0, paddingLeft: 0,
      borderRadius: 0, borderTopLeftRadius: 0, borderTopRightRadius: 0,
      borderBottomRightRadius: 0, borderBottomLeftRadius: 0,
    } as ComponentStyle,
  }
}

export function normalizeTitle(value: unknown): string {
  if (typeof value !== 'string') return DEFAULT_TITLE
  const trimmed = value.trim()
  return trimmed && Array.from(trimmed).length <= TITLE_MAX_LENGTH ? trimmed : DEFAULT_TITLE
}

export function normalizeBoolean(value: unknown): boolean {
  return typeof value === 'boolean' ? value : true
}

export function normalizeBoundedNumber(value: unknown, min: number, max: number, defaultValue: number): number {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue >= min && numberValue <= max
    ? Math.round(numberValue) : defaultValue
}

export function normalizeColor(value: unknown, defaultColor: string): string {
  const color = typeof value === 'string' ? value.trim() : ''
  return /^#[0-9A-Fa-f]{6}$/.test(color) ? color.toUpperCase() : defaultColor
}

/** 管理端 375px 画布与 Mall 的换算：1px = 2rpx。 */
export function rpxToPreviewPx(value: number): number {
  return Math.round(value / 2)
}

/** 仅供管理端预览的匿名静态夹具，不进入 property 或保存 JSON。 */
export const SAMPLE_MOMENT = Object.freeze({
  content: '周末和阳光撞了个满怀，把平凡的小日子过成喜欢的样子。',
  images: ['#F3C9C5', '#F3DEBA', '#CFE1D5'],
  publishTime: '2小时前',
  likeCount: 28,
  commentCount: 6,
})

export const component = {
  id: 'PartnerProfileMoment',
  name: 'TA 的动态',
  icon: 'ep:chat-dot-round',
  property: createDefaultPartnerProfileMomentProperty(),
} as DiyComponent<PartnerProfileMomentProperty>
