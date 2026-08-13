import request from '@/config/axios'

export interface HelpVO {
  id: number
  partnerId?: number
  nickname?: string
  mobile?: string
  name?: string
  phone?: string
  idCard?: string
  reason?: string
  applyAmount?: number
  actualAmount?: number
  materialUrls?: string[]
  status?: number
  statusName?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export const HELP_STATUS_OPTIONS = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' },
  { value: 3, label: '已撤销' }
]

export const getHelpPage = async (params: any) => {
  return await request.get({ url: '/rongjh/help/page', params })
}

export const getHelp = async (id: number) => {
  return await request.get({ url: '/rongjh/help/get?id=' + id })
}

export const approveHelp = async (data: { id: number; actualAmount: number; remark?: string }) => {
  return await request.put({ url: '/rongjh/help/approve', data })
}

export const rejectHelp = async (data: { id: number; remark?: string }) => {
  return await request.put({ url: '/rongjh/help/reject', data })
}
