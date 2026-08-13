import request from '@/config/axios'

// 查询积分记录分页
export const getPointRecordPage = async (params) => {
  return await request.get({ url: `/partner/point-record/page`, params })
}
