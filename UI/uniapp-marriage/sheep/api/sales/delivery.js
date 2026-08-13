import request from '@/sheep/request';

const DeliveryApi = {
  // 获得快递公司列表
  getDeliveryExpressList: () => {
    return request({
      url: `/sales/delivery/express/list`,
      method: 'get',
    });
  },
  // 获得自提门店列表
  getDeliveryPickUpStoreList: (params) => {
    return request({
      url: `/sales/delivery/pick-up-store/list`,
      method: 'GET',
      params,
    });
  },
  // 获得自提门店
  getDeliveryPickUpStore: (id) => {
    return request({
      url: `/sales/delivery/pick-up-store/get`,
      method: 'GET',
      params: {
        id,
      },
    });
  },
};

export default DeliveryApi;
