import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import test from 'node:test';
import vm from 'node:vm';

const providerSource = readFileSync(
  new URL('../sheep/platform/provider/wechat/miniProgram.js', import.meta.url),
  'utf8',
)
  .replace(/^import .*;\r?\n/gm, '')
  .replace('export default {', 'globalThis.__provider = {');

function loadProvider({ auth, social, uniOverrides = {} }) {
  const context = vm.createContext({
    AuthUtil: auth,
    SocialApi: social,
    UserApi: {},
    console: {
      ...console,
      warn() {},
    },
    setTimeout,
    clearTimeout,
    sheep: {
      $helper: { toast() {} },
    },
    uni: {
      login: async () => ({ errMsg: 'login:ok', code: 'login-code' }),
      getStorageSync: () => '',
      setStorageSync() {},
      ...uniOverrides,
    },
    wx: {},
  });
  vm.runInContext(providerSource, context, { filename: 'miniProgram.js' });
  return context.__provider;
}

test('successful phone login completes without a protected social-user follow-up request', async () => {
  let socialUserRequests = 0;
  const provider = loadProvider({
    auth: {
      weixinMiniLogin: async () => ({
        code: 0,
        data: {
          accessToken: 'access-token',
          refreshToken: 'refresh-token',
        },
      }),
    },
    social: {
      getSocialUser: async () => {
        socialUserRequests += 1;
        return { code: 401, msg: '请先登录' };
      },
    },
  });

  const result = await provider.mobileLogin({
    errMsg: 'getPhoneNumber:ok',
    code: 'phone-code',
  });

  assert.equal(result, true);
  assert.equal(socialUserRequests, 0);
});

test('phone login request rejection settles as false instead of leaking an unhandled promise', async () => {
  const unhandledRejections = [];
  const onUnhandledRejection = (reason) => unhandledRejections.push(reason);
  process.on('unhandledRejection', onUnhandledRejection);

  try {
    const provider = loadProvider({
      auth: {
        weixinMiniLogin: async () => Promise.reject({ code: 401, msg: '请先登录' }),
      },
      social: {
        getSocialUser: async () => ({ code: 0, data: {} }),
      },
    });

    const result = await Promise.race([
      provider.mobileLogin({ errMsg: 'getPhoneNumber:ok', code: 'phone-code' }),
      new Promise((resolve) => setTimeout(() => resolve('pending'), 30)),
    ]);

    assert.equal(result, false);
    assert.deepEqual(unhandledRejections, []);
  } finally {
    process.off('unhandledRejection', onUnhandledRejection);
  }
});

test('Partner auth controller stays in the app controller package used by the /app-api prefix', () => {
  const controllerCandidates = [
    new URL(
      '../../../suxin-module/src/main/java/vip/appap/suxin/module/sales/controller/app/AppPartnerAuthController.java',
      import.meta.url,
    ),
    new URL(
      '../../../suxin-module/src/main/java/vip/appap/suxin/module/sales/controller/AppPartnerAuthController.java',
      import.meta.url,
    ),
  ];
  const controllerUrl = controllerCandidates.find((candidate) => existsSync(candidate));
  assert.ok(controllerUrl, 'AppPartnerAuthController.java must exist');

  const controller = readFileSync(controllerUrl, 'utf8');
  assert.match(controller, /package vip\.appap\.suxin\.module\.sales\.controller\.app;/);
  assert.match(controller, /@PermitAll\s+@PostMapping\("\/weixin-mini-login"\)/);
});
