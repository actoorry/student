import request from '@/config/axios'
import type { Contract } from '@/api/hr/contract'

/**
 * 职工「我的合同」API
 * 仅操作本人数据，partnerId 由后端强制写入，前端无需传。
 */
export const MyContractApi = {
  // 查询我的合同分页
  getMyContractPage: async (params: any) => {
    return await request.get({ url: `/hr/my/contract/page`, params })
  },

  // 查询我的合同详情
  getMyContract: async (id: number): Promise<Contract> => {
    return await request.get({ url: `/hr/my/contract/get?id=` + id })
  },

  // 新增我的合同
  createMyContract: async (data: Partial<Contract>) => {
    return await request.post({ url: `/hr/my/contract/create`, data })
  },

  // 修改我的合同
  updateMyContract: async (data: Partial<Contract>) => {
    return await request.put({ url: `/hr/my/contract/update`, data })
  },

  // 删除我的合同
  deleteMyContract: async (id: number) => {
    return await request.delete({ url: `/hr/my/contract/delete?id=` + id })
  }
}
