<!-- 实名认证标志：只读消费当前人物作用域，仅在 realVerified === 1 时渲染 -->
<template>
  <view v-if="hasContext && isVerified" class="partner-real-name-verification-badge">
    <view
      class="partner-real-name-verification-badge__card"
      :style="{
        backgroundColor: validBadgeBackgroundColor,
        borderRadius: '40rpx',
        padding: '28rpx 32rpx',
        marginBottom: '20rpx',
      }"
    >
      <view class="partner-real-name-verification-badge__header">
        <view
          class="partner-real-name-verification-badge__icon"
          :style="{
            width: '72rpx',
            height: '72rpx',
            color: validAccentColor,
          }"
        >
          ✓
        </view>
        <text
          class="partner-real-name-verification-badge__title"
          :style="{ color: validTitleColor }"
        >
          实名认证
        </text>
      </view>
      <text
        class="partner-real-name-verification-badge__description"
        :style="{ color: validDescriptionColor }"
      >
        {{ description }}
      </text>
    </view>
  </view>
</template>

<script setup>
  import { computed, inject } from 'vue';
  import {
    buildRealNameDescription,
    isRealVerified,
    normalizeAccentColor,
    normalizeBadgeBackgroundColor,
    normalizeDescriptionColor,
    normalizeTitleColor,
  } from './partnerRealNameVerificationBadge';

  /**
   * 实名认证标志
   *
   * 仅消费当前人物推荐作用域的只读 profile，不自行请求认证或推荐接口，
   * 不维护推荐队列、索引或当前选中位置，也不把认证状态、身份证片段写入 property、Store 或分享参数。
   */
  const props = defineProps({
    data: { type: Object, default: () => ({}) },
    styles: { type: Object, default: () => ({}) },
  });

  const partnerContext = inject('partnerRecommendationContext', null);

  const hasContext = computed(() => {
    const ctx = partnerContext?.value;
    return !!ctx && typeof ctx.partnerId === 'number' && !!ctx.profile;
  });

  const profile = computed(() => partnerContext?.value?.profile || null);
  const isVerified = computed(() => isRealVerified(profile.value));

  const validBadgeBackgroundColor = computed(() =>
    normalizeBadgeBackgroundColor(props.data?.badgeBackgroundColor),
  );
  const validAccentColor = computed(() => normalizeAccentColor(props.data?.accentColor));
  const validTitleColor = computed(() => normalizeTitleColor(props.data?.titleColor));
  const validDescriptionColor = computed(() =>
    normalizeDescriptionColor(props.data?.descriptionColor),
  );

  const description = computed(() => buildRealNameDescription(profile.value));
</script>

<style scoped lang="scss">
  .partner-real-name-verification-badge {
    width: 100%;
    box-sizing: border-box;

    &__card {
      box-sizing: border-box;
    }

    &__header {
      display: flex;
      align-items: center;
      gap: 16rpx;
      margin-bottom: 8rpx;
    }

    &__icon {
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      border-radius: 50%;
      font-size: 28rpx;
      font-weight: bold;
      background-color: rgba(255, 255, 255, 0.6);
    }

    &__title {
      font-size: 30rpx;
      font-weight: bold;
      line-height: 40rpx;
    }

    &__description {
      font-size: 24rpx;
      line-height: 36rpx;
    }
  }
</style>
