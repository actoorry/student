<!-- 装修基础组件：文字分类导航区 -->
<template>
  <view
    class="ui-swiper menu-text-grid"
    :class="[props.mode, props.ui]"
    :style="[bgStyle, { height: swiperHeight + 'rpx' }]"
  >
    <swiper
      :circular="props.circular"
      :current="state.cur"
      :autoplay="props.autoplay"
      :interval="props.interval"
      :duration="props.duration"
      :style="[{ height: swiperHeight + 'rpx' }]"
      @change="swiperChange"
    >
      <swiper-item v-for="(arr, index) in menuList" :key="index" :class="{ cur: state.cur == index }">
        <view class="grid-wrap">
          <view
            v-for="(item, itemIndex) in arr"
            :key="itemIndex"
            class="grid-item ss-flex ss-col-center ss-row-center"
            :style="[{ width: `${100 * (1 / data.column)}%`, height: rowHeight + 'rpx' }]"
            hover-class="ss-hover-btn"
            @tap="sheep.$router.go(item.url)"
          >
            <text
              class="menu-title line-clamp-2"
              :style="{
                color: item.titleColor || data.textColor,
                fontSize: (data.fontSize || 13) * 2 + 'rpx'
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
  import { computed, reactive } from 'vue'
  import sheep from '@/sheep'

  const state = reactive({
    cur: 0
  })

  const props = defineProps({
    data: {
      type: Object,
      default: () => ({})
    },
    styles: {
      type: Object,
      default: () => ({})
    },
    circular: {
      type: Boolean,
      default: true
    },
    autoplay: {
      type: Boolean,
      default: false
    },
    interval: {
      type: Number,
      default: 5000
    },
    duration: {
      type: Number,
      default: 500
    },
    ui: {
      type: String,
      default: ''
    },
    mode: {
      type: String,
      default: 'default'
    },
    dotStyle: {
      type: String,
      default: 'long'
    },
    dotCur: {
      type: String,
      default: 'ui-BG-Main'
    }
  })

  const bgStyle = computed(() => {
    const { bgType, bgImg, bgColor } = props.styles
    return {
      background: bgType === 'img' ? `url(${bgImg}) no-repeat top center / 100% 100%` : bgColor
    }
  })

  const rowHeight = computed(() => (props.data.fontSize || 13) * 5.6)

  const menuList = computed(() => splitData(props.data.list, props.data.row * props.data.column))

  const swiperHeight = computed(() => props.data.row * rowHeight.value)

  const swiperChange = (e) => {
    state.cur = e.detail.current
  }

  const splitData = (oArr = [], length = 1) => {
    const arr = []
    let minArr = []
    oArr.forEach((c) => {
      if (minArr.length === length) {
        minArr = []
      }
      if (minArr.length === 0) {
        arr.push(minArr)
      }
      minArr.push(c)
    })
    return arr
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

  .line-clamp-2 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }
</style>
