import type { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

export const DEFAULT_PARTNER_PROFILE_WATERFALL_STYLE: ComponentStyle = {
  bgType: 'color', bgColor: '', bgImg: '', margin: 0, marginTop: 0, marginRight: 0,
  marginBottom: 0, marginLeft: 0, padding: 0, paddingTop: 0, paddingRight: 16,
  paddingBottom: 0, paddingLeft: 16, borderRadius: 0, borderTopLeftRadius: 0,
  borderTopRightRadius: 0, borderBottomRightRadius: 0, borderBottomLeftRadius: 0
}
export interface PartnerProfileWaterfallProperty {
  rule: {
    realVerifiedOnly: boolean
    backgroundImageRequired: boolean
    oppositeSexOnly: boolean
  }
  fields: Record<'name' | 'age' | 'sex' | 'job' | 'city', { show: boolean; color: string }>
  badges: Record<'member' | 'realName' | 'marriage', { show: boolean; textColor: string; backgroundColor: string }>
  imageWidthPercent: number; cardHeight: number; cardGap: number; cardBackgroundColor: string
  cardRadius: number; shadowColor: string; shadowOpacity: number; shadowBlur: number; accentColor: string
  contactButton: { show: boolean; textColor: string; backgroundColor: string; radius: number }
  profileButton: { show: boolean; textColor: string; backgroundColor: string; borderColor: string; radius: number }
  style: ComponentStyle
}
const field = () => ({ show: true, color: '#6F5A55' })
export function createDefaultPartnerProfileWaterfallProperty(): PartnerProfileWaterfallProperty {
  return { rule: { realVerifiedOnly: false, backgroundImageRequired: false, oppositeSexOnly: false },
    fields: { name: { show: true, color: '#241A18' }, age: field(), sex: field(), job: field(), city: field() },
    badges: { member: { show: true, textColor: '#D92D45', backgroundColor: '#FFF0F2' }, realName: { show: true, textColor: '#D92D45', backgroundColor: '#FFF0F2' }, marriage: { show: true, textColor: '#D92D45', backgroundColor: '#FFF0F2' } },
    imageWidthPercent: 45, cardHeight: 320, cardGap: 24, cardBackgroundColor: '#FFFFFF', cardRadius: 32,
    shadowColor: '#000000', shadowOpacity: 0.08, shadowBlur: 24, accentColor: '#D92D45',
    contactButton: { show: true, textColor: '#FFFFFF', backgroundColor: '#D92D45', radius: 28 },
    profileButton: { show: true, textColor: '#D92D45', backgroundColor: '#FFFFFF', borderColor: '#D92D45', radius: 28 },
    style: { ...DEFAULT_PARTNER_PROFILE_WATERFALL_STYLE } }
}
export const component = { id: 'PartnerProfileWaterfall', name: '人物信息卡瀑布流', icon: 'ep:postcard', property: createDefaultPartnerProfileWaterfallProperty() } as DiyComponent<PartnerProfileWaterfallProperty>
