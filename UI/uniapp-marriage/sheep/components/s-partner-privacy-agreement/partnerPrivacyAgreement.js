export const COMPONENT_ID = 'PartnerPrivacyAgreement'
export const ICONS = Object.freeze({ LOCK: '🔒', SHIELD: '🛡', DOCUMENT: '▤' })
export const DEFAULTS = Object.freeze({
  entryTitle: '隐私协议', entryIcon: 'LOCK', detailTitle: '隐私协议',
  detailSubtitle: '我们会按照必要、正当、透明的原则保护你的个人信息。',
  style: Object.freeze({ bgType: 'color', bgColor: '', bgImg: '', margin: 0, marginTop: 0, marginRight: 0, marginBottom: 0, marginLeft: 0, padding: 0, paddingTop: 0, paddingRight: 0, paddingBottom: 0, paddingLeft: 0, borderRadius: 0, borderTopLeftRadius: 0, borderTopRightRadius: 0, borderBottomRightRadius: 0, borderBottomLeftRadius: 0 })
})

export function normalizeText(value, maximum, fallback = '', allowEmpty = false) {
  if (typeof value !== 'string') return fallback
  const trimmed = value.trim()
  return (allowEmpty || trimmed) && Array.from(trimmed).length <= maximum ? trimmed : fallback
}

export function normalizeProperty(raw) {
  const value = raw && typeof raw === 'object' && !Array.isArray(raw) ? raw : {}
  const sections = Array.isArray(value.sections) ? value.sections.slice(0, 10).flatMap((section) => {
    const item = section && typeof section === 'object' && !Array.isArray(section) ? section : {}
    const title = normalizeText(item.title, 40)
    const content = normalizeText(item.content, 2000)
    return title && content ? [{ title, content }] : []
  }) : []
  const entryIcon = Object.prototype.hasOwnProperty.call(ICONS, value.entryIcon) ? value.entryIcon : DEFAULTS.entryIcon
  return { ...value, entryTitle: normalizeText(value.entryTitle, 40, DEFAULTS.entryTitle), entryIcon,
    detailTitle: normalizeText(value.detailTitle, 40, DEFAULTS.detailTitle), detailSubtitle: normalizeText(value.detailSubtitle, 200, '', true), sections,
    style: { ...DEFAULTS.style, ...(value.style && typeof value.style === 'object' && !Array.isArray(value.style) ? value.style : {}) } }
}

export function normalizeJsonObject(value) {
  if (typeof value === 'string') { try { return normalizeJsonObject(JSON.parse(value)) } catch { return null } }
  return value && typeof value === 'object' && !Array.isArray(value) ? value : null
}

function positiveId(value) { return typeof value === 'string' && /^[1-9]\d{0,15}$/.test(value) && Number.isSafeInteger(Number(value)) ? Number(value) : 0 }
function componentIndex(value) { return typeof value === 'string' && /^(0|[1-9]\d{0,3})$/.test(value) ? Number(value) : -1 }
function isScalarString(value) { return typeof value === 'string' && !Array.isArray(value) }

export function parseSourceDescriptor(query) {
  if (!query || typeof query !== 'object' || Array.isArray(query) || Object.values(query).some((value) => !isScalarString(value))) return null
  const keys = Object.keys(query).sort()
  const source = query.source
  const expected = source === 'template' ? ['id', 'index', 'slot', 'source'] : source === 'page' ? ['id', 'index', 'source'] : []
  if (keys.length !== expected.length || keys.some((key, index) => key !== expected[index])) return null
  const id = positiveId(query.id); const index = componentIndex(query.index)
  if (!id || index < 0) return null
  if (source === 'template' && (query.slot === 'home' || query.slot === 'user')) return { source, id, slot: query.slot, index }
  return source === 'page' ? { source, id, index } : null
}

export function buildPrivacyAgreementUrl(source) {
  if (!source || !Number.isSafeInteger(source.id) || source.id <= 0 || !Number.isInteger(source.index) || source.index < 0 || source.index > 9999) return ''
  if (source.source === 'template' && (source.slot === 'home' || source.slot === 'user')) return `/pages/public/privacy-agreement?source=template&id=${source.id}&slot=${source.slot}&index=${source.index}`
  if (source.source === 'page') return `/pages/public/privacy-agreement?source=page&id=${source.id}&index=${source.index}`
  return ''
}

export function findAgreementProperty(payload, source) {
  const data = normalizeJsonObject(payload)
  if (!data || !source) return null
  const page = source.source === 'template' ? normalizeJsonObject(data[source.slot]) || normalizeJsonObject(normalizeJsonObject(data.property)?.[source.slot]) : normalizeJsonObject(data.property)
  const component = page?.components?.[source.index]
  if (!component || component.id !== COMPONENT_ID) return null
  const property = normalizeProperty(component.property)
  return property.sections.length ? property : null
}
