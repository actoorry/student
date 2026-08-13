import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 人员证书信息 */
export interface Certificate {
  id: number // 主键
  partnerId?: number // 关联员工
  name?: string // 姓名（联表 partner）
  employeeNo?: string // 员工工号
  dept?: number // 所在部门（关联 system_dept.id）
  deptName?: string // 科室名（联表展示）
  certificateType?: string // 证书类型（字典 hr_certificate_type）
  certificateName?: string // 证书名称
  certificateNo?: string // 证书编号
  issuingAuthority?: string // 发证机关
  issueDate?: number // 发证日期（毫秒时间戳）
  expireDate?: number // 到期日期
  lastAssessmentDate?: number // 上次考核日
  nextAssessmentDate?: number // 下次考核日
  attachment?: string // 证书文件 URL
  remark?: string // 备注
  status?: string // 计算状态：valid/expiring/expiring_60/expiring_90/expired/assessment_overdue/assessment_due_60
  createTime?: number // 创建时间
}

/** 督查台账汇总 */
export interface CertificateLedgerSummary {
  expiredCount: number
  expiringCount: number // 30 天内（累计基数）
  expiring60Count: number // 60 天内（累计，含 30 天）
  expiring90Count: number // 90 天内（累计，含 30/60 天）
  assessmentOverdueCount: number
  assessmentDue60Count: number // 考核前 60 天
}

/** 证书推送日志 */
export interface CertificateNotifyLog {
  id: number
  certificateId?: number
  partnerId?: number
  targetUserId?: number
  targetType?: string // self / dept_leader / hr
  notifyType?: string // expired / expire_90 / expire_60 / expire_30 / assessment_60
  referenceDate?: string | Dayjs
  notifyChannel?: string // internal / zhiye
  pushStatus?: string // success / failed / skipped
  templateCode?: string
  contentSnapshot?: string
  notifyMessageId?: number
  errorMsg?: string
  notifyTime?: number
  // 联表字段
  targetName?: string
  certificateName?: string
  certificateNo?: string
  certificateType?: string
  expireDate?: string | Dayjs
  nextAssessmentDate?: string | Dayjs
}

// 人员证书 API
export const CertificateApi = {
  // 查询证书分页（证书管理页，全量）
  getCertificatePage: async (params: any) => {
    return await request.get({ url: `/hr/certificate/page`, params })
  },

  // 督查台账分页（默认只显示风险证书）
  getCertificateLedgerPage: async (params: any) => {
    return await request.get({ url: `/hr/certificate/ledger-page`, params })
  },

  // 督查台账汇总
  getCertificateLedgerSummary: async () => {
    return await request.get({ url: `/hr/certificate/ledger-summary` })
  },

  // 查询证书详情
  getCertificate: async (id: number) => {
    return await request.get({ url: `/hr/certificate/get?id=` + id })
  },

  // 新增证书
  createCertificate: async (data: Certificate) => {
    return await request.post({ url: `/hr/certificate/create`, data })
  },

  // 修改证书
  updateCertificate: async (data: Certificate) => {
    return await request.put({ url: `/hr/certificate/update`, data })
  },

  // 删除证书
  deleteCertificate: async (id: number) => {
    return await request.delete({ url: `/hr/certificate/delete?id=` + id })
  },

  /** 批量删除证书 */
  deleteCertificateList: async (ids: number[]) => {
    return await request.delete({ url: `/hr/certificate/delete-list?ids=${ids.join(',')}` })
  },

  // 导出证书 Excel（全量）
  exportCertificate: async (params) => {
    return await request.download({ url: `/hr/certificate/export-excel`, params })
  },

  // 导出督查台账 Excel（仅风险证书）
  exportCertificateLedger: async (params) => {
    return await request.download({ url: `/hr/certificate/ledger-export-excel`, params })
  },

  // 查询推送日志分页
  getCertificateNotifyLogPage: async (params: any) => {
    return await request.get({ url: `/hr/certificate/notify-log/page`, params })
  },

  // 补推一条失败的推送日志
  retryCertificateNotifyLog: async (id: number) => {
    return await request.post({ url: `/hr/certificate/notify-log/retry?id=` + id })
  }
}
