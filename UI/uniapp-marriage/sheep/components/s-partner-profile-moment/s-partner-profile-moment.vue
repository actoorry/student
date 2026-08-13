<template>
  <view v-if="moment" class="partner-profile-moment">
    <view class="header"><text class="title" :style="{ color: property.titleColor }">{{ property.title }}</text><text v-if="property.showAllMoments && partnerId" class="all" :style="{ color: property.allMomentsColor }" @tap="openAllMoments">全部动态 ›</text></view>
    <view class="card" :style="{ backgroundColor: property.cardBackgroundColor, borderRadius: property.cardRadius + 'rpx' }">
      <text v-if="moment.content" class="content" :style="{ color: property.contentColor }">{{ moment.content }}</text>
      <view v-if="active && visibleImages.length" class="images" :class="'images-' + visibleImages.length">
        <image v-for="image in visibleImages" :key="image" class="image" :src="image" mode="aspectFill" lazy-load @error="onImageError(image, mediaGeneration)" />
      </view>
      <text class="meta" :style="{ color: property.metaColor }">{{ relativeTime }} · {{ moment.likeCount }} 赞 · {{ moment.commentCount }} 评论</text>
    </view>
  </view>
</template>
<script setup>
  import { computed, inject, ref, watch } from 'vue';
  import sheep from '@/sheep';
  import { formatRelativeTime, identityKey, normalizeMoment, normalizeProperty, positiveId } from './partnerProfileMoment';
  const props = defineProps({ data: { type: Object, default: () => ({}) } });
  const contextRef = inject('partnerRecommendationContext', null);
  const context = computed(() => contextRef?.value || null);
  const property = computed(() => normalizeProperty(props.data));
  const partnerId = computed(() => positiveId(context.value?.partnerId));
  const active = computed(() => !!context.value?.active);
  const moment = computed(() => normalizeMoment(context.value?.profile?.latestMoment, property.value, (url) => sheep.$url.cdn(url)));
  const mediaGeneration = ref(0); const failedImages = ref(new Set()); let latestIdentity = '';
  watch(() => identityKey(partnerId.value, moment.value), (next) => { if (next !== latestIdentity) { latestIdentity = next; mediaGeneration.value += 1; failedImages.value = new Set(); } }, { immediate: true });
  const visibleImages = computed(() => (moment.value?.images || []).filter((image) => !failedImages.value.has(image)));
  const relativeTime = computed(() => formatRelativeTime(moment.value?.publishTime));
  function onImageError(image, generation) { if (generation !== mediaGeneration.value || !moment.value?.images.includes(image)) return; failedImages.value = new Set([...failedImages.value, image]); }
  function openAllMoments() { if (!property.value.showAllMoments || !partnerId.value) return; sheep.$router.go('/pages/partner-dynamics/index', { partnerId: partnerId.value }); }
</script>
<style scoped>
.partner-profile-moment { padding: 28rpx 32rpx 0; }.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:28rpx; }.title { padding-left:18rpx; border-left:8rpx solid #c84449; font-size:36rpx; font-weight:700; line-height:48rpx; }.all { font-size:26rpx; }.card { padding:32rpx; }.content { display:block; font-size:28rpx; line-height:44rpx; white-space:pre-wrap; }.images { display:flex; gap:16rpx; margin-top:24rpx; }.image { flex:1; min-width:0; height:180rpx; border-radius:16rpx; }.images-1 .image { flex:0 0 100%; height:280rpx; }.meta { display:block; margin-top:24rpx; font-size:24rpx; line-height:32rpx; }
</style>
