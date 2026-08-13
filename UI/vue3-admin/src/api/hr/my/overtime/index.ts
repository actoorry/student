import request from '@/config/axios'
import type { Overtime } from '@/api/hr/overtime'

/**
 * 职工「加班登记」API
 * 仅操作本人数据，partnerId 由后端强制写入，前端无需传。
 */
export const MyOvertimeApi = {
  // 查询我的加班分页
  getMyOvertimePage: async (params: any) => {
    return await request.get({ url: `/hr/my/overtime/page`, params })
  },

  // 查询我的加班详情
  getMyOvertime: async (id: number): Promise<Overtime> => {
    return await request.get({ url: `/hr/my/overtime/get?id=` + id })
  },

  // 新增我的加班登记
  createMyOvertime: async (data: Partial<Overtime>) => {
    return await request.post({ url: `/hr/my/overtime/create`, data })
  },

  // 修改我的加班登记
  updateMyOvertime: async (data: Partial<Overtime>) => {
    return await request.put({ url: `/hr/my/overtime/update`, data })
  },

  // 删除我的加班登记
  deleteMyOvertime: async (id: number) => {
    return await request.delete({ url: `/hr/my/overtime/delete?id=` + id })
  }
}
