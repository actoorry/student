import request from '@/config/axios'

export interface UnitVO {
  id?: number
  name: string
  status: number
  type: number
  relativeFactor?: string
  createTime?: Date
}

export interface UnitPageReqVO extends PageParam {
  name?: string
  status?: number
  type?: number
  createTime?: Date[]
}

export const getUnitSimpleList = async () => {
  return await request.get({ url: '/product/unit/list-all-simple' })
}

export const getUnitPage = async (params: UnitPageReqVO) => {
  return await request.get({ url: '/product/unit/page', params })
}

export const getUnit = async (id: number) => {
  return await request.get({ url: '/product/unit/get', params: { id } })
}

export const createUnit = async (data: UnitVO) => {
  return await request.post({ url: '/product/unit/create', data })
}

export const updateUnit = async (data: UnitVO) => {
  return await request.put({ url: '/product/unit/update', data })
}

export const deleteUnit = async (id: number) => {
  return await request.delete({ url: '/product/unit/delete', params: { id } })
}
