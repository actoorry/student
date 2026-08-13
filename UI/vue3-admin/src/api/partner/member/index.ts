import request from '@/config/axios'

export interface MemberVO {
  id: number
  memberType?: number
  durationQuantity?: number
  durationUnitId?: number
  mobile: string
  nickname: string
  avatar: string
  name: string
  sex: number
  areaId: number
  areaName: string
  birthday: string
  remark: string
  tagIds: number[]
  levelId: number
  groupId: number
  point: number
  experience: number
  registerIp: string
  registerTerminal: number
  createTime: string
}

export interface MemberGrantReqVO {
  userId: number
  memberType: number
  durationQuantity: number
  durationUnitId: number
  remark: string
}

// 查询会员分页
export const getMemberPage = async (params) => {
  return await request.get({ url: `/partner/member/page`, params })
}

// 查询会员详情
export const getMember = async (id: number) => {
  return await request.get({ url: `/partner/member/get?id=` + id })
}

// 修改会员
export const updateMember = async (data: MemberVO) => {
  return await request.put({ url: `/partner/member/update`, data })
}

// 赠送会员
export const grantMember = async (data: MemberGrantReqVO) => {
  return await request.post({ url: `/partner/member/grant`, data })
}
