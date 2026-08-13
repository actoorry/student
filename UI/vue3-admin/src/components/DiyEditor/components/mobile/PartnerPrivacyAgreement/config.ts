import type { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

export const PRIVACY_AGREEMENT_ICON_VALUES = ['LOCK', 'SHIELD', 'DOCUMENT'] as const
export type PartnerPrivacyAgreementIcon = (typeof PRIVACY_AGREEMENT_ICON_VALUES)[number]

export interface PartnerPrivacyAgreementSection {
  title: string
  content: string
}

export interface PartnerPrivacyAgreementProperty {
  entryTitle: string
  entryIcon: PartnerPrivacyAgreementIcon
  detailTitle: string
  detailSubtitle: string
  sections: PartnerPrivacyAgreementSection[]
  style: ComponentStyle
}

export const DEFAULT_TITLE = '隐私协议'
export const DEFAULT_SUBTITLE = '我们会按照必要、正当、透明的原则保护你的个人信息。'
export const DEFAULT_SECTIONS: PartnerPrivacyAgreementSection[] = [
  { title: '信息收集', content: '为提供账号登录、资料展示、匹配推荐、订单支付、活动报名和客户服务，我们会根据你使用的功能收集必要的信息。' },
  { title: '信息使用', content: '我们仅在实现产品功能、保障账号安全、优化服务体验、履行法律法规要求的范围内使用相关信息。' },
  { title: '信息共享', content: '未经你的授权，我们不会向无关第三方共享你的个人信息。涉及支付、消息通知等必要服务时，会按照最小必要原则提供所需信息。' },
  { title: '信息保护', content: '我们会采取访问控制、传输加密、权限隔离等措施保护你的信息安全，并持续改进安全管理能力。' },
  { title: '用户权利', content: '你可以在应用内查看和修改个人资料，也可以通过客服渠道申请查询、更正或删除相关个人信息。' }
]

export function createDefaultPartnerPrivacyAgreementProperty(): PartnerPrivacyAgreementProperty {
  return {
    entryTitle: DEFAULT_TITLE,
    entryIcon: 'LOCK',
    detailTitle: DEFAULT_TITLE,
    detailSubtitle: DEFAULT_SUBTITLE,
    sections: DEFAULT_SECTIONS.map((section) => ({ ...section })),
    style: {
      bgType: 'color', bgColor: '', bgImg: '',
      margin: 0, marginTop: 0, marginRight: 0, marginBottom: 0, marginLeft: 0,
      padding: 0, paddingTop: 0, paddingRight: 0, paddingBottom: 0, paddingLeft: 0,
      borderRadius: 0, borderTopLeftRadius: 0, borderTopRightRadius: 0,
      borderBottomRightRadius: 0, borderBottomLeftRadius: 0
    }
  }
}

function text(value: unknown, max: number, fallback: string, allowEmpty = false): string {
  if (typeof value !== 'string') return fallback
  const trimmed = value.trim()
  return (allowEmpty || trimmed) && Array.from(trimmed).length <= max ? trimmed : fallback
}

export function normalizePartnerPrivacyAgreementProperty(raw: unknown): PartnerPrivacyAgreementProperty {
  const value = raw && typeof raw === 'object' && !Array.isArray(raw) ? raw as Record<string, unknown> : {}
  const sections = Array.isArray(value.sections) ? value.sections.slice(0, 10).flatMap((section) => {
    const item = section && typeof section === 'object' && !Array.isArray(section) ? section as Record<string, unknown> : {}
    const title = text(item.title, 40, '')
    const content = text(item.content, 2000, '')
    return title && content ? [{ title, content }] : []
  }) : []
  return {
    entryTitle: text(value.entryTitle, 40, DEFAULT_TITLE),
    entryIcon: PRIVACY_AGREEMENT_ICON_VALUES.includes(value.entryIcon as PartnerPrivacyAgreementIcon)
      ? value.entryIcon as PartnerPrivacyAgreementIcon : 'LOCK',
    detailTitle: text(value.detailTitle, 40, DEFAULT_TITLE),
    detailSubtitle: text(value.detailSubtitle, 200, '', true),
    sections,
    style: {
      ...createDefaultPartnerPrivacyAgreementProperty().style,
      ...(value.style && typeof value.style === 'object' && !Array.isArray(value.style)
        ? value.style as ComponentStyle : {})
    }
  }
}

export const component = {
  id: 'PartnerPrivacyAgreement',
  name: '隐私协议',
  icon: 'ep:lock',
  property: createDefaultPartnerPrivacyAgreementProperty()
} as DiyComponent<PartnerPrivacyAgreementProperty>
