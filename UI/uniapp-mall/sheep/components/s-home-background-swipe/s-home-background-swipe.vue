<!-- 历史装修图文组件：静态场景轮播，不承担页面级人物切换 -->
<template>
  <view v-if="validSlides.length > 0" class="s-home-background-swipe">
    <swiper
      :current="state.cur"
      :style="{ height: validHeight + 'rpx' }"
      :previous-margin="`${validPreviousMargin}rpx`"
      :next-margin="`${validNextMargin}rpx`"
      :indicator-dots="property.indicator === 'dot' && validSlides.length > 1"
      indicator-color="rgba(255,255,255,0.4)"
      indicator-active-color="#ffffff"
      @change="swiperChange"
    >
      <swiper-item v-for="(slide, index) in validSlides" :key="index">
        <view class="slide-wrap" :style="[getSlideBackground(slide)]">
          <!-- 背景图 -->
          <image
            v-if="slide.backgroundImage"
            class="slide-bg-image"
            :src="sheep.$url.cdn(slide.backgroundImage)"
            mode="aspectFill"
          />
          <!-- 内容层 -->
          <view class="slide-content">
            <image
              v-if="slide.foregroundImage"
              class="slide-foreground"
              :src="sheep.$url.cdn(slide.foregroundImage)"
              mode="aspectFit"
            />
            <view v-if="slide.title" class="slide-title">{{ slide.title }}</view>
            <view v-if="slide.subtitle" class="slide-subtitle">{{ slide.subtitle }}</view>
            <view v-if="slide.tags?.filter(Boolean).length" class="slide-tags">
              <text
                v-for="(tag, tagIndex) in slide.tags.filter(Boolean)"
                :key="tagIndex"
                class="slide-tag"
              >
                {{ tag }}
              </text>
            </view>
          </view>
        </view>
      </swiper-item>
    </swiper>
    <!-- 数字指示器 -->
    <view v-if="property.indicator === 'number' && validSlides.length > 1" class="indicator-number">
      {{ state.cur + 1 }} / {{ validSlides.length }}
    </view>
  </view>
</template>

<script setup>
  /**
   * 历史静态场景轮播
   * 使用原生 swiper 实现横向滑动、相邻场景露出、切换动画
   */
  import { computed, reactive, watch } from 'vue';
  import sheep from '@/sheep';

  const DEFAULT_HEIGHT = 960;
  const MIN_HEIGHT = 200;
  const MAX_HEIGHT = 1600;
  const DEFAULT_MARGIN = 24;
  const MAX_MARGIN = 200;
  const DEFAULT_BG_COLOR = '#F6D7DF';

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

  const state = reactive({
    cur: 0,
  });

  const property = computed(() => props.data || {});

  const validHeight = computed(() => {
    const height = Number(property.value.height);
    if (!height || height < MIN_HEIGHT) return DEFAULT_HEIGHT;
    if (height > MAX_HEIGHT) return MAX_HEIGHT;
    return height;
  });

  const validPreviousMargin = computed(() => {
    const margin = Number(property.value.previousMargin);
    if (!margin || margin < 0) return DEFAULT_MARGIN;
    if (margin > MAX_MARGIN) return MAX_MARGIN;
    return margin;
  });

  const validNextMargin = computed(() => {
    const margin = Number(property.value.nextMargin);
    if (!margin || margin < 0) return DEFAULT_MARGIN;
    if (margin > MAX_MARGIN) return MAX_MARGIN;
    return margin;
  });

  const validSlides = computed(() => {
    const slides = Array.isArray(property.value.slides) ? property.value.slides : [];
    return slides.filter((slide) => !!slide);
  });

  const validInitialIndex = computed(() => {
    const index = Number(property.value.initialIndex) || 0;
    if (index < 0) return 0;
    if (validSlides.value.length === 0) return 0;
    if (index >= validSlides.value.length) return validSlides.value.length - 1;
    return index;
  });

  watch(
    () => property.value,
    () => {
      state.cur = validInitialIndex.value;
    },
    { immediate: true, deep: true },
  );

  const swiperChange = (e) => {
    state.cur = e.detail.current;
  };

  const getSlideBackground = (slide) => {
    const isValidColor = (color) =>
      typeof color === 'string' && /^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$/.test(color);
    return {
      backgroundColor: isValidColor(slide.backgroundColor)
        ? slide.backgroundColor
        : DEFAULT_BG_COLOR,
    };
  };
</script>

<style lang="scss" scoped>
  .s-home-background-swipe {
    position: relative;
    width: 100%;

    .slide-wrap {
      position: relative;
      width: 100%;
      height: 100%;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .slide-bg-image {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 0;
    }

    .slide-content {
      position: relative;
      z-index: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40rpx;
      box-sizing: border-box;
      width: 100%;
    }

    .slide-foreground {
      width: 300rpx;
      height: 300rpx;
      margin-bottom: 30rpx;
    }

    .slide-title {
      font-size: 40rpx;
      font-weight: bold;
      color: #ffffff;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
      text-align: center;
      line-height: 1.4;
    }

    .slide-subtitle {
      font-size: 26rpx;
      color: #ffffff;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
      text-align: center;
      margin-top: 16rpx;
      line-height: 1.4;
    }

    .slide-tags {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 16rpx;
      margin-top: 30rpx;
    }

    .slide-tag {
      font-size: 22rpx;
      color: #ffffff;
      background-color: rgba(255, 255, 255, 0.2);
      border-radius: 200rpx;
      padding: 8rpx 20rpx;
      backdrop-filter: blur(4rpx);
    }

    .indicator-number {
      position: absolute;
      bottom: 20rpx;
      right: 20rpx;
      background-color: rgba(0, 0, 0, 0.4);
      color: #ffffff;
      font-size: 20rpx;
      border-radius: 20rpx;
      padding: 6rpx 16rpx;
      z-index: 2;
    }
  }
</style>
