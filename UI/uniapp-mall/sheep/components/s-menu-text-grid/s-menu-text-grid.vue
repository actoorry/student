<!-- 装修基础组件：文字分类导航区 -->
<template>
  <view
    class="ui-swiper menu-text-grid"
    :class="[props.mode, props.ui]"
    :style="[bgStyle, { height: swiperHeight + 'rpx' }]"
  >
    <scroll-view
      v-if="props.data.scrollable === true"
      class="menu-text-grid-scroll"
      scroll-x
      :show-scrollbar="false"
      scroll-anchoring
      :style="[{ height: rowHeight + 'rpx' }]"
    >
      <view class="menu-text-grid-scroll-row">
        <view
          v-for="(item, itemIndex) in props.data.list || []"
          :key="itemIndex"
          class="menu-text-grid-scroll-item ss-flex ss-col-center ss-row-center"
          :class="{ 'menu-text-grid-bottom-align': props.verticalAlign === 'bottom' }"
          :style="[{ width: `${750 / columnCount}rpx`, height: rowHeight + 'rpx' }]"
          hover-class="ss-hover-btn"
          @tap="onItemClick(item, itemIndex)"
        >
          <text
            class="menu-title menu-text-grid-scroll-title"
            :class="{ 'menu-text-active': itemIndex === props.activeIndex }"
            :style="{
              color: itemIndex === props.activeIndex ? '' : item.titleColor || props.data.textColor,
              fontWeight: itemIndex === props.activeIndex ? 'bold' : 'normal',
              fontSize: (props.data.fontSize || 13) * 2 + 'rpx',
            }"
          >
            {{ item.title }}
          </text>
        </view>
      </view>
    </scroll-view>
    <swiper
      v-else
      :circular="props.circular"
      :current="state.cur"
      :autoplay="props.autoplay"
      :interval="props.interval"
      :duration="props.duration"
      :style="[{ height: swiperHeight + 'rpx' }]"
      @change="swiperChange"
    >
      <swiper-item
        v-for="(arr, index) in menuList"
        :key="index"
        :class="{ cur: state.cur == index }"
      >
        <view class="grid-wrap">
          <view
            v-for="(item, itemIndex) in arr"
            :key="itemIndex"
            class="grid-item ss-flex ss-col-center ss-row-center"
            :class="{ 'menu-text-grid-bottom-align': props.verticalAlign === 'bottom' }"
            :style="[{ width: `${100 * (1 / columnCount)}%`, height: rowHeight + 'rpx' }]"
            hover-class="ss-hover-btn"
            @tap="onItemClick(item, index * rowCount * columnCount + itemIndex)"
          >
            <text
              class="menu-title line-clamp-2"
              :class="{ 'menu-text-active': index * rowCount * columnCount + itemIndex === props.activeIndex }"
              :style="{
                color:
                  index * rowCount * columnCount + itemIndex === props.activeIndex
                    ? ''
                    : item.titleColor || props.data.textColor,
                fontWeight:
                  index * rowCount * columnCount + itemIndex === props.activeIndex ? 'bold' : 'normal',
                fontSize: (props.data.fontSize || 13) * 2 + 'rpx',
              }"
            >
              {{ item.title }}
            </text>
          </view>
        </view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import sheep from '@/sheep';

  const state = reactive({
    cur: 0,
  });

  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    styles: {
      type: Object,
      default: () => ({}),
    },
    circular: {
      type: Boolean,
      default: true,
    },
    autoplay: {
      type: Boolean,
      default: false,
    },
    interval: {
      type: Number,
      default: 5000,
    },
    duration: {
      type: Number,
      default: 500,
    },
    ui: {
      type: String,
      default: '',
    },
    mode: {
      type: String,
      default: 'default',
    },
    verticalAlign: {
      type: String,
      default: 'center',
    },
    compact: {
      type: Boolean,
      default: false,
    },
    dotStyle: {
      type: String,
      default: 'long',
    },
    dotCur: {
      type: String,
      default: 'ui-BG-Main',
    },
    /**
     * 拦截点击跳转：
     * - false（默认）：点击菜单项后照常 sheep.$router.go(item.url)，行为与旧版一致
     * - true：点击只 emit('clickItem')，由父级全权分发（首页拼多多式交互使用）
     */
    interceptTap: {
      type: Boolean,
      default: false,
    },
    /**
     * 选中高亮索引（对应 data.list 的原始下标）：
     * - 命中项文字使用主题色 + 加粗
     * - -1（默认）表示不高亮任何项
     */
    activeIndex: {
      type: Number,
      default: -1,
    },
  });

  const emit = defineEmits(['clickItem']);

  const bgStyle = computed(() => {
    const { bgType, bgImg, bgColor } = props.styles;
    return {
      background: bgType === 'img' ? `url(${bgImg}) no-repeat top center / 100% 100%` : bgColor,
    };
  });

  const rowHeight = computed(() => {
    const fontSize = props.data.fontSize || 13;
    return props.compact ? fontSize * 2.8 + 8 : fontSize * 5.6;
  });

  const rowCount = computed(() => Number(props.data.row) || 1);
  const columnCount = computed(() => Number(props.data.column) || 1);

  const menuList = computed(() => splitData(props.data.list, rowCount.value * columnCount.value));

  const swiperHeight = computed(
    () => (props.data.scrollable === true ? 1 : rowCount.value) * rowHeight.value,
  );

  const swiperChange = (e) => {
    state.cur = e.detail.current;
  };

  const splitData = (oArr = [], length = 1) => {
    const arr = [];
    let minArr = [];
    oArr.forEach((c) => {
      if (minArr.length === length) {
        minArr = [];
      }
      if (minArr.length === 0) {
        arr.push(minArr);
      }
      minArr.push(c);
    });
    return arr;
  };

  /**
   * 菜单项点击统一入口。
   * @param item 菜单项数据（含 title/url 等装修配置字段）
   * @param index 该项在 data.list 中的原始索引（swiper 分页布局下用于定位高亮）
   */
  function onItemClick(item, index) {
    // 无论是否拦截，都先把点击事件抛给父级（携带原始索引供高亮联动）
    emit('clickItem', { item, index });
    // 拦截模式下不执行默认跳转，交给父级分发
    if (!props.interceptTap && item.url) {
      sheep.$router.go(item.url);
    }
  }
</script>

<style lang="scss" scoped>
  .grid-wrap {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    box-sizing: border-box;
  }

  .menu-text-grid-scroll {
    width: 100%;
    box-sizing: border-box;
    white-space: nowrap;
  }

  .menu-text-grid-scroll-row {
    display: inline-flex;
    flex-wrap: nowrap;
    align-items: center;
    min-width: 100%;
  }

  .menu-text-grid-scroll-item {
    flex: 0 0 auto;
    box-sizing: border-box;
    padding: 8rpx 12rpx;
  }

  .menu-text-grid-bottom-align {
    padding-bottom: 0;
    align-items: flex-end !important;
  }

  .grid-item {
    box-sizing: border-box;
    padding: 8rpx 4rpx;
  }

  .menu-title {
    width: 100%;
    text-align: center;
    line-height: 1.4;
    word-break: break-all;
  }

  .menu-text-grid-scroll-title {
    width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-break: normal;
  }

  /* 选中项高亮：主题色 + 加粗（与 category.vue 三级菜单选中态风格一致） */
  .menu-text-active {
    color: var(--ui-BG-Main) !important;
    font-weight: bold;
  }

  .sticky-menu-text-grid .menu-text-active {
    color: #e8c46a !important;
  }

  .line-clamp-2 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }
</style>
