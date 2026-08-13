<template>
  <div class="relative" :style="{ height: validHeight + 'px' }">
    <el-empty v-if="!validSlides.length" description="暂无场景" class="h-full" />
    <el-carousel
      v-else
      :height="validHeight + 'px'"
      :autoplay="false"
      :initial-index="validInitialIndex"
      indicator-position="outside"
      arrow="never"
      @change="handleIndexChange"
    >
      <el-carousel-item v-for="(slide, index) in validSlides" :key="index">
        <div
          class="h-full w-full flex flex-col items-center justify-center relative overflow-hidden"
          :style="[getSlideBackground(slide)]"
        >
          <!-- 背景图 -->
          <el-image
            v-if="slide.backgroundImage"
            :src="slide.backgroundImage"
            class="absolute inset-0 h-full w-full object-cover"
          />
          <!-- 前景图 -->
          <el-image
            v-if="slide.foregroundImage"
            :src="slide.foregroundImage"
            class="relative z-1 h-160px w-160px object-contain"
          />
          <!-- 标题 -->
          <div
            v-if="slide.title"
            class="relative z-1 text-24px font-bold m-t-16px text-white text-shadow"
          >
            {{ slide.title }}
          </div>
          <!-- 副标题 -->
          <div v-if="slide.subtitle" class="relative z-1 text-14px m-t-8px text-white text-shadow">
            {{ slide.subtitle }}
          </div>
          <!-- 标签 -->
          <div v-if="slide.tags?.length" class="relative z-1 flex flex-wrap gap-8px m-t-16px">
            <span
              v-for="(tag, tagIndex) in slide.tags.filter(Boolean)"
              :key="tagIndex"
              class="rounded-full px-12px py-4px text-12px bg-white/20 text-white backdrop-blur"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
    <!-- 数字指示器 -->
    <div
      v-if="property.indicator === 'number' && validSlides.length > 1"
      class="absolute bottom-10px right-10px rounded-xl bg-black/40 px-8px py-2px text-10px text-white"
    >
      {{ currentIndex + 1 }} / {{ validSlides.length }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { HomeBackgroundSwipeItemProperty, HomeBackgroundSwipeProperty } from './config'

/** 历史静态场景轮播，不承担页面级人物切换 */
defineOptions({ name: 'HomeBackgroundSwipe' })

const props = defineProps<{ property: HomeBackgroundSwipeProperty }>()

const DEFAULT_HEIGHT = 960
const MIN_HEIGHT = 200
const MAX_HEIGHT = 1600

const currentIndex = ref(props.property.initialIndex || 0)

const validHeight = computed(() => {
  const height = Number(props.property.height)
  if (!height || height < MIN_HEIGHT) return MIN_HEIGHT
  if (height > MAX_HEIGHT) return MAX_HEIGHT
  return height
})

const validSlides = computed(() => {
  const slides = Array.isArray(props.property.slides) ? props.property.slides : []
  return slides.filter((slide): slide is HomeBackgroundSwipeItemProperty => !!slide)
})

const validInitialIndex = computed(() => {
  const index = Number(props.property.initialIndex) || 0
  if (index < 0) return 0
  if (validSlides.value.length === 0) return 0
  if (index >= validSlides.value.length) return validSlides.value.length - 1
  return index
})

const handleIndexChange = (index: number) => {
  currentIndex.value = index
}

const getSlideBackground = (slide: HomeBackgroundSwipeItemProperty) => {
  const isValidColor = (color: string) =>
    typeof color === 'string' && /^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$/.test(color)
  return {
    background: isValidColor(slide.backgroundColor) ? slide.backgroundColor : '#F6D7DF'
  }
}
</script>

<style scoped lang="scss">
.text-shadow {
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}
</style>
