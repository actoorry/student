import request from '@/config/axios'

export interface MemberSignInConfigVO {
  id?: number
  day: number
  point: number
  experience: number
  status: number
}

// 查询签到配置列表
export const getSignInConfigList = async () => {
  return await request.get({ url: `/partner/sign-in-config/list` })
}

// 保存签到配置
export const saveSignInConfig = async (data: MemberSignInConfigVO[]) => {
  return await request.put({ url: `/partner/sign-in-config/save`, data })
}
