import request from '@/config/axios'

export interface SkuVO {
  id: number
  spuId: number
  spuName?: string // SPU 名称（冗余，方便前端展示）
  properties?: { propertyId: number; propertyName: string; valueId: number; valueName: string }[]
  price: number // 商品价格，单位：分
  marketPrice: number // 市场价，单位：分
  costPrice: number // 成本价，单位：分
  barCode: string // 商品条码
  picUrl: string // 图片地址
  stock: number // 库存
  quantity?: number // 主单位数量
  weight: number // 商品重量，单位：kg 千克
  volume: number // 商品体积，单位：m^3 平米
  salesCount: number // 商品销量
  unitId?: number // 产品单位编号（关联 product_unit.id）
  unitName?: string // 产品单位名称
  status?: number // 商品状态（来自 SPU）
}

// 获得 SKU 精简列表（商机/合同选产品下拉用）
export const getSkuSimpleList = async () => {
  return await request.get({ url: '/product/sku/simple-list' })
}

// 获得 SKU 分页
export const getSkuPage = async (params: any) => {
  return await request.get({ url: '/product/sku/page', params })
}

// 获得 SKU 详情
export const getSku = async (id: number) => {
  return await request.get({ url: '/product/sku/get?id=' + id })
}
