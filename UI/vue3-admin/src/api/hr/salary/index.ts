import request from '@/config/axios'

/** 薪资信息 */
export interface Salary {
          id: number; // 主键
          partnerId?: number; // 关联员工 id
          name: string; // 姓名
          deptName?: string; // 部门名称
          dept?: number; // 所在部门（关联 system_dept.id）
          employeeNo: string; // 人员编号
          personnelCategory: string; // 人员类别
          year: number; // 年份
          month: number; // 月份
          basicSalary: number; // 岗位工资
          salaryGrade: number; // 薪级工资
          unitAllowance: number; // 单位职补
          postAllowance: number; // 岗位津贴
          onlyChildAllowance: number; // 独生子女
          huiEthnicAllowance: number; // 回民补贴
          familyPlanningAllowance: number; // 计生兼职
          welfareFee: number; // 福利费
          officialTransportAllowance: number; // 公务交通补贴
          rentAllowance: number; // 提租补贴
          rehireFee: number; // 反聘费
          backPay: number; // 补发工资
          otherWage: number; // 其他工资
          basicPerformance: number; // 基础性绩效
          performanceSalary: number; // 绩效工资
          grossSalaryTotal: number; // 应发合计
          socialSecurity: number; // 社保基金
          medicalInsurance: number; // 医保金
          occupationalAnnuity: number; // 职业年金
          unemploymentInsurance: number; // 失业金
          housingFund: number; // 住房公积金
          unionFee: number; // 工会经费
          rentFee: number; // 房租费用
          sickLeaveDeduction: number; // 病事假
          incomeTax: number; // 代扣所得税
          otherDeduction: number; // 其他扣款
          totalDeduction: number; // 扣款合计
          netSalaryTotal: number; // 实发合计
          taxBase: number; // 所得基数
          childEducation: number; // 子女教育
          continuingEducation: number; // 继续教育
          housingLoanInterest: number; // 住房贷款利息
          housingRent: number; // 住房租金
          elderlySupport: number; // 老人赡养费
          otherLegalDeduction: number; // 其他合法扣除
  }

// 薪资 API
export const SalaryApi = {
  // 查询薪资分页
  getSalaryPage: async (params: any) => {
    return await request.get({ url: `/hr/salary/page`, params })
  },

  // 查询薪资详情
  getSalary: async (id: number) => {
    return await request.get({ url: `/hr/salary/get?id=` + id })
  },

  // 新增薪资
  createSalary: async (data: Salary) => {
    return await request.post({ url: `/hr/salary/create`, data })
  },

  // 修改薪资
  updateSalary: async (data: Salary) => {
    return await request.put({ url: `/hr/salary/update`, data })
  },

  // 删除薪资
  deleteSalary: async (id: number) => {
    return await request.delete({ url: `/hr/salary/delete?id=` + id })
  },

  /** 批量删除薪资 */
  deleteSalaryList: async (ids: number[]) => {
    return await request.delete({ url: `/hr/salary/delete-list?ids=${ids.join(',')}` })
  },

  // 导出薪资 Excel
  exportSalary: async (params) => {
    return await request.download({ url: `/hr/salary/export-excel`, params })
  },

  // 薪资导入预检：解析文件名年月、校验表头、查同年月同工号重复
  checkSalaryImport: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    const res: any = await request.upload({
      url: `/hr/salary/check-import`,
      data: formData
    })
    return res.data
  },

  // 薪资导入：confirmOverwrite=true 时覆盖已存在的同年月同工号记录
  importSalary: async (file: File, confirmOverwrite: boolean) => {
    const formData = new FormData()
    formData.append('file', file)
    const res: any = await request.upload({
      url: `/hr/salary/import?confirmOverwrite=${confirmOverwrite}`,
      data: formData
    })
    return res.data
  },

  // 下载薪资导入模板（39 列客户工资表格式）
  importSalaryTemplate: async () => {
    return await request.download({ url: `/hr/salary/get-import-template` })
  }
}
