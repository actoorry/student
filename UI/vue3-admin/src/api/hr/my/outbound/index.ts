import request from '@/config/axios'
import type { OutboundVO } from '@/api/hr/outbound'

/**
 * 职工「外出申请」API
 * 仅操作本人数据，partnerId 由后端强制写入，前端无需传。
 */
export const MyOutboundApi = {
  // 查询我的外出分页
  getMyOutboundPage: async (params: any) => {
    return await request.get({ url: `/hr/my/outbound/page`, params })
  },

  // 查询我的外出详情
  getMyOutbound: async (id: number): Promise<OutboundVO> => {
    return await request.get({ url: `/hr/my/outbound/get?id=` + id })
  },

  // 新增我的外出申请
  createMyOutbound: async (data: Partial<OutboundVO>) => {
    return await request.post({ url: `/hr/my/outbound/create`, data })
  },

  // 修改我的外出申请
  updateMyOutbound: async (data: Partial<OutboundVO>) => {
    return await request.put({ url: `/hr/my/outbound/update`, data })
  },

  // 删除我的外出申请
  deleteMyOutbound: async (id: number) => {
    return await request.delete({ url: `/hr/my/outbound/delete?id=` + id })
  }
}
