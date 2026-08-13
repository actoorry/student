import request from '@/config/axios'

/** 支付统计 */
export interface PaySummaryRespVO {
  /** 充值金额，单位分 */
  rechargePrice: number
}

/** 获取账户充值金额 */
export const getAccountRechargePrice = async () => {
  return await request.get<PaySummaryRespVO>({ url: `/sales/statistics/pay/summary` })
}
