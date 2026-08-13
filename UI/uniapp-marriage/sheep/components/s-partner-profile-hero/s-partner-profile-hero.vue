<!-- 人物主图：消费当前人物推荐作用域，只展示主图、头像、姓名和有效年龄 -->
<template>
  <view
    v-if="hasContext"
    class="partner-profile-hero"
    :style="{ height: validHeight + 'rpx' }"
  >
    <!-- 主图 -->
    <image
      :key="mainImageKey"
      class="partner-profile-hero__main"
      :src="mainImageUrl"
      mode="aspectFill"
      @error="handleMainImageError"
    />

    <!-- 主图空态背景：全部候选失败时显示 -->
    <view
      v-if="!mainImageUrl"
      class="partner-profile-hero__main partner-profile-hero__placeholder"
    />

    <!-- 底部可读性遮罩 -->
    <view class="partner-profile-hero__mask" />

    <!-- 身份面板 -->
    <view class="partner-profile-hero__panel">
      <view class="partner-profile-hero__identity">
        <!-- 头像 -->
        <image
          :key="avatarKey"
          class="partner-profile-hero__avatar"
          :style="{
            width: validAvatarSize + 'rpx',
            height: validAvatarSize + 'rpx',
          }"
          :src="avatarUrl"
          mode="aspectFill"
          @error="handleAvatarError"
        />
        <view class="partner-profile-hero__text">
          <text
            v-if="displayName"
            class="partner-profile-hero__name"
            :style="{ color: validNameColor }"
          >
            {{ displayName }}
          </text>
          <text
            v-if="displayAge"
            class="partner-profile-hero__age"
            :style="{ color: validAgeColor }"
          >
            {{ displayAge }}岁
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { computed, inject, ref, watch } from 'vue';
  import {
    buildAvatarCandidates,
    buildMainCandidates,
    DEFAULT_AVATAR_URL,
    DEFAULT_AGE_COLOR,
    DEFAULT_AVATAR_SIZE,
    DEFAULT_HEIGHT,
    DEFAULT_NAME_COLOR,
    formatDisplayAge,
    formatDisplayName,
    normalizeAgeColor,
    normalizeAvatarSize,
    normalizeHeight,
    normalizeNameColor,
  } from './partnerProfileHero';

  /**
   * 人物主图
   *
   * 仅消费当前人物推荐作用域的只读 profile，不自行请求推荐接口，
   * 不维护推荐队列/索引/active，也不把人物上下文写入 property、Store 或分享参数。
   */
  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    styles: {
      type: Object,
      default: () => ({}),
    },
  });

  const partnerContext = inject('partnerRecommendationContext', null);

  const hasContext = computed(() => {
    const ctx = partnerContext?.value;
    return !!ctx && typeof ctx.partnerId === 'number' && !!ctx.profile;
  });

  const profile = computed(() => partnerContext?.value?.profile || null);
  const partnerId = computed(() => partnerContext?.value?.partnerId || null);

  // 视觉字段规范化
  const validHeight = computed(() => normalizeHeight(props.data?.height));
  const validAvatarSize = computed(() => normalizeAvatarSize(props.data?.avatarSize));
  const validNameColor = computed(() => normalizeNameColor(props.data?.nameColor));
  const validAgeColor = computed(() => normalizeAgeColor(props.data?.ageColor));

  // 人物字段
  const displayName = computed(() => formatDisplayName(profile.value));
  const displayAge = computed(() => formatDisplayAge(profile.value));

  // 主图本地状态
  const mainImageVersion = ref(0);
  const mainCandidateIndex = ref(0);
  const mainImageKey = computed(() => `${mainImageVersion.value}:${mainCandidateIndex.value}`);
  const mainImageUrl = computed(() => {
    const candidates = buildMainCandidates(profile.value);
    if (candidates.length === 0) return '';
    const index = Math.min(mainCandidateIndex.value, candidates.length - 1);
    return candidates[index];
  });

  function resetMainImageState() {
    mainImageVersion.value += 1;
    mainCandidateIndex.value = 0;
  }

  function handleMainImageError() {
    const candidates = buildMainCandidates(profile.value);
    if (mainCandidateIndex.value < candidates.length - 1) {
      mainCandidateIndex.value += 1;
    }
  }

  // 头像本地状态
  const avatarVersion = ref(0);
  const avatarFailed = ref(false);
  const avatarKey = computed(() => `${avatarVersion.value}:${avatarFailed.value ? 'default' : 'profile'}`);

  const avatarUrl = computed(() => {
    const candidates = buildAvatarCandidates(profile.value);
    if (avatarFailed.value || candidates.length === 0) {
      return DEFAULT_AVATAR_URL;
    }
    return candidates[0];
  });

  function resetAvatarState() {
    avatarVersion.value += 1;
    avatarFailed.value = false;
  }

  function handleAvatarError() {
    // 当前已使用默认头像时不再循环重试
    if (avatarUrl.value === DEFAULT_AVATAR_URL) return;
    avatarFailed.value = true;
  }

  // 人物上下文变化时重置图片状态，避免串图
  watch(
    () => [
      partnerId.value,
      profile.value?.mainImage,
      profile.value?.albumImages?.join('|'),
      profile.value?.avatarImage,
    ].join('|'),
    () => {
      resetMainImageState();
      resetAvatarState();
    },
  );
</script>

<style scoped lang="scss">
  .partner-profile-hero {
    position: relative;
    width: 100%;
    overflow: hidden;

    &__main,
    &__placeholder {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
    }

    &__placeholder {
      background: linear-gradient(180deg, #F6D7DF 0%, #D7E6F6 100%);
    }

    &__mask {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 320rpx;
      background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.45) 100%);
      pointer-events: none;
    }

    &__panel {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 24rpx 32rpx 48rpx;
      pointer-events: none;
    }

    &__identity {
      display: flex;
      align-items: flex-end;
    }

    &__avatar {
      flex-shrink: 0;
      border-radius: 50%;
      overflow: hidden;
      margin-right: 24rpx;
      background-color: #e0e0e0;
    }

    &__text {
      display: flex;
      flex-direction: column;
      padding-bottom: 8rpx;
    }

    &__name {
      font-size: 36rpx;
      font-weight: bold;
      line-height: 1.3;
    }

    &__age {
      font-size: 26rpx;
      line-height: 1.4;
      margin-top: 6rpx;
    }
  }
</style>
