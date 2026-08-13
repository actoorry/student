import request from '@/config/axios'

// 查询会员地址分页
export const getAddressPage = async (params) => {
  return await request.get({ url: `/partner/address/page`, params })
}

// 查询会员地址详情
export const getAddress = async (id: number) => {
  return await request.get({ url: `/partner/address/get?id=` + id })
}

// 新增会员地址
export const createAddress = async (data) => {
  return await request.post({ url: `/partner/address/create`, data })
}

// 修改会员地址
export const updateAddress = async (data) => {
  return await request.put({ url: `/partner/address/update`, data })
}

// 删除会员地址
export const deleteAddress = async (id: number) => {
  return await request.delete({ url: `/partner/address/delete?id=` + id })
}
