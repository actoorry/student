import request from '@/config/axios'

export interface PartnerVO {
  id: number
  avatar: string | undefined
  birthday: number | undefined
  createTime: number | undefined
  remark: string
  mobile: string
  nickname: string | undefined
  email: string | undefined
  sex: number
  status: number
  areaId: number | undefined
  areaName: string | undefined
  isCustomer: boolean | undefined
  isSupplier: boolean | undefined
  isCompany: boolean | undefined
}

// 查询客商列表
export const getPartnerPage = async (params) => {
  return await request.get({ url: `/partner/partner/page`, params })
}

// 查询客商详情
export const getPartner = async (id: number) => {
  return await request.get({ url: `/partner/partner/get?id=` + id })
}

// 新增客商
export const createPartner = async (data: PartnerVO) => {
  return await request.post({ url: `/partner/partner/create`, data })
}

// 修改客商
export const updatePartner = async (data: PartnerVO) => {
  return await request.put({ url: `/partner/partner/update`, data })
}

// 删除客商
export const deletePartner = async (id: number) => {
  return await request.delete({ url: `/partner/partner/delete?id=` + id })
}

// 查询未绑定系统用户的客商列表
export const getWithoutUserList = async () => {
  return await request.get({ url: `/partner/partner/list-without-user` })
}
