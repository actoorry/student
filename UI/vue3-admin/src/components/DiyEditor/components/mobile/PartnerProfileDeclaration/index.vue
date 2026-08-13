<template>
  <div class="partner-profile-declaration">
    <div class="partner-profile-declaration__title" :style="{ color: validTitleColor }">
      {{ validTitle }}
    </div>
    <div
      class="partner-profile-declaration__content"
      :style="{
        color: validContentColor,
        backgroundColor: validContentBackgroundColor,
        borderRadius: validContentRadius + 'px'
      }"
    >
      {{ sampleDeclaration }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  DEFAULT_CONTENT_BACKGROUND_COLOR,
  DEFAULT_CONTENT_COLOR,
  DEFAULT_TITLE_COLOR,
  normalizeColor,
  normalizeContentRadius,
  normalizeTitle,
  rpxToPreviewPx,
  SAMPLE_DECLARATION
} from './config'
import type { PartnerProfileDeclarationProperty } from './config'

/** 择偶宣言 - 管理端静态样例预览 */
defineOptions({ name: 'PartnerProfileDeclaration' })

const props = defineProps<{ property: PartnerProfileDeclarationProperty }>()

const validTitle = computed(() => normalizeTitle(props.property.title))
const validTitleColor = computed(() =>
  normalizeColor(props.property.titleColor, DEFAULT_TITLE_COLOR)
)
const validContentColor = computed(() =>
  normalizeColor(props.property.contentColor, DEFAULT_CONTENT_COLOR)
)
const validContentBackgroundColor = computed(() =>
  normalizeColor(props.property.contentBackgroundColor, DEFAULT_CONTENT_BACKGROUND_COLOR)
)
const validContentRadius = computed(() =>
  rpxToPreviewPx(normalizeContentRadius(props.property.contentRadius))
)
const sampleDeclaration = SAMPLE_DECLARATION
</script>

<style scoped lang="scss">
.partner-profile-declaration {
  width: 100%;
  box-sizing: border-box;

  &__title {
    margin-bottom: 12px;
    font-size: 13px;
    line-height: 18px;
  }

  &__content {
    box-sizing: border-box;
    padding: 16px;
    font-size: 14px;
    line-height: 24px;
    white-space: pre-wrap;
    overflow-wrap: anywhere;
  }
}
</style>
