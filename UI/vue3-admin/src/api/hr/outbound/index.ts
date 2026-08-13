import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 外出记录 */
export interface OutboundVO {
  id?: number
  partnerId: number // 关联员工 partner.id
  partnerName?: string // 员工姓名（联表展示）
  employeeNo?: string // 员工工号（联表展示）
  deptName?: string // 部门（联表展示）
  recordType: string // 外出类型 hr_outbound_type
  province?: string // 省份
  city?: string // 市
  county?: string // 区/县
  organization?: string // 单位
  practiceName?: string // 进修/培训名称
  startDate?: number // 开始日期（毫秒时间戳）
  endDate?: number // 结束日期
  durationDays?: number // 天数（系统计算）
  supportYears?: number // 服务年限（仅下乡支援）
  continuingEducationCredit?: number // 继教学分
  effective?: number // 是否计入汇总 1是 0否
  summary?: string // 总结
  createTime?: string
}

/** 外出汇总（员工档案 Tab 用） */
export interface OutboundSummaryVO {
  supportYears: number // 累计下乡支援年限
  continuingEducationCredit: number // 累计继教学分
}

export const OutboundApi = {
  // 新增外出记录
  createOutbound: async (data: OutboundVO) => {
    return await request.post({ url: `/hr/outbound/create`, data })
  },

  // 更新外出记录
  updateOutbound: async (data: OutboundVO) => {
    return await request.put({ url: `/hr/outbound/update`, data })
  },

  // 获得外出记录详情
  getOutbound: async (id: number) => {
    return await request.get({ url: `/hr/outbound/get?id=` + id })
  },

  // 获得外出记录分页
  getOutboundPage: async (params: any) => {
    return await request.get({ url: `/hr/outbound/page`, params })
  },

  // 删除外出记录
  deleteOutbound: async (id: number) => {
    return await request.delete({ url: `/hr/outbound/delete?id=` + id })
  },

  // 导出外出记录 Excel
  exportOutbound: async (params: any) => {
    return await request.download({ url: `/hr/outbound/export-excel`, params })
  },

  // 获得员工外出汇总（累计年限 + 累计学分）
  getOutboundSummary: async (partnerId: number) => {
    return await request.get({ url: `/hr/outbound/summary?partnerId=` + partnerId })
  }
}
