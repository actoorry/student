export const ClientPlatform = Object.freeze({
  MP_WEIXIN: 'MP_WEIXIN',
  APP_ANDROID: 'APP_ANDROID',
  APP_IOS: 'APP_IOS',
  H5: 'H5',
  UNKNOWN: 'UNKNOWN',
});

export function normalizeClientPlatform(info = {}) {
  const uniPlatform = String(info.uniPlatform || info.platform || '').toLowerCase();
  const osName = String(info.osName || info.system || '').toLowerCase();
  if (uniPlatform === 'mp-weixin') return ClientPlatform.MP_WEIXIN;
  if (uniPlatform === 'h5' || uniPlatform === 'web') return ClientPlatform.H5;
  if (uniPlatform === 'app' || uniPlatform === 'app-plus') {
    if (osName.includes('android')) return ClientPlatform.APP_ANDROID;
    if (osName.includes('ios')) return ClientPlatform.APP_IOS;
  }
  return ClientPlatform.UNKNOWN;
}

export function getClientContext(uniApi = globalThis.uni) {
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
    platform: normalizeClientPlatform({ ...systemInfo, ...appInfo }),
    appVersion: typeof appInfo.appVersion === 'string' ? appInfo.appVersion.slice(0, 64) : '',
  };
}
