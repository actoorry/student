import request from '@/sheep/request';

const AddressApi = {
  // 获得用户收件地址列表
  getAddressList: () => {
    return request({
      url: '/partner/address/list',
      method: 'GET'
    });
  },
  // 创建用户收件地址
  createAddress: (data) => {
    return request({
      url: '/partner/address/create',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        successMsg: '保存成功'
      },
    });
  },
  // 更新用户收件地址
  updateAddress: (data) => {
    return request({
      url: '/partner/address/update',
      method: 'PUT',
      data,
      custom: {
        showSuccess: true,
        successMsg: '更新成功'
      },
    });
  },
  // 获得用户收件地址
  getAddress: (id) => {
    return request({
      url: '/partner/address/get',
      method: 'GET',
      params: { id }
    });
  },
  // 删除用户收件地址
  deleteAddress: (id) => {
    return request({
      url: '/partner/address/delete',
      method: 'DELETE',
      params: { id }
    });
  },
};

export default AddressApi;
