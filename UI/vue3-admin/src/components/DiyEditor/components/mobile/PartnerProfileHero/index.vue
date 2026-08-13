<template>
  <div
    class="partner-profile-hero relative overflow-hidden"
    :style="{ height: validHeight + 'px' }"
  >
    <!-- 主图占位：管理端使用 CSS 渐变，不请求网络真实人物 -->
    <div
      class="absolute inset-0 partner-profile-hero__main"
      :style="{ background: 'linear-gradient(180deg, #F6D7DF 0%, #D7E6F6 100%)' }"
    ></div>

    <!-- 底部可读性遮罩 -->
    <div class="absolute bottom-0 left-0 right-0 partner-profile-hero__mask"></div>

    <!-- 身份面板 -->
    <div class="absolute bottom-0 left-0 right-0 partner-profile-hero__panel">
      <div class="flex items-end gap-12px">
        <!-- 头像占位 -->
        <div
          class="partner-profile-hero__avatar flex-shrink-0 rounded-full overflow-hidden"
          :style="{
            width: validAvatarSize + 'px',
            height: validAvatarSize + 'px',
          }"
        >
          <div
            class="w-full h-full"
            :style="{ background: 'linear-gradient(135deg, #E0E0E0 0%, #F5F5F5 100%)' }"
          ></div>
        </div>

        <div class="flex flex-col gap-4px pb-4px">
          <div
            class="font-bold text-18px"
            :style="{ color: validNameColor }"
          >
            {{ sampleName }}
          </div>
          <div
            class="text-14px"
            :style="{ color: validAgeColor }"
          >
            {{ sampleAge }}岁
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  DEFAULT_AGE_COLOR,
  DEFAULT_NAME_COLOR,
  normalizeAvatarSize,
  normalizeColor,
  normalizeHeight,
  SAMPLE_PROFILE,
} from './config'
import type { PartnerProfileHeroProperty } from './config'

/** 人物主图 - 管理端静态样例预览 */
defineOptions({ name: 'PartnerProfileHero' })

const props = defineProps<{ property: PartnerProfileHeroProperty }>()

const validHeight = computed(() => normalizeHeight(props.property.height) / 2)
const validAvatarSize = computed(() => normalizeAvatarSize(props.property.avatarSize) / 2)
const validNameColor = computed(() => normalizeColor(props.property.nameColor, DEFAULT_NAME_COLOR))
const validAgeColor = computed(() => normalizeColor(props.property.ageColor, DEFAULT_AGE_COLOR))

const sampleName = SAMPLE_PROFILE.name
const sampleAge = SAMPLE_PROFILE.age
</script>

<style scoped lang="scss">
.partner-profile-hero {
  &__main {
    width: 100%;
    height: 100%;
  }

  &__mask {
    height: 160px;
    background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.45) 100%);
    pointer-events: none;
  }

  &__panel {
    padding: 16px 20px 24px;
    pointer-events: none;
  }
}
</style>
