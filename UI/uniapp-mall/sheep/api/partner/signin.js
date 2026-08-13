import request from '@/sheep/request';

const SignInApi = {
  // 获得签到规则列表
  getSignInConfigList: () => {
    return request({
      url: '/partner/sign-in/config/list',
      method: 'GET',
    });
  },
  // 获得个人签到统计
  getSignInRecordSummary: (custom) => {
    return request({
      url: '/partner/sign-in/record/get-summary',
      method: 'GET',
      custom,
    });
  },
  // 签到
  createSignInRecord: (custom) => {
    return request({
      url: '/partner/sign-in/record/create',
      method: 'POST',
      custom,
    });
  },
  // 获得签到记录分页
  getSignRecordPage: (params) => {
    const queryString = Object.keys(params)
      .map((key) => encodeURIComponent(key) + '=' + params[key])
      .join('&');
    return request({
      url: `/partner/sign-in/record/page?${queryString}`,
      method: 'GET',
    });
  },
};

export default SignInApi;
