<template>
  <div class="partner-marriage-verification-badge">
    <div
      class="partner-marriage-verification-badge__card"
      :style="{
        backgroundColor: validBadgeBackgroundColor,
        borderRadius: validBorderRadius + 'px',
        padding: validPadding,
        marginBottom: validMarginBottom + 'px',
      }"
    >
      <div class="partner-marriage-verification-badge__header">
        <div
          class="partner-marriage-verification-badge__icon"
          :style="{
            width: validIconSize + 'px',
            height: validIconSize + 'px',
            color: validAccentColor,
          }"
        >
          ✓
        </div>
        <div
          class="partner-marriage-verification-badge__title"
          :style="{ color: validTitleColor }"
        >
          婚姻认证
        </div>
      </div>
      <div
        class="partner-marriage-verification-badge__description"
        :style="{ color: validDescriptionColor }"
      >
        状态已核验
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  DEFAULT_ACCENT_COLOR,
  DEFAULT_BADGE_BACKGROUND_COLOR,
  DEFAULT_DESCRIPTION_COLOR,
  DEFAULT_TITLE_COLOR,
  normalizeColor,
  rpxToPreviewPx,
} from './config'
import type { PartnerMarriageVerificationBadgeProperty } from './config'

/** 婚恋认证标志 - 管理端静态样例预览 */
defineOptions({ name: 'PartnerMarriageVerificationBadge' })

const props = defineProps<{ property: PartnerMarriageVerificationBadgeProperty }>()

const validBadgeBackgroundColor = computed(() =>
  normalizeColor(props.property.badgeBackgroundColor, DEFAULT_BADGE_BACKGROUND_COLOR),
)
const validAccentColor = computed(() =>
  normalizeColor(props.property.accentColor, DEFAULT_ACCENT_COLOR),
)
const validTitleColor = computed(() =>
  normalizeColor(props.property.titleColor, DEFAULT_TITLE_COLOR),
)
const validDescriptionColor = computed(() =>
  normalizeColor(props.property.descriptionColor, DEFAULT_DESCRIPTION_COLOR),
)

const validBorderRadius = computed(() => rpxToPreviewPx(40))
const validPadding = computed(() => `${rpxToPreviewPx(28)}px ${rpxToPreviewPx(32)}px`)
const validMarginBottom = computed(() => rpxToPreviewPx(20))
const validIconSize = computed(() => rpxToPreviewPx(72))
</script>

<style scoped lang="scss">
.partner-marriage-verification-badge {
  width: 100%;
  box-sizing: border-box;

  &__card {
    box-sizing: border-box;
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    border-radius: 50%;
    font-size: 14px;
    font-weight: bold;
    background-color: rgba(255, 255, 255, 0.6);
  }

  &__title {
    font-size: 15px;
    font-weight: bold;
    line-height: 20px;
  }

  &__description {
    font-size: 12px;
    line-height: 18px;
  }
}
</style>
