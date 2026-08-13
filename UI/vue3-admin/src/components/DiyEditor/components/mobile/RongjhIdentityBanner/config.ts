import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 戎集汇身份认证横幅 */
export interface RongjhIdentityBannerProperty {
  /** 未认证时跳转路径 */
  applyUrl: string
  /** 未认证时展示文案 */
  textNone: string
  /** 认证审核中展示文案 */
  textPending: string
  /** 认证通过展示文案 */
  textApproved: string
  /** 未认证时按钮文案 */
  actionText: string
  /** 认证被拒时按钮文案 */
  actionTextRejected: string
  style: ComponentStyle
}

export const DEFAULT_RONGJH_IDENTITY_BANNER = {
  applyUrl: '/pages/rongjh/identity/apply',
  textNone: '退役军人及三属人员认证可享更多优惠',
  textPending: '您的认证申请审核中，请耐心等待',
  textApproved: '您已通过军人及三属人员认证',
  actionText: '立即认证',
  actionTextRejected: '重新认证'
}

export const component = {
  id: 'RongjhIdentityBanner',
  name: '身份认证横幅',
  icon: 'mdi:shield-star',
  property: {
    ...DEFAULT_RONGJH_IDENTITY_BANNER,
    style: {
      bgType: 'color',
      bgColor: '',
      marginBottom: 8
    } as ComponentStyle
  }
} as DiyComponent<RongjhIdentityBannerProperty>
