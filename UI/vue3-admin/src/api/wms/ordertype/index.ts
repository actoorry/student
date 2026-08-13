import request from '@/config/axios'

export interface OrderType {
  id: number
  name?: string
  fromLocationId?: number
  toLocationId?: number
  stockImpact?: number
  needSupplier?: number
  needCustomer?: number
  active?: number
  sort?: number
  remark?: string
}

export const OrderTypeApi = {
  getOrderTypePage: async (params: any) => {
    return await request.get({ url: `/wms/order-type/page`, params })
  },
  getOrderType: async (id: number) => {
    return await request.get({ url: `/wms/order-type/get?id=` + id })
  },
  createOrderType: async (data: OrderType) => {
    return await request.post({ url: `/wms/order-type/create`, data })
  },
  updateOrderType: async (data: OrderType) => {
    return await request.put({ url: `/wms/order-type/update`, data })
  },
  deleteOrderType: async (id: number) => {
    return await request.delete({ url: `/wms/order-type/delete?id=` + id })
  },
  deleteOrderTypeList: async (ids: number[]) => {
    return await request.delete({ url: `/wms/order-type/delete-list?ids=${ids.join(',')}` })
  },
  exportOrderType: async (params: any) => {
    return await request.download({ url: `/wms/order-type/export-excel`, params })
  }
}