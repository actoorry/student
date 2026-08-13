import request from '@/config/axios'

export interface MemberGroupVO {
  id: number
  name: string
  remark: string
  status: number
}

// 查询会员分组分页
export const getMemberGroupPage = async (params) => {
  return await request.get({ url: `/partner/group/page`, params })
}

// 查询会员分组详情
export const getMemberGroup = async (id: number) => {
  return await request.get({ url: `/partner/group/get?id=` + id })
}

// 查询会员分组精简列表
export const getMemberGroupSimpleList = async () => {
  return await request.get({ url: `/partner/group/list-all-simple` })
}

// 新增会员分组
export const createMemberGroup = async (data: MemberGroupVO) => {
  return await request.post({ url: `/partner/group/create`, data })
}

// 修改会员分组
export const updateMemberGroup = async (data: MemberGroupVO) => {
  return await request.put({ url: `/partner/group/update`, data })
}

// 删除会员分组
export const deleteMemberGroup = async (id: number) => {
  return await request.delete({ url: `/partner/group/delete?id=` + id })
}
