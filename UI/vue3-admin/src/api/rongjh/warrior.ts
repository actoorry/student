import request from '@/config/axios'

export interface WarriorVO {
  id: number
  nickname?: string
  mobile?: string
  name?: string
  idCard?: string
  phone?: string
  type?: string
  typeName?: string
  province?: string
  stateId?: number
  city?: string
  militaryBranch?: string
  serviceYears?: number
  serviceUnit?: string
  serviceYear?: string
  description?: string
  certificateImg?: string
  status?: number
  statusName?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export const WARRIOR_STATUS_OPTIONS = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' }
]

export const WARRIOR_TYPE_OPTIONS = [
  { value: 'SELF', label: '战友' },
  { value: 'MARTYR', label: '烈士遗属' },
  { value: 'SACRIFICE', label: '因公牺牲遗属' },
  { value: 'ILLNESS', label: '病故遗属' },
  { value: 'FAMILY', label: '军人家属' }
]

/** 三属及军人家属（非战友本人） */
export const SANSHU_TYPE_OPTIONS = WARRIOR_TYPE_OPTIONS.filter((item) => item.value !== 'SELF')

export const getWarriorPage = async (params: any) => {
  return await request.get({ url: '/rongjh/warrior/page', params })
}

export const getWarrior = async (id: number) => {
  return await request.get({ url: '/rongjh/warrior/get?id=' + id })
}

export const approveWarrior = async (data: { id: number; remark?: string }) => {
  return await request.put({ url: '/rongjh/warrior/approve', data })
}

export const rejectWarrior = async (data: { id: number; remark?: string }) => {
  return await request.put({ url: '/rongjh/warrior/reject', data })
}

export const blacklistWarrior = async (data: { id: number; remark?: string }) => {
  return await request.put({ url: '/rongjh/warrior/blacklist', data })
}
