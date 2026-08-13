import request from '@/config/axios'

/** 员工信息 */
export interface Employee {
  id: number // 主键
  partnerId?: number // 关联 partner.id

  // 基础信息，对应 partner 表
  name?: string // 姓名
  idCard?: string // 身份证号
  sex?: number // 性别
  birthday?: number // 出生日期，毫秒时间戳
  avatar?: string // 头像
  detailAddress?: string // 详细地址
  remark?: string // 备注

  // 查询展示字段
  deptName?: string // 部门名称

  // 扩展信息，对应 partner_employee 表
  employeeNo?: string // 员工工号
  employeeMobile?: string // 员工手机号
  dept: number // 所属部门，关联 system_dept.id
  postId?: number // 岗位 ID，关联 system_post.id
  postName?: string // 岗位名称，列表展示用
  personnelCategory: string // 人员类别
  professionalTitle: string // 职称
  politicalStatus: string // 政治面貌
  partyJoinDate?: number // 入党时间，毫秒时间戳
  careerStartDate?: number // 参加工作时间
  hireDate?: number // 加入单位时间
  position: string // 岗位，历史文本字段
  duty: string // 职务
  appointmentDate?: number // 任职时间
  highestEducation: string // 最高学历
  fullTimeEducation: string // 全日制学历
  establishmentStatus: string // 是否在编
  personnelIdentity: string // 人员身份
  recruitmentSource: string // 人员来源
  entryMode: string // 进入方式
  employmentStatus: string // 在职状态
  ethnicity: string // 民族
  nativeProvince?: string // 籍贯-省
  nativeCity?: string // 籍贯-市
  bankCard: string // 银行卡卡号
  homeInformation: string // 家庭信息
  schoolMajor: string // 毕业院校及专业
  professionalCategory: string // 执业医师或技师类别
  nameAbbreviation: string // 姓名缩写
}

// 员工 API
export const EmployeeApi = {
  // 查询员工分页
  getEmployeePage: async (params: any) => {
    return await request.get({ url: `/hr/employee/page`, params })
  },

  // 查询员工详情
  getEmployee: async (id: number) => {
    return await request.get({ url: `/hr/employee/get?id=` + id })
  },

  // 新增员工
  createEmployee: async (data: Employee) => {
    return await request.post({ url: `/hr/employee/create`, data })
  },

  // 修改员工
  updateEmployee: async (data: Employee) => {
    return await request.put({ url: `/hr/employee/update`, data })
  },

  // 删除员工
  deleteEmployee: async (id: number) => {
    return await request.delete({ url: `/hr/employee/delete?id=` + id })
  },

  // 批量删除员工
  deleteEmployeeList: async (ids: number[]) => {
    return await request.delete({ url: `/hr/employee/delete-list?ids=${ids.join(',')}` })
  },

  // 导出员工 Excel
  exportEmployee: async (params: any) => {
    return await request.download({ url: `/hr/employee/export-excel`, params })
  }
}
