import request from '@/config/axios'

/** 用户账户查询参数 */
export interface AccountUserReqVO {
  partnerId: number
}

/** 账户 VO */
export interface AccountVO {
  id: number
  partnerId: number
  balance: number
  totalExpense: number
  totalRecharge: number
  freezePrice: number
}

/** 查询用户账户详情 */
export const getAccount = async (params: AccountUserReqVO) => {
  return await request.get<AccountVO>({ url: `/accountant/account/get`, params })
}

/** 查询会员账户列表 */
export const getAccountPage = async (params: any) => {
  return await request.get({ url: `/accountant/account/page`, params })
}

/** 修改会员账户余额 */
export const updateAccountBalance = async (data: any) => {
  return await request.put({ url: `/accountant/account/update-balance`, data })
}
