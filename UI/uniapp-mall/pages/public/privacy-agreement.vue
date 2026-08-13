<template>
  <view class="privacy-page">
    <view v-if="loading" class="privacy-state">加载中...</view>
    <view v-else-if="property" class="privacy-content">
      <text class="privacy-title">{{ property.detailTitle }}</text>
      <text v-if="property.detailSubtitle" class="privacy-subtitle">{{ property.detailSubtitle }}</text>
      <view v-for="(section, index) in property.sections" :key="index" class="privacy-section">
        <text class="privacy-section__title">{{ section.title }}</text>
        <text class="privacy-section__content">{{ section.content }}</text>
      </view>
    </view>
    <view v-else class="privacy-state">协议内容暂不可用</view>
    <button class="privacy-back" @tap="goBack">返回</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import DiyApi from '@/sheep/api/promotion/diy'
import { findAgreementProperty, parseSourceDescriptor } from '@/sheep/helper/diy-agreement'

const loading = ref(false)
const property = ref(null)
let source = null
let requestGeneration = 0
let requestedTenantId = ''
const tenantId = () => String(uni.getStorageSync('tenant-id') || '')

async function load() {
  const generation = ++requestGeneration
  property.value = null
  if (!source) return
  requestedTenantId = tenantId()
  loading.value = true
  try {
    const result = source.source === 'template' ? await DiyApi.getDiyTemplate(source.id) : await DiyApi.getDiyPage(source.id)
    if (generation !== requestGeneration || requestedTenantId !== tenantId()) return
    property.value = result?.code === 0 ? findAgreementProperty(result.data, source) : null
  } catch {
    if (generation === requestGeneration && requestedTenantId === tenantId()) property.value = null
  } finally {
    if (generation === requestGeneration) loading.value = false
  }
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/index/user' }) })
  else uni.switchTab({ url: '/pages/index/user' })
}

onLoad((query) => { source = parseSourceDescriptor(query); load() })
onShow(() => { if (source && requestedTenantId && requestedTenantId !== tenantId()) load() })
onUnload(() => { requestGeneration += 1 })
</script>

<style scoped>
.privacy-page { min-height:100vh; box-sizing:border-box; padding:32rpx 32rpx 56rpx; background:#fff; color:#241818; }.privacy-content { padding-bottom:24rpx; }.privacy-title { display:block; font-size:48rpx; font-weight:800; line-height:64rpx; }.privacy-subtitle { display:block; margin-top:8rpx; margin-bottom:32rpx; color:#705c58; font-size:28rpx; line-height:44rpx; white-space:pre-wrap; }.privacy-section { margin-bottom:28rpx; padding:32rpx; border:2rpx solid rgba(214,198,190,.62); border-radius:32rpx; background:#fffbf8; }.privacy-section__title { display:block; margin-bottom:16rpx; font-size:34rpx; font-weight:700; line-height:48rpx; }.privacy-section__content { display:block; color:#705c58; font-size:28rpx; line-height:44rpx; white-space:pre-wrap; }.privacy-state { padding:64rpx 0; color:#705c58; font-size:28rpx; text-align:center; }.privacy-back { margin-top:16rpx; color:#705c58; background:#fff7f4; border:0; }
</style>
