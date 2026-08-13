/**
 * WMS 仓库模块全局常量
 *
 * 状态类枚举已改为字典（通过 DICT_TYPE 从后端加载），
 * 此文件仅保留业务逻辑常量。
 */

import { getDictLabel, getIntDictOptions, DICT_TYPE } from '@/utils/dict'

// ==================== 虚拟仓（兼容旧版 ID 硬编码） ====================

/** 虚拟仓 ID 映射（兼容旧版，新系统使用 usage 字段） */
export const VIRTUAL_WAREHOUSE: Record<number, string> = {
  1: '供应商', 2: '客户', 3: '盘点差异', 4: '报废', 5: '生产', 6: '在途'
}

export const VIRTUAL_WAREHOUSE_IDS = Object.keys(VIRTUAL_WAREHOUSE).map(Number)

/** 判断是否为虚拟仓库（兼容旧版 ID 硬编码 + 新版 usage 字段） */
export const isVirtualWarehouse = (id: number, usage?: number): boolean => {
  if (usage !== undefined) return usage >= 4
  return VIRTUAL_WAREHOUSE_IDS.includes(id)
}

export const getVirtualWarehouseName = (id: number): string => VIRTUAL_WAREHOUSE[id] || `虚拟仓(${id})`

// ==================== 库位类型 usage ====================

export const USAGE_LABEL: Record<number, string> = {
  0: '虚拟节点', 1: '实体仓库', 2: '库区', 3: '库位',
  4: '供应商虚拟', 5: '客户虚拟', 6: '盘点差异', 7: '报废',
  8: '生产', 9: '在途'
}

export const USAGE_OPTIONS = Object.entries(USAGE_LABEL)
  .filter(([k]) => Number(k) >= 4) // 仅虚拟类型
  .map(([value, label]) => ({ value: Number(value), label }))

export const getUsageLabel = (usage: number): string => USAGE_LABEL[usage] || `未知(${usage})`

// ==================== 单据状态常量值（用于条件判断，不可用字典） ====================

export const ORDER_STATUS = {
  DRAFT: 0, PENDING: 1, APPROVED: 2, FINISHED: 3, CANCELLED: -1
} as const

/** 字典辅助：获取单据状态标签 */
export const getOrderStatusLabel = (status: number): string =>
  getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, status)

/** 字典辅助：获取单据状态选项 */
export const getOrderStatusOptions = () =>
  getIntDictOptions(DICT_TYPE.WMS_ORDER_STATUS)

// ==================== 库存影响方向 ====================

export const STOCK_IMPACT = { INBOUND: 1, OUTBOUND: 2, TRANSFER: 3 } as const

export const getStockImpactLabel = (impact: number): string => {
  if (impact === 3) return '内部调拨'
  return getDictLabel(DICT_TYPE.WMS_STOCK_IMPACT, impact)
}

export const getStockImpactOptions = () =>
  getIntDictOptions(DICT_TYPE.WMS_STOCK_IMPACT)

// ==================== 库存管控方式 ====================

export const getStockModeLabel = (mode: number): string =>
  getDictLabel(DICT_TYPE.WMS_STOCK_MODE, mode)

export const getStockModeOptions = () =>
  getIntDictOptions(DICT_TYPE.WMS_STOCK_MODE)

// ==================== 盘点类型 ====================

export const getCheckTypeLabel = (type: number): string =>
  getDictLabel(DICT_TYPE.WMS_CHECK_TYPE, type)

export const getCheckTypeOptions = () =>
  getIntDictOptions(DICT_TYPE.WMS_CHECK_TYPE)

// ==================== 出库策略 ====================

export const getRemovalStrategyLabel = (strategy: number): string =>
  getDictLabel(DICT_TYPE.WMS_REMOVAL_STRATEGY, strategy)

export const getRemovalStrategyOptions = () =>
  getIntDictOptions(DICT_TYPE.WMS_REMOVAL_STRATEGY)

// ==================== 盘点调整状态 ====================

export const ADJUST_STATUS = { DRAFT: 0, FINISHED: 1 } as const

export const getAdjustStatusLabel = (status: number): string =>
  getDictLabel(DICT_TYPE.WMS_ADJUST_STATUS, status)

export const getAdjustStatusOptions = () =>
  getIntDictOptions(DICT_TYPE.WMS_ADJUST_STATUS)

// ==================== 库存流水业务类型 ====================

export const getBizTypeLabel = (type: string): string =>
  getDictLabel(DICT_TYPE.WMS_BIZ_TYPE, type)

export const getBizTypeOptions = () =>
  getIntDictOptions(DICT_TYPE.WMS_BIZ_TYPE)

// ==================== 仓库层级（树形结构业务逻辑） ====================

export const WAREHOUSE_LEVEL = {
  ROOT: 0, AREA: 1, LOCATION: 2
} as const

export const WAREHOUSE_LEVEL_NAME: Record<number, string> = {
  [WAREHOUSE_LEVEL.ROOT]: '实体仓库',
  [WAREHOUSE_LEVEL.AREA]: '库区',
  [WAREHOUSE_LEVEL.LOCATION]: '库位'
}