import request from '@/config/axios'

// 查询经验记录分页
export const getExperienceRecordPage = async (params) => {
  return await request.get({ url: `/partner/experience-record/page`, params })
}
