<!-- 基本资料：只读展示当前人物推荐作用域中的资料，无内容时保留空状态 -->
<template>
  <view v-if="shouldRender" class="partner-profile-basic-info">
    <view class="partner-profile-basic-info__title">
      <text class="partner-profile-basic-info__title-text">基本资料</text>
    </view>
    <view v-if="displayItems.length" class="partner-profile-basic-info__grid">
      <view
        v-for="item in displayItems"
        :key="item.key"
        class="partner-profile-basic-info__cell"
      >
        <text class="partner-profile-basic-info__label">{{ item.label }}</text>
        <text class="partner-profile-basic-info__value">{{ item.value }}</text>
      </view>
    </view>
    <view v-else class="partner-profile-basic-info__empty">
      <text class="partner-profile-basic-info__empty-text">该用户暂未填写资料</text>
    </view>
  </view>
</template>

<script setup>
  import { computed, inject } from 'vue';
  import { buildPartnerProfileBasicInfoItems } from './partnerProfileBasicInfo';

  /**
   * 组件只消费当前人物推荐作用域，不请求接口、不读取登录用户，
   * 也不维护推荐队列、索引、active 或任何人物持久化状态。
   */
  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
  });

  const partnerContext = inject('partnerRecommendationContext', null);
  const context = computed(() => partnerContext?.value || null);
  const hasContext = computed(
    () =>
      typeof context.value?.partnerId === 'number' &&
      !!context.value?.profile &&
      typeof context.value.profile === 'object',
  );
  const profile = computed(() => context.value?.profile || null);
  const displayItems = computed(() =>
    hasContext.value ? buildPartnerProfileBasicInfoItems(profile.value, props.data) : [],
  );
  const shouldRender = computed(() => hasContext.value);
</script>

<style scoped lang="scss">
  .partner-profile-basic-info {
    box-sizing: border-box;
    width: 100%;
    padding: 36rpx 32rpx 0;

    &__title {
      box-sizing: border-box;
      padding-left: 20rpx;
      margin-bottom: 28rpx;
      border-left: 8rpx solid #c84449;
    }

    &__title-text {
      display: block;
      color: #2d2324;
      font-size: 36rpx;
      font-weight: 700;
      line-height: 48rpx;
    }

    &__grid {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      justify-content: space-between;
    }

    &__cell {
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      width: 48.4%;
      min-height: 160rpx;
      padding: 28rpx;
      margin-bottom: 20rpx;
      background-color: #f7f3ef;
      border-radius: 32rpx;
    }

    &__label {
      display: block;
      margin-bottom: 16rpx;
      color: #8f7e79;
      font-size: 24rpx;
      line-height: 32rpx;
    }

    &__value {
      display: block;
      color: #000;
      font-size: 32rpx;
      font-weight: 700;
      line-height: 44rpx;
      word-break: break-all;
    }

    &__empty {
      box-sizing: border-box;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 160rpx;
      padding: 28rpx;
      background-color: #f7f3ef;
      border: 2rpx dashed #dfd4ce;
      border-radius: 32rpx;
    }

    &__empty-text {
      color: #a4938e;
      font-size: 26rpx;
      line-height: 36rpx;
    }
  }
</style>
