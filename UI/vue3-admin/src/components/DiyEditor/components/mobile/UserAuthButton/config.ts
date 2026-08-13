import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 登录/退出按钮 */
export interface UserAuthButtonProperty {
  /** 未登录时按钮文案 */
  loginText: string
  /** 已登录时按钮文案 */
  logoutText: string
  /** 退出确认提示 */
  logoutConfirmText: string
  style: ComponentStyle
}

export const DEFAULT_USER_AUTH_BUTTON = {
  loginText: '登录',
  logoutText: '退出登录',
  logoutConfirmText: '确认退出账号？'
}

export const component = {
  id: 'UserAuthButton',
  name: '登录/退出',
  icon: 'ep:switch-button',
  property: {
    ...DEFAULT_USER_AUTH_BUTTON,
    style: {
      bgType: 'color',
      bgColor: '',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8
    } as ComponentStyle
  }
} as DiyComponent<UserAuthButtonProperty>
