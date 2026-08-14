import request from '@/config/axios'

/** 教师信息 */
export interface Teacher {
  id: number // 主键
  teacherNo: string // 工号
  name: string // 姓名
  gender?: number // 性别
  title?: string // 职称
  userId?: number // 关联登录账号 id
  mobile?: string // 手机号
  createTime?: string // 创建时间
}

/** 教师分页查询参数 */
export interface TeacherPageReqVO extends PageParam {
  teacherNo?: string // 工号
  name?: string // 姓名
  gender?: number // 性别
  title?: string // 职称
}

// 教师 API
export const TeacherApi = {
  // 查询教师分页
  getTeacherPage: async (params: TeacherPageReqVO) => {
    return await request.get<PageResult<Teacher[]>>({ url: `/campus/teacher/page`, params })
  },

  // 查询教师详情
  getTeacher: async (id: number) => {
    return await request.get({ url: `/campus/teacher/get?id=` + id })
  },

  // 新增教师
  createTeacher: async (data: Teacher) => {
    return await request.post({ url: `/campus/teacher/create`, data })
  },

  // 修改教师
  updateTeacher: async (data: Teacher) => {
    return await request.put({ url: `/campus/teacher/update`, data })
  },

  // 删除教师
  deleteTeacher: async (id: number) => {
    return await request.delete({ url: `/campus/teacher/delete?id=` + id })
  }
}
