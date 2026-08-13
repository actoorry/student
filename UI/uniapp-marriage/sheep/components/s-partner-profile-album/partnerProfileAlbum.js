/**
 * 人物资料相册组件运行时常量与规范化 helpers
 *
 * 这些函数是纯函数，不依赖 Vue 上下文或业务请求，便于单元测试。
 */

// ---- 属性默认值与范围 ----

export const DEFAULT_TITLE = '相册'
export const DEFAULT_SUBTITLE = '真实生活照片'
export const DEFAULT_TITLE_COLOR = '#2D2324'
export const DEFAULT_SUBTITLE_COLOR = '#8B7670'
export const DEFAULT_IMAGE_HEIGHT = 240
export const DEFAULT_IMAGE_RADIUS = 28

export const MIN_IMAGE_HEIGHT = 120
export const MAX_IMAGE_HEIGHT = 480
export const MIN_IMAGE_RADIUS = 0
export const MAX_IMAGE_RADIUS = 64
export const TITLE_MAX_LENGTH = 12
export const SUBTITLE_MAX_LENGTH = 20
export const MAX_ALBUM_IMAGES = 9

// ---- 工具函数 ----

/** 判断是否为有效图片 URL（非空字符串） */
export function isValidImageUrl(url) {
  return typeof url === 'string' && url.trim().length > 0
}

// ---- 属性规范化 ----

export function normalizeTitle(value) {
  if (typeof value !== 'string') return DEFAULT_TITLE
  const trimmed = value.trim()
  if (trimmed.length === 0 || Array.from(trimmed).length > TITLE_MAX_LENGTH) return DEFAULT_TITLE
  return trimmed
}

export function normalizeSubtitle(value) {
  if (typeof value !== 'string') return DEFAULT_SUBTITLE
  const trimmed = value.trim()
  if (Array.from(trimmed).length > SUBTITLE_MAX_LENGTH) return DEFAULT_SUBTITLE
  return trimmed
}

export function normalizeColor(value, defaultColor) {
  if (typeof value !== 'string') return defaultColor
  const trimmed = value.trim()
  if (/^#[0-9A-Fa-f]{6}$/.test(trimmed)) return trimmed.toUpperCase()
  return defaultColor
}

export function normalizeImageHeight(value) {
  const num = Number(value)
  if (!Number.isFinite(num) || num < MIN_IMAGE_HEIGHT || num > MAX_IMAGE_HEIGHT) {
    return DEFAULT_IMAGE_HEIGHT
  }
  return Math.round(num)
}

export function normalizeImageRadius(value) {
  const num = Number(value)
  if (!Number.isFinite(num) || num < MIN_IMAGE_RADIUS || num > MAX_IMAGE_RADIUS) {
    return DEFAULT_IMAGE_RADIUS
  }
  return Math.round(num)
}

/**
 * 安全规范化所有属性字段为完整默认值对象。
 * 缺失、错误类型或非法值回退到对应默认值。
 * 保留未知字段但不在视觉中使用。
 */
export function normalizeProperty(raw) {
  const p = raw && typeof raw === 'object' && !Array.isArray(raw) ? raw : {}
  return {
    ...p,
    title: normalizeTitle(p.title),
    subtitle: normalizeSubtitle(p.subtitle),
    titleColor: normalizeTitleColor(p.titleColor),
    subtitleColor: normalizeSubtitleColor(p.subtitleColor),
    imageHeight: normalizeImageHeight(p.imageHeight),
    imageRadius: normalizeImageRadius(p.imageRadius),
  }
}

export function normalizeTitleColor(value) {
  return normalizeColor(value, DEFAULT_TITLE_COLOR)
}

export function normalizeSubtitleColor(value) {
  return normalizeColor(value, DEFAULT_SUBTITLE_COLOR)
}

// ---- 相册候选构造 ----

/**
 * 从人物 profile 构造相册候选列表。
 * 只读取 albumImages，排除 mainImage。
 * 去空、保序去重、最多 9 张。
 * 返回纯字符串数组，不修改输入。
 */
export function buildAlbumCandidates(profile) {
  if (!profile || typeof profile !== 'object') return []

  const raw = profile.albumImages
  if (!Array.isArray(raw) || raw.length === 0) return []

  const seen = new Set()
  const candidates = []

  for (const item of raw) {
    if (!isValidImageUrl(item)) continue
    const cleaned = item.trim()
    if (seen.has(cleaned)) continue
    seen.add(cleaned)
    candidates.push(cleaned)
    if (candidates.length >= MAX_ALBUM_IMAGES) break
  }

  return candidates
}

/**
 * 检查 mainImage 是否出现在了 albumImages 候选列表中。
 * 用于测试校验——组件本身已经只读 albumImages。
 */
export function isMainImageInAlbumCandidates(profile) {
  const candidates = buildAlbumCandidates(profile)
  if (!profile || typeof profile !== 'object') return false
  if (!isValidImageUrl(profile.mainImage)) return false
  return candidates.includes(profile.mainImage.trim())
}

// ---- 身份与 Generation ----

/**
 * 计算无歧义的身份键。
 * 使用结构化数组而非分隔符拼接，避免 URL 内容碰撞。
 */
export function computeIdentityKey(partnerId, albumImages) {
  const id = typeof partnerId === 'number' ? partnerId : -1
  const fingerprints = Array.isArray(albumImages)
    ? albumImages.map((url) => (typeof url === 'string' ? url.trim() : ''))
    : []
  // 使用 JSON 稳定序列化数组，不是简单分隔符拼接
  return JSON.stringify([id, ...fingerprints])
}

/** 为一次人物作用域创建独立媒体状态。 */
export function createAlbumMediaState(profile, generation = 0) {
  return {
    generation,
    candidates: buildAlbumCandidates(profile),
    states: new Map(),
  }
}

/**
 * 将图片状态从 pending 单向转换为 success 或 failed。
 * 旧 generation、未知候选和终态的重复/相反事件都会被忽略。
 */
export function transitionAlbumMediaState(state, eventGeneration, candidate, nextState) {
  if (!state || eventGeneration !== state.generation) return state
  if (!state.candidates.includes(candidate)) return state
  if (nextState !== 'success' && nextState !== 'failed') return state
  const current = state.states.get(candidate) || 'pending'
  if (current !== 'pending') return state
  const states = new Map(state.states)
  states.set(candidate, nextState)
  return { ...state, states }
}

export function getVisibleAlbumItems(state) {
  if (!state) return []
  return state.candidates
    .filter((candidate) => state.states.get(candidate) !== 'failed')
    .map((candidate) => ({ candidate, generation: state.generation, state: state.states.get(candidate) || 'pending' }))
}

export function getSuccessfulAlbumCandidates(state) {
  if (!state) return []
  return state.candidates.filter((candidate) => state.states.get(candidate) === 'success')
}

// ---- 查看请求状态常量 ----

export const VIEW_REQUEST_STATE = Object.freeze({
  IDLE: 'idle',
  PENDING: 'pending',
  SUCCESS: 'success',
})
