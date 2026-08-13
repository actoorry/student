<!-- 择偶条件：只读展示当前人物推荐作用域服务端生成的 interestTags，无内容时保留空状态 -->
<template>
  <view v-if="shouldRender" class="partner-profile-preference">
    <view
      class="partner-profile-preference__title"
      :style="{
        color: validTitleColor,
        borderLeftColor: validAccentColor
      }"
    >
      <text class="partner-profile-preference__title-text">{{ validTitle }}</text>
    </view>
    <view v-if="validTags.length" class="partner-profile-preference__tags">
      <text
        v-for="(tag, idx) in validTags"
        :key="idx"
        class="partner-profile-preference__tag"
        :style="{
          color: validTagTextColor,
          backgroundColor: validTagBackgroundColor,
          borderRadius: validTagRadius + 'rpx'
        }"
      >
        {{ tag }}
      </text>
    </view>
    <view v-else class="partner-profile-preference__empty">
      <text class="partner-profile-preference__empty-text">该用户暂未填写择偶条件</text>
    </view>
  </view>
</template>

<script setup>
  import { computed, inject } from 'vue';
  import {
    hasInterestTags,
    normalizeAccentColor,
    normalizeInterestTags,
    normalizeTagBackgroundColor,
    normalizeTagRadius,
    normalizeTagTextColor,
    normalizeTitle,
    normalizeTitleColor,
  } from './partnerProfilePreference';

  /**
   * 组件只消费当前人物推荐作用域，不请求接口、不读取登录用户，
   * 也不维护推荐队列、索引、active 或任何人物持久化状态。
   * 所有标签通过 text 和模板插值按纯文本展示，禁止 HTML/富文本解释。
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

  const validTitle = computed(() => normalizeTitle(props.data?.title));
  const validTitleColor = computed(() => normalizeTitleColor(props.data?.titleColor));
  const validAccentColor = computed(() => normalizeAccentColor(props.data?.accentColor));
  const validTagTextColor = computed(() => normalizeTagTextColor(props.data?.tagTextColor));
  const validTagBackgroundColor = computed(() =>
    normalizeTagBackgroundColor(props.data?.tagBackgroundColor),
  );
  const validTagRadius = computed(() => normalizeTagRadius(props.data?.tagRadius));

  const validTags = computed(() =>
    hasContext.value ? normalizeInterestTags(profile.value?.interestTags) : [],
  );
  const shouldRender = computed(() => hasContext.value);
</script>

<style scoped lang="scss">
  .partner-profile-preference {
    box-sizing: border-box;
    width: 100%;
    padding: 0 32rpx;

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

    &__tags {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
    }

    &__tag {
      display: block;
      box-sizing: border-box;
      padding: 16rpx 28rpx;
      margin-right: 20rpx;
      margin-bottom: 20rpx;
      color: #8f675d;
      font-size: 26rpx;
      line-height: 36rpx;
      background-color: #fff2e7;
      border-radius: 32rpx;
      white-space: nowrap;
    }

    &__empty {
      box-sizing: border-box;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 140rpx;
      padding: 28rpx;
      background-color: #faf6f2;
      border: 2rpx dashed #e5d8d0;
      border-radius: 28rpx;
    }

    &__empty-text {
      color: #a4938e;
      font-size: 26rpx;
      line-height: 36rpx;
    }
  }
</style>
