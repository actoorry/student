import { describe, it } from 'node:test';
import assert from 'node:assert';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  buildWeixinMiniLoginPayload,
  getPhoneAuthorizationCode,
  getWechatMiniLoginErrorMessage,
} from '../sheep/helper/wechat-mini-login.js';

const root = dirname(fileURLToPath(import.meta.url));
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8');

describe('Shared WeChat Mini Program login contract', () => {
  it('keeps only the Partner VO-compatible original request fields', () => {
    assert.deepStrictEqual(
      buildWeixinMiniLoginPayload({
        code: 'wx-login-code',
        phoneCode: 'wx-phone-code',
        bindUserId: 123,
      }),
      {
        code: 'wx-login-code',
        nickname: '',
        avatarUrl: '',
        phoneCode: 'wx-phone-code',
        bindUserId: 123,
      },
    );
    assert.strictEqual(
      Object.hasOwn(buildWeixinMiniLoginPayload({ code: 'c', phoneCode: 'p' }), 'salesSource'),
      false,
    );
  });

  it('does not manufacture a bind user and handles authorization failures deterministically', () => {
    assert.deepStrictEqual(buildWeixinMiniLoginPayload({ code: 'c', phoneCode: 'p', bindUserId: 0 }), {
      code: 'c',
      nickname: '',
      avatarUrl: '',
      phoneCode: 'p',
    });
    assert.strictEqual(getPhoneAuthorizationCode({ detail: { code: 'phone-code' } }), 'phone-code');
    assert.strictEqual(getPhoneAuthorizationCode({ detail: {} }), '');
    assert.strictEqual(getWechatMiniLoginErrorMessage({ msg: '服务失败' }), '服务失败');
    assert.strictEqual(getWechatMiniLoginErrorMessage(null), '授权登录失败，请稍后重试');
  });
});

describe('Shared WeChat Mini Program login integration boundaries', () => {
  const component = read('../sheep/components/s-wechat-login/s-wechat-login.vue');
  const authApi = read('../sheep/api/partner/auth.js');
  const minePage = read('../pages/index/user.vue');
  const messagesPage = read('../pages/messages/index.vue');
  const authModal = read('../sheep/components/s-auth-modal/s-auth-modal.vue');
  const modalHook = read('../sheep/hooks/useModal.js');
  const router = read('../sheep/router/index.js');
  const request = read('../sheep/request/index.js');
  const userStore = read('../sheep/store/user.js');
  const userAuthButton = read('../sheep/components/s-user-auth-button/s-user-auth-button.vue');
  const userInfoPage = read('../pages/user/info.vue');
  const miniProgramProvider = read('../sheep/platform/provider/wechat/miniProgram.js');

  it('requires explicit phone authorization before uni.login and the Partner call', () => {
    assert.match(component, /open-type="getPhoneNumber"/);
    assert.match(component, /@getphonenumber="onGetPhoneNumber"/);
    assert.match(component, /const phoneCode = getPhoneAuthorizationCode\(event\)/);
    assert.match(component, /const code = await getMiniProgramLoginCode\(\)/);
    assert.match(component, /AuthUtil\.weixinMiniLogin/);
    assert.match(component, /sheep\.\$store\('social'\)\.startRealtime\(\)/);
    assert.match(component, /emit\('success'\)/);
    assert.doesNotMatch(component, /salesSource|showAuthModal|window\.|document\./);
  });

  it('strips WeChat-branded copy, logo and brand green from the shared phone-authorization sheet', () => {
    assert.match(component, /手机号快捷登录/);
    assert.doesNotMatch(component, /微信手机号授权登录/);
    assert.doesNotMatch(component, /请在微信小程序中完成手机号授权登录/);
    assert.doesNotMatch(component, /#07c160/);
    assert.doesNotMatch(component, /#07C160/);
  });

  it('keeps the shared sheet closable for default protected-action prompts and never forces login on Mine', () => {
    assert.match(component, /暂不登录/);
    assert.match(component, /v-if="closable"/);
    assert.doesNotMatch(minePage, /<s-wechat-login auto/);
    assert.match(minePage, /s-user-auth-button v-if="!hasAuthButton"/);
    assert.match(minePage, /watch\(isLoggedIn,/);
    assert.match(userAuthButton, /const shouldShow = computed\(\(\) => true\)/);
    assert.doesNotMatch(userAuthButton, /isMinePage/);
    assert.match(modalHook, /showAuthModal\(type = 'wechatMiniLogin'\)/);
    assert.match(authModal, /:show="authType === 'wechatMiniLogin'"/);
    assert.match(authModal, /@success="onWechatLoginSuccess"/);
    assert.match(
      authModal,
      /:show="authType !== '' && authType !== 'wechatMiniLogin'"/,
    );
    assert.match(authModal, /#ifndef MP-WEIXIN[\s\S]*wechat\.png/);
    assert.doesNotMatch(authModal, /\['WechatOfficialAccount', 'WechatMiniProgram', 'App'\]/);
    assert.match(messagesPage, /function openLogin\(\)\s*\{\s*showAuthModal\(\);/);
    assert.doesNotMatch(messagesPage, /showAuthModal\(['"](?:smsLogin|accountLogin)['"]\)/);
    assert.match(messagesPage, /watch\(isLoggedIn,[\s\S]*loadMessages\(\);[\s\S]*\}\);/);
    assert.match(userAuthButton, /const onLogin = \(\) => \{\s*showAuthModal\(\)/);
  });

  it('does not auto-open the auth sheet on relationship and notification pages for anonymous users', () => {
    const mineView = read('../pages/mine-view/index.vue');
    const mineFollow = read('../pages/mine-follow/index.vue');
    const mutual = read('../pages/messages/mutual/index.vue');
    [mineView, mineFollow, mutual].forEach((page) => {
      assert.doesNotMatch(page, /requireLogin\(\)\s*\{[^}]*showAuthModal/);
      assert.match(page, /openLogin\(\)/);
      assert.match(page, /立即登录/);
    });
    assert.doesNotMatch(mutual, /if \(!sheep\.\$store\('user'\)\.isLogin\)\s*\{/);
  });

  it('retains explicit account-management modal types without using them as login defaults', () => {
    assert.match(userInfoPage, /showAuthModal\('changeMobile'\)/);
    assert.match(userInfoPage, /showAuthModal\('changePassword'\)/);
    assert.match(authModal, /authType === 'changeMobile'/);
    assert.match(authModal, /authType === 'changePassword'/);
  });

  it('uses the reviewed endpoint and token lifecycle', () => {
    assert.match(authApi, /weixinMiniLogin: \(data\)/);
    assert.match(authApi, /url: '\/partner\/auth\/weixin-mini-login'/);
    assert.doesNotMatch(authApi, /weixin-mini-app-login/);
    assert.match(miniProgramProvider, /AuthUtil\.weixinMiniLogin\(\{/);
    assert.doesNotMatch(miniProgramProvider, /weixinMiniAppLogin/);
  });

  it('does not add automatic route, request or 401 login prompts', () => {
    assert.doesNotMatch(router, /showAuthModal/);
    assert.doesNotMatch(request, /showAuthModal/);
    assert.match(request, /return Promise\.reject\(\{ code: 401, msg: '请先登录' \}\)/);
    assert.match(request, /不因 401 自动打开认证弹层/);
  });

  it('does not let realtime or post-login refresh errors invalidate persisted tokens', () => {
    assert.match(userStore, /try\s*\{\s*ensureRealtimeConnection\(\);/);
    assert.match(userStore, /this\.loginAfter\(\)\.catch/);
  });
});
