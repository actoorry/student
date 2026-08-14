import request from '@/config/axios'

/** 学生信息 */
export interface Student {
  id: number // 主键
  studentNo: string // 学号
  name: string // 姓名
  gender?: number // 性别
  className?: string // 班级
  userId?: number // 关联登录账号 id
  mobile?: string // 手机号
  currentSemesterTotal?: number | null // 当前学期总分（需同时传 schoolYear + semester 才返回）
  createTime?: string // 创建时间
}

/** 学生分页查询参数 */
export interface StudentPageReqVO extends PageParam {
  studentNo?: string // 学号
  name?: string // 姓名
  gender?: number // 性别
  className?: string // 班级
  schoolYear?: string // 当前学年
  semester?: number // 当前学期
}

// 学生 API
export const StudentApi = {
  // 查询学生分页
  getStudentPage: async (params: StudentPageReqVO) => {
    return await request.get<PageResult<Student[]>>({ url: `/campus/student/page`, params })
  },

  // 查询学生详情
  getStudent: async (id: number) => {
    return await request.get({ url: `/campus/student/get?id=` + id })
  },

  // 新增学生
  createStudent: async (data: Student) => {
    return await request.post({ url: `/campus/student/create`, data })
  },

  // 修改学生
  updateStudent: async (data: Student) => {
    return await request.put({ url: `/campus/student/update`, data })
  },

  // 删除学生
  deleteStudent: async (id: number) => {
    return await request.delete({ url: `/campus/student/delete?id=` + id })
  }
}
