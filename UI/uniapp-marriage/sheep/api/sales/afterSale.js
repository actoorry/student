import request from '@/sheep/request';

const AfterSaleApi = {
  // 获得售后分页
  getAfterSalePage: (params) => {
    return request({
      url: `/sales/after-sale/page`,
      method: 'GET',
      params,
      custom: {
        showLoading: false,
      },
    });
  },
  // 创建售后
  createAfterSale: (data) => {
    return request({
      url: `/sales/after-sale/create`,
      method: 'POST',
      data,
    });
  },
  // 获得售后
  getAfterSale: (id) => {
    return request({
      url: `/sales/after-sale/get`,
      method: 'GET',
      params: {
        id,
      },
    });
  },
  // 取消售后
  cancelAfterSale: (id) => {
    return request({
      url: `/sales/after-sale/cancel`,
      method: 'DELETE',
      params: {
        id,
      },
    });
  },
  // 获得售后日志列表
  getAfterSaleLogList: (afterSaleId) => {
    return request({
      url: `/sales/after-sale-log/list`,
      method: 'GET',
      params: {
        afterSaleId,
      },
    });
  },
  // 退回货物
  deliveryAfterSale: (data) => {
    return request({
      url: `/sales/after-sale/delivery`,
      method: 'PUT',
      data,
    });
  }
};

export default AfterSaleApi;
