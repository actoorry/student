<!-- 装修商品组件：商品双列（单行两个） -->
<template>
  <view class="product-row">
    <!-- 标题 + 展开更多 -->
    <view class="header ss-flex ss-row-between ss-col-center">
      <view class="header-left">
        <view v-if="data.title" class="title" :style="titleStyles">{{ data.title }}</view>
        <view v-if="data.description" class="description" :style="descStyles">{{ data.description }}</view>
      </view>
      <view
        v-if="data.more?.show"
        class="more-box ss-flex ss-col-center"
        @tap="onMoreTap"
        :style="{ color: data.descriptionColor }"
      >
        <view class="more-text" v-if="data.more.type !== 'icon'">{{ data.more.text }}</view>
        <text class="_icon-forward" v-if="data.more.type !== 'text'"></text>
      </view>
    </view>

    <!-- 双列商品 -->
    <view v-if="state.goodsList.length" class="goods-wrap ss-flex ss-flex-wrap ss-col-top">
      <view
        class="goods-item"
        v-for="(item, index) in state.goodsList"
        :key="item.id"
        :style="[
          {
            width: '50%',
            paddingRight: index % 2 === 0 ? data.space + 'rpx' : '0',
            paddingLeft: index % 2 === 1 ? data.space + 'rpx' : '0',
            marginBottom: data.space + 'rpx',
            boxSizing: 'border-box',
          },
        ]"
      >
        <s-goods-column
          class="goods-card"
          size="md"
          :goodsFields="data.fields"
          :tagStyle="data.badge"
          :data="item"
          :titleColor="data.fields.name?.color"
          :topRadius="data.borderRadiusTop"
          :bottomRadius="data.borderRadiusBottom"
          @click="sheep.$router.go('/pages/goods/index', { id: item.id })"
        />
      </view>
    </view>
  </view>
</template>

<script setup>
  import { computed, onMounted, reactive } from 'vue';
  import sheep from '@/sheep';
  import SpuApi from '@/sheep/api/product/spu';
  import OrderApi from '@/sheep/api/sales/order';
  import { appendSettlementProduct } from '@/sheep/hooks/useGoods';

  const DISPLAY_COUNT = 2;

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
    goodsList: [],
  });

  const titleStyles = computed(() => ({
    color: props.data.titleColor,
    fontSize: `${props.data.titleSize || 16}px`,
  }));

  const descStyles = computed(() => ({
    color: props.data.descriptionColor,
    fontSize: `${props.data.descriptionSize || 12}px`,
  }));

  function onMoreTap() {
    if (props.data.more?.url) {
      sheep.$router.go(props.data.more.url);
      return;
    }
    const { dataSource = 'rule', rule = {} } = props.data;
    if (dataSource !== 'rule') {
      return;
    }
    const params = {};
    if (rule.categorySales) {
      params.categorySales = rule.categorySales;
    }
    if (rule.sortField === 'salesCount') {
      sheep.$router.go('/pages/goods/sales-rank', params);
      return;
    }
    if (rule.sortField === 'createTime') {
      sheep.$router.go('/pages/goods/new-rank', params);
    }
  }

  async function loadGoodsList() {
    const { dataSource = 'rule', spuIds = [], rule = {} } = props.data;

    if (dataSource === 'manual') {
      const ids = spuIds.slice(0, DISPLAY_COUNT);
      if (!ids.length) {
        state.goodsList = [];
        return;
      }
      const { data } = await SpuApi.getSpuListByIds(ids.join(','));
      state.goodsList = (data || []).slice(0, DISPLAY_COUNT);
    } else {
      const { code, data: pageData } = await SpuApi.getSpuPage({
        pageNo: 1,
        pageSize: DISPLAY_COUNT,
        categorySales: rule.categorySales || undefined,
        keyword: rule.keyword || undefined,
        sortField: rule.sortField || undefined,
        sortAsc: rule.sortAsc,
      });
      if (code !== 0) {
        state.goodsList = [];
        return;
      }
      state.goodsList = (pageData?.list || []).slice(0, DISPLAY_COUNT);
    }

    if (!state.goodsList.length) {
      return;
    }
    await OrderApi.getSettlementProduct(state.goodsList.map((item) => item.id).join(',')).then((res) => {
      if (res.code !== 0) {
        return;
      }
      appendSettlementProduct(state.goodsList, res.data);
    });
  }

  onMounted(loadGoodsList);
</script>

<style lang="scss" scoped>
  .product-row {
    width: 100%;
  }

  .header {
    padding: 16rpx 20rpx 20rpx;
    box-sizing: border-box;

    .header-left {
      flex: 1;
      min-width: 0;
    }

    .title {
      font-weight: 600;
      line-height: 1.4;
    }

    .description {
      margin-top: 4rpx;
      line-height: 1.4;
    }

    .more-box {
      flex-shrink: 0;
      margin-left: 16rpx;
      font-size: 24rpx;
      white-space: nowrap;
    }
  }

  .goods-wrap {
    padding: 0 20rpx 4rpx;
    box-sizing: border-box;
  }
</style>
