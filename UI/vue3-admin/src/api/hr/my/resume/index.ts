import request from '@/config/axios'
import type { Resume } from '@/api/hr/resume'

/**
 * 职工「我的履历」API
 * 仅操作本人数据，partnerId 由后端强制写入，前端无需传。
 */
export const MyResumeApi = {
  // 查询我的履历分页
  getMyResumePage: async (params: any) => {
    return await request.get({ url: `/hr/my/resume/page`, params })
  },

  // 查询我的履历详情
  getMyResume: async (id: number): Promise<Resume> => {
    return await request.get({ url: `/hr/my/resume/get?id=` + id })
  },

  // 新增我的履历
  createMyResume: async (data: Partial<Resume>) => {
    return await request.post({ url: `/hr/my/resume/create`, data })
  },

  // 修改我的履历
  updateMyResume: async (data: Partial<Resume>) => {
    return await request.put({ url: `/hr/my/resume/update`, data })
  },

  // 删除我的履历
  deleteMyResume: async (id: number) => {
    return await request.delete({ url: `/hr/my/resume/delete?id=` + id })
  }
}
