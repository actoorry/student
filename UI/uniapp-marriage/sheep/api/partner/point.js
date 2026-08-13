import request from '@/sheep/request';

const PointApi = {
  // 获得用户积分记录分页
  getPointRecordPage: (params, custom) => {
    if (params.addStatus === undefined) {
      delete params.addStatus
    }
    const queryString = Object.keys(params)
      .map((key) => encodeURIComponent(key) + '=' + params[key])
      .join('&');
    return request({
      url: `/partner/point/record/page?${queryString}`,
      method: 'GET',
      custom,
    });
  }
};

export default PointApi;
