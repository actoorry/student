<!-- 分销中心用户信息 -->
<template>
  <view class="user-card">
    <view class="head-img-box">
      <image class="head-img" :src="sheep.$url.cdn(userInfo.avatar)" mode="aspectFill" />
    </view>
    <view class="user-copy">
      <view class="user-name">{{ userInfo.nickname || '婚恋服务用户' }}</view>
      <view class="user-subtitle">婚恋分销伙伴</view>
    </view>
  </view>
</template>

<script setup>
  import { computed } from 'vue';
  import sheep from '@/sheep';

  const props = defineProps({
    userInfo: {
      type: Object,
      default: () => ({}),
    },
  });

  const userInfo = computed(() => props.userInfo || {});
</script>

<style lang="scss" scoped>
  .user-card {
    display: flex;
    align-items: center;
    margin: 24rpx 30rpx 20rpx;
    padding: 28rpx;
    background: var(--marriage-surface);
    border: 1rpx solid var(--marriage-line);
    border-radius: 24rpx;
    box-shadow: var(--marriage-shadow);
  }

  .head-img-box {
    width: 96rpx;
    height: 96rpx;
    margin-right: 22rpx;
    overflow: hidden;
    background: var(--marriage-primary-soft);
    border-radius: 50%;
  }

  .head-img {
    width: 100%;
    height: 100%;
  }

  .user-copy {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
  }

  .user-name {
    color: var(--marriage-text);
    font-size: 32rpx;
    font-weight: 700;
  }

  .user-subtitle {
    color: var(--marriage-muted);
    font-size: 24rpx;
  }
</style>
