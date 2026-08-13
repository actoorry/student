<template>
  <div class="partner-profile-moment">
    <div class="header">
      <span class="title" :style="{ color: titleColor }">{{ title }}</span>
      <span v-if="showAllMoments" class="all" :style="{ color: allMomentsColor }">全部动态 ›</span>
    </div>
    <div class="card" :style="{ backgroundColor: cardBackgroundColor, borderRadius: cardRadius + 'px' }">
      <p class="content" :style="{ color: contentColor }">{{ content }}</p>
      <div v-if="images.length" class="images">
        <div v-for="(color, index) in images" :key="index" class="image" :style="{ backgroundColor: color }">动态图片</div>
      </div>
      <p class="meta" :style="{ color: metaColor }">{{ SAMPLE_MOMENT.publishTime }} · {{ SAMPLE_MOMENT.likeCount }} 赞 · {{ SAMPLE_MOMENT.commentCount }} 评论</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  CARD_RADIUS_RANGE, CONTENT_LENGTH_RANGE, DEFAULT_ALL_MOMENTS_COLOR, DEFAULT_CARD_BACKGROUND_COLOR,
  DEFAULT_CARD_RADIUS, DEFAULT_CONTENT_COLOR, DEFAULT_CONTENT_MAX_LENGTH, DEFAULT_MAX_IMAGES,
  DEFAULT_META_COLOR, DEFAULT_TITLE_COLOR, SAMPLE_MOMENT, normalizeBoolean, normalizeBoundedNumber,
  normalizeColor, normalizeTitle, rpxToPreviewPx,
} from './config'
import type { PartnerProfileMomentProperty } from './config'

defineOptions({ name: 'PartnerProfileMoment' })
const props = defineProps<{ property: PartnerProfileMomentProperty }>()
const title = computed(() => normalizeTitle(props.property.title))
const showAllMoments = computed(() => normalizeBoolean(props.property.showAllMoments))
const maxImages = computed(() => normalizeBoundedNumber(props.property.maxImages, 0, 3, DEFAULT_MAX_IMAGES))
const cardRadius = computed(() => rpxToPreviewPx(normalizeBoundedNumber(props.property.cardRadius, CARD_RADIUS_RANGE.min, CARD_RADIUS_RANGE.max, DEFAULT_CARD_RADIUS)))
const content = computed(() => Array.from(SAMPLE_MOMENT.content).slice(0, normalizeBoundedNumber(props.property.contentMaxLength, CONTENT_LENGTH_RANGE.min, CONTENT_LENGTH_RANGE.max, DEFAULT_CONTENT_MAX_LENGTH)).join(''))
const images = computed(() => SAMPLE_MOMENT.images.slice(0, maxImages.value))
const titleColor = computed(() => normalizeColor(props.property.titleColor, DEFAULT_TITLE_COLOR))
const allMomentsColor = computed(() => normalizeColor(props.property.allMomentsColor, DEFAULT_ALL_MOMENTS_COLOR))
const contentColor = computed(() => normalizeColor(props.property.contentColor, DEFAULT_CONTENT_COLOR))
const metaColor = computed(() => normalizeColor(props.property.metaColor, DEFAULT_META_COLOR))
const cardBackgroundColor = computed(() => normalizeColor(props.property.cardBackgroundColor, DEFAULT_CARD_BACKGROUND_COLOR))
</script>

<style scoped lang="scss">
.partner-profile-moment { padding: 18px 16px 0; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.title { padding-left: 10px; border-left: 4px solid #c84449; font-size: 18px; font-weight: 700; line-height: 24px; }
.all { font-size: 13px; }
.card { padding: 16px; }
.content { margin: 0; font-size: 14px; line-height: 22px; white-space: pre-wrap; }
.images { display: flex; gap: 8px; margin-top: 12px; }
.image { display: flex; flex: 1; height: 90px; align-items: center; justify-content: center; color: rgba(70,50,45,.55); font-size: 11px; border-radius: 8px; }
.meta { margin: 12px 0 0; font-size: 12px; line-height: 16px; }
</style>
