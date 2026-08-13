import request from '@/config/axios'

export interface PartnerPoolConfigVO {
  enabled?: boolean
  contactExpireDays?: number
  dealExpireDays?: number
  notifyEnabled?: boolean
  notifyDays?: number
}

// 获取客户公海规则设置
export const getPartnerPoolConfig = async () => {
  return await request.get({ url: `/partner/sales-pool-config/get` })
}

// 更新客户公海规则设置
export const savePartnerPoolConfig = async (data: PartnerPoolConfigVO) => {
  return await request.put({ url: `/partner/sales-pool-config/save`, data })
}
