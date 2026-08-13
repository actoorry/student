<template>
  <div class="sticky-search-menu-header" :style="headerStyle">
    <div class="navigation-safe-area">
      <img src="@/assets/imgs/diy/app-nav-bar-mp.png" alt="小程序胶囊" class="capsule" />
    </div>
    <div class="brand-spacer"></div>
    <div class="bottom-content">
      <div :style="searchContainerStyle">
        <SearchBar :property="searchProperty" />
      </div>
      <div class="spacing-block" :style="searchMenuGapStyle"></div>
      <div :style="menuContainerStyle">
        <MenuTextGrid :property="menuProperty" vertical-align="bottom" compact />
      </div>
      <div class="spacing-block" :style="menuBottomGapStyle"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import SearchBar from '@/components/DiyEditor/components/mobile/SearchBar/index.vue'
import MenuTextGrid from '@/components/DiyEditor/components/mobile/MenuTextGrid/index.vue'
import { component as SEARCH_BAR_COMPONENT } from '@/components/DiyEditor/components/mobile/SearchBar/config'
import { component as MENU_TEXT_GRID_COMPONENT } from '@/components/DiyEditor/components/mobile/MenuTextGrid/config'
import { StickySearchMenuHeaderProperty } from './config'

defineOptions({ name: 'StickySearchMenuHeader' })

const props = defineProps<{ property: StickySearchMenuHeaderProperty }>()

const background = computed(() => ({
  image: props.property.background?.image || '',
  color: props.property.background?.color || '#e60012',
  height: Number(props.property.background?.height) || 360
}))

const spacing = computed(() => ({
  searchMenuGap: Math.min(Math.max(Number(props.property.spacing?.searchMenuGap) || 0, 0), 40),
  menuBottomGap: Math.min(Math.max(Number(props.property.spacing?.menuBottomGap) || 0, 0), 40)
}))

const searchProperty = computed(() => ({
  ...SEARCH_BAR_COMPONENT.property,
  ...(props.property.search || {}),
  style: {
    ...SEARCH_BAR_COMPONENT.property.style,
    ...(props.property.search?.style || {})
  }
}))

const menuProperty = computed(() => ({
  ...MENU_TEXT_GRID_COMPONENT.property,
  ...(props.property.menu || {}),
  scrollable: true,
  row: 1,
  style: {
    ...MENU_TEXT_GRID_COMPONENT.property.style,
    ...(props.property.menu?.style || {})
  }
}))

const headerStyle = computed(() => ({
  height: `${Math.max(background.value.height, 320) / 2}px`,
  background: background.value.image
    ? `${background.value.color} url(${background.value.image}) no-repeat top center / cover`
    : background.value.color
}))

const toContainerStyle = (style: any) => ({
  paddingTop: `${style?.paddingTop || 0}px`,
  paddingRight: `${style?.paddingRight || 0}px`,
  paddingBottom: `${style?.paddingBottom || 0}px`,
  paddingLeft: `${style?.paddingLeft || 0}px`,
  background:
    style?.bgType === 'img' && style?.bgImg
      ? `url(${style.bgImg}) no-repeat top center / cover`
      : style?.bgColor || 'transparent'
})

const searchContainerStyle = computed(() => ({
  ...toContainerStyle(searchProperty.value.style),
  paddingBottom: '0px'
}))
const menuContainerStyle = computed(() => ({
  ...toContainerStyle(menuProperty.value.style),
  paddingTop: '0px',
  paddingBottom: '0px'
}))
const searchMenuGapStyle = computed(() => ({ height: `${spacing.value.searchMenuGap / 2}px` }))
const menuBottomGapStyle = computed(() => ({ height: `${spacing.value.menuBottomGap / 2}px` }))
</script>

<style scoped lang="scss">
.sticky-search-menu-header {
  display: flex;
  width: 100%;
  overflow: hidden;
  box-sizing: border-box;
  flex-direction: column;
}

.navigation-safe-area {
  position: relative;
  height: 44px;
  flex: 0 0 44px;
}

.capsule {
  position: absolute;
  top: 7px;
  right: 6px;
  width: 86px;
  height: 30px;
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
