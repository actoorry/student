<template>
  <view
    v-if="visible"
    class="wechat-login-mask"
    @tap.self="close"
  >
    <view class="wechat-login-sheet">
      <view
        v-if="closable"
        class="close-button"
        @tap="close"
      >
        ×
      </view>
      <view class="title">手机号快捷登录</view>
      <view class="description">完成手机号验证后，即可继续查看推荐、互动与消息提醒。</view>

      <!-- #ifdef MP-WEIXIN -->
      <button
        class="authorize-button ss-reset-button"
        open-type="getPhoneNumber"
        :loading="submitting"
        @getphonenumber="onGetPhoneNumber"
      >
        手机号快捷登录
      </button>
      <!-- #endif -->

      <!-- #ifndef MP-WEIXIN -->
      <view class="unsupported-tip">请在小程序端使用手机号快捷登录</view>
      <!-- #endif -->

      <view
        v-if="closable"
        class="cancel-button"
        @tap="close"
      >
        暂不登录
      </view>
    </view>
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import sheep from '@/sheep';
  import AuthUtil from '@/sheep/api/partner/auth';
  import {
    buildWeixinMiniLoginPayload,
    getPhoneAuthorizationCode,
    getWechatMiniLoginErrorMessage,
  } from '@/sheep/helper/wechat-mini-login';
  import {
    clearPendingBindUserId,
    getPendingBindUserId,
  } from '@/sheep/helper/member-profile-share';

  const props = defineProps({
    auto: {
      type: Boolean,
      default: false,
    },
    show: {
      type: Boolean,
      default: false,
    },
    closable: {
      type: Boolean,
      default: false,
    },
  });
  const emit = defineEmits(['close', 'success']);
  const submitting = ref(false);
  const visible = computed(
    () => !sheep.$store('user').isLogin && (props.auto || props.show),
  );

  function showError(message) {
    uni.showToast({ title: message, icon: 'none' });
  }

  function close() {
    if (props.closable && !submitting.value) emit('close');
  }

  async function getMiniProgramLoginCode() {
    return new Promise((resolve) => {
      uni.login({
        success: (result) => resolve(result?.code || ''),
        fail: () => resolve(''),
      });
    });
  }

  async function onGetPhoneNumber(event) {
    if (submitting.value) return;

    const phoneCode = getPhoneAuthorizationCode(event);
    if (!phoneCode) {
      showError('请先授权手机号');
      return;
    }

    submitting.value = true;
    try {
      const code = await getMiniProgramLoginCode();
      if (!code) {
        showError('登录失败，请重试');
        return;
      }

      const response = await AuthUtil.weixinMiniLogin(
        buildWeixinMiniLoginPayload({ code, phoneCode, bindUserId: getPendingBindUserId() }),
      );
      if (!response || response.code !== 0) {
        showError(getWechatMiniLoginErrorMessage(response));
        return;
      }

      clearPendingBindUserId();

      try {
        sheep.$store('social').startRealtime();
      } catch (error) {
        // 实时消息连接失败不影响已经成功完成的微信登录。
        console.warn('[wechat-mini-login] realtime startup failed after login', error);
      }
      emit('success');
      uni.showToast({ title: '登录成功', icon: 'none' });
    } catch (error) {
      showError(getWechatMiniLoginErrorMessage(error));
    } finally {
      submitting.value = false;
    }
  }
</script>

<style lang="scss" scoped>
  .wechat-login-mask {
    position: fixed;
    z-index: 1000;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    display: flex;
    align-items: flex-end;
    padding: 24rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
    box-sizing: border-box;
    background: rgba(0, 0, 0, 0.38);
  }

  .wechat-login-sheet {
    position: relative;
    width: 100%;
    padding: 44rpx 32rpx 32rpx;
    box-sizing: border-box;
    border-radius: 24rpx;
    background: #fff;
  }

  .close-button {
    position: absolute;
    top: 16rpx;
    right: 22rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 52rpx;
    height: 52rpx;
    color: #999;
    font-size: 42rpx;
    line-height: 1;
  }

  .title {
    color: #222;
    font-size: 36rpx;
    font-weight: 600;
  }

  .description,
  .unsupported-tip {
    margin-top: 18rpx;
    color: #777;
    font-size: 26rpx;
    line-height: 1.6;
  }

  .authorize-button {
    width: 100%;
    height: 88rpx;
    margin-top: 34rpx;
    border-radius: 44rpx;
    color: #fff;
    font-size: 30rpx;
    line-height: 88rpx;
    background: var(--ui-BG-Main, #ff6b6b);
  }

  .cancel-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 88rpx;
    margin-top: 20rpx;
    box-sizing: border-box;
    border: 2rpx solid #e6e6e6;
    border-radius: 44rpx;
    color: #666;
    font-size: 30rpx;
  }
</style>
