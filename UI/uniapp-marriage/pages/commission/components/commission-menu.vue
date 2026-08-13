<!-- 分销中心功能入口 -->
<template>
  <view class="menu-box">
    <view class="header-box">
      <view class="header-title">功能专区</view>
    </view>
    <view class="menu-list">
      <view
        v-for="item in state.menuList"
        :key="item.path"
        class="item-box"
        :class="{ 'item-box--disabled': disabled || state.locked }"
        @tap="handleTap(item)"
      >
        <image class="menu-icon" :src="sheep.$url.static(item.img)" mode="aspectFill" />
        <view class="item-title">{{ item.title }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { reactive } from 'vue';

  const props = defineProps({
    disabled: Boolean,
  });

  const state = reactive({
    locked: false,
    menuList: [
      {
        img: '/static/img/shop/commission/commission_icon1.png',
        title: '我的团队',
        path: '/pages/commission/team',
      },
      {
        img: '/static/img/shop/commission/commission_icon2.png',
        title: '佣金明细',
        path: '/pages/commission/wallet',
      },
      {
        img: '/static/img/shop/commission/commission_icon3.png',
        title: '分销订单',
        path: '/pages/commission/order',
      },
      {
        img: '/static/img/shop/commission/commission_icon4.png',
        title: '推广商品',
        path: '/pages/commission/goods',
      },
      {
        img: '/static/img/shop/commission/commission_icon6.png',
        title: '申请提现',
        path: '/pages/commission/withdraw',
      },
      {
        img: '/static/img/shop/commission/commission_icon7.png',
        title: '邀请海报',
        path: 'action:showShareModal',
      },
      {
        img: '/static/img/shop/commission/commission_icon8.png',
        title: '推广排行',
        path: '/pages/commission/promoter',
      },
      {
        img: '/static/img/shop/commission/commission_icon9.png',
        title: '佣金排行',
        path: '/pages/commission/commission-ranking',
      },
    ],
  });

  function handleTap(item) {
    if (props.disabled || state.locked) return;
    state.locked = true;
    sheep.$router.go(item.path);
    setTimeout(() => {
      state.locked = false;
    }, 500);
  }
</script>

<style lang="scss" scoped>
  .menu-box {
    margin: 0 30rpx 20rpx;
    overflow: hidden;
    background: var(--marriage-surface);
    border: 1rpx solid var(--marriage-line);
    border-radius: 24rpx;
    box-shadow: var(--marriage-shadow);
  }

  .header-box {
    padding: 24rpx 28rpx;
    background: var(--marriage-primary);
  }

  .header-title {
    color: #fff;
    font-size: 28rpx;
    font-weight: 700;
  }

  .menu-list {
    display: flex;
    flex-wrap: wrap;
    padding: 30rpx 0 8rpx;
  }

  .item-box {
    display: flex;
    width: 25%;
    margin-bottom: 28rpx;
    flex-direction: column;
    align-items: center;
    color: var(--marriage-text);
    font-size: 23rpx;
  }

  .item-box--disabled {
    opacity: 0.55;
  }

  .menu-icon {
    width: 68rpx;
    height: 68rpx;
    margin-bottom: 12rpx;
    background: var(--marriage-primary-soft);
    border-radius: 50%;
  }

  .item-title {
    color: var(--marriage-text);
  }
</style>
