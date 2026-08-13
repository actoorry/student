import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 履历信息 */
export interface Resume {
  id: number // 主键
  partnerId?: number // 关联员工
  name: string // 姓名
  employeeNo: string // 员工工号
  dept: string // 所在部门 ID
  deptName?: string // 所在部门名称（联表展示）
  resumeContent: string // 个人简历
  awardsPunishments: string // 奖惩情况
  certificates: string // 证书
  papers: string // 论文
  annualReview: string // 年度考核情况
  remark: string // 备注
  province: string // 省份
  city: string // 市
  county: string // 区/县
  createTime?: number // 创建时间
}

// 履历 API
export const ResumeApi = {
  // 查询履历分页
  getResumePage: async (params: any) => {
    return await request.get({ url: `/hr/resume/page`, params })
  },

  // 查询履历详情
  getResume: async (id: number) => {
    return await request.get({ url: `/hr/resume/get?id=` + id })
  },

  // 新增履历
  createResume: async (data: Resume) => {
    return await request.post({ url: `/hr/resume/create`, data })
  },

  // 修改履历
  updateResume: async (data: Resume) => {
    return await request.put({ url: `/hr/resume/update`, data })
  },

  // 删除履历
  deleteResume: async (id: number) => {
    return await request.delete({ url: `/hr/resume/delete?id=` + id })
  },

  /** 批量删除履历 */
  deleteResumeList: async (ids: number[]) => {
    return await request.delete({ url: `/hr/resume/delete-list?ids=${ids.join(',')}` })
  },

  // 导出履历 Excel
  exportResume: async (params) => {
    return await request.download({ url: `/hr/resume/export-excel`, params })
  },
}
