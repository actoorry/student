import request from '@/config/axios'

export interface MemberLevelVO {
  id: number
  name: string
  experience: number
  level: number
  discountPercent: number
  icon: string
  backgroundUrl: string
  status: number
}

// 查询会员等级分页
export const getMemberLevelPage = async (params) => {
  return await request.get({ url: `/partner/level/page`, params })
}

// 查询会员等级详情
export const getMemberLevel = async (id: number) => {
  return await request.get({ url: `/partner/level/get?id=` + id })
}

// 查询会员等级精简列表
export const getMemberLevelSimpleList = async () => {
  return await request.get({ url: `/partner/level/list-all-simple` })
}

// 新增会员等级
export const createMemberLevel = async (data: MemberLevelVO) => {
  return await request.post({ url: `/partner/level/create`, data })
}

// 修改会员等级
export const updateMemberLevel = async (data: MemberLevelVO) => {
  return await request.put({ url: `/partner/level/update`, data })
}

// 删除会员等级
export const deleteMemberLevel = async (id: number) => {
  return await request.delete({ url: `/partner/level/delete?id=` + id })
}
