import request from '@/config/axios'
import type { Certificate } from '@/api/hr/certificate'

/**
 * 职工「我的证书」API
 * 仅操作本人数据，partnerId 由后端强制写入，前端无需传。
 * 不暴露督查台账 /ledger 系列接口。
 */
export const MyCertificateApi = {
  // 查询我的证书分页
  getMyCertificatePage: async (params: any) => {
    return await request.get({ url: `/hr/my/certificate/page`, params })
  },

  // 查询我的证书详情
  getMyCertificate: async (id: number): Promise<Certificate> => {
    return await request.get({ url: `/hr/my/certificate/get?id=` + id })
  },

  // 新增我的证书
  createMyCertificate: async (data: Partial<Certificate>) => {
    return await request.post({ url: `/hr/my/certificate/create`, data })
  },

  // 修改我的证书
  updateMyCertificate: async (data: Partial<Certificate>) => {
    return await request.put({ url: `/hr/my/certificate/update`, data })
  },

  // 删除我的证书
  deleteMyCertificate: async (id: number) => {
    return await request.delete({ url: `/hr/my/certificate/delete?id=` + id })
  }
}
