<!-- 分销账户摘要：数据由分销中心父页统一加载 -->
<template>
  <view class="account-card">
    <view class="card-header">
      <view class="header-title">账户信息</view>
      <view class="detail-link" @tap="sheep.$router.go('/pages/commission/wallet')">
        查看明细
        <text class="cicon-play-arrow" />
      </view>
    </view>

    <view v-if="loading" class="account-loading">账户摘要加载中…</view>
    <view v-else class="card-content">
      <view class="account-item">
        <view class="item-title">当前佣金(元)</view>
        <view class="item-detail">{{ fen2yuan(summary.brokeragePrice || 0) }}</view>
      </view>
      <view class="account-item">
        <view class="item-title">昨天佣金(元)</view>
        <view class="item-detail">{{ fen2yuan(summary.yesterdayPrice || 0) }}</view>
      </view>
      <view class="account-item">
        <view class="item-title">累计已提(元)</view>
        <view class="item-detail">{{ fen2yuan(summary.withdrawPrice || 0) }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { computed } from 'vue';
  import { fen2yuan } from '@/sheep/hooks/useGoods';

  const props = defineProps({
    summary: {
      type: Object,
      default: () => ({}),
    },
    loading: Boolean,
  });

  const summary = computed(() => props.summary || {});
</script>

<style lang="scss" scoped>
  .account-card {
    margin: 0 30rpx 20rpx;
    overflow: hidden;
    background: var(--marriage-surface);
    border: 1rpx solid var(--marriage-line);
    border-radius: 24rpx;
    box-shadow: var(--marriage-shadow);
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 28rpx;
    border-bottom: 1rpx solid var(--marriage-line);
  }

  .header-title {
    color: var(--marriage-text);
    font-size: 28rpx;
    font-weight: 700;
  }

  .detail-link {
    color: var(--marriage-primary);
    font-size: 24rpx;
  }

  .card-content {
    display: flex;
    padding: 34rpx 16rpx;
  }

  .account-item {
    flex: 1;
    text-align: center;
  }

  .item-title,
  .account-loading {
    color: var(--marriage-muted);
    font-size: 23rpx;
  }

  .item-detail {
    margin-top: 14rpx;
    color: var(--marriage-text);
    font-size: 34rpx;
    font-weight: 700;
  }

  .account-loading {
    padding: 48rpx 24rpx;
    text-align: center;
  }
</style>
