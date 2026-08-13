import request from '@/config/axios'

export interface PositionChangeVO {
  id?: number
  partnerId: number
  partnerName?: string
  employeeNo?: string
  changeType: string
  fromDept?: number
  toDept?: number
  fromDeptName?: string
  toDeptName?: string
  fromPostId?: number
  toPostId?: number
  fromPost?: string
  toPost?: string
  startDate?: number
  endDate?: number
  reason?: string
  docAttachment?: string
  syncEmployee?: number
  remark?: string
  createTime?: string
}

export interface PositionTimelineVO {
  sourceType: string
  changeType: string
  title: string
  fromDeptName?: string
  toDeptName?: string
  fromPostId?: number
  toPostId?: number
  fromPost?: string
  toPost?: string
  startDate?: number
  endDate?: number
  remark?: string
}

export const PositionChangeApi = {
  createPositionChange: async (data: PositionChangeVO) => {
    return await request.post({ url: `/hr/position-change/create`, data })
  },
  updatePositionChange: async (data: PositionChangeVO) => {
    return await request.put({ url: `/hr/position-change/update`, data })
  },
  deletePositionChange: async (id: number) => {
    return await request.delete({ url: `/hr/position-change/delete?id=` + id })
  },
  getPositionChange: async (id: number) => {
    return await request.get({ url: `/hr/position-change/get?id=` + id })
  },
  getPositionChangePage: async (params: any) => {
    return await request.get({ url: `/hr/position-change/page`, params })
  },
  exportPositionChange: async (params: any) => {
    return await request.download({ url: `/hr/position-change/export-excel`, params })
  },
  getPositionTimeline: async (partnerId: number) => {
    return await request.get({ url: `/hr/position-change/timeline?partnerId=` + partnerId })
  }
}
