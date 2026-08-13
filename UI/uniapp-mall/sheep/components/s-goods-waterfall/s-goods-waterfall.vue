<!-- 装修商品组件：商品瀑布流（自动获取全部商品，双列先左后右） -->
<template>
  <view class="goods-waterfall">
    <view v-if="state.goodsList.length" class="goods-md-wrap ss-flex ss-flex-wrap ss-col-top">
      <view class="goods-list-box">
        <view
          class="left-list"
          :style="[{ paddingRight: data.space + 'rpx', marginBottom: data.space + 'px' }]"
          v-for="item in state.leftGoodsList"
          :key="item.id"
        >
          <s-goods-column
            class="goods-md-box"
            size="md"
            :goodsFields="data.fields"
            :tagStyle="data.badge"
            :data="item"
            :titleColor="data.fields.name?.color"
            :subTitleColor="data.fields.introduction?.color"
            :topRadius="data.borderRadiusTop"
            :bottomRadius="data.borderRadiusBottom"
            :titleWidth="330 - marginLeft - marginRight"
            @click="sheep.$router.go('/pages/goods/index', { id: item.id })"
          >
            <template v-slot:cart>
              <button class="ss-reset-button cart-btn" :style="[buyStyle]">
                {{ btnBuy.type === 'text' ? btnBuy.text : '' }}
              </button>
            </template>
          </s-goods-column>
        </view>
      </view>
      <view class="goods-list-box">
        <view
          class="right-list"
          :style="[{ paddingLeft: data.space + 'rpx', marginBottom: data.space + 'px' }]"
          v-for="item in state.rightGoodsList"
          :key="item.id"
        >
          <s-goods-column
            class="goods-md-box"
            size="md"
            :goodsFields="data.fields"
            :tagStyle="data.badge"
            :data="item"
            :titleColor="data.fields.name?.color"
            :subTitleColor="data.fields.introduction?.color"
            :topRadius="data.borderRadiusTop"
            :bottomRadius="data.borderRadiusBottom"
            :titleWidth="330 - marginLeft - marginRight"
            @click="sheep.$router.go('/pages/goods/index', { id: item.id })"
          >
            <template v-slot:cart>
              <button class="ss-reset-button cart-btn" :style="[buyStyle]">
                {{ btnBuy.type === 'text' ? btnBuy.text : '' }}
              </button>
            </template>
          </s-goods-column>
        </view>
      </view>
    </view>

    <view class="load-more-sentinel" />
    <uni-load-more
      v-if="state.goodsList.length || state.loadStatus === 'loading'"
      :status="state.loadStatus"
      :content-text="{ contentdown: '上拉加载更多' }"
    />
    <s-empty
      v-if="!state.goodsList.length && state.loadStatus !== 'loading'"
      icon="/static/soldout-empty.png"
      text="暂无商品"
    />
  </view>
</template>

<script setup>
  import {
    computed,
    getCurrentInstance,
    nextTick,
    onMounted,
    onUnmounted,
    reactive,
    watch,
  } from 'vue';
  import sheep from '@/sheep';
  import SpuApi from '@/sheep/api/product/spu';
  import OrderApi from '@/sheep/api/sales/order';
  import { appendSettlementProduct } from '@/sheep/hooks/useGoods';
  import { concat } from 'lodash-es';

  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    styles: {
      type: Object,
      default: () => ({}),
    },
    /** 页面调用方可直接控制分类；未传时继续读取装修规则。 */
    categoryId: {
      type: [Number, String],
      default: undefined,
    },
  });

  const { btnBuy } = props.data || {};
  const { marginLeft, marginRight } = props.styles || {};

  const state = reactive({
    goodsList: [],
    leftGoodsList: [],
    rightGoodsList: [],
    pageNo: 1,
    total: 0,
    loadStatus: 'more',
  });

  let observer = null;
  let requestGeneration = 0;

  const selectedCategoryId = computed(() => {
    if (props.categoryId !== undefined && props.categoryId !== null) {
      return Number(props.categoryId) || 0;
    }
    return Number(props.data?.rule?.categorySales) || 0;
  });

  const buyStyle = computed(() => {
    if (btnBuy?.type === 'text') {
      return {
        background: `linear-gradient(to right, ${btnBuy.bgBeginColor}, ${btnBuy.bgEndColor})`,
      };
    }
    if (btnBuy?.type === 'img') {
      return {
        width: '54rpx',
        height: '54rpx',
        background: `url(${sheep.$url.cdn(btnBuy.imgUrl)}) no-repeat`,
        backgroundSize: '100% 100%',
      };
    }
    return {};
  });

  /** 先左后右：偶数下标放左列，奇数下标放右列 */
  function splitWaterfall() {
    state.leftGoodsList = state.goodsList.filter((_, index) => index % 2 === 0);
    state.rightGoodsList = state.goodsList.filter((_, index) => index % 2 === 1);
  }

  async function loadGoodsList(reset = false) {
    if (state.loadStatus === 'loading' || state.loadStatus === 'noMore') {
      if (!reset) {
        return;
      }
    }

    const currentGeneration = ++requestGeneration;
    if (reset) {
      state.pageNo = 1;
      state.goodsList = [];
      state.leftGoodsList = [];
      state.rightGoodsList = [];
      state.total = 0;
      state.loadStatus = 'more';
    }

    state.loadStatus = 'loading';
    const rule = props.data.rule || {};
    const pageSize = props.data.pageSize || 10;
    const { code, data } = await SpuApi.getSpuPage({
      pageNo: state.pageNo,
      pageSize,
      categorySales: selectedCategoryId.value || undefined,
      keyword: rule.keyword || undefined,
      sortField: rule.sortField || undefined,
      sortAsc: rule.sortAsc,
    });
    if (currentGeneration !== requestGeneration) {
      return;
    }
    if (code !== 0) {
      state.loadStatus = 'more';
      return;
    }

    const list = data?.list || [];
    if (list.length) {
      await OrderApi.getSettlementProduct(list.map((item) => item.id).join(',')).then((res) => {
        if (currentGeneration === requestGeneration && res.code === 0) {
          appendSettlementProduct(list, res.data);
        }
      });
    }
    if (currentGeneration !== requestGeneration) {
      return;
    }

    // 首次加载或分类切换时列表为空，分页加载时把新商品追加到后面
    state.goodsList = concat(state.goodsList, list);
    state.total = data?.total || 0;
    state.loadStatus = state.goodsList.length < state.total ? 'more' : 'noMore';
    splitWaterfall();
  }

  function loadMore() {
    if (state.loadStatus !== 'more') {
      return;
    }
    state.pageNo++;
    loadGoodsList(false);
  }

  function initObserver() {
    const instance = getCurrentInstance();
    observer = uni.createIntersectionObserver(instance?.proxy, { observeAll: false });
    observer.relativeToViewport({ bottom: 80 }).observe('.load-more-sentinel', (res) => {
      if (res.intersectionRatio > 0) {
        loadMore();
      }
    });
  }

  onMounted(async () => {
    await loadGoodsList(true);
    await nextTick();
    initObserver();
  });

  onUnmounted(() => {
    requestGeneration++;
    observer?.disconnect();
  });

  watch(selectedCategoryId, () => {
    loadGoodsList(true);
  });
</script>

<style lang="scss" scoped>
  .goods-md-wrap {
    width: 100%;
  }

  .goods-list-box {
    width: 50%;
    box-sizing: border-box;

    .left-list,
    .right-list {
      &:nth-last-child(1) {
        margin-bottom: 0 !important;
      }
    }
  }

  .goods-md-box {
    position: relative;

    .cart-btn {
      position: absolute;
      bottom: 18rpx;
      right: 20rpx;
      z-index: 11;
      height: 50rpx;
      line-height: 50rpx;
      padding: 0 20rpx;
      border-radius: 25rpx;
      font-size: 24rpx;
      color: #fff;
    }
  }

  .load-more-sentinel {
    height: 1px;
  }
</style>
