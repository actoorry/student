import request from '@/config/axios'

export interface AppointmentVO {
  id?: number
  partnerId: number
  partnerName?: string
  employeeNo?: string
  deptName?: string
  dept?: number
  postCategory: string
  postLevel: string
  postId?: number
  postName?: string
  startDate?: number
  endDate?: number
  termYears?: number
  appointmentDoc?: string
  status?: string
  isCurrent?: number
  remark?: string
  createTime?: string
  daysToExpire?: number
}

export interface AppointmentBatchVO {
  partnerIds: number[]
  postCategory: string
  postLevel: string
  postId: number
  postName?: string
  startDate: number
  termYears: number
  appointmentDoc?: string
  remark?: string
}

export interface AppointmentNotifyLogVO {
  id?: number
  appointmentId?: number
  partnerId?: number
  targetUserId?: number
  targetType?: string
  notifyType?: string
  referenceDate?: string
  notifyChannel?: string
  pushStatus?: string
  templateCode?: string
  contentSnapshot?: string
  notifyMessageId?: number
  errorMsg?: string
  notifyTime?: string
  targetName?: string
  partnerName?: string
  postName?: string
  endDate?: string
}

export const AppointmentApi = {
  createAppointment: async (data: AppointmentVO) => {
    return await request.post({ url: `/hr/appointment/create`, data })
  },
  batchCreateAppointment: async (data: AppointmentBatchVO) => {
    return await request.post({ url: `/hr/appointment/batch-create`, data })
  },
  updateAppointment: async (data: AppointmentVO) => {
    return await request.put({ url: `/hr/appointment/update`, data })
  },
  deleteAppointment: async (id: number) => {
    return await request.delete({ url: `/hr/appointment/delete?id=` + id })
  },
  getAppointment: async (id: number) => {
    return await request.get({ url: `/hr/appointment/get?id=` + id })
  },
  getAppointmentPage: async (params: any) => {
    return await request.get({ url: `/hr/appointment/page`, params })
  },
  exportAppointment: async (params: any) => {
    return await request.download({ url: `/hr/appointment/export-excel`, params })
  },
  getExpiringSummary: async () => {
    return await request.get({ url: `/hr/appointment/expiring-summary` })
  },
  getNotifyLogPage: async (params: any) => {
    return await request.get({ url: `/hr/appointment/notify-log/page`, params })
  }
}
