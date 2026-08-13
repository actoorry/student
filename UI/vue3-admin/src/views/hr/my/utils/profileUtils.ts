import type { MyProfile } from '@/api/hr/my/profile'



/** 毫秒时间戳 / ISO 字符串统一格式化为日期展示（与 profile 页原逻辑一致） */

export const formatDate = (v: any): string => {

  if (v === null || v === undefined || v === '') return '-'

  const d = typeof v === 'number' ? new Date(v) : new Date(v as string)

  if (Number.isNaN(d.getTime())) return '-'

  const pad = (n: number) => String(n).padStart(2, '0')

  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`

}



/** 证书状态 → el-tag type（沿用英文枚举，勿改 API 值） */

export const certStatusTag = (status?: string): string => {

  switch (status) {

    case 'expired':

    case 'assessment_overdue':

      return 'danger'

    case 'expiring':

    case 'expiring_30':

    case 'expiring_60':

    case 'expiring_90':

    case 'assessment_due_60':

      return 'warning'

    default:

      return 'success'

  }

}



/** 证书状态 → 中文标签 */

export const certStatusLabel = (status?: string): string => {

  switch (status) {

    case 'expired':

      return '已过期'

    case 'expiring':

    case 'expiring_30':

      return '30天内到期'

    case 'expiring_60':

      return '60天内到期'

    case 'expiring_90':

      return '90天内到期'

    case 'assessment_overdue':

      return '考核逾期'

    case 'assessment_due_60':

      return '考核将到期'

    case 'valid':

      return '有效'

    default:

      return '-'

  }

}



/** 证书是否未过期（与后端 status=expired 口径一致，含即将到期、考核提醒等） */

export const isNotExpiredStatus = (status?: string): boolean => status !== 'expired'

/** 状态是否为「需要提醒」（即将到期 / 已过期 / 考核相关） */

export const isAlertStatus = (status?: string): boolean => {

  return (

    status === 'expired' ||

    status === 'expiring' ||

    status === 'expiring_30' ||

    status === 'expiring_60' ||

    status === 'expiring_90' ||

    status === 'assessment_overdue' ||

    status === 'assessment_due_60'

  )

}



/** 从 profile 聚合统计 */

export interface ProfileStat {

  certificateTotal: number

  /** 有效证书数（首页口径：status !== expired，含即将到期） */
  certificateValid: number

  certificateAlert: number

  resumeTotal: number

}



export const computeStat = (profile?: MyProfile): ProfileStat => {

  const certs = profile?.certificates || []

  const certificateTotal = certs.length

  const certificateValid = certs.filter((c) => isNotExpiredStatus((c as any).status)).length

  const certificateAlert = certs.filter((c) => isAlertStatus((c as any).status)).length

  const resumeTotal = profile?.resumes?.length || 0

  return { certificateTotal, certificateValid, certificateAlert, resumeTotal }

}



/** 工作经历行（与 ResumeForm parseWorkRows 结构一致） */

export interface ResumeWorkRow {

  startYear: string

  endYear: string

  unit: string

  position: string

}



/** 解析 resume_content：JSON 数组或旧版自由文本 */

export const parseResumeWorkRows = (content?: string): ResumeWorkRow[] => {

  if (!content) return []

  try {

    const parsed = JSON.parse(content)

    if (Array.isArray(parsed) && parsed.length) {

      return parsed.map((r: any) => ({

        startYear: r.startYear ?? '',

        endYear: r.endYear ?? '',

        unit: r.unit ?? '',

        position: r.position ?? ''

      }))

    }

  } catch {

    return [{ startYear: '', endYear: '', unit: content, position: '' }]

  }

  return []

}



/** 工作经历单行展示文案 */

export const formatWorkRowLabel = (row: ResumeWorkRow): string => {

  const period =

    row.startYear || row.endYear

      ? `${row.startYear || '?'} — ${row.endYear || '至今'}`

      : ''

  const place = [row.unit, row.position].filter(Boolean).join(' · ')

  return [period, place].filter(Boolean).join('  ')

}


