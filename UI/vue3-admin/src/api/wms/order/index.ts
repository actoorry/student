import request from '@/config/axios'

export interface Order {
  id?: number
  no?: number
  typeId?: number
  status?: number
  supplierId?: number
  customerId?: number
  fromWarehouseId?: number
  toWarehouseId?: number
  originId?: number
  operatorId?: number
  orderTime?: string
  remark?: string
  items?: OrderItem[]
}

export interface OrderItem {
  id?: number
  orderId?: number
  skuId?: number
  originId?: number
  fromLocationId?: number
  toLocationId?: number
  batchNo?: string
  productionDate?: string
  expiryDate?: string
  quantity?: number
  unitPrice?: number
  sort?: number
  remark?: string
}

export const OrderApi = {
  getOrderPage: async (params: any) => {
    return await request.get({ url: `/wms/order/page`, params })
  },
  getOrder: async (id: number) => {
    return await request.get({ url: `/wms/order/get?id=` + id })
  },
  createOrder: async (data: Order) => {
    return await request.post({ url: `/wms/order/create`, data })
  },
  updateOrder: async (data: Order) => {
    return await request.put({ url: `/wms/order/update`, data })
  },
  deleteOrder: async (id: number) => {
    return await request.delete({ url: `/wms/order/delete?id=` + id })
  },
  deleteOrderList: async (ids: number[]) => {
    return await request.delete({ url: `/wms/order/delete-list?ids=${ids.join(',')}` })
  },
  submitOrder: async (id: number) => {
    return await request.put({ url: `/wms/order/submit`, params: { id } })
  },
  approveOrder: async (id: number) => {
    return await request.put({ url: `/wms/order/approve`, params: { id } })
  },
  finishOrder: async (id: number) => {
    return await request.put({ url: `/wms/order/finish`, params: { id } })
  },
  cancelOrder: async (id: number) => {
    return await request.put({ url: `/wms/order/cancel`, params: { id } })
  },
  reverseOrder: async (id: number) => {
    return await request.put({ url: `/wms/order/reverse`, params: { id } })
  },
  exportOrder: async (params: any) => {
    return await request.download({ url: `/wms/order/export-excel`, params })
  }
}

export const OrderItemApi = {
  getOrderItems: async (params: any) => {
    return await request.get({ url: `/wms/order-item/page`, params })
  },
  getOrderItem: async (id: number) => {
    return await request.get({ url: `/wms/order-item/get?id=` + id })
  },
  createOrderItem: async (data: OrderItem) => {
    return await request.post({ url: `/wms/order-item/create`, data })
  },
  updateOrderItem: async (data: OrderItem) => {
    return await request.put({ url: `/wms/order-item/update`, data })
  },
  deleteOrderItem: async (id: number) => {
    return await request.delete({ url: `/wms/order-item/delete?id=` + id })
  }
}