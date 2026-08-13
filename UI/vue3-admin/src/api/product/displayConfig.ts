import request from '@/config/axios'

/**
 * 商品页面展示配置
 */
export interface ProductDisplayConfigVO {
  /**
   * 展示场景编码
   */
  sceneCode: string
  /**
   * 展示场景名称
   */
  sceneName: string
  /**
   * 配置状态
   */
  configState: 'UNCONFIGURED' | 'ACTIVE' | 'DISABLED' | 'INVALID_CATEGORY' | 'MULTIPLE_CATEGORIES'
  /**
   * 存储的商品销售分类编号列表（原始值）
   */
  storedCategoryIds: number[]
  /**
   * 当前有效的商品销售分类编号
   */
  categoryId?: number
  /**
   * 当前有效的商品销售分类名称
   */
  categoryName?: string
  /**
   * 开启状态
   */
  status: number
  /**
   * 排序
   */
  sort: number
  /**
   * 备注
   */
  remark?: string
  /**
   * 更新时间
   */
  updateTime?: string
}

/**
 * 商品页面展示配置更新
 */
export interface ProductDisplayConfigUpdateReqVO {
  /**
   * 展示场景编码
   */
  sceneCode: string
  /**
   * 商品销售分类编号
   */
  categoryId: number
  /**
   * 开启状态
   */
  status: number
  /**
   * 排序
   */
  sort: number
  /**
   * 备注
   */
  remark?: string
  /**
   * 期望的更新时间（用于并发控制）
   */
  updateTime?: string
}

// 获得商品页面展示配置列表
export const getDisplayConfigList = (): Promise<ProductDisplayConfigVO[]> => {
  return request.get({ url: '/product/display-config/list' })
}

// 更新商品页面展示配置
export const updateDisplayConfig = (data: ProductDisplayConfigUpdateReqVO): Promise<ProductDisplayConfigVO> => {
  return request.put({ url: '/product/display-config/update', data })
}
