import request from '@/sheep/request';

const quiet = { showLoading: false, showError: false };

const MarriageProfileApi = {
  getLoginUser: () =>
    request({
      url: '/marriage/auth/get-login-user',
      method: 'GET',
      custom: { ...quiet, auth: true },
    }),
  getMyProfile: () =>
    request({
      url: '/marriage/auth/get-my-profile',
      method: 'GET',
      custom: { ...quiet, auth: true },
    }),
  updateMyProfile: (data) =>
    request({
      url: '/marriage/auth/update-my-profile',
      method: 'PUT',
      data,
      custom: { ...quiet, auth: true },
    }),
  updateBackgroundImage: (backgroundImage) =>
    request({
      url: '/marriage/auth/update-background-image',
      method: 'PUT',
      params: { backgroundImage },
      custom: { ...quiet, auth: true },
    }),
  updateMyPreference: (data) =>
    request({
      url: '/marriage/auth/update-my-preference',
      method: 'PUT',
      data,
      custom: { ...quiet, auth: true },
    }),
  get: (id) =>
    request({
      url: '/marriage/recommend-member/get',
      method: 'GET',
      params: { id },
      custom: quiet,
    }),
  getRealVerifiedStatus: () =>
    request({
      url: '/marriage/auth/get-real-verified-status',
      method: 'GET',
      custom: { ...quiet, auth: true },
    }),
  getRealNameInfo: () =>
    request({
      url: '/marriage/auth/get-real-name-info',
      method: 'GET',
      custom: { ...quiet, auth: true },
    }),
};

export default MarriageProfileApi;
