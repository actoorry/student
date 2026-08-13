import request from '@/sheep/request'

const WarriorApi = {
  submit: (data) => {
    return request({
      url: '/rongjh/warrior/submit',
      method: 'POST',
      data,
      custom: { auth: true, showSuccess: true, successMsg: '提交成功' }
    })
  },
  get: () => {
    return request({
      url: '/rongjh/warrior/get',
      method: 'GET',
      custom: { auth: true, showLoading: false }
    })
  },
  getStatus: (stateId) => {
    return request({
      url: '/rongjh/warrior/status',
      method: 'GET',
      params: stateId ? { state_id: stateId } : {},
      custom: { auth: true, showLoading: false }
    })
  }
}
export default WarriorApi
