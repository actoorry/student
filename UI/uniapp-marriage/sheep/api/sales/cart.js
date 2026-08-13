import request from '@/sheep/request';

const CartApi = {
  addCart: (data) => {
    return request({
      url: '/sales/cart/add',
      method: 'POST',
      data: data,
      custom: {
        showSuccess: true,
        successMsg: '已添加到购物车~',
      }
    });
  },
  updateCartCount: (data) => {
    return request({
      url: '/sales/cart/update-count',
      method: 'PUT',
      data: data
    });
  },
  updateCartSelected: (data) => {
    return request({
      url: '/sales/cart/update-selected',
      method: 'PUT',
      data: data
    });
  },
  resetCart: (data) => {
    return request({
      url: '/sales/cart/reset',
      method: 'PUT',
      data,
    });
  },
  deleteCart: (ids) => {
    return request({
      url: '/sales/cart/delete',
      method: 'DELETE',
      params: {
        ids
      }
    });
  },
  getCartList: () => {
    return request({
      url: '/sales/cart/list',
      method: 'GET',
      custom: {
        showLoading: false,
        auth: true,
      },
    });
  },
  getCartCount: () => {
    return request({
      url: '/sales/cart/get-count',
      method: 'GET',
      custom: {
        showLoading: false,
        auth: true,
      },
    });
  },
};

export default CartApi;
