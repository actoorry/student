import request from '@/config/axios'

export interface PartnerSignInRecordPageReqVO {
  pageNo: number
  pageSize: number
  userId?: number
  day?: number
  createTime?: Date[]
}

export const getSignInRecordPage = async (params: PartnerSignInRecordPageReqVO) => {
  return await request.get({ url: `/partner/sign-in-record/page`, params })
}
