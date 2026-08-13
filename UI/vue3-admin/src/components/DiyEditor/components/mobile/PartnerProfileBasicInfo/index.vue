<template>
  <div class="partner-profile-basic-info">
    <template v-if="previewItems.length > 0">
      <div class="partner-profile-basic-info__title">基本资料</div>
      <div class="partner-profile-basic-info__grid">
        <div
          v-for="item in previewItems"
          :key="item.key"
          class="partner-profile-basic-info__cell"
        >
          <div class="partner-profile-basic-info__label">{{ item.label }}</div>
          <div class="partner-profile-basic-info__value">{{ item.value }}</div>
        </div>
      </div>
    </template>

    <div v-else class="partner-profile-basic-info__empty">已关闭全部资料项</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { buildPreviewBasicInfoItems } from './config'
import type { PartnerProfileBasicInfoProperty } from './config'

/** 基本资料 - 管理端匿名静态预览 */
defineOptions({ name: 'PartnerProfileBasicInfo' })

const props = defineProps<{ property: PartnerProfileBasicInfoProperty }>()

const previewItems = computed(() => buildPreviewBasicInfoItems(props.property))
</script>

<style scoped lang="scss">
.partner-profile-basic-info {
  box-sizing: border-box;
  width: 100%;
  padding: 18px 16px 0;

  &__title {
    padding-left: 10px;
    margin-bottom: 14px;
    color: #2d2324;
    font-size: 18px;
    font-weight: 700;
    line-height: 24px;
    border-left: 4px solid #c84449;
  }

  &__grid {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
  }

  &__cell {
    box-sizing: border-box;
    width: 48.4%;
    min-height: 80px;
    padding: 14px;
    margin-bottom: 10px;
    background-color: #f7f3ef;
    border-radius: 16px;
  }

  &__label {
    margin-bottom: 8px;
    color: #8f7e79;
    font-size: 12px;
  }

  &__value {
    color: #000;
    font-size: 16px;
    font-weight: 700;
    line-height: 22px;
    word-break: break-all;
  }

  &__empty {
    padding: 20px 16px;
    color: #909399;
    font-size: 13px;
    text-align: center;
    background-color: #fafafa;
    border: 1px dashed #dcdfe6;
    border-radius: 8px;
  }
}
</style>
