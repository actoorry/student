import request from '@/config/axios'
import { TransferReqVO } from '@/api/crm/permission'

export interface PartnerVO {
  id: number // 编号
  name: string // 真实姓名
  nickname?: string // 客户昵称
  sex?: number // 性别
  birthday?: Date // 生日
  followUpStatus: boolean // 跟进状态
  contactLastTime: Date // 最后跟进时间
  contactLastContent: string // 最后跟进内容
  contactNextTime: Date // 下次联系时间
  ownerUserId: number // 负责人的用户编号
  ownerUserName?: string // 负责人的用户名称
  ownerUserDept?: string // 负责人的部门名称
  lockStatus?: boolean
  dealStatus?: boolean
  mobile: string // 手机号
  telephone: string // 电话
  qq: string // QQ
  wechat: string // wechat
  email: string // email
  areaId: number // 所在地
  areaName?: string // 所在地名称
  detailAddress: string // 详细地址
  industryId: number // 所属行业
  level: number // 客户等级
  source: number // 客户来源
  remark: string // 备注
  creator: string // 创建人
  creatorName?: string // 创建人名称
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

// 查询客户列表
export const getPartnerPage = async (params) => {
  return await request.get({ url: `/partner/sales/page`, params })
}

// 进入公海客户提醒的客户列表
export const getPutPoolRemindPartnerPage = async (params) => {
  return await request.get({ url: `/partner/sales/put-pool-remind-page`, params })
}

// 获得待进入公海客户数量
export const getPutPoolRemindPartnerCount = async () => {
  return await request.get({ url: `/partner/sales/put-pool-remind-count` })
}

// 获得今日需联系客户数量
export const getTodayContactPartnerCount = async () => {
  return await request.get({ url: `/partner/sales/today-contact-count` })
}

// 获得分配给我、待跟进的线索数量的客户数量
export const getFollowPartnerCount = async () => {
  return await request.get({ url: `/partner/sales/follow-count` })
}

// 查询客户详情
export const getPartner = async (id: number) => {
  return await request.get({ url: `/partner/sales/get?id=` + id })
}

// 新增客户
export const createPartner = async (data: PartnerVO) => {
  return await request.post({ url: `/partner/sales/create`, data })
}

// 修改客户
export const updatePartner = async (data: PartnerVO) => {
  return await request.put({ url: `/partner/sales/update`, data })
}

// 更新客户的成交状态
export const updatePartnerDealStatus = async (id: number, dealStatus: boolean) => {
  return await request.put({ url: `/partner/sales/update-deal-status`, params: { id, dealStatus } })
}

// 删除客户
export const deletePartner = async (id: number) => {
  return await request.delete({ url: `/partner/sales/delete?id=` + id })
}

// 导出客户 Excel
export const exportPartner = async (params: any) => {
  return await request.download({ url: `/partner/sales/export-excel`, params })
}

// 下载客户导入模板
export const importPartnerTemplate = () => {
  return request.download({ url: '/partner/sales/get-import-template' })
}

// 导入客户
export const handleImport = async (formData) => {
  return await request.upload({ url: `/partner/sales/import`, data: formData })
}

// 客户列表
export const getPartnerSimpleList = async () => {
  return await request.get({ url: `/partner/sales/simple-list` })
}

// ======================= 业务操作 =======================

// 客户转移
export const transferPartner = async (data: TransferReqVO) => {
  return await request.put({ url: '/partner/sales/transfer', data })
}

// 锁定/解锁客户
export const lockPartner = async (id: number, lockStatus: boolean) => {
  return await request.put({ url: `/partner/sales/lock`, data: { id, lockStatus } })
}

// 领取公海客户
export const receivePartner = async (ids: any[]) => {
  return await request.put({ url: '/partner/sales/receive', params: { ids: ids.join(',') } })
}

// 分配公海给对应负责人
export const distributePartner = async (ids: any[], ownerUserId: number) => {
  return await request.put({
    url: '/partner/sales/distribute',
    data: { ids: ids, ownerUserId }
  })
}

// 客户放入公海
export const putPartnerPool = async (id: number) => {
  return await request.put({ url: `/partner/sales/put-pool?id=${id}` })
}
