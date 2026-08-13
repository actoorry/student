<template>
  <view v-if="scene" class="verification-mask" @tap="close">
    <view class="verification-sheet" @tap.stop>
      <text class="verification-icon">✓</text>
      <text class="verification-title">完成实名认证</text>
      <text class="verification-description">{{ isPersonScene ? '实名认证后即可安心联系和关注心仪对象。' : '实名认证后即可发布、评论和点赞动态。' }}</text>
      <view class="verification-benefits"><text>提升资料可信度</text><text>解锁互动功能</text><text>获得更多推荐</text></view>
      <view class="verification-primary" @tap="goCertification">前往认证中心</view>
      <view class="verification-later" @tap="close">稍后再说</view>
    </view>
  </view>
</template>
<script setup>
import { computed } from 'vue';
import sheep from '@/sheep';
import { closeRealNameVerificationModal } from '@/sheep/hooks/useModal';
const modal = sheep.$store('modal');
const scene = computed(() => modal.realNameVerification);
const isPersonScene = computed(() => scene.value === 'message' || scene.value === 'follow');
function close() { closeRealNameVerificationModal(); }
function goCertification() { close(); sheep.$router.go('/pages/mine-certifications/index'); }
</script>
<style scoped>
.verification-mask { position: fixed; z-index: 1000; inset: 0; display:flex; align-items:flex-end; background:rgba(36,25,27,.5); }
.verification-sheet { width:100%; padding:46rpx 40rpx calc(34rpx + env(safe-area-inset-bottom)); box-sizing:border-box; text-align:center; background:var(--marriage-surface,#fff); border-radius:40rpx 40rpx 0 0; box-shadow:0 -16rpx 48rpx var(--marriage-shadow,rgba(74,44,50,.2)); }
.verification-icon { display:flex; width:76rpx; height:76rpx; margin:auto; align-items:center; justify-content:center; color:#fff; font-weight:700; background:var(--marriage-primary,#df5a87); border-radius:50%; }.verification-title{display:block;margin-top:20rpx;color:var(--marriage-text,#4d3f41);font-size:36rpx;font-weight:700}.verification-description{display:block;margin-top:16rpx;color:var(--marriage-muted,#927d80);font-size:25rpx;line-height:1.6}.verification-benefits{display:flex;justify-content:space-between;margin:30rpx 0;color:var(--marriage-primary,#df5a87);font-size:21rpx}.verification-primary{padding:25rpx;color:#fff;background:var(--marriage-primary,#df5a87);border-radius:999rpx;font-weight:700}.verification-later{padding-top:28rpx;color:var(--marriage-muted,#927d80);font-size:25rpx}
</style>
