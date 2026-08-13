import request from '@/config/axios'

export interface StockLog {
  id: number
  skuId?: number
  warehouseId?: number
  fromLocationId?: number
  toLocationId?: number
  batchNo?: string
  productionDate?: string
  expiryDate?: string
  bizType?: string
  bizId?: number
  bizNo?: number
  beforeQuantity?: number
  changeQuantity?: number
  afterQuantity?: number
  createTime?: string
}

export const StockLogApi = {
  getStockLogPage: async (params: any) => {
    return await request.get({ url: `/wms/stock-log/page`, params })
  },
  exportStockLog: async (params: any) => {
    return await request.download({ url: `/wms/stock-log/export-excel`, params })
  }
}