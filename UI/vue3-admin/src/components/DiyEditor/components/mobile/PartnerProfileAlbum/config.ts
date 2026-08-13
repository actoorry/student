import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 相册组件属性 */
export interface PartnerProfileAlbumProperty {
  /** 标题 */
  title: string
  /** 副标题 */
  subtitle: string
  /** 标题颜色 */
  titleColor: string
  /** 副标题颜色 */
  subtitleColor: string
  /** 图片高度（rpx） */
  imageHeight: number
  /** 图片圆角（rpx） */
  imageRadius: number
  /** 通用容器样式 */
  style: ComponentStyle
}

/** 标题默认值 */
export const DEFAULT_TITLE = '相册'
/** 副标题默认值 */
export const DEFAULT_SUBTITLE = '真实生活照片'
/** 标题色默认值 */
export const DEFAULT_TITLE_COLOR = '#2D2324'
/** 副标题色默认值 */
export const DEFAULT_SUBTITLE_COLOR = '#8B7670'
/** 默认图片高度（rpx） */
export const DEFAULT_IMAGE_HEIGHT = 240
/** 默认图片圆角（rpx） */
export const DEFAULT_IMAGE_RADIUS = 28

/** 图片高度有效范围 */
export const IMAGE_HEIGHT_RANGE = { min: 120, max: 480 } as const
/** 图片圆角有效范围 */
export const IMAGE_RADIUS_RANGE = { min: 0, max: 64 } as const
/** 标题最长字符数 */
export const TITLE_MAX_LENGTH = 12
/** 副标题最长字符数 */
export const SUBTITLE_MAX_LENGTH = 20

/** 创建组件默认属性 */
export function createDefaultPartnerProfileAlbumProperty(): PartnerProfileAlbumProperty {
  return {
    title: DEFAULT_TITLE,
    subtitle: DEFAULT_SUBTITLE,
    titleColor: DEFAULT_TITLE_COLOR,
    subtitleColor: DEFAULT_SUBTITLE_COLOR,
    imageHeight: DEFAULT_IMAGE_HEIGHT,
    imageRadius: DEFAULT_IMAGE_RADIUS,
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

/** 将任意值规范化为有效标题（1..12 个 Unicode 字符） */
export function normalizeTitle(value: unknown): string {
  if (typeof value !== 'string') return DEFAULT_TITLE
  const trimmed = value.trim()
  if (trimmed.length === 0 || Array.from(trimmed).length > TITLE_MAX_LENGTH) return DEFAULT_TITLE
  return trimmed
}

/** 将任意值规范化为有效副标题（0..20 个 Unicode 字符，空串表示隐藏） */
export function normalizeSubtitle(value: unknown): string {
  if (typeof value !== 'string') return DEFAULT_SUBTITLE
  const trimmed = value.trim()
  if (Array.from(trimmed).length > SUBTITLE_MAX_LENGTH) return DEFAULT_SUBTITLE
  return trimmed
}

/** 将任意值规范化为 #RRGGBB 颜色，非法值回退到 defaultColor */
export function normalizeColor(value: unknown, defaultColor: string): string {
  if (typeof value !== 'string') return defaultColor
  const trimmed = value.trim()
  if (/^#[0-9A-Fa-f]{6}$/.test(trimmed)) return trimmed.toUpperCase()
  return defaultColor
}

/** 将任意值规范化为有效图片高度（120..480rpx 取整） */
export function normalizeImageHeight(value: unknown): number {
  const num = Number(value)
  if (!Number.isFinite(num) || num < IMAGE_HEIGHT_RANGE.min || num > IMAGE_HEIGHT_RANGE.max) {
    return DEFAULT_IMAGE_HEIGHT
  }
  return Math.round(num)
}

/** 将任意值规范化为有效图片圆角（0..64rpx 取整） */
export function normalizeImageRadius(value: unknown): number {
  const num = Number(value)
  if (!Number.isFinite(num) || num < IMAGE_RADIUS_RANGE.min || num > IMAGE_RADIUS_RANGE.max) {
    return DEFAULT_IMAGE_RADIUS
  }
  return Math.round(num)
}

/** 管理端 375px 画布按 1px = 2rpx 换算为预览像素 */
export function rpxToPreviewPx(rpx: number): number {
  return Math.round(rpx / 2)
}

/**
 * 管理端匿名静态图片夹具：仅用于预览，不进入属性默认值或保存 JSON，
 * 也不发起真实网络请求。
 */
export const SAMPLE_ALBUM_IMAGES = Object.freeze([
  'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22240%22%3E%3Crect fill=%22%23F6D7DF%22 width=%22200%22 height=%22240%22/%3E%3Ctext x=%2250%22 y=%22125%22 font-size=%2220%22 fill=%22%23C84449%22%3E照片%201%3C/text%3E%3C/svg%3E',
  'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22240%22%3E%3Crect fill=%22%23D7E6F6%22 width=%22200%22 height=%22240%22/%3E%3Ctext x=%2250%22 y=%22125%22 font-size=%2220%22 fill=%22%23336699%22%3E照片%202%3C/text%3E%3C/svg%3E',
  'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22240%22%3E%3Crect fill=%22%23E6F6D7%22 width=%22200%22 height=%22240%22/%3E%3Ctext x=%2250%22 y=%22125%22 font-size=%2220%22 fill=%22%23449933%22%3E照片%203%3C/text%3E%3C/svg%3E',
])

// 定义组件
export const component = {
  id: 'PartnerProfileAlbum',
  name: '相册',
  icon: 'ep:picture',
  property: createDefaultPartnerProfileAlbumProperty(),
} as DiyComponent<PartnerProfileAlbumProperty>
