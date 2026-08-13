import request from '@/config/axios'

export interface Stock {
  id: number
  skuId?: number
  warehouseId?: number
  locationId?: number
  stockMode?: number
  batchNo?: string
  productionDate?: string
  expiryDate?: string
  quantity?: number
  unitPrice?: number
}

export const StockApi = {
  getStockPage: async (params: any) => {
    return await request.get({ url: `/wms/stock/page`, params })
  },
  getStock: async (id: number) => {
    return await request.get({ url: `/wms/stock/get?id=` + id })
  },
  exportStock: async (params: any) => {
    return await request.download({ url: `/wms/stock/export-excel`, params })
  }
}