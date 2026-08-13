export function getPhoneAuthorizationCode(event) {
  return event?.detail?.code || '';
}

export function buildWeixinMiniLoginPayload({
  code,
  phoneCode,
  bindUserId,
  nickname = '',
  avatarUrl = '',
}) {
  const payload = { code, nickname, avatarUrl, phoneCode };
  if (Number.isInteger(bindUserId) && bindUserId > 0) {
    payload.bindUserId = bindUserId;
  }
  return payload;
}

export function getWechatMiniLoginErrorMessage(error) {
  if (typeof error === 'string' && error) return error;
  return error?.msg || error?.data?.msg || error?.errMsg || '授权登录失败，请稍后重试';
}
