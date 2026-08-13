import request from '@/config/axios'

export interface AccountRechargePackageVO {
  id: number
  name: string
  payPrice: number
  bonusPrice: number
  status: number
}

// 查询套餐充值列表
export const getAccountRechargePackagePage = async (params) => {
  return await request.get({ url: '/accountant/account-recharge-package/page', params })
}

// 查询套餐充值详情
export const getAccountRechargePackage = async (id: number) => {
  return await request.get({ url: '/accountant/account-recharge-package/get?id=' + id })
}

// 新增套餐充值
export const createAccountRechargePackage = async (data: AccountRechargePackageVO) => {
  return await request.post({ url: '/accountant/account-recharge-package/create', data })
}

// 修改套餐充值
export const updateAccountRechargePackage = async (data: AccountRechargePackageVO) => {
  return await request.put({ url: '/accountant/account-recharge-package/update', data })
}

// 删除套餐充值
export const deleteAccountRechargePackage = async (id: number) => {
  return await request.delete({ url: '/accountant/account-recharge-package/delete?id=' + id })
}
