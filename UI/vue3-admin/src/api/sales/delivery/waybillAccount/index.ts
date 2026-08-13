import request from '@/config/axios'

export interface WaybillAccountVO {
  id?: number | null
  name?: string // 账户名称
  expressId?: number | null // 快递公司编号
  expressName?: string // 快递公司名称
  key?: string // 快递100平台授权 key（掩码）
  secret?: string // 快递100平台授权密钥（掩码）
  partnerId?: string // 电子面单月结账号（掩码）
  partnerKey?: string // 电子面单密码（掩码）
  partnerSecret?: string // 电子面单密钥（掩码）
  net?: string // 电子面单网点
  code?: string // 电子面单承载编号
  partnerName?: string // 电子面单客户账户名称
  checkMan?: string // 取消电子面单的操作人
  expType?: string // 快递产品类型
  tempId?: string // 快递100模板 ID
  defaultAddressId?: number | null // 默认寄件地址编号
  status?: number // 状态
  createTime?: Date | null
}

export interface SenderAddressVO {
  id: number
  name: string
  mobile: string
  areaId: number
  areaName: string
  detailAddress: string
}

// 查询电子面单账户分页
export const getWaybillAccountPage = async (params: any) => {
  return await request.get({ url: '/sales/electronic-waybill/account/page', params })
}

// 查询电子面单账户详情
export const getWaybillAccount = async (id: number) => {
  return await request.get({ url: `/sales/electronic-waybill/account/get?id=${id}` })
}

// 查询启用的电子面单账户精简列表
export const getSimpleWaybillAccountList = async () => {
  return await request.get({ url: '/sales/electronic-waybill/account/list-all-simple' })
}

// 查询指定快递公司启用的电子面单账户列表
export const getWaybillAccountListByExpress = async (expressId: number) => {
  return await request.get({ url: `/sales/electronic-waybill/account/list-by-express?expressId=${expressId}` })
}

// 查询当前租户商户寄件地址列表
export const getSenderAddressList = async () => {
  return await request.get({ url: '/sales/electronic-waybill/account/list-sender-address' })
}

// 新增电子面单账户
export const createWaybillAccount = async (data: WaybillAccountVO) => {
  return await request.post({ url: '/sales/electronic-waybill/account/create', data })
}

// 更新电子面单账户
export const updateWaybillAccount = async (data: WaybillAccountVO) => {
  return await request.put({ url: '/sales/electronic-waybill/account/update', data })
}

// 删除电子面单账户
export const deleteWaybillAccount = async (id: number) => {
  return await request.delete({ url: `/sales/electronic-waybill/account/delete?id=${id}` })
}
