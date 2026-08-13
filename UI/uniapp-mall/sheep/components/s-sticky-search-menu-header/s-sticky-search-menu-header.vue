<!-- 装修基础组件：吸顶搜索导航头部 -->
<template>
  <view class="sticky-search-menu-header" :style="headerStyle">
    <su-status-bar />
    <view class="navigation-safe-area" :style="navigationSafeStyle"></view>
    <view class="brand-spacer"></view>
    <view class="bottom-content">
      <s-block :styles="searchData.style">
        <s-search-block
          :data="searchData"
          :placeholder="searchData.placeholder"
          :placeholder-position="searchData.placeholderPosition"
          :radius="searchData.borderRadius"
          :el-background="searchData.backgroundColor"
          :font-color="searchData.textColor"
          :height="searchData.height"
          :show-scan="searchData.showScan"
          width="100%"
          @click="sheep.$router.go('/pages/index/search')"
        />
      </s-block>
      <view class="spacing-block" :style="searchMenuGapStyle"></view>
      <s-block :styles="menuData.style">
        <s-menu-text-grid
          :data="menuData"
          :styles="transparentStyle"
          ui="sticky-menu-text-grid"
          vertical-align="bottom"
          compact
          :intercept-tap="interceptMenuTap"
          :active-index="activeMenuIndex"
          @clickItem="onMenuClickItem"
        />
      </s-block>
      <view class="spacing-block" :style="menuBottomGapStyle"></view>
    </view>
  </view>
</template>

<script setup>
  import { computed } from 'vue';
  import sheep from '@/sheep';

  defineOptions({ name: 'StickySearchMenuHeader' });

  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    /**
     * 菜单点击拦截开关，透传给内部 s-menu-text-grid 的 interceptTap。
     * 首页拼多多式交互设为 true，由首页统一分发菜单点击。
     */
    interceptMenuTap: {
      type: Boolean,
      default: false,
    },
    /**
     * 菜单选中高亮索引，透传给内部 s-menu-text-grid 的 activeIndex。
     * -1 表示不高亮。
     */
    activeMenuIndex: {
      type: Number,
      default: -1,
    },
  });

  const defaultContainerStyle = Object.freeze({
    bgType: 'color',
    bgColor: 'transparent',
    bgImg: '',
    marginTop: 0,
    marginRight: 0,
    marginBottom: 0,
    marginLeft: 0,
    paddingTop: 0,
    paddingRight: 0,
    paddingBottom: 0,
    paddingLeft: 0,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
    borderBottomRightRadius: 0,
    borderBottomLeftRadius: 0,
  });

  const searchData = computed(() => {
    const search = props.data?.search || {};
    return {
      height: Number(search.height) || 28,
      showScan: search.showScan === true,
      borderRadius: Number(search.borderRadius) || 0,
      placeholder: search.placeholder || '搜索商品',
      placeholderPosition: search.placeholderPosition === 'center' ? 'center' : 'left',
      backgroundColor: search.backgroundColor || '#ffffff',
      textColor: search.textColor || '#969799',
      hotKeywords: Array.isArray(search.hotKeywords) ? search.hotKeywords : [],
      style: {
        ...defaultContainerStyle,
        paddingTop: 8,
        paddingRight: 12,
        paddingBottom: 8,
        paddingLeft: 12,
        ...(search.style || {}),
        paddingBottom: 0,
      },
    };
  });

  const menuData = computed(() => {
    const menu = props.data?.menu || {};
    return {
      ...menu,
      scrollable: true,
      row: 1,
      column: Math.max(Number(menu.column) || 5, 1),
      textColor: menu.textColor || '#ffffff',
      fontSize: Number(menu.fontSize) || 13,
      list: Array.isArray(menu.list) ? menu.list : [],
      style: {
        ...defaultContainerStyle,
        paddingTop: 6,
        paddingBottom: 8,
        ...(menu.style || {}),
        paddingTop: 0,
        paddingBottom: 0,
      },
    };
  });

  const background = computed(() => props.data?.background || {});
  const spacing = computed(() => {
    const value = props.data?.spacing || {};
    return {
      searchMenuGap: Math.min(Math.max(Number(value.searchMenuGap) || 0, 0), 40),
      menuBottomGap: Math.min(Math.max(Number(value.menuBottomGap) || 0, 0), 40),
    };
  });
  const headerStyle = computed(() => {
    const color = background.value.color || '#e60012';
    const image = background.value.image;
    return {
      height: `${Math.max(Number(background.value.height) || 360, 320)}rpx`,
      background: image
        ? `${color} url(${sheep.$url.cdn(image)}) no-repeat top center / cover`
        : color,
    };
  });

  const statusBarHeight = Number(sheep.$platform.device?.statusBarHeight) || 0;
  const navbarBottom = Number(sheep.$platform.navbar) || statusBarHeight + 44;
  const capsuleBottom = Number(sheep.$platform.capsule?.bottom) || navbarBottom;
  const navigationSafeHeight = Math.max(navbarBottom, capsuleBottom) - statusBarHeight;
  const navigationSafeStyle = computed(() => ({
    height: `${Math.max(navigationSafeHeight, 44)}px`,
  }));
  const searchMenuGapStyle = computed(() => ({ height: `${spacing.value.searchMenuGap}rpx` }));
  const menuBottomGapStyle = computed(() => ({ height: `${spacing.value.menuBottomGap}rpx` }));

  const transparentStyle = Object.freeze({ bgType: 'color', bgColor: 'transparent' });

  const emit = defineEmits(['menuClickItem']);

  /** 菜单项点击：原样透传 { item, index } 给页面级调用方分发 */
  function onMenuClickItem(payload) {
    emit('menuClickItem', payload);
  }
</script>

<style lang="scss" scoped>
  .sticky-search-menu-header {
    display: flex;
    width: 100%;
    overflow: hidden;
    box-sizing: border-box;
    flex-direction: column;
  }

  .navigation-safe-area {
    width: 100%;
    flex: 0 0 auto;
  }

  .brand-spacer {
    width: 100%;
    min-height: 0;
    flex: 1 1 auto;
  }

  .bottom-content,
  .spacing-block {
    width: 100%;
    flex: 0 0 auto;
  }
</style>
