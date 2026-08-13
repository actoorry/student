import request from '@/config/axios'

export interface CertificationRecordVO {
  id: number
  partnerId: number
  certType: string
  requestKey: string
  providerCode: string
  providerSeqNo: string
  code: string
  message: string
  state: string
  stateLabel: string
  charged: boolean
  requestParams: string
  responseBody: string
  createTime: string
  partnerName: string
  partnerMobile: string
}

// 查询认证记录分页
export const getCertificationRecordPage = async (params) => {
  return await request.get({ url: `/partner/certification-record/page`, params })
}

// 查询认证记录详情
export const getCertificationRecord = async (id: number) => {
  return await request.get({ url: `/partner/certification-record/get?id=` + id })
}
