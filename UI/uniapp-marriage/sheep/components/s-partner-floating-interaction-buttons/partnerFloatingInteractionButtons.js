/**
 * 互动按钮纯函数 helper。
 * 不依赖组件实例、DOM 或任一平台的特定 API。
 */

/** 图标 URL 最大字符数 */
export const ICON_URL_MAX_LENGTH = 2048

/** 允许的图标 URL 协议前缀 */
const ALLOWED_URL_PREFIXES = ['http://', 'https://', '/']

/** 默认分享图标资源路径（相对 static 目录） */
export const DEFAULT_SHARE_ICON = '/static/icons/mine/share-export.svg'

/** 规范化单个图标 URL */
export function normalizeIconUrl(value) {
  if (typeof value !== 'string') return ''
  const trimmed = value.trim()
  if (!trimmed) return ''
  if (trimmed.length > ICON_URL_MAX_LENGTH) return ''
  if (!ALLOWED_URL_PREFIXES.some((prefix) => trimmed.startsWith(prefix))) return ''
  return trimmed
}

/** 滚动阈值常量 */
export const SCROLL_TOP_THRESHOLD = 40
export const SCROLL_HIDE_DELTA = 16
export const SCROLL_SHOW_DELTA = -12

/** 按钮视觉常量（rpx） */
export const BUTTON_SIZE = 120
export const BUTTON_GAP = 32
export const BUTTON_BOTTOM = 176
export const BUTTON_BORDER_RADIUS = 60 /* 50% of 120 */
export const ICON_BASE_SIZE = 68

/** 隐藏态 */
export const HIDDEN_TRANSLATE = 168 /* rpx */
export const HIDDEN_SCALE = 0.92

/** 过渡时长 ms */
export const TRANSITION_DURATION = 220

/** 关注反馈展示时长 ms */
export const FOLLOW_FEEDBACK_DURATION = 2400

/**
 * 构建分享按钮显示图标路径：
 * 优先使用自定义 URL，为空时使用默认分享 SVG。
 */
export function resolveShareIcon(customUrl, imageFailed) {
  if (imageFailed) return ''
  const normalized = normalizeIconUrl(customUrl)
  if (normalized) return normalized
  return DEFAULT_SHARE_ICON
}

export { buildMemberShareInfo as buildPublicShareInfo } from '@/sheep/helper/member-profile-share'

/**
 * 判断人物上下文是否有效。
 * 必须包含数字 partnerId 和非 null 的 profile。
 */
export function hasValidPartnerContext(context) {
  if (!context) return false
  const ctx = context.value
  return !!ctx && typeof ctx.partnerId === 'number' && !!ctx.profile
}
