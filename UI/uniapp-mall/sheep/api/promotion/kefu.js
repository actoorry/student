import request from '@/sheep/request';
import { unwrapResponse } from '@/sheep/helper/realtime-messaging';

const quietAuth = { auth: true, showLoading: false, showError: false };

const KeFuApi = {
  sendKefuMessage: async (data) => {
    const response = await request({
      url: '/sales/promotion/kefu-message/send',
      method: 'POST',
      data,
      custom: quietAuth,
    });
    return unwrapResponse(response, '发送失败');
  },
  getKefuMessageList: async (params) => {
    const response = await request({
      url: '/sales/promotion/kefu-message/list',
      method: 'GET',
      params,
      custom: quietAuth,
    });
    return unwrapResponse(response, '客服记录加载失败') || [];
  },
  updateReadStatus: async (conversationId) => unwrapResponse(await request({
    url: '/sales/promotion/kefu-message/update-read-status', method: 'PUT', params: { conversationId }, custom: quietAuth,
  }), '标记客服消息已读失败'),
};

export default KeFuApi;
