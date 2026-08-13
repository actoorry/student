export const DEFAULTS = Object.freeze({
  title: 'TA 的动态', showAllMoments: true, contentMaxLength: 80, maxImages: 3,
  titleColor: '#2D2324', allMomentsColor: '#C84449', contentColor: '#4A3937',
  metaColor: '#7C6660', cardBackgroundColor: '#FFF6F2', cardRadius: 40,
})

export function positiveId(value) {
  const id = Number(value)
  return Number.isInteger(id) && id > 0 ? id : 0
}
export function normalizeColor(value, fallback) {
  const color = typeof value === 'string' ? value.trim() : ''
  return /^#[0-9A-Fa-f]{6}$/.test(color) ? color.toUpperCase() : fallback
}
export function bounded(value, min, max, fallback) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue >= min && numberValue <= max ? Math.round(numberValue) : fallback
}
export function title(value) {
  const result = typeof value === 'string' ? value.trim() : ''
  return result && Array.from(result).length <= 12 ? result : DEFAULTS.title
}
export function normalizeProperty(raw) {
  const value = raw && typeof raw === 'object' && !Array.isArray(raw) ? raw : {}
  return { ...value, title: title(value.title), showAllMoments: typeof value.showAllMoments === 'boolean' ? value.showAllMoments : true,
    contentMaxLength: bounded(value.contentMaxLength, 20, 200, DEFAULTS.contentMaxLength), maxImages: bounded(value.maxImages, 0, 3, DEFAULTS.maxImages),
    titleColor: normalizeColor(value.titleColor, DEFAULTS.titleColor), allMomentsColor: normalizeColor(value.allMomentsColor, DEFAULTS.allMomentsColor),
    contentColor: normalizeColor(value.contentColor, DEFAULTS.contentColor), metaColor: normalizeColor(value.metaColor, DEFAULTS.metaColor),
    cardBackgroundColor: normalizeColor(value.cardBackgroundColor, DEFAULTS.cardBackgroundColor), cardRadius: bounded(value.cardRadius, 0, 80, DEFAULTS.cardRadius) }
}
export function uniqueImages(value, maxImages, cdn = (url) => url) {
  if (!Array.isArray(value)) return []
  const seen = new Set(); const result = []
  value.forEach((item) => { if (typeof item !== 'string') return; const raw = item.trim(); if (!raw || seen.has(raw)) return; const url = cdn(raw); if (typeof url !== 'string' || !url.trim()) return; seen.add(raw); result.push(url.trim()) })
  return result.slice(0, maxImages)
}
export function truncateUnicode(value, maxLength) {
  if (typeof value !== 'string') return ''
  const characters = Array.from(value.trim())
  return characters.length > maxLength ? `${characters.slice(0, maxLength).join('')}...` : characters.join('')
}
export function normalizeMoment(moment, property, cdn) {
  const id = positiveId(moment?.id); if (!id) return null
  const content = truncateUnicode(moment.content, property.contentMaxLength)
  const images = uniqueImages(moment.imageUrls, property.maxImages, cdn)
  if (!content && !images.length) return null
  return { id, content, images, publishTime: moment.publishTime || '', likeCount: Math.max(0, Math.floor(Number(moment.likeCount) || 0)), commentCount: Math.max(0, Math.floor(Number(moment.commentCount) || 0)) }
}
export function identityKey(partnerId, moment) { return JSON.stringify([positiveId(partnerId), moment?.id || 0, ...(moment?.images || [])]) }
export function formatRelativeTime(value, now = Date.now()) { const time = new Date(value).getTime(); if (!Number.isFinite(time)) return ''; const seconds = Math.max(0, Math.floor((now - time) / 1000)); if (seconds < 60) return '刚刚'; if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟前`; if (seconds < 86400) return `${Math.floor(seconds / 3600)}小时前`; return `${Math.floor(seconds / 86400)}天前` }
