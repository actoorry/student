import request from '@/sheep/request';

const AuthUtil = {
  // 使用手机 + 密码登录
  login: (data) => {
    return request({
      url: '/partner/auth/login',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        loadingMsg: '登录中',
        successMsg: '登录成功',
      },
    });
  },
  // 使用手机 + 验证码登录
  smsLogin: (data) => {
    return request({
      url: '/partner/auth/sms-login',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        loadingMsg: '登录中',
        successMsg: '登录成功',
      },
    });
  },
  // 发送手机验证码
  sendSmsCode: (mobile, scene) => {
    return request({
      url: '/partner/auth/send-sms-code',
      method: 'POST',
      data: {
        mobile,
        scene,
      },
      custom: {
        loadingMsg: '发送中',
        showSuccess: true,
        successMsg: '发送成功',
      },
    });
  },
  // 登出系统
  logout: () => {
    const accessToken = uni.getStorageSync('token') || '';
    const refreshToken = uni.getStorageSync('refresh-token') || '';
    if (!accessToken) {
      return Promise.resolve({ code: 0, data: true });
    }
    return request({
      url: '/partner/auth/logout',
      method: 'POST',
      params: {
        accessToken,
        refreshToken,
      },
      custom: {
        showError: false,
      },
    });
  },
  // 刷新令牌
  refreshToken: (refreshToken) => {
    return request({
      url: '/partner/auth/refresh-token',
      method: 'POST',
      params: {
        refreshToken,
      },
      custom: {
        showLoading: false, // 不用加载中
        showError: false, // 不展示错误提示
      },
    });
  },
  // 社交授权的跳转
  socialAuthRedirect: (type, redirectUri) => {
    return request({
      url: '/partner/auth/social-auth-redirect',
      method: 'GET',
      params: {
        type,
        redirectUri,
      },
      custom: {
        showSuccess: true,
        loadingMsg: '登录中',
      },
    });
  },
  // 社交快捷登录
  socialLogin: (type, code, state) => {
    return request({
      url: '/partner/auth/social-login',
      method: 'POST',
      data: {
        type,
        code,
        state,
      },
      custom: {
        showSuccess: true,
        loadingMsg: '登录中',
      },
    });
  },
  // 微信小程序手机号授权登录（仅 Mine 专用登录页使用）
  weixinMiniLogin: (data) => {
    return request({
      url: '/partner/auth/weixin-mini-login',
      method: 'POST',
      data,
      custom: {
        showError: false,
        loadingMsg: '登录中',
      },
    });
  },
  // 创建微信 JS SDK 初始化所需的签名
  createWeixinMpJsapiSignature: (url) => {
    return request({
      url: '/partner/auth/create-weixin-jsapi-signature',
      method: 'POST',
      params: {
        url,
      },
      custom: {
        showError: false,
        showLoading: false,
      },
    });
  },
  //
};

export default AuthUtil;
