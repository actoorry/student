<template>
  <div class="partner-profile-album">
    <!-- 标题行：标题 + 可选副标题 -->
    <div class="partner-profile-album__header">
      <div class="partner-profile-album__title-row">
        <span
          class="partner-profile-album__title"
          :style="{ color: validTitleColor }"
        >{{ validTitle }}</span>
        <span
          v-if="validSubtitle"
          class="partner-profile-album__subtitle"
          :style="{ color: validSubtitleColor }"
        >{{ validSubtitle }}</span>
      </div>
    </div>

    <!-- 三列图片网格：使用匿名静态夹具，不请求真实人物 -->
    <div class="partner-profile-album__grid">
      <div
        v-for="(img, index) in sampleImages"
        :key="index"
        class="partner-profile-album__image-wrap"
        :style="{
          height: previewImageHeight + 'px',
          borderRadius: previewImageRadius + 'px',
          width: '31%',
        }"
      >
        <img
          class="partner-profile-album__image"
          :src="img"
          :style="{
            borderRadius: previewImageRadius + 'px',
          }"
          alt="相册图片"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  DEFAULT_IMAGE_HEIGHT,
  DEFAULT_IMAGE_RADIUS,
  DEFAULT_SUBTITLE,
  DEFAULT_SUBTITLE_COLOR,
  DEFAULT_TITLE,
  DEFAULT_TITLE_COLOR,
  normalizeColor,
  normalizeImageHeight,
  normalizeImageRadius,
  normalizeSubtitle,
  normalizeTitle,
  rpxToPreviewPx,
  SAMPLE_ALBUM_IMAGES,
} from './config'
import type { PartnerProfileAlbumProperty } from './config'

/** 相册 - 管理端静态样例预览 */
defineOptions({ name: 'PartnerProfileAlbum' })

const props = defineProps<{ property: PartnerProfileAlbumProperty }>()

const validTitle = computed(() => normalizeTitle(props.property.title))
const validSubtitle = computed(() => normalizeSubtitle(props.property.subtitle))
const validTitleColor = computed(() => normalizeColor(props.property.titleColor, DEFAULT_TITLE_COLOR))
const validSubtitleColor = computed(() =>
  normalizeColor(props.property.subtitleColor, DEFAULT_SUBTITLE_COLOR),
)
const validImageHeight = computed(() => normalizeImageHeight(props.property.imageHeight))
const validImageRadius = computed(() => normalizeImageRadius(props.property.imageRadius))

const previewImageHeight = computed(() => rpxToPreviewPx(validImageHeight.value))
const previewImageRadius = computed(() => rpxToPreviewPx(validImageRadius.value))

// 管理端始终使用匿名静态夹具预览，不访问真实人物网络数据
const sampleImages = SAMPLE_ALBUM_IMAGES as unknown as string[]
</script>

<style scoped lang="scss">
.partner-profile-album {
  padding: 18px 16px 0;

  &__header {
    margin-bottom: 16px;
  }

  &__title-row {
    display: flex;
    align-items: baseline;
    gap: 10px;
  }

  &__title {
    font-size: 18px;
    font-weight: 700;
    line-height: 24px;
    padding-left: 10px;
    border-left: 4px solid #c84449;
  }

  &__subtitle {
    font-size: 12px;
    line-height: 16px;
  }

  &__grid {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    row-gap: 10px;
  }

  &__image-wrap {
    overflow: hidden;
    background-color: #f0f0f0;
  }

  &__image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}
</style>
