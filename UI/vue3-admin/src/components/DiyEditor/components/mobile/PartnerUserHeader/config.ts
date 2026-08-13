import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 当前登录用户人物头属性；用户资料和动作均由运行时代码提供。 */
export interface PartnerUserHeaderProperty {
  style: ComponentStyle
}

/** 创建公共容器的完整安全默认样式。 */
export function createDefaultPartnerUserHeaderProperty(): PartnerUserHeaderProperty {
  return {
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
      borderBottomLeftRadius: 0
    }
  }
}

/** 管理端预览使用的匿名静态夹具，不会写入装修 JSON。 */
export const PARTNER_USER_HEADER_PREVIEW = Object.freeze({
  nickname: '林小鹿',
  avatarLabel: '鹿'
})

export const component = {
  id: 'PartnerUserHeader',
  name: '人物头',
  icon: 'mdi:account-heart-outline',
  property: createDefaultPartnerUserHeaderProperty()
} as DiyComponent<PartnerUserHeaderProperty>
