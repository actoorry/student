import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 劳动合同信息 */
export interface Contract {
  id: number // 主键
  partnerId?: number // 关联员工
  name: string // 姓名
  contractNo: string // 合同编号
  partyA: string // 甲方
  dept: string // 乙方所在部门 ID
  deptName?: string // 乙方所在部门名称（联表展示）
  startTime?: number // 合同开始时间（毫秒时间戳）
  endTime?: number // 合同结束时间
  signDateA?: number // 签订日期-甲方
  signDateB?: number // 签订日期-乙方
  contractFile: string // 上传纸质合同图片
  photo: string // 上传可视化图片
  status: string // 合同状态
  createTime?: number // 创建时间
}

// 劳动合同 API
export const ContractApi = {
  // 查询劳动合同分页
  getContractPage: async (params: any) => {
    return await request.get({ url: `/hr/contract/page`, params })
  },

  // 查询劳动合同详情
  getContract: async (id: number) => {
    return await request.get({ url: `/hr/contract/get?id=` + id })
  },

  // 预览合同编号（新增表单展示）
  previewContractNo: async () => {
    return await request.get({ url: `/hr/contract/preview-no` })
  },

  // 新增劳动合同
  createContract: async (data: Contract) => {
    return await request.post({ url: `/hr/contract/create`, data })
  },

  // 修改劳动合同
  updateContract: async (data: Contract) => {
    return await request.put({ url: `/hr/contract/update`, data })
  },

  // 删除劳动合同
  deleteContract: async (id: number) => {
    return await request.delete({ url: `/hr/contract/delete?id=` + id })
  },

  /** 批量删除劳动合同 */
  deleteContractList: async (ids: number[]) => {
    return await request.delete({ url: `/hr/contract/delete-list?ids=${ids.join(',')}` })
  },

  // 导出劳动合同 Excel
  exportContract: async (params) => {
    return await request.download({ url: `/hr/contract/export-excel`, params })
  },
}
