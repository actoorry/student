<template>
  <s-layout
    navbar="normal"
    :title="pageTitle"
    :leftWidth="0"
    :rightWidth="0"
    tools="search"
    :defaultSearch="state.keyword"
    @search="onSearch"
  >
    <!-- 筛选 -->
    <su-sticky bgColor="#fff">
      <view class="ss-flex">
        <view class="ss-flex-1">
          <su-tabs
            :list="state.tabList"
            :scrollable="false"
            @change="onTabsChange"
            :current="state.currentTab"
          />
        </view>
        <view class="list-icon" @tap="state.iconStatus = !state.iconStatus">
          <text v-if="state.iconStatus" class="sicon-goods-list" />
          <text v-else class="sicon-goods-card" />
        </view>
      </view>
    </su-sticky>

    <!-- 弹窗 -->
    <su-popup
      :show="state.showFilter"
      type="top"
      round="10"
      :space="sys_navBar + 38"
      backgroundColor="#F6F6F6"
      :zIndex="10"
      @close="state.showFilter = false"
    >
      <view class="filter-list-box">
        <view
          class="filter-item"
          v-for="(item, index) in state.tabList[state.currentTab].list"
          :key="item.value"
          :class="[{ 'filter-item-active': index === state.curFilter }]"
          @tap="onFilterItem(index)"
        >
          {{ item.label }}
        </view>
      </view>
    </su-popup>

    <!-- 情况一：单列布局 -->
    <view v-if="state.iconStatus && state.pagination.total > 0" class="goods-list ss-m-t-20">
      <view
        class="ss-p-l-20 ss-p-r-20 ss-m-b-20"
        v-for="item in state.pagination.list"
        :key="item.id"
      >
        <s-goods-column
          class=""
          size="lg"
          :data="item"
          :topRadius="10"
          :bottomRadius="10"
          @click="sheep.$router.go('/pages/goods/index', { id: item.id })"
        />
      </view>
    </view>
    <!-- 情况二：双列布局 -->
    <view
      v-if="!state.iconStatus && state.pagination.total > 0"
      class="ss-flex ss-flex-wrap ss-p-x-20 ss-m-t-20 ss-col-top"
    >
      <view class="goods-list-box">
        <view class="left-list" v-for="item in state.leftGoodsList" :key="item.id">
          <s-goods-column
            class="goods-md-box"
            size="md"
            :data="item"
            :topRadius="10"
            :bottomRadius="10"
            @click="sheep.$router.go('/pages/goods/index', { id: item.id })"
            @getHeight="mountMasonry($event, 'left')"
          >
            <template v-slot:cart>
              <button class="ss-reset-button cart-btn" />
            </template>
          </s-goods-column>
        </view>
      </view>
      <view class="goods-list-box">
        <view class="right-list" v-for="item in state.rightGoodsList" :key="item.id">
          <s-goods-column
            class="goods-md-box"
            size="md"
            :topRadius="10"
            :bottomRadius="10"
            :data="item"
            @click="sheep.$router.go('/pages/goods/index', { id: item.id })"
            @getHeight="mountMasonry($event, 'right')"
          >
            <template v-slot:cart>
              <button class="ss-reset-button cart-btn" />
            </template>
          </s-goods-column>
        </view>
      </view>
    </view>
    <uni-load-more
      v-if="state.pagination.total > 0"
      :status="state.loadStatus"
      :content-text="{
        contentdown: '上拉加载更多',
      }"
      @tap="loadMore"
    />
    <s-empty v-if="state.pagination.total === 0" icon="/static/soldout-empty.png" text="暂无商品" />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import sheep from '@/sheep';
  import { concat } from 'lodash-es';
  import { resetPagination } from '@/sheep/helper/utils';
  import SpuApi from '@/sheep/api/product/spu';
  import OrderApi from '@/sheep/api/sales/order';
  import { appendSettlementProduct } from '@/sheep/hooks/useGoods';

  const props = defineProps({
    /** default: 通用商品列表；sales: 热卖榜单；new: 新品优先 */
    listMode: {
      type: String,
      default: 'default',
    },
  });

  const pageTitle = computed(() => {
    if (props.listMode === 'sales') {
      return '热卖榜单';
    }
    if (props.listMode === 'new') {
      return '新品优先';
    }
    return '商品列表';
  });

  const sys_navBar = sheep.$platform.navbar;

  const state = reactive({
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 6,
    },
    currentSort: undefined,
    currentOrder: undefined,
    currentTab: 0,
    curFilter: 0,
    showFilter: false,
    iconStatus: false,
    keyword: '',
    categoryId: 0,
    tabList: [
      {
        name: '综合推荐',
        list: [
          {
            label: '综合推荐',
          },
          {
            label: '价格升序',
            sort: 'price',
            order: true,
          },
          {
            label: '价格降序',
            sort: 'price',
            order: false,
          },
        ],
      },
      {
        name: '销量',
        sort: 'salesCount',
        order: false,
      },
      {
        name: '新品优先',
        sort: 'createTime',
        order: false,
      },
    ],
    loadStatus: '',
    leftGoodsList: [],
    rightGoodsList: [],
  });

  let count = 0;
  let leftHeight = 0;
  let rightHeight = 0;

  function mountMasonry(height = 0, where = 'left') {
    if (where === 'left') {
      leftHeight += height;
    } else {
      rightHeight += height;
    }
    if (!state.pagination.list[count]) {
      return;
    }

    if (leftHeight <= rightHeight) {
      state.leftGoodsList.push(state.pagination.list[count]);
    } else {
      state.rightGoodsList.push(state.pagination.list[count]);
    }
    count++;
  }

  function emptyList() {
    resetPagination(state.pagination);
    state.leftGoodsList = [];
    state.rightGoodsList = [];
    count = 0;
    leftHeight = 0;
    rightHeight = 0;
  }

  function applyListMode(options = {}) {
    if (props.listMode === 'sales') {
      state.currentTab = 1;
      state.currentSort = 'salesCount';
      state.currentOrder = false;
      return;
    }
    if (props.listMode === 'new') {
      state.currentTab = 2;
      state.currentSort = 'createTime';
      state.currentOrder = false;
      return;
    }
    state.currentSort = options.sortField;
    if (options.sortAsc !== undefined) {
      state.currentOrder = options.sortAsc === 'true';
    }
    if (options.sortField === 'salesCount') {
      state.currentTab = 1;
    } else if (options.sortField === 'createTime') {
      state.currentTab = 2;
    }
  }

  function onSearch(e) {
    state.keyword = e;
    emptyList();
    getList();
  }

  function onTabsChange(e) {
    if (state.tabList[e.index].list) {
      state.currentTab = e.index;
      state.showFilter = !state.showFilter;
      return;
    }
    state.showFilter = false;

    if (e.index === state.currentTab) {
      return;
    }

    state.currentTab = e.index;
    state.currentSort = e.sort;
    state.currentOrder = e.order;
    emptyList();
    getList();
  }

  const onFilterItem = (val) => {
    if (
      state.currentSort === state.tabList[0].list[val].sort &&
      state.currentOrder === state.tabList[0].list[val].order
    ) {
      state.showFilter = false;
      return;
    }
    state.showFilter = false;

    state.curFilter = val;
    state.tabList[0].name = state.tabList[0].list[val].label;
    state.currentSort = state.tabList[0].list[val].sort;
    state.currentOrder = state.tabList[0].list[val].order;
    emptyList();
    getList();
  };

  async function getList() {
    state.loadStatus = 'loading';
    const { code, data } = await SpuApi.getSpuPage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
      sortField: state.currentSort,
      sortAsc: state.currentOrder,
      categorySales: state.categoryId,
      keyword: state.keyword,
    });
    if (code !== 0) {
      return;
    }
    await OrderApi.getSettlementProduct(data.list.map((item) => item.id).join(',')).then((res) => {
      if (res.code !== 0) {
        return;
      }
      appendSettlementProduct(data.list, res.data);
    });
    state.pagination.list = concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
    mountMasonry();
  }

  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getList();
  }

  function init(options = {}) {
    state.categoryId = options.categoryId || options.categorySales;
    state.keyword = options.keyword || '';
    applyListMode(options);
    getList();
  }

  defineExpose({
    init,
    loadMore,
  });
</script>

<style lang="scss" scoped>
  .goods-list-box {
    width: 50%;
    box-sizing: border-box;

    .left-list {
      margin-right: 10rpx;
      margin-bottom: 20rpx;
    }

    .right-list {
      margin-left: 10rpx;
      margin-bottom: 20rpx;
    }
  }

  .list-icon {
    width: 80rpx;

    .sicon-goods-card {
      font-size: 40rpx;
    }

    .sicon-goods-list {
      font-size: 40rpx;
    }
  }

  .filter-list-box {
    padding: 28rpx 52rpx;

    .filter-item {
      font-size: 28rpx;
      font-weight: 500;
      color: #333333;
      line-height: normal;
      margin-bottom: 24rpx;

      &:nth-last-child(1) {
        margin-bottom: 0;
      }
    }

    .filter-item-active {
      color: var(--ui-BG-Main);
    }
  }
</style>
