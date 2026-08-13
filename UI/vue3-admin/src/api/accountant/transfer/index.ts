import request from '@/config/axios'

// 查询转账单列表
export const getTransferPage = async (params: PageParam) => {
  return await request.get({ url: `/accountant/transfer/page`, params })
}

// 查询转账单详情
export const getTransfer = async (id: number) => {
  return await request.get({ url: '/accountant/transfer/get?id=' + id })
}

// 导出转账单
export const exportTransfer = async (params: PageParam) => {
  return await request.download({ url: '/accountant/transfer/export-excel', params })
}
