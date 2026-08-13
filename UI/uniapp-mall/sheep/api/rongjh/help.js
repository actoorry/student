import request from '@/sheep/request'

const HelpApi = {
  submit: (data) => {
    return request({
      url: '/rongjh/help/create',
      method: 'POST',
      data,
      custom: { auth: true, showSuccess: true, successMsg: '提交成功' }
    })
  },
  cancel: (id) => {
    return request({
      url: '/rongjh/help/cancel',
      method: 'POST',
      params: { id },
      custom: { auth: true, showSuccess: true, successMsg: '已撤销' }
    })
  },
  list: () => {
    return request({
      url: '/rongjh/help/list',
      method: 'GET',
      custom: { auth: true }
    })
  },
  detail: (id) => {
    return request({
      url: `/rongjh/help/get?id=${id}`,
      method: 'GET',
      custom: { auth: true, showLoading: false }
    })
  },
  totalAmount: () => {
    return request({
      url: '/rongjh/help/total-amount',
      method: 'GET',
      custom: { showLoading: false, showError: false }
    })
  }
}
export default HelpApi
