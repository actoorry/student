import request from '@/config/axios'

export interface MemberConfigVO {
  pointTradeDeductEnable: boolean
  pointTradeDeductUnitPrice: number
  pointTradeDeductMaxPrice: number
  pointTradeGivePoint: number
}

// 查询会员配置
export const getMemberConfig = async () => {
  return await request.get({ url: `/partner/config/get` })
}

// 保存会员配置
export const saveMemberConfig = async (data: MemberConfigVO) => {
  return await request.put({ url: `/partner/config/save`, data })
}
