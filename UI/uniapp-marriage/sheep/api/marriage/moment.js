import request from '@/sheep/request';

const quiet = { showLoading: false, showError: false };

const MomentApi = {
  getPage: ({ pageNo = 1, pageSize = 10, following = false, partnerId }) =>
    request({
      url: '/marriage/moment/page',
      method: 'GET',
      params: {
        pageNo,
        pageSize,
        following,
        ...(Number(partnerId) > 0 ? { partnerId: Number(partnerId) } : {}),
      },
      custom: quiet,
    }),
  getMyPage: ({ pageNo = 1, pageSize = 10 }) =>
    request({
      url: '/marriage/moment/my-page',
      method: 'GET',
      params: { pageNo, pageSize },
      custom: { ...quiet, auth: true },
    }),
  create: ({ content, imageUrls }) =>
    request({
      url: '/marriage/moment/create',
      method: 'POST',
      data: { content, imageUrls },
      custom: { ...quiet, auth: true },
    }),
  delete: (id) =>
    request({
      url: '/marriage/moment/delete',
      method: 'DELETE',
      params: { id },
      custom: { ...quiet, auth: true },
    }),
  toggleLike: (momentId) =>
    request({
      url: '/marriage/moment/like',
      method: 'POST',
      data: { momentId },
      custom: { ...quiet, auth: true },
    }),
  getCommentPage: ({ momentId, pageNo = 1, pageSize = 20 }) =>
    request({
      url: '/marriage/moment/comment/page',
      method: 'GET',
      params: { momentId, pageNo, pageSize },
      custom: quiet,
    }),
  createComment: ({ momentId, parentId = 0, replyToPartnerId = 0, content }) =>
    request({
      url: '/marriage/moment/comment/create',
      method: 'POST',
      data: { momentId, parentId, replyToPartnerId, content },
      custom: { ...quiet, auth: true },
    }),
};

export default MomentApi;
