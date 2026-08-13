import request from '@/sheep/request';

const WechatVirtualPayApi = {
  getCheckoutProfile: (payOrderId) =>
    request({
      url: '/accountant/wechat-virtual-pay/checkout-profile',
      method: 'GET',
      params: { payOrderId },
    }),

  submit: (payOrderId, openid) =>
    request({
      url: '/accountant/wechat-virtual-pay/submit',
      method: 'POST',
      data: { payOrderId, openid },
    }),

  getResult: (payOrderId) =>
    request({
      url: '/accountant/wechat-virtual-pay/result',
      method: 'GET',
      params: { payOrderId },
    }),
};

export default WechatVirtualPayApi;
