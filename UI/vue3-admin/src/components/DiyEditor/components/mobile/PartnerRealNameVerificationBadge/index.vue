<template>
  <div class="partner-real-name-verification-badge">
    <div
      class="partner-real-name-verification-badge__card"
      :style="{
        backgroundColor: validBadgeBackgroundColor,
        borderRadius: validBorderRadius + 'px',
        padding: validPadding,
        marginBottom: validMarginBottom + 'px',
      }"
    >
      <div class="partner-real-name-verification-badge__header">
        <div
          class="partner-real-name-verification-badge__icon"
          :style="{
            width: validIconSize + 'px',
            height: validIconSize + 'px',
            color: validAccentColor,
          }"
        >
          ✓
        </div>
        <div
          class="partner-real-name-verification-badge__title"
          :style="{ color: validTitleColor }"
        >
          实名认证
        </div>
      </div>
      <div
        class="partner-real-name-verification-badge__description"
        :style="{ color: validDescriptionColor }"
      >
        {{ sampleDescription }}
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
  extractIdCardPrefix,
  normalizeColor,
  rpxToPreviewPx,
  SAMPLE_MASKED_ID_CARD_PREFIX,
} from './config'
import type { PartnerRealNameVerificationBadgeProperty } from './config'

/** 实名认证标志 - 管理端静态样例预览 */
defineOptions({ name: 'PartnerRealNameVerificationBadge' })

const props = defineProps<{ property: PartnerRealNameVerificationBadgeProperty }>()

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

const sampleDescription = computed(() => {
  const prefix = extractIdCardPrefix(SAMPLE_MASKED_ID_CARD_PREFIX)
  return prefix ? `身份证前四位：${prefix}` : '身份已核验'
})
</script>

<style scoped lang="scss">
.partner-real-name-verification-badge {
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
