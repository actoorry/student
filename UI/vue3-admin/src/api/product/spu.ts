import request from '@/config/axios'

export interface Property {
  propertyId?: number // 属性编号
  propertyName?: string // 属性名称
  valueId?: number // 属性值编号
  valueName?: string // 属性值名称
}

export interface Sku {
  id?: number // 产品 SKU 编号
  name?: string // 产品 SKU 名称
  spuId?: number // SPU 编号
  properties?: Property[] // 属性数组
  price?: number | string // 产品价格
  marketPrice?: number | string // 市场价
  costPrice?: number | string // 成本价
  barCode?: string // 产品条码
  picUrl?: string // 图片地址
  stock?: number // 库存
  weight?: number // 产品重量，单位：kg 千克
  volume?: number // 产品体积，单位：m^3 平米
  quantity?: number // 主单位数量
  firstBrokeragePrice?: number | string // 一级分销的佣金
  secondBrokeragePrice?: number | string // 二级分销的佣金
  salesCount?: number // 产品销量
  wechatVirtualProductId?: string
  wechatVirtualUploadStatus?: number
  wechatVirtualPublishStatus?: number
  wechatVirtualReviewStatus?: number
  wechatVirtualReviewFailReason?: string
  wechatVirtualLastSyncTime?: Date
}

export interface GiveCouponTemplate {
  id?: number
  name?: string // 优惠券名称
}

export interface Spu {
  id?: number
  name?: string // 产品名称
  categorySales?: number // 产品销售分类
  categoryStore?: number // 产品存储分类
  keyword?: string // 关键字
  unitId?: number | undefined // 单位
  picUrl?: string // 产品封面图
  sliderPicUrls?: string[] // 产品轮播图
  introduction?: string // 产品简介
  deliveryTypes?: number[] // 配送方式
  deliveryTemplateId?: number | undefined // 运费模版
  brandId?: number // 产品品牌编号
  specType?: boolean // 产品规格
  subCommissionType?: boolean // 分销类型
  skus?: Sku[] // sku数组
  description?: string // 产品详情
  sort?: number // 产品排序
  giveIntegral?: number // 赠送积分
  virtualSalesCount?: number // 虚拟销量
  price?: number // 产品价格
  combinationPrice?: number // 产品拼团价格
  seckillPrice?: number // 产品秒杀价格
  salesCount?: number // 产品销量
  marketPrice?: number // 市场价
  costPrice?: number // 成本价
  stock?: number // 产品库存
  createTime?: Date // 产品创建时间
  status?: number // 产品状态
  isSale?: boolean // 是否可销售
  isPurchase?: boolean // 是否可采购
  isMes?: boolean // 是否 MES 管理
  isMilitary?: boolean // 是否军创区商品（仅用于军创区展示范围）
  type?: number // 产品类型
  isWechatMiniappVirtualGoods?: boolean // 是否微信小程序虚拟商品（创建后不可修改）
  cityId?: number // 归属城市（同城特产）
}

// 获得 Spu 列表
export const getSpuPage = (params: PageParam) => {
  return request.get({ url: '/product/spu/page', params })
}

// 获得 Spu 列表 tabsCount
export const getTabsCount = () => {
  return request.get({ url: '/product/spu/get-count' })
}

// 创建产品 Spu
export const createSpu = (data: Spu) => {
  return request.post({ url: '/product/spu/create', data })
}

// 更新产品 Spu
export const updateSpu = (data: Spu) => {
  return request.put({ url: '/product/spu/update', data })
}

// 更新产品 Spu status
export const updateStatus = (data: { id: number; status: number }) => {
  return request.put({ url: '/product/spu/update-status', data })
}

// 获得产品 Spu
export const getSpu = (id: number) => {
  return request.get({ url: `/product/spu/get-detail?id=${id}` })
}

// 获得产品 Spu 详情列表
export const getSpuDetailList = (ids: number[]) => {
  return request.get({ url: `/product/spu/list?spuIds=${ids}` })
}

// 删除产品 Spu
export const deleteSpu = (id: number) => {
  return request.delete({ url: `/product/spu/delete?id=${id}` })
}

// 导出产品 Spu Excel
export const exportSpu = async (params: any) => {
  return await request.download({ url: '/product/spu/export-excel', params })
}

// 获得产品 SPU 精简列表
export const getSpuSimpleList = async () => {
  return request.get({ url: '/product/spu/list-all-simple' })
}

export interface WechatVirtualGoods {
  skuId: number
  spuId: number
  skuName: string
  wechatVirtualProductId?: string
  wechatVirtualUploadTaskId?: string
  wechatVirtualUploadStatus?: number
  wechatVirtualPublishTaskId?: string
  wechatVirtualPublishStatus?: number
  wechatVirtualReviewStatus?: number
  wechatVirtualReviewFailReason?: string
  wechatVirtualLastSyncTime?: Date
  action?: string
  actionSuccess?: boolean
  actionMessage?: string
}

export const getWechatVirtualGoodsList = (spuId: number) => {
  return request.get({ url: '/product/wechat-virtual-goods/list', params: { spuId } })
}

export const syncWechatVirtualGoods = (spuId: number) => {
  return request.post({ url: '/product/wechat-virtual-goods/sync', data: { spuId } })
}

export const publishWechatVirtualGoods = (spuId: number) => {
  return request.post({ url: '/product/wechat-virtual-goods/publish', data: { spuId } })
}

export const refreshWechatVirtualGoods = (spuId: number) => {
  return request.post({ url: '/product/wechat-virtual-goods/refresh', data: { spuId } })
}
