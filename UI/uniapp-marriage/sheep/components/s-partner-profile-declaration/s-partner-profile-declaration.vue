<!-- 择偶宣言：只读展示当前人物推荐作用域的真实 bio -->
<template>
  <view v-if="hasContext" class="partner-profile-declaration">
    <text
      class="partner-profile-declaration__title"
      :style="{ color: validTitleColor }"
    >
      {{ validTitle }}
    </text>
    <view
      class="partner-profile-declaration__content"
      :style="{
        color: validContentColor,
        backgroundColor: validContentBackgroundColor,
        borderRadius: validContentRadius + 'rpx',
      }"
    >
      <text
        class="partner-profile-declaration__text"
        :class="{ 'partner-profile-declaration__text--empty': isEmpty }"
      >
        {{ declarationText }}
      </text>
    </view>
  </view>
</template>

<script setup>
  import { computed, inject } from 'vue';
  import {
    formatDeclarationBio,
    hasDeclarationBio,
    normalizeContentBackgroundColor,
    normalizeContentColor,
    normalizeContentRadius,
    normalizeTitle,
    normalizeTitleColor,
  } from './partnerProfileDeclaration';

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
    () => typeof context.value?.partnerId === 'number' && !!context.value?.profile,
  );
  const profile = computed(() => context.value?.profile || null);

  const validTitle = computed(() => normalizeTitle(props.data?.title));
  const validTitleColor = computed(() => normalizeTitleColor(props.data?.titleColor));
  const validContentColor = computed(() => normalizeContentColor(props.data?.contentColor));
  const validContentBackgroundColor = computed(() =>
    normalizeContentBackgroundColor(props.data?.contentBackgroundColor),
  );
  const validContentRadius = computed(() => normalizeContentRadius(props.data?.contentRadius));

  const declarationText = computed(() => formatDeclarationBio(profile.value));
  const isEmpty = computed(() => !hasDeclarationBio(profile.value));
</script>

<style scoped lang="scss">
  .partner-profile-declaration {
    width: 100%;
    box-sizing: border-box;

    &__title {
      display: block;
      margin-bottom: 24rpx;
      font-size: 26rpx;
      line-height: 36rpx;
    }

    &__content {
      box-sizing: border-box;
      padding: 32rpx;
    }

    &__text {
      display: block;
      font-size: 28rpx;
      line-height: 48rpx;
      white-space: pre-wrap;
      word-break: break-word;

      &--empty {
        opacity: 0.68;
      }
    }
  }
</style>
