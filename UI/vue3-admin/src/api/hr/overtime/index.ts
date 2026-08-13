import request from '@/config/axios'

/** 加班登记信息 */
export interface Overtime {
  id: number // 主键
  partnerId?: number // 关联员工 id
  name?: string // 姓名（联表）
  employeeNo?: string // 员工工号
  dept?: number // 所属科室 id
  deptName?: string // 所属科室（联表）
  overtimeType: string // 加班类型（字典 hr_overtime_type）
  overtimeReason: string // 加班原因（字典 hr_overtime_reason）
  workSummary: string // 工作内容简述
  overtimeDate: number // 加班日期（毫秒时间戳）
  startTime: number // 加班开始时间（毫秒时间戳）
  endTime: number // 加班结束时间（毫秒时间戳）
  durationHours?: number // 加班时长（小时）
  workDeptId?: number // 实际值班科室 id
  workDeptName?: string // 实际值班科室（联表）
  workLocation?: string // 值班地点
  holidayName?: string // 节假日名称（仅 legal_holiday_duty）
  remark?: string // 备注
  createTime?: number // 创建时间
}

// 加班登记 API
export const OvertimeApi = {
  // 查询加班登记分页
  getOvertimePage: async (params: any) => {
    return await request.get({ url: `/hr/overtime/page`, params })
  },

  // 查询加班登记详情
  getOvertime: async (id: number) => {
    return await request.get({ url: `/hr/overtime/get?id=` + id })
  },

  // 新增加班登记
  createOvertime: async (data: Partial<Overtime>) => {
    return await request.post({ url: `/hr/overtime/create`, data })
  },

  // 修改加班登记
  updateOvertime: async (data: Partial<Overtime>) => {
    return await request.put({ url: `/hr/overtime/update`, data })
  },

  // 删除加班登记
  deleteOvertime: async (id: number) => {
    return await request.delete({ url: `/hr/overtime/delete?id=` + id })
  },

  /** 批量删除加班登记 */
  deleteOvertimeList: async (ids: number[]) => {
    return await request.delete({ url: `/hr/overtime/delete-list?ids=${ids.join(',')}` })
  },

  // 导出加班登记 Excel
  exportOvertime: async (params: any) => {
    return await request.download({ url: `/hr/overtime/export-excel`, params })
  }
}
