import request from '@/config/axios'

export interface ConfigVO {
  brokerageEnabled: boolean
  brokerageEnabledCondition: number
  brokerageBindMode: number
  brokeragePosterUrls: string
  brokerageFirstPercent: number
  brokerageSecondPercent: number
  brokerageWithdrawMinPrice: number
  brokerageFrozenDays: number
  brokerageWithdrawTypes: string
}

// 查询交易中心配置详情
export const getTradeConfig = async () => {
  return await request.get({ url: `/sales/config/get` })
}

// 保存交易中心配置
export const saveTradeConfig = async (data: ConfigVO) => {
  return await request.put({ url: `/sales/config/save`, data })
}
