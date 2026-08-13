<!-- 分销权限提示弹窗；资格数据由分销中心父页决定 -->
<template>
  <su-popup
    :show="show"
    type="center"
    round="10"
    :isMaskClick="false"
    maskBackgroundColor="rgba(0, 0, 0, 0.7)"
    @close="$emit('close')"
  >
    <view class="notice-box">
      <view class="notice-title">{{ title }}</view>
      <view class="notice-detail">{{ message || '该功能暂不可用' }}</view>
      <button class="notice-btn" @tap="$emit('close')">知道了</button>
    </view>
  </su-popup>
</template>

<script setup>
  defineProps({
    show: Boolean,
    title: {
      type: String,
      default: '分销资格暂不可用',
    },
    message: {
      type: String,
      default: '',
    },
  });

  defineEmits(['close']);
</script>

<style lang="scss" scoped>
  .notice-box {
    display: flex;
    width: 612rpx;
    min-height: 300rpx;
    box-sizing: border-box;
    padding: 48rpx 36rpx;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: var(--marriage-surface);
    border-radius: 24rpx;
  }

  .notice-title {
    color: var(--marriage-text);
    font-size: 32rpx;
    font-weight: 700;
  }

  .notice-detail {
    margin: 24rpx 0 34rpx;
    color: var(--marriage-muted);
    font-size: 25rpx;
    text-align: center;
  }

  .notice-btn {
    width: 492rpx;
    color: #fff;
    background: var(--marriage-primary);
    border-radius: 999rpx;
    font-size: 28rpx;
  }
</style>
