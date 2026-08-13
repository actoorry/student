import request from '@/sheep/request';
import { unwrapResponse } from '@/sheep/helper/realtime-messaging';

export const IM_MESSAGE_TYPES = Object.freeze({
  TEXT: 101,
  RECALL: 2101,
  RECEIPT: 2200,
  READ: 2201,
});

const quietAuth = { showLoading: false, showError: false, auth: true };

export function createClientMessageId() {
  return `app-${Date.now()}-${Math.floor(Math.random() * 1000000)}`;
}

const MessageApi = {
  getPrivateList: async ({ receiverId, maxId, limit = 30 }) => unwrapResponse(await request({
      url: '/im/message/private/list',
      method: 'GET',
      params: {
        receiverId,
        limit,
        ...(Number(maxId) > 0 ? { maxId: Number(maxId) } : {}),
      },
      custom: quietAuth,
    }), '聊天记录加载失败') || [],
  sendPrivateText: async ({ receiverId, text, clientMessageId = createClientMessageId() }) => unwrapResponse(await request({
      url: '/im/message/private/send',
      method: 'POST',
      data: {
        clientMessageId,
        receiverId,
        type: IM_MESSAGE_TYPES.TEXT,
        content: JSON.stringify({ text }),
      },
      custom: quietAuth,
    }), '发送失败'),
  readPrivate: async ({ receiverId, messageId }) => unwrapResponse(await request({
      url: '/im/message/private/read',
      method: 'PUT',
      params: { receiverId, messageId },
      custom: quietAuth,
    }), '标记已读失败'),
  getUnreadCount: () =>
    request({
      url: '/marriage/chat/unread-count',
      method: 'GET',
      custom: quietAuth,
    }),
  getConversationList: () =>
    request({
      url: '/marriage/chat/conversation-list',
      method: 'GET',
      custom: quietAuth,
    }),
};

export default MessageApi;
