import request from '@/config/axios'

export interface UserVO {
  id: number
  username: string
  nickname: string
  deptId: number
  postIds: number[]
  email: string
  mobile: string
  sex: number
  avatar: string
  loginIp: string
  status: number
  remark: string
  loginDate: Date
  createTime: Date
  partnerId: number
}

export type PartnerVO = UserVO

// 查询用户管理列表
export const getUserPage = (params: PageParam) => {
  return request.get({ url: '/system/user/page', params })
}

// 查询用户详情
export const getUser = (id: number) => {
  return request.get({ url: '/system/user/get?id=' + id })
}

// 新增用户
export const createUser = (data: UserVO) => {
  return request.post({ url: '/system/user/create', data })
}

// 修改用户
export const updateUser = (data: UserVO) => {
  return request.put({ url: '/system/user/update', data })
}

// 删除用户
export const deleteUser = (id: number) => {
  return request.delete({ url: '/system/user/delete?id=' + id })
}

// 批量删除用户
export const deleteUserList = (ids: number[]) => {
  return request.delete({ url: '/system/user/delete-list', params: { ids: ids.join(',') } })
}

// 导出用户
export const exportUser = (params: any) => {
  return request.download({ url: '/system/user/export-excel', params })
}

// 下载用户导入模板
export const importUserTemplate = () => {
  return request.download({ url: '/system/user/get-import-template' })
}

// 用户密码重置
export const resetUserPassword = (id: number, password: string) => {
  const data = {
    id,
    password
  }
  return request.put({ url: '/system/user/update-password', data: data })
}

// 用户状态修改
export const updateUserStatus = (id: number, status: number) => {
  const data = {
    id,
    status
  }
  return request.put({ url: '/system/user/update-status', data: data })
}

// 查询客商用户详情（共享主键：user.id == partner.id）
export const getPartner = (id: number) => {
  return request.get({ url: '/system/user/get?id=' + id })
}

// 新增客商用户
export const createPartner = (data: UserVO) => {
  return request.post({ url: '/system/user/create', data })
}

// 修改客商用户
export const updatePartner = (data: UserVO) => {
  return request.put({ url: '/system/user/update', data })
}

// 查询客商用户分页
export const getPartnerPage = (params: PageParam) => {
  return request.get({ url: '/system/user/page', params })
}

// 修改客商用户状态
export const updatePartnerStatus = (id: number, status: number) => {
  return request.put({ url: '/system/user/update-status', data: { id, status } })
}

// 获取用户精简信息列表
export const getSimpleUserList = (): Promise<UserVO[]> => {
  return request.get({ url: '/system/user/simple-list' })
}

// 别名：客商/人员用户语义（实际调用 system/user 接口）
export const deletePartner = deleteUser
export const deletePartnerList = deleteUserList
