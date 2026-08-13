import request from '@/config/axios'
import type { Employee } from '@/api/hr/employee'
import type { Certificate } from '@/api/hr/certificate'
import type { Resume } from '@/api/hr/resume'

/** 职工个人档案（只读聚合） */
export interface MyProfile {
  employee?: Employee
  certificates?: Certificate[]
  resumes?: Resume[]
}

/** 个人档案 API（只读，仅 get） */
export const MyProfileApi = {
  // 获得我的个人档案（基本信息 + 证书列表 + 履历列表）
  getMyProfile: async (): Promise<MyProfile> => {
    return await request.get({ url: `/hr/my/profile/get` })
  }
}
