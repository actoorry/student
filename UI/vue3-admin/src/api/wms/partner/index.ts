import request from '@/config/axios'

export interface PartnerOption {
  id: number
  name: string
}

export const WmsPartnerApi = {
  /** 获取供应商列表（下拉选择用） */
  getSupplierList: async (): Promise<PartnerOption[]> => {
    return await request.get({ url: `/wms/partner/supplier-list` })
  },
  /** 获取客户列表（下拉选择用） */
  getCustomerList: async (): Promise<PartnerOption[]> => {
    return await request.get({ url: `/wms/partner/customer-list` })
  },
  /** 获取公司列表（仓库负责人下拉选择用） */
  getCompanyList: async (): Promise<PartnerOption[]> => {
    return await request.get({ url: `/wms/partner/company-list` })
  },
  /** 查询客商信息 */
  getPartner: async (id: number): Promise<any> => {
    return await request.get({ url: `/wms/partner/get?id=` + id })
  }
}