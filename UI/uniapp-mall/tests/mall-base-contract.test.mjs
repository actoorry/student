import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');

test('request layer carries tenant/terminal/token headers and refreshes via partner auth', () => {
  const request = read('sheep/request/index.js');
  assert.match(request, /config\.header\['tenant-id'\] = getTenantId\(\)/);
  assert.match(request, /config\.header\['terminal'\] = getTerminal\(\)/);
  assert.match(request, /config\.header\['Authorization'\] = token/);
  assert.match(request, /X-Client-Platform/);
  assert.match(request, /X-App-Version/);
  assert.match(request, /\/partner\/auth\/refresh-token/);
  // 登录令牌只来自 Partner auth，不引入 /trade /member /pay 兼容入口
  assert.doesNotMatch(request, /\/member\/auth\/|\/trade\/|\/pay\/order\/submit/);
  // 商城不引入婚恋实名守卫
  assert.doesNotMatch(request, /marriage-check|name-check|marriage\//);
});

test('router guides login on auth pages and preserves the target for restoration', () => {
  const router = read('sheep/router/index.js');
  const userStore = read('sheep/store/user.js');
  assert.match(router, /nextRoute\.meta\?\.auth && !\$store\('user'\)\.isLogin/);
  assert.match(router, /saveAuthRedirect\(url\)/);
  assert.match(router, /showAuthModal\(\)/);
  assert.match(router, /auth-redirect-url/);
  // 登录成功后恢复目标地址
  assert.match(userStore, /restorePendingRoute\(\)/);
  assert.match(userStore, /getAuthRedirectUrl/);
  assert.match(userStore, /clearAuthRedirect/);
  // tabBar 跳转走 switchTab
  assert.match(router, /TABBAR\.includes\(page\)/);
  assert.match(router, /uni\.switchTab/);
  // 未找到目标页面有受控守卫
  assert.match(router, /if \(!nextRoute\)/);
});

test('partner auth and wechat platform use the current contracts without marriage guards', () => {
  const auth = read('sheep/api/partner/auth.js');
  const wechat = read('sheep/platform/provider/wechat/miniProgram.js');
  assert.match(auth, /\/partner\/auth\/login/);
  assert.match(auth, /\/partner\/auth\/weixin-mini-login/);
  assert.match(auth, /\/partner\/auth\/refresh-token/);
  assert.match(auth, /\/partner\/auth\/logout/);
  assert.doesNotMatch(auth, /\/member\/auth\/|\/trade\//);
  assert.match(wechat, /AuthUtil\.weixinMiniLogin/);
  assert.match(wechat, /AuthUtil\.socialLogin/);
  assert.doesNotMatch(wechat, /\/marriage\/|marriage-check|name-check/);
});

test('config derives baseUrl from environment without hardcoding a backend host', () => {
  const config = read('sheep/config/index.js');
  assert.match(config, /import\.meta\.env\.SHOPRO_DEV_BASE_URL/);
  assert.match(config, /import\.meta\.env\.SHOPRO_BASE_URL/);
  assert.match(config, /import\.meta\.env\.SHOPRO_API_PATH/);
  assert.match(config, /import\.meta\.env\.SHOPRO_TENANT_ID/);
  // 不把任意环境密钥或地址写入源码
  assert.doesNotMatch(config, /password|secret|token\s*=\s*['"][^'"]{4,}/);
});
