<!-- 装修用户组件：登录/退出 -->
<template>
  <view v-if="shouldShow" class="user-auth-btn-wrap" :style="[wrapStyle]">
    <button
      class="user-auth-btn ss-reset-button"
      @tap="onTap"
    >
      {{ buttonText }}
    </button>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import sheep from '@/sheep'
import { showAuthModal } from '@/sheep/hooks/useModal'
import AuthUtil from '@/sheep/api/partner/auth'

const DEFAULT_CONFIG = {
  loginText: '登录',
  logoutText: '退出登录',
  logoutConfirmText: '确认退出账号？',
}

const props = defineProps({
  data: {
    type: Object,
    default: () => ({}),
  },
  styles: {
    type: Object,
    default: () => ({}),
  },
})

const config = computed(() => ({
  loginText: props.data?.loginText || DEFAULT_CONFIG.loginText,
  logoutText: props.data?.logoutText || DEFAULT_CONFIG.logoutText,
  logoutConfirmText: props.data?.logoutConfirmText || DEFAULT_CONFIG.logoutConfirmText,
}))

const isLogin = computed(() => sheep.$store('user').isLogin)

// 登录/退出按钮在个人中心与其它页面均展示。登录改为用户主动点击后弹出授权弹窗，
// 不再由个人中心自动弹出登录面板，避免强制用户登录才能浏览。
const shouldShow = computed(() => true)

const buttonText = computed(() =>
  isLogin.value ? config.value.logoutText : config.value.loginText,
)

const wrapStyle = computed(() => {
  const style = props.styles || {}
  return {
    marginLeft: `${style.marginLeft || 0}px`,
    marginRight: `${style.marginRight || 0}px`,
    marginBottom: `${style.marginBottom || 0}px`,
  }
})

const onLogin = () => {
  showAuthModal()
}

const onLogout = () => {
  uni.showModal({
    title: '提示',
    content: config.value.logoutConfirmText,
    success: async (res) => {
      if (!res.confirm) return
      const { code } = await AuthUtil.logout()
      if (code !== 0) return
      await sheep.$store('user').logout()
    },
  })
}

const onTap = () => {
  if (isLogin.value) {
    onLogout()
  } else {
    onLogin()
  }
}
</script>

<style lang="scss" scoped>
.user-auth-btn-wrap {
  padding: 0 24rpx 20rpx;
}

.user-auth-btn {
  width: 100%;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  color: #fff;
  background: linear-gradient(135deg, #d62d24, #b81d18);
  box-shadow: 0 8rpx 20rpx rgba(214, 45, 36, 0.28);
}
</style>
