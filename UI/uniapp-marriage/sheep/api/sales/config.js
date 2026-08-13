import request from '@/sheep/request';

const SalesConfigApi = {
  // 获得交易配置
  getSalesConfig: () => {
    return request({
      url: `/sales/config/get`,
      method: 'GET',
      custom: {
        showLoading: false,
      },
    });
  },
};

export default SalesConfigApi;
