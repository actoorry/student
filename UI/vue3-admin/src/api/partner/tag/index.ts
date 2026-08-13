import request from '@/config/axios'

export interface MemberTagVO {
  id: number
  name: string
}

// 查询会员标签分页
export const getMemberTagPage = async (params) => {
  return await request.get({ url: `/partner/tag/page`, params })
}

// 查询会员标签详情
export const getMemberTag = async (id: number) => {
  return await request.get({ url: `/partner/tag/get?id=` + id })
}

// 查询会员标签精简列表
export const getMemberTagSimpleList = async () => {
  return await request.get({ url: `/partner/tag/list-all-simple` })
}

// 新增会员标签
export const createMemberTag = async (data: MemberTagVO) => {
  return await request.post({ url: `/partner/tag/create`, data })
}

// 修改会员标签
export const updateMemberTag = async (data: MemberTagVO) => {
  return await request.put({ url: `/partner/tag/update`, data })
}

// 删除会员标签
export const deleteMemberTag = async (id: number) => {
  return await request.delete({ url: `/partner/tag/delete?id=` + id })
}
