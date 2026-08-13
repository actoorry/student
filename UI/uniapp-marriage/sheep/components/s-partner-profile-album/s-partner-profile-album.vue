<!-- 人物相册：只读消费当前人物推荐作用域的 albumImages，无内容时保留空状态 -->
<template>
  <view v-if="shouldRender" class="partner-profile-album">
    <!-- 标题行 -->
    <view class="partner-profile-album__header">
      <view class="partner-profile-album__title-row">
        <text
          class="partner-profile-album__title"
          :style="{ color: validTitleColor }"
        >{{ validTitle }}</text>
        <text
          v-if="validSubtitle"
          class="partner-profile-album__subtitle"
          :style="{ color: validSubtitleColor }"
        >{{ validSubtitle }}</text>
      </view>
    </view>

    <!-- 三列图片网格 -->
    <view v-if="hasVisibleImages" class="partner-profile-album__grid">
      <view
        v-for="item in visibleItems"
        :key="item.generation + ':' + item.candidate"
        class="partner-profile-album__image-wrap"
        :style="{
          width: '31%',
          height: validImageHeight + 'rpx',
          borderRadius: validImageRadius + 'rpx',
        }"
      >
        <image
          class="partner-profile-album__image"
          :src="resolveDisplayUrl(item.candidate)"
          mode="aspectFill"
          lazy-load
          :style="{ borderRadius: validImageRadius + 'rpx' }"
          @load="handleImageLoad(item.candidate, item.generation)"
          @error="handleImageError(item.candidate, item.generation)"
          @click="handleImageClick(item.candidate, item.generation)"
        />
      </view>
    </view>
    <view v-else class="partner-profile-album__empty">
      <text class="partner-profile-album__empty-icon">＋</text>
      <text class="partner-profile-album__empty-text">该用户暂未上传相册</text>
    </view>
  </view>
</template>

<script setup>
  import { computed, inject, ref, watch } from 'vue';
  import sheep from '@/sheep';
  import InteractionApi from '@/sheep/api/marriage/interaction';
  import useUserStore from '@/sheep/store/user';
  import {
    computeIdentityKey,
    createAlbumMediaState,
    DEFAULT_TITLE_COLOR,
    DEFAULT_SUBTITLE_COLOR,
    getSuccessfulAlbumCandidates,
    getVisibleAlbumItems,
    normalizeImageHeight,
    normalizeImageRadius,
    normalizeColor,
    normalizeSubtitle,
    normalizeTitle,
    transitionAlbumMediaState,
    VIEW_REQUEST_STATE,
  } from './partnerProfileAlbum';

  /**
   * 人物资料相册
   *
   * 仅消费当前人物推荐作用域的只读 profile.albumImages，不自行请求推荐接口，
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

  // ---- 上下文 ----

  const hasContext = computed(() => {
    const ctx = partnerContext?.value;
    return !!ctx && typeof ctx.partnerId === 'number' && !!ctx.profile;
  });

  const profile = computed(() => partnerContext?.value?.profile || null);
  const partnerId = computed(() => partnerContext?.value?.partnerId || null);

  // ---- 属性规范化 ----

  const validTitle = computed(() => normalizeTitle(props.data?.title));
  const validSubtitle = computed(() => normalizeSubtitle(props.data?.subtitle));
  const validTitleColor = computed(() => normalizeColor(props.data?.titleColor, DEFAULT_TITLE_COLOR));
  const validSubtitleColor = computed(() => normalizeColor(props.data?.subtitleColor, DEFAULT_SUBTITLE_COLOR));
  const validImageHeight = computed(() => normalizeImageHeight(props.data?.imageHeight));
  const validImageRadius = computed(() => normalizeImageRadius(props.data?.imageRadius));

  // ---- 媒体状态机 ----

  const mediaState = ref(createAlbumMediaState(null));

  /** 当前 generation 所有已渲染项（成功 + pending） */
  const visibleItems = computed(() => {
    return getVisibleAlbumItems(mediaState.value);
  });

  const allFailed = computed(() => {
    const candidates = mediaState.value.candidates;
    if (candidates.length === 0) return true;
    return candidates.every((url) => mediaState.value.states.get(url) === 'failed');
  });

  const hasVisibleImages = computed(() => !allFailed.value && visibleItems.value.length > 0);

  /** 有有效人物上下文时始终渲染；无相册或全部加载失败时展示空状态 */
  const shouldRender = computed(() => hasContext.value);

  // ---- Generation 与状态重置 ----

  function resetImageStates() {
    mediaState.value = createAlbumMediaState(profile.value, mediaState.value.generation + 1);
  }

  const identityKey = computed(() => computeIdentityKey(partnerId.value, profile.value?.albumImages));

  watch(identityKey, (newKey, oldKey) => {
    if (newKey !== oldKey) {
      resetImageStates();
    }
  }, { immediate: true });

  // ---- 图片事件处理 ----

  function handleImageLoad(url, eventGeneration) {
    mediaState.value = transitionAlbumMediaState(mediaState.value, eventGeneration, url, 'success');
  }

  function handleImageError(url, eventGeneration) {
    mediaState.value = transitionAlbumMediaState(mediaState.value, eventGeneration, url, 'failed');
  }

  // ---- 图片预览 ----

  function getSuccessUrls() {
    return getSuccessfulAlbumCandidates(mediaState.value).map(resolveDisplayUrl);
  }

  function resolveDisplayUrl(url) {
    return sheep.$url.cdn(url);
  }

  function handleImageClick(url, eventGeneration) {
    if (eventGeneration !== mediaState.value.generation) return;
    const displayUrl = resolveDisplayUrl(url);
    const successUrls = getSuccessUrls();

    // 仅已成功加载的图片可点击
    if (!successUrls.includes(displayUrl)) return;

    // 1. 原生图片预览（独立于记录请求）
    uni.previewImage({
      current: displayUrl,
      urls: successUrls,
      fail: () => {
        // 预览失败不改变组件状态
      },
    });

    // 2. 已登录用户静默记录"谁看过我"
    if (eventGeneration !== mediaState.value.generation) return;

    const userStore = useUserStore();
    if (!userStore.isLogin) return;

    const currentPartnerId = partnerId.value;
    if (typeof currentPartnerId !== 'number') return;

    triggerRecordView(currentPartnerId);
  }

  // ---- 查看请求状态机 ----

  const viewRequestStates = ref(new Map());

  function getViewRequestState(targetPartnerId) {
    return viewRequestStates.value.get(targetPartnerId) || VIEW_REQUEST_STATE.IDLE;
  }

  function setViewRequestState(targetPartnerId, state) {
    const states = new Map(viewRequestStates.value);
    states.set(targetPartnerId, state);
    viewRequestStates.value = states;
  }

  function triggerRecordView(targetPartnerId) {
    if (getViewRequestState(targetPartnerId) !== VIEW_REQUEST_STATE.IDLE) return;

    setViewRequestState(targetPartnerId, VIEW_REQUEST_STATE.PENDING);

    InteractionApi.recordView(targetPartnerId)
      .then((res) => {
        if (res && res.code === 0) {
          setViewRequestState(targetPartnerId, VIEW_REQUEST_STATE.SUCCESS);
        } else {
          setViewRequestState(targetPartnerId, VIEW_REQUEST_STATE.IDLE);
        }
      })
      .catch(() => {
        setViewRequestState(targetPartnerId, VIEW_REQUEST_STATE.IDLE);
      });
  }

</script>

<style scoped lang="scss">
  .partner-profile-album {
    box-sizing: border-box;
    width: 100%;
    padding: 36rpx 32rpx 0;

    &__header {
      margin-bottom: 28rpx;
    }

    &__title-row {
      display: flex;
      align-items: baseline;
    }

    &__title {
      display: block;
      padding-left: 20rpx;
      border-left: 8rpx solid #c84449;
      font-size: 36rpx;
      font-weight: 700;
      line-height: 48rpx;
      margin-right: 20rpx;
    }

    &__subtitle {
      display: block;
      font-size: 24rpx;
      line-height: 32rpx;
    }

    &__grid {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      justify-content: space-between;
    }

    &__image-wrap {
      overflow: hidden;
      background-color: #f0f0f0;
      margin-bottom: 20rpx;
    }

    &__image {
      width: 100%;
      height: 100%;
      display: block;
    }

    &__empty {
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 260rpx;
      padding: 36rpx;
      background-color: #f7f3ef;
      border: 2rpx dashed #dfd4ce;
      border-radius: 28rpx;
    }

    &__empty-icon {
      display: block;
      margin-bottom: 12rpx;
      color: #c7b8b2;
      font-size: 56rpx;
      font-weight: 300;
      line-height: 56rpx;
    }

    &__empty-text {
      color: #a4938e;
      font-size: 26rpx;
      line-height: 36rpx;
    }
  }
</style>
