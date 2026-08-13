<template>
  <div v-if="property.scrollable === true" class="menu-text-grid-scroll">
    <div class="menu-text-grid-scroll-row">
      <div
        v-for="(item, index) in property.list"
        :key="index"
        class="menu-text-grid-scroll-item flex items-center justify-center text-center"
        :class="{ 'menu-text-grid-bottom-align': verticalAlign === 'bottom' }"
        :style="{ width: columnWidth, height: `${rowHeight}px` }"
      >
        <span
          class="menu-text-grid-scroll-title"
          :style="{
            color: item.titleColor || property.textColor,
            fontSize: `${property.fontSize}px`,
            lineHeight: '1.4'
          }"
        >
          {{ item.title }}
        </span>
      </div>
    </div>
  </div>
  <el-carousel
    v-else
    :height="`${carouselHeight}px`"
    :autoplay="false"
    arrow="never"
    :indicator-position="pages.length > 1 ? 'outside' : ''"
    class="menu-text-grid-carousel"
  >
    <el-carousel-item v-for="(page, pageIndex) in pages" :key="pageIndex">
      <div class="flex flex-row flex-wrap">
        <div
          v-for="(item, index) in page"
          :key="index"
          class="flex items-center justify-center text-center"
          :class="{ 'menu-text-grid-bottom-align': verticalAlign === 'bottom' }"
          :style="{ width: columnWidth, height: `${rowHeight}px` }"
        >
          <span
            class="line-clamp-2 px-4px"
            :style="{
              color: item.titleColor || property.textColor,
              fontSize: `${property.fontSize}px`,
              lineHeight: '1.4'
            }"
          >
            {{ item.title }}
          </span>
        </div>
      </div>
    </el-carousel-item>
  </el-carousel>
</template>

<script setup lang="ts">
import { MenuTextGridItemProperty, MenuTextGridProperty } from './config'

/** 文字分类导航区 */
defineOptions({ name: 'MenuTextGrid' })

const props = withDefaults(
  defineProps<{
    property: MenuTextGridProperty
    verticalAlign?: 'center' | 'bottom'
    compact?: boolean
  }>(),
  { verticalAlign: 'center', compact: false }
)

const ROW_PADDING = 16
const COMPACT_ROW_PADDING = 4

const pages = ref<MenuTextGridItemProperty[][]>([])
const carouselHeight = ref(0)
const rowHeight = ref(0)
const columnWidth = ref('')

watch(
  () => props.property,
  () => {
    columnWidth.value = `${100 * (1 / props.property.column)}%`
    rowHeight.value = props.compact
      ? props.property.fontSize * 1.4 + COMPACT_ROW_PADDING
      : props.property.fontSize * 2.8 + ROW_PADDING
    carouselHeight.value = props.property.row * rowHeight.value

    const pageSize = props.property.row * props.property.column
    pages.value = []
    let pageItems: MenuTextGridItemProperty[] = []
    for (const item of props.property.list) {
      if (pageItems.length === pageSize) {
        pageItems = []
      }
      if (pageItems.length === 0) {
        pages.value.push(pageItems)
      }
      pageItems.push(item)
    }
  },
  { immediate: true, deep: true }
)
</script>

<style lang="scss">
.menu-text-grid-carousel {
  :deep(.el-carousel__arrow) {
    display: none;
  }
  :deep(.el-carousel__indicators) {
    display: none;
  }
}

.menu-text-grid-scroll {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar {
    display: none;
    width: 0;
    height: 0;
  }
}

.menu-text-grid-scroll-row {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  width: max-content;
  min-width: 100%;
}

.menu-text-grid-scroll-item {
  flex: 0 0 auto;
  box-sizing: border-box;
  padding: 0 12px;
}

.menu-text-grid-bottom-align {
  padding-bottom: 0;
  align-items: flex-end !important;
}

.menu-text-grid-scroll-title {
  display: block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  word-break: normal;
}
</style>
