<!-- 层级商品页：一级分类进入后展示二级菜单，二级分类进入后展示三级筛选 -->
<template>
  <s-layout navbar="normal" :title="pageTitle" :leftWidth="0" :rightWidth="0" showLeftButton>
    <view v-if="state.valid" class="category-product-page">
      <view v-if="menuList.length" class="category-menu">
        <view
          v-for="item in menuList"
          :key="item.id"
          class="category-menu-item"
          :class="[
            state.mode === 'primary'
              ? 'category-menu-item-primary'
              : 'category-menu-item-secondary',
            { 'category-menu-item-active': item.id === state.activeMenuId },
          ]"
          @tap="onMenuTap(item)"
        >
          <template v-if="state.mode === 'primary'">
            <image
              v-if="item.picUrl"
              class="category-menu-image"
              :src="sheep.$url.cdn(item.picUrl)"
              mode="aspectFill"
            />
            <view v-else class="category-menu-image category-menu-image-placeholder">
              {{ item.name }}
            </view>
            <view class="category-menu-title ss-line-1">{{ item.name }}</view>
          </template>
          <view v-else class="category-menu-text">{{ item.name }}</view>
        </view>
      </view>

      <s-goods-waterfall
        :data="waterfallData"
        :styles="waterfallStyles"
        :category-id="waterfallCategoryId"
      />
    </view>

    <uni-load-more v-else-if="!state.loaded" status="loading" />
    <s-empty v-else icon="/static/soldout-empty.png" text="暂无可用分类" />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import CategoryApi from '@/sheep/api/product/category';
  import { handleTree } from '@/sheep/helper/utils';

  const SALES_ROOT_ID = 1;

  const state = reactive({
    loaded: false,
    valid: false,
    mode: '',
    category: null,
    activeMenuId: 0,
  });

  const pageTitle = computed(() => state.category?.name || '商品');
  const menuList = computed(() => {
    const children = state.category?.children || [];
    if (state.mode === 'secondary') {
      return [{ id: state.category.id, name: '全部', isAll: true, picUrl: '' }, ...children];
    }
    return children;
  });
  const waterfallCategoryId = computed(() => {
    if (state.mode === 'secondary') {
      return state.activeMenuId || state.category?.id || 0;
    }
    return state.category?.id || 0;
  });

  // 商品页使用代码自有展示配置，不依赖装修 JSON 的商品组件配置。
  const waterfallData = {
    fields: {
      name: { show: true, color: '#000' },
      introduction: { show: true, color: '#999' },
      price: { show: true, color: '#ff3000' },
      marketPrice: { show: true, color: '#c4c4c4' },
      salesCount: { show: true, color: '#c4c4c4' },
      stock: { show: false, color: '#c4c4c4' },
    },
    badge: { show: false, imgUrl: '' },
    btnBuy: {
      type: 'text',
      text: '立即购买',
      bgBeginColor: '#FF6000',
      bgEndColor: '#FE832A',
      imgUrl: '',
    },
    borderRadiusTop: 6,
    borderRadiusBottom: 6,
    space: 8,
    pageSize: 10,
    rule: {
      sortField: '',
      sortAsc: false,
      keyword: '',
    },
  };
  const waterfallStyles = {
    marginLeft: 8,
    marginRight: 8,
  };

  function toPositiveId(value) {
    const id = Number(value);
    return Number.isSafeInteger(id) && id > 0 ? id : 0;
  }

  function findCategory(nodes, id) {
    for (const node of nodes || []) {
      if (Number(node.id) === id) {
        return node;
      }
      const found = findCategory(node.children, id);
      if (found) {
        return found;
      }
    }
    return null;
  }

  function initializeCategory(categoryId, categoryTree) {
    const category = findCategory(categoryTree, categoryId);
    if (!category) {
      state.valid = false;
      state.category = null;
      state.mode = '';
      state.activeMenuId = 0;
      return;
    }

    state.category = category;
    state.mode = Number(category.parentId) === SALES_ROOT_ID ? 'primary' : 'secondary';
    state.activeMenuId = state.mode === 'secondary' ? category.id : 0;
    state.valid = true;
  }

  async function loadCategoryPage(categoryId) {
    const id = toPositiveId(categoryId);
    if (!id) {
      state.loaded = true;
      return;
    }

    const { code, data } = await CategoryApi.getCategoryList();
    if (code === 0) {
      initializeCategory(id, handleTree(data));
    }
    state.loaded = true;
  }

  function onMenuTap(item) {
    if (state.mode === 'primary') {
      // 普通 navigateTo：二级商品页必须创建新的页面实例，保留一级页 A。
      sheep.$router.go('/pages/index/category', { id: item.id });
      return;
    }

    // 二级页的“全部”和三级分类均只在当前实例 B 内刷新商品。
    state.activeMenuId = item.id;
  }

  onLoad((options) => {
    loadCategoryPage(options?.id);
  });
</script>

<style lang="scss" scoped>
  .category-product-page {
    min-height: 100%;
    background: #fff;
  }

  .category-menu {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    padding: 20rpx;
    box-sizing: border-box;
    background: #fff;
    border-bottom: 1rpx solid #f2f2f2;
  }

  .category-menu-item {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #666;
    font-size: 28rpx;
  }

  .category-menu-item-primary {
    flex-direction: column;
    width: calc((100% - 80rpx) / 5);
    margin: 0 20rpx 20rpx 0;
  }

  .category-menu-item-primary:nth-child(5n) {
    margin-right: 0;
  }

  .category-menu-image {
    width: 100%;
    height: calc((100vw - 120rpx) / 5);
    border-radius: 8rpx;
    background: #f6f6f6;
  }

  .category-menu-image-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 30rpx;
  }

  .category-menu-title {
    width: 100%;
    margin-top: 8rpx;
    color: #333;
    font-size: 24rpx;
    line-height: 32rpx;
    text-align: center;
  }

  // 三级分类只展示文字，但沿用分类卡片的间距和点击区域，避免文字挤在一起。
  .category-menu-item-secondary {
    min-width: 120rpx;
    height: 64rpx;
    margin: 0 16rpx 16rpx 0;
    padding: 0 24rpx;
    box-sizing: border-box;
    border-radius: 10rpx;
    background: #f6f6f6;
    color: #666;
    font-size: 26rpx;
  }

  .category-menu-text {
    line-height: 64rpx;
    white-space: nowrap;
  }

  .category-menu-item-active {
    color: var(--ui-BG-Main);
    font-weight: 600;
  }

  .category-menu-item-active::after {
    content: '';
    position: absolute;
    left: 50%;
    bottom: -10rpx;
    width: 40rpx;
    height: 6rpx;
    border-radius: 6rpx;
    background: var(--ui-BG-Main);
    transform: translateX(-50%);
  }

  .category-menu-item-secondary.category-menu-item-active {
    background: transparent;
    color: var(--ui-BG-Main);
  }

  .category-menu-item-secondary.category-menu-item-active::after {
    bottom: 0;
    width: 40rpx;
    height: 4rpx;
  }
</style>
