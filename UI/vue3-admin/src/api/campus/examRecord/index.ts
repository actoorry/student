import request from '@/config/axios'

/** 考试记录 */
export interface ExamRecord {
  id: number // 主键
  studentId: number // 学生 id
  studentName?: string // 学生姓名（后端联表返回）
  courseId: number // 课程 id
  courseName?: string // 课程名称（后端联表返回）
  schoolYear: string // 学年（如 2026-2027）
  semester: number // 学期
  score: number // 分数
  createTime?: string // 创建时间
}

/** 考试记录分页查询参数 */
export interface ExamRecordPageReqVO extends PageParam {
  studentId?: number // 学生 id
  courseId?: number // 课程 id
  schoolYear?: string // 学年
  semester?: number // 学期
}

// 考试记录 API
export const ExamRecordApi = {
  // 查询考试记录分页
  getExamRecordPage: async (params: ExamRecordPageReqVO) => {
    return await request.get<PageResult<ExamRecord[]>>({ url: `/campus/exam-record/page`, params })
  },

  // 查询考试记录详情（表单编辑回填用）
  getExamRecord: async (id: number) => {
    return await request.get({ url: `/campus/exam-record/get?id=` + id })
  },

  // 新增考试记录
  createExamRecord: async (data: ExamRecord) => {
    return await request.post({ url: `/campus/exam-record/create`, data })
  },

  // 修改考试记录
  updateExamRecord: async (data: ExamRecord) => {
    return await request.put({ url: `/campus/exam-record/update`, data })
  },

  // 删除考试记录
  deleteExamRecord: async (id: number) => {
    return await request.delete({ url: `/campus/exam-record/delete?id=` + id })
  }
}
