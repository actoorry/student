import request from '@/config/axios'
import type { Employee } from '@/api/hr/employee'

/** 职工「我的」公用只读 API */
export const MySelfApi = {
  /** 当前登录职工基本信息（表单只读展示，勿调管理端员工分页） */
  getMyEmployee: async (): Promise<Employee> => {
    return await request.get({ url: `/hr/my/self/employee` })
  }
}
