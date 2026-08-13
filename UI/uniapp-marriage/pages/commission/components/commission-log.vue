<!-- 分销中心佣金动态：由父页传入已加载的数据 -->
<template>
  <view class="distribution-log-wrap">
    <view class="section-title">实时动态</view>
    <view v-if="loading" class="log-state">佣金动态加载中…</view>
    <view v-else-if="error" class="log-state log-state--error">
      <text>{{ error }}</text>
      <text class="retry" @tap="$emit('retry')">重试</text>
    </view>
    <view v-else-if="!records.length" class="log-state">暂无佣金动态</view>
    <scroll-view v-else scroll-y="true" class="log-scroll">
      <view class="log-item-box" v-for="item in records" :key="item.id">
        <view class="log-item">
          <image
            class="log-img"
            :src="sheep.$url.static('/static/img/shop/avatar/notice.png')"
            mode="aspectFill"
          />
          <view class="log-copy">
            <view class="log-text">{{ item.title || '佣金变动' }}</view>
            <view class="log-time">{{ formatTime(item.createTime) }}</view>
          </view>
          <text class="log-price">{{ formatPrice(item) }} 元</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
  import dayjs from 'dayjs';
  import sheep from '@/sheep';
  import { fen2yuan } from '@/sheep/hooks/useGoods';

  defineProps({
    records: {
      type: Array,
      default: () => [],
    },
    loading: Boolean,
    error: {
      type: String,
      default: '',
    },
  });

  defineEmits(['retry']);

  function formatTime(value) {
    return value ? dayjs(value).fromNow() : '';
  }

  function formatPrice(item) {
    return fen2yuan(item.price ?? item.totalPrice ?? 0);
  }
</script>

<style lang="scss" scoped>
  .distribution-log-wrap {
    margin: 0 30rpx 24rpx;
    overflow: hidden;
    background: var(--marriage-surface);
    border: 1rpx solid var(--marriage-line);
    border-radius: 24rpx;
    box-shadow: var(--marriage-shadow);
  }

  .section-title {
    padding: 24rpx 28rpx;
    color: var(--marriage-text);
    border-bottom: 1rpx solid var(--marriage-line);
    font-size: 28rpx;
    font-weight: 700;
  }

  .log-scroll {
    max-height: 560rpx;
  }

  .log-item-box {
    padding: 22rpx 24rpx;
    border-bottom: 1rpx solid var(--marriage-line);
  }

  .log-item {
    display: flex;
    align-items: center;
  }

  .log-img {
    width: 44rpx;
    height: 44rpx;
    margin-right: 14rpx;
    border-radius: 50%;
  }

  .log-copy {
    min-width: 0;
    flex: 1;
  }

  .log-text {
    overflow: hidden;
    color: var(--marriage-text);
    font-size: 25rpx;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .log-time,
  .log-state {
    color: var(--marriage-muted);
    font-size: 22rpx;
  }

  .log-price {
    margin-left: 16rpx;
    color: var(--marriage-primary);
    font-size: 24rpx;
    font-weight: 700;
  }

  .log-state {
    padding: 60rpx 24rpx;
    text-align: center;
  }

  .log-state--error {
    display: flex;
    justify-content: center;
    gap: 18rpx;
  }

  .retry {
    color: var(--marriage-primary);
  }
</style>
