export const ClientPlatform = Object.freeze({
  MP_WEIXIN: 'MP_WEIXIN',
  APP_ANDROID: 'APP_ANDROID',
  APP_IOS: 'APP_IOS',
  H5: 'H5',
  UNKNOWN: 'UNKNOWN',
});

export function normalizeClientPlatform(info = {}, platformHint = '') {
  const platformValues = [info.uniPlatform, platformHint, info.platform]
    .map((value) => String(value || '').toLowerCase())
    .filter(Boolean);
  const osName = String(info.osName || info.system || '').toLowerCase();
  if (
    platformValues.some((value) =>
      ['mp-weixin', 'wechatminiprogram', 'miniprogram'].includes(value),
    )
  ) {
    return ClientPlatform.MP_WEIXIN;
  }
  if (platformValues.some((value) => value === 'h5' || value === 'web')) {
    return ClientPlatform.H5;
  }
  if (platformValues.some((value) => value === 'app' || value === 'app-plus')) {
    if (osName.includes('android')) return ClientPlatform.APP_ANDROID;
    if (osName.includes('ios')) return ClientPlatform.APP_IOS;
  }
  return ClientPlatform.UNKNOWN;
}

export function getClientContext(platformHint = '', uniApi = globalThis.uni) {
  let appInfo = {};
  let systemInfo = {};
  try {
    appInfo = typeof uniApi?.getAppBaseInfo === 'function' ? uniApi.getAppBaseInfo() || {} : {};
  } catch (_) {
    appInfo = {};
  }
  try {
    systemInfo =
      typeof uniApi?.getSystemInfoSync === 'function' ? uniApi.getSystemInfoSync() || {} : {};
  } catch (_) {
    systemInfo = {};
  }
  return {
    platform: normalizeClientPlatform({ ...systemInfo, ...appInfo }, platformHint),
    appVersion: typeof appInfo.appVersion === 'string' ? appInfo.appVersion.slice(0, 64) : '',
  };
}
