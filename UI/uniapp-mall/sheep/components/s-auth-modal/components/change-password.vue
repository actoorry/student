<!-- 修改密码（登录时）  -->
<template>
  <view>
    <!-- 标题栏 -->
    <view class="head-box ss-m-b-60">
      <view class="head-title ss-m-b-20">修改密码</view>
      <view class="head-subtitle">如密码丢失或未设置,请点击忘记密码重新设置</view>
    </view>

    <!-- 表单项 -->
    <uni-forms
      ref="changePasswordRef"
      v-model="state.model"
      :rules="state.rules"
      validateTrigger="bind"
      labelWidth="140"
      labelAlign="center"
    >
      <uni-forms-item name="code" label="验证码">
        <uni-easyinput
          placeholder="请输入验证码"
          v-model="state.model.code"
          type="number"
          maxlength="6"
          :inputBorder="false"
        >
          <template v-slot:right>
            <button
              class="ss-reset-button code-btn code-btn-start"
              :disabled="state.isMobileEnd"
              :class="{ 'code-btn-end': state.isMobileEnd }"
              @tap="sendChangePasswordCode"
            >
              {{ getSmsTimer('changePassword') }}
            </button>
          </template>
        </uni-easyinput>
      </uni-forms-item>

      <uni-forms-item name="reNewPassword" label="密码">
        <uni-easyinput
          type="password"
          placeholder="请输入密码"
          v-model="state.model.password"
          :inputBorder="false"
        >
          <template v-slot:right>
            <button class="ss-reset-button login-btn-start" @tap="changePasswordSubmit">
              确认
            </button>
          </template>
        </uni-easyinput>
      </uni-forms-item>
    </uni-forms>

    <button class="ss-reset-button type-btn" @tap="closeAuthModal">
      取消修改
    </button>
  </view>
</template>

<script setup>
  import { ref, reactive, unref, computed } from 'vue';
  import sheep from '@/sheep';
  import { code, password } from '@/sheep/validate/form';
  import { closeAuthModal, getSmsCode, getSmsTimer } from '@/sheep/hooks/useModal';
  import UserApi from '@/sheep/api/partner/user';

  const changePasswordRef = ref(null);

  // 当前登录用户手机号（场景 3 发送与消费以当前登录手机号为准）
  const currentMobile = computed(() => sheep.$store('user').userInfo.mobile);

  // 数据
  const state = reactive({
    model: {
      mobile: '', // 手机号
      code: '', // 验证码
      password: '', // 密码
    },
    rules: {
      code,
      password,
    },
  });

  // 发送验证码（场景 3：使用当前登录手机号）
  function sendChangePasswordCode() {
    getSmsCode('changePassword', currentMobile.value);
  }

  // 更改密码
  async function changePasswordSubmit() {
    // 参数校验
    const validate = await unref(changePasswordRef)
      .validate()
      .catch((error) => {
        console.log('error: ', error);
      });
    if (!validate) {
      return;
    }
    // 发起请求（契约：{code, password}，手机号由后端按当前登录用户确定）
    const { code } = await UserApi.updateUserPassword({
      code: state.model.code,
      password: state.model.password,
    });
    if (code !== 0) {
      return;
    }
    // 成功后，只需要关闭弹窗
    closeAuthModal();
  }
</script>

<style lang="scss" scoped>
  @import '../index.scss';
</style>
