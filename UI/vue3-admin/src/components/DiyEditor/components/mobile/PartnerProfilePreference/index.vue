<template>
  <div class="partner-profile-preference">
    <div
      class="partner-profile-preference__title"
      :style="{
        color: validTitleColor,
        borderLeftColor: validAccentColor
      }"
    >
      {{ validTitle }}
    </div>
    <div class="partner-profile-preference__tags">
      <span
        v-for="(tag, idx) in sampleTags"
        :key="idx"
        class="partner-profile-preference__tag"
        :style="{
          color: validTagTextColor,
          backgroundColor: validTagBackgroundColor,
          borderRadius: validTagRadius + 'px'
        }"
      >
        {{ tag }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  DEFAULT_ACCENT_COLOR,
  DEFAULT_TAG_BACKGROUND_COLOR,
  DEFAULT_TAG_TEXT_COLOR,
  DEFAULT_TITLE_COLOR,
  normalizeColor,
  normalizeTagRadius,
  normalizeTitle,
  rpxToPreviewPx,
  SAMPLE_TAGS
} from './config'
import type { PartnerProfilePreferenceProperty } from './config'

/** 择偶条件 - 管理端匿名静态预览 */
defineOptions({ name: 'PartnerProfilePreference' })

const props = defineProps<{ property: PartnerProfilePreferenceProperty }>()

const validTitle = computed(() => normalizeTitle(props.property.title))
const validTitleColor = computed(() =>
  normalizeColor(props.property.titleColor, DEFAULT_TITLE_COLOR)
)
const validAccentColor = computed(() =>
  normalizeColor(props.property.accentColor, DEFAULT_ACCENT_COLOR)
)
const validTagTextColor = computed(() =>
  normalizeColor(props.property.tagTextColor, DEFAULT_TAG_TEXT_COLOR)
)
const validTagBackgroundColor = computed(() =>
  normalizeColor(props.property.tagBackgroundColor, DEFAULT_TAG_BACKGROUND_COLOR)
)
const validTagRadius = computed(() =>
  rpxToPreviewPx(normalizeTagRadius(props.property.tagRadius))
)
const sampleTags = SAMPLE_TAGS
</script>

<style scoped lang="scss">
.partner-profile-preference {
  box-sizing: border-box;
  width: 100%;
  padding: 0 16px;

  &__title {
    padding-left: 10px;
    margin-bottom: 14px;
    color: #2d2324;
    font-size: 18px;
    font-weight: 700;
    line-height: 24px;
    border-left: 4px solid #c84449;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
  }

  &__tag {
    box-sizing: border-box;
    padding: 8px 14px;
    margin-right: 10px;
    margin-bottom: 10px;
    color: #8f675d;
    font-size: 13px;
    line-height: 18px;
    background-color: #fff2e7;
    border-radius: 16px;
    white-space: nowrap;
  }
}
</style>
