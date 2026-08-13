<template>
  <view class="privacy-entry" @tap="openDetail">
    <text class="privacy-entry__icon">{{ icon }}</text><text class="privacy-entry__title">{{ property.entryTitle }}</text><text class="privacy-entry__arrow">›</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { buildPrivacyAgreementUrl, ICONS, normalizeProperty } from './partnerPrivacyAgreement'

const props = defineProps({ data: { type: Object, default: () => ({}) }, sourceContext: { type: Object, default: null } })
const property = computed(() => normalizeProperty(props.data))
const icon = computed(() => ICONS[property.value.entryIcon] || ICONS.LOCK)
function openDetail() { const url = buildPrivacyAgreementUrl(props.sourceContext); if (url) uni.navigateTo({ url }) }
</script>

<style scoped>
.privacy-entry { display:flex; height:88rpx; align-items:center; gap:24rpx; padding:0 32rpx; border-radius:24rpx; background:#fff7f4; color:#241818; }
.privacy-entry__icon { width:40rpx; color:#9a655d; font-size:36rpx; line-height:1; text-align:center; }.privacy-entry__title { flex:1; font-size:30rpx; font-weight:600; }.privacy-entry__arrow { color:#b9a29d; font-size:40rpx; }
</style>
