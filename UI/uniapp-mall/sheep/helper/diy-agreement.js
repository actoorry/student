/**
 * DIY 内容协议页帮助函数
 * @description 从标准 Sales DIY 模板/页面的 `{ id, property }` 契约中定位并读取
 * 指定组件的 property，供独立协议内容页（如隐私协议）渲染。本模块只做 JSON
 * 契约读取与参数解析，不依赖任何婚恋运行时或客户端上下文。
 */

export const AGREEMENT_COMPONENT_ID = 'PartnerPrivacyAgreement'

function normalizeJsonObject(value) {
  if (typeof value === 'string') { try { return normalizeJsonObject(JSON.parse(value)) } catch { return null } }
  return value && typeof value === 'object' && !Array.isArray(value) ? value : null
}

function positiveId(value) { return typeof value === 'string' && /^[1-9]\d{0,15}$/.test(value) && Number.isSafeInteger(Number(value)) ? Number(value) : 0 }
function componentIndex(value) { return typeof value === 'string' && /^(0|[1-9]\d{0,3})$/.test(value) ? Number(value) : -1 }
function isScalarString(value) { return typeof value === 'string' && !Array.isArray(value) }

/** 解析协议页 query 中的 source 描述（template|page）。 */
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

function normalizeText(value, maximum, fallback = '', allowEmpty = false) {
  if (typeof value !== 'string') return fallback
  const trimmed = value.trim()
  return (allowEmpty || trimmed) && Array.from(trimmed).length <= maximum ? trimmed : fallback
}

function normalizeProperty(raw) {
  const value = raw && typeof raw === 'object' && !Array.isArray(raw) ? raw : {}
  const sections = Array.isArray(value.sections) ? value.sections.slice(0, 10).flatMap((section) => {
    const item = section && typeof section === 'object' && !Array.isArray(section) ? section : {}
    const title = normalizeText(item.title, 40)
    const content = normalizeText(item.content, 2000)
    return title && content ? [{ title, content }] : []
  }) : []
  return {
    ...value,
    detailTitle: normalizeText(value.detailTitle, 40, '协议内容'),
    detailSubtitle: normalizeText(value.detailSubtitle, 200, '', true),
    sections,
  }
}

/** 在模板/页面的组件列表中查找目标协议组件并返回其 property。 */
export function findAgreementProperty(payload, source) {
  const data = normalizeJsonObject(payload)
  if (!data || !source) return null
  const page = source.source === 'template'
    ? normalizeJsonObject(data[source.slot]) || normalizeJsonObject(normalizeJsonObject(data.property)?.[source.slot])
    : normalizeJsonObject(data.property)
  const component = page?.components?.[source.index]
  if (!component || component.id !== AGREEMENT_COMPONENT_ID) return null
  const property = normalizeProperty(component.property)
  return property.sections.length ? property : null
}
