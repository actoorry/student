import request from '@/config/axios'

export interface PartnerLimitConfigVO {
  id?: number
  type?: number
  userIds?: string
  deptIds?: string
  maxCount?: number
  dealCountEnabled?: boolean
}

/**
 * 客户限制配置类型
 */
export enum LimitConfType {
  /**
   * 拥有客户数限制
   */
  PARTNER_QUANTITY_LIMIT = 1,
  /**
   * 锁定客户数限制
   */
  PARTNER_LOCK_LIMIT = 2
}

// 查询客户限制配置列表
export const getPartnerLimitConfigPage = async (params) => {
  return await request.get({ url: `/partner/sales-limit-config/page`, params })
}

// 查询客户限制配置详情
export const getPartnerLimitConfig = async (id: number) => {
  return await request.get({ url: `/partner/sales-limit-config/get?id=` + id })
}

// 新增客户限制配置
export const createPartnerLimitConfig = async (data: PartnerLimitConfigVO) => {
  return await request.post({ url: `/partner/sales-limit-config/create`, data })
}

// 修改客户限制配置
export const updatePartnerLimitConfig = async (data: PartnerLimitConfigVO) => {
  return await request.put({ url: `/partner/sales-limit-config/update`, data })
}

// 删除客户限制配置
export const deletePartnerLimitConfig = async (id: number) => {
  return await request.delete({ url: `/partner/sales-limit-config/delete?id=` + id })
}
