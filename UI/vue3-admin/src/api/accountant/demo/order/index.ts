import request from '@/config/axios'

export interface DemoOrderVO {
  spuId: number
  createTime: Date
}

// 创建示例订单
export function createDemoOrder(data: DemoOrderVO) {
  return request.post({
    url: '/accountant/demo-order/create',
    data: data
  })
}

// 获得示例订单分页
export function getDemoOrderPage(query: PageParam) {
  return request.get({
    url: '/accountant/demo-order/page',
    params: query
  })
}

// 退款示例订单
export function refundDemoOrder(id: number) {
  return request.put({
    url: '/accountant/demo-order/refund?id=' + id
  })
}
