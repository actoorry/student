import request from '@/config/axios'

export interface Warehouse {
  id: number
  parentId?: number
  name?: string
  code?: string
  locType?: number
  areaId?: number
  areaName?: string
  address?: string
  partnerId?: number
  status?: number
  sort?: number
  remark?: string
  removalStrategy?: number
  children?: Warehouse[]
}

export const WarehouseApi = {
  getWarehousePage: async (params: any) => {
    return await request.get({ url: `/wms/warehouse/page`, params })
  },
  getWarehouse: async (id: number) => {
    return await request.get({ url: `/wms/warehouse/get?id=` + id })
  },
  createWarehouse: async (data: Warehouse) => {
    return await request.post({ url: `/wms/warehouse/create`, data })
  },
  updateWarehouse: async (data: Warehouse) => {
    return await request.put({ url: `/wms/warehouse/update`, data })
  },
  deleteWarehouse: async (id: number) => {
    return await request.delete({ url: `/wms/warehouse/delete?id=` + id })
  },
  deleteWarehouseList: async (ids: number[]) => {
    return await request.delete({ url: `/wms/warehouse/delete-list?ids=${ids.join(',')}` })
  },
  exportWarehouse: async (params: any) => {
    return await request.download({ url: `/wms/warehouse/export-excel`, params })
  }
}