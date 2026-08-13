import request from '@/config/axios'

export interface InventoryAdjust {
  id: number
  no?: number
  warehouseId?: number
  skuId?: number
  locationId?: number
  batchNo?: string
  productionDate?: string
  expiryDate?: string
  bookQuantity?: number
  actualQuantity?: number
  status?: number
  checkType?: number
  checkTime?: string
  operatorId?: number
  remark?: string
}

export const InventoryAdjustApi = {
  /** 创建盘点调整草稿（自动读取当前账面库存） */
  createInventoryAdjust: async (data: { stockId: number; actualQuantity: number; checkType?: number; remark?: string }) => {
    return await request.post({ url: `/wms/inventory-adjust/create`, data })
  },
  /** 更新盘点调整草稿 */
  updateInventoryAdjust: async (data: { id: number; actualQuantity: number; checkType?: number; remark?: string }) => {
    return await request.put({ url: `/wms/inventory-adjust/update`, data })
  },
  /** 审核盘点调整 */
  approveInventoryAdjust: async (id: number) => {
    return await request.put({ url: `/wms/inventory-adjust/approve`, params: { id } })
  },
  /** 删除盘点调整草稿 */
  deleteInventoryAdjust: async (id: number) => {
    return await request.delete({ url: `/wms/inventory-adjust/delete?id=` + id })
  },
  /** 获得盘点调整记录 */
  getInventoryAdjust: async (id: number) => {
    return await request.get({ url: `/wms/inventory-adjust/get?id=` + id })
  },
  /** 获得盘点调整记录分页 */
  getInventoryAdjustPage: async (params: any) => {
    return await request.get({ url: `/wms/inventory-adjust/page`, params })
  }
}