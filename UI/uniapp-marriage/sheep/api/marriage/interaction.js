import request from '@/sheep/request';

export const INTERACTION_SCENES = Object.freeze({
  FOLLOW: 'FOLLOW',
  MUTUAL_FOLLOW: 'MUTUAL_FOLLOW',
  VIEW_ME_SUMMARY: 'VIEW_ME_SUMMARY',
});

const quietAuth = { showLoading: false, showError: false, auth: true };

const InteractionApi = {
  follow: (relPartnerId) =>
    request({
      url: '/marriage/interaction/follow',
      method: 'POST',
      data: { relPartnerId },
      custom: quietAuth,
    }),
  unfollow: (relPartnerId) =>
    request({
      url: '/marriage/interaction/unfollow',
      method: 'DELETE',
      params: { relPartnerId },
      custom: quietAuth,
    }),
  recordView: (targetPartnerId) =>
    request({
      url: '/marriage/interaction/record-view',
      method: 'POST',
      data: { targetPartnerId },
      custom: quietAuth,
    }),
  getFollowMePage: ({ pageNo = 1, pageSize = 20 } = {}) =>
    request({
      url: '/marriage/interaction/follow-me-page',
      method: 'GET',
      params: { pageNo, pageSize },
      custom: quietAuth,
    }),
  getMyFollowPage: ({ pageNo = 1, pageSize = 20 } = {}) =>
    request({
      url: '/marriage/interaction/my-follow-page',
      method: 'GET',
      params: { pageNo, pageSize },
      custom: quietAuth,
    }),
  getMutualFollowPage: ({ pageNo = 1, pageSize = 100 } = {}) =>
    request({
      url: '/marriage/interaction/my-follow-page',
      method: 'GET',
      params: { pageNo, pageSize },
      custom: quietAuth,
    }),
  getViewMePage: ({ pageNo = 1, pageSize = 20 } = {}) =>
    request({
      url: '/marriage/interaction/view-me-page',
      method: 'GET',
      params: { pageNo, pageSize },
      custom: quietAuth,
    }),
  getMyViewPage: ({ pageNo = 1, pageSize = 20 } = {}) =>
    request({
      url: '/marriage/interaction/my-view-page',
      method: 'GET',
      params: { pageNo, pageSize },
      custom: quietAuth,
    }),
  getNotificationPage: ({ pageNo = 1, pageSize = 20, scene, readStatus } = {}) =>
    request({
      url: '/marriage/interaction-notification/page',
      method: 'GET',
      params: {
        pageNo,
        pageSize,
        ...(scene ? { scene } : {}),
        ...(typeof readStatus === 'boolean' ? { readStatus } : {}),
      },
      custom: quietAuth,
    }),
  getUnreadCountGroup: () =>
    request({
      url: '/marriage/interaction-notification/unread-count-group',
      method: 'GET',
      custom: quietAuth,
    }),
  readScene: (scene) =>
    request({
      url: '/marriage/interaction-notification/read-scene',
      method: 'PUT',
      params: { scene },
      custom: quietAuth,
    }),
  readAll: () =>
    request({
      url: '/marriage/interaction-notification/read-all',
      method: 'PUT',
      custom: quietAuth,
    }),
};

export default InteractionApi;
