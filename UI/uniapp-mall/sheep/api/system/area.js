import request from '@/sheep/request';

const AreaApi = {
  // 获得地区树
  getAreaTree: () => {
    return request({
      url: '/system/area/tree',
      method: 'GET',
    });
  },
  // 根据 IP 解析城市（不传 ip 则使用当前请求 IP）
  getAreaByIp: (ip) => {
    return request({
      url: '/system/area/get-by-ip',
      method: 'GET',
      params: ip ? { ip } : {},
      custom: {
        showError: false,
        showLoading: false,
      },
    });
  },
};

export default AreaApi;
