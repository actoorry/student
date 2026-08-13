import request from '@/config/axios'

export interface SkuOption {
  id: number
  skuId: number
  spuId: number
  spuName: string
  barCode?: string
  price?: number
  displayName: string
  /** 安全库存下限：低于该值触发缺货预警 */
  minStock?: string | number | null
  /** 安全库存上限：高于该值触发积压预警 */
  maxStock?: string | number | null
}

export const WmsProductApi = {
  /** 模糊搜索 SKU（远程搜索下拉用） */
  searchSku: async (keyword: string, limit = 20): Promise<SkuOption[]> => {
    return await request.get({ url: `/wms/product/search-sku`, params: { keyword, limit } })
  },

  /** 查询 SKU 信息 */
  getSku: async (id: number): Promise<SkuOption> => {
    return await request.get({ url: `/wms/product/get-sku?id=` + id })
  },

  /** 批量查询 SKU 列表 */
  getSkuList: async (ids: number[]): Promise<SkuOption[]> => {
    return await request.get({ url: `/wms/product/list-sku?ids=` + ids.join(',') })
  },

  /** 获取 SKU 显示名称 */
  getSkuName: async (id: number): Promise<string> => {
    return await request.get({ url: `/wms/product/sku-name?id=` + id })
  }
}