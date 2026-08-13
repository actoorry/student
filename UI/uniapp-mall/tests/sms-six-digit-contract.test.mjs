import { describe, it } from 'node:test';
import assert from 'node:assert';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = dirname(fileURLToPath(import.meta.url));
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8');

const components = ['sms-login', 'change-mobile', 'change-password', 'reset-password'];

describe('uniapp-mall 短信契约：六位验证码', () => {
  it('四个短信弹窗的验证码输入均允许六位且不被截断', () => {
    for (const name of components) {
      const component = read(`../sheep/components/s-auth-modal/components/${name}.vue`);
      // 找到验证码输入框的 maxlength，必须是 6
      const codeBlock = component.slice(
        component.indexOf('name="code"'),
        component.indexOf('</uni-forms-item>', component.indexOf('name="code"')),
      );
      assert.match(codeBlock, /maxlength="6"/, `${name}.vue 验证码输入必须 maxlength=6`);
      assert.doesNotMatch(codeBlock, /maxlength="4"/, `${name}.vue 不得再限制四位`);
    }
  });

  it('useModal 场景号固定为 1/2/3/4', () => {
    const modalHook = read('../sheep/hooks/useModal.js');
    assert.match(modalHook, /case 'resetPassword':\s*scene = 4;/);
    assert.match(modalHook, /case 'changePassword':\s*scene = 3;/);
    assert.match(modalHook, /case 'changeMobile':\s*scene = 2;/);
    assert.match(modalHook, /case 'smsLogin':\s*scene = 1;/);
  });

  it('验证码校验规则必须为六位数字', () => {
    const form = read('../sheep/validate/form.js');
    assert.match(form, /验证码必须为 6 位数字/);
    assert.match(form, /\\d\{6\}/);
  });

  it('短信修改密码只提交 {code, password}，不携带旧密码', () => {
    const changePassword = read('../sheep/components/s-auth-modal/components/change-password.vue');
    assert.doesNotMatch(changePassword, /oldPassword/, '不得提交 oldPassword 字段');
    assert.match(changePassword, /updateUserPassword\(\{\s*code:/, '修改密码必须提交 code');
    assert.match(changePassword, /password:/, '修改密码必须提交 password');
  });

  it('场景 3 发送验证码使用当前登录手机号', () => {
    const changePassword = read('../sheep/components/s-auth-modal/components/change-password.vue');
    assert.match(changePassword, /currentMobile = computed/, '必须从当前登录用户取手机号');
    assert.match(changePassword, /getSmsCode\('changePassword', currentMobile\.value\)/, '场景 3 发送必须携带当前手机号');
  });

  it('后端错误不会被误报为成功', () => {
    // 发送验证码：只有 code === 0 才记录发送时间（其余响应视为失败，不进入倒计时）
    const modalHook = read('../sheep/hooks/useModal.js');
    assert.match(modalHook, /res\.code === 0/);
    // 登录：只有 code === 0 才关闭弹窗
    const smsLogin = read('../sheep/components/s-auth-modal/components/sms-login.vue');
    assert.match(smsLogin, /if \(code === 0\)/);
    // 修改密码：code !== 0 时不关闭弹窗
    const changePassword = read('../sheep/components/s-auth-modal/components/change-password.vue');
    assert.match(changePassword, /if \(code !== 0\)/);
  });

  it('戎集汇修改密码页已迁移到场景 3 短信改密契约 {code, password}', () => {
    const page = read('../pages/rongjh/change-password/index.vue');
    // 不再要求旧密码
    assert.doesNotMatch(page, /oldPassword|old_passwd|旧密码/, '不得再提交旧密码');
    // 验证码输入为六位
    assert.match(page, /maxlength="6"/, '验证码输入必须 maxlength=6');
    assert.match(page, /\\d\{6\}/, '提交前必须校验六位验证码');
    // 以当前登录手机号发送场景 3 验证码
    assert.match(page, /getSmsCode\('changePassword', mobile\)/, '场景 3 发送必须携带当前手机号');
    // 提交 {code, password}
    assert.match(page, /code: form\.code/, '必须提交 code');
    assert.match(page, /password: form\.newPassword/, '必须提交 password');
    // 适配器只传 {code, password}
    const adapter = read('../sheep/api/rongjh/adapter.js');
    assert.doesNotMatch(adapter, /oldPassword|old_passwd|new_passwd/, '适配器不得再传旧密码字段');
    assert.match(adapter, /code: data\.code/, '适配器必须传 code');
  });
});
