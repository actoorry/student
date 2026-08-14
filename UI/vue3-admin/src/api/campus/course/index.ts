import request from '@/config/axios'

/** 课程信息 */
export interface Course {
  id: number // 主键
  courseCode: string // 课程编号
  courseName: string // 课程名
  credit?: number // 学分
  teacherId?: number // 授课教师 id
  teacherName?: string // 授课教师姓名（后端联表返回）
  passCount?: number | null // 及格人数（后端动态返回）
  createTime?: string // 创建时间
}

/** 课程分页查询参数 */
export interface CoursePageReqVO extends PageParam {
  courseCode?: string // 课程编号
  courseName?: string // 课程名
  teacherId?: number // 授课教师 id
}

// 课程 API
export const CourseApi = {
  // 查询课程分页
  getCoursePage: async (params: CoursePageReqVO) => {
    return await request.get<PageResult<Course[]>>({ url: `/campus/course/page`, params })
  },

  // 查询课程详情
  getCourse: async (id: number) => {
    return await request.get({ url: `/campus/course/get?id=` + id })
  },

  // 新增课程
  createCourse: async (data: Course) => {
    return await request.post({ url: `/campus/course/create`, data })
  },

  // 修改课程
  updateCourse: async (data: Course) => {
    return await request.put({ url: `/campus/course/update`, data })
  },

  // 删除课程
  deleteCourse: async (id: number) => {
    return await request.delete({ url: `/campus/course/delete?id=` + id })
  },

  // 学生选课
  selectCourse: async (studentId: number, courseId: number) => {
    return await request.post({
      url: `/campus/course/select?studentId=${studentId}&courseId=${courseId}`
    })
  }
}
