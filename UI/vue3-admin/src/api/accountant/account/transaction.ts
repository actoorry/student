import request from '@/config/axios'

export interface AccountTransactionVO {
  id: number
  accountId: number
  title: string
  price: number
  balance: number
}

// 查询会员账户流水列表
export const getAccountTransactionPage = async (params) => {
  return await request.get({ url: `/accountant/account-transaction/page`, params })
}
