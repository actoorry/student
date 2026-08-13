<template>
  <view class="moment-item" @tap="openDetail">
    <view class="moment-author" @tap.stop="openMember">
      <view class="moment-avatar">
        <image v-if="item.avatar" class="moment-avatar__image" :src="item.avatar" mode="aspectFill" />
        <text v-else class="moment-avatar__text">{{ avatarText(item.nickname) }}</text>
      </view>
      <view class="moment-author__copy">
        <view class="moment-author__name-row">
          <text class="moment-author__name">{{ item.nickname }}</text>
          <text v-if="item.verifiedLabel" class="moment-author__verified">{{ item.verifiedLabel }}</text>
        </view>
        <text class="moment-author__time">{{ formatMarriageTime(item.publishTime) }}</text>
      </view>
      <view v-if="item.mine" class="moment-more" @tap.stop="openMore">⋯</view>
      <view
        v-else-if="mode === 'feed' && !item.followed"
        class="moment-follow"
        @tap.stop="follow"
      >
        + 关注
      </view>
    </view>

    <text v-if="item.content" class="moment-content">{{ item.content }}</text>

    <view v-if="item.imageUrls.length" class="moment-images" :class="layout.className">
      <image
        v-for="(image, imageIndex) in item.imageUrls"
        :key="`${item.id}-${imageIndex}`"
        class="moment-image"
        :src="image"
        mode="aspectFill"
      />
    </view>

    <view class="moment-actions">
      <view class="moment-action" @tap.stop="share">
        <uni-icons type="forward" size="18" color="#867475" />
        <text>分享</text>
      </view>
      <view class="moment-action" :class="{ 'moment-action--liked': item.liked }" @tap.stop="like">
        <uni-icons
          :type="item.liked ? 'hand-up-filled' : 'hand-up'"
          size="18"
          :color="item.liked ? '#df4f83' : '#867475'"
        />
        <text>{{ item.liked ? '已赞' : '点赞' }} {{ formatMomentCount(item.likeCount) }}</text>
      </view>
      <view class="moment-action" @tap.stop="comment">
        <uni-icons type="chatbubble" size="18" color="#867475" />
        <text>评论 {{ formatMomentCount(item.commentCount) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { computed } from 'vue';
  import { formatMarriageTime } from '@/sheep/helper/marriage';
  import {
    avatarText,
    formatMomentCount,
    momentLayout,
    normalizeMomentItem,
  } from '@/sheep/helper/marriage-moment';

  const props = defineProps({
    // 动态对象；组件内部规范化后仅用于展示
    item: {
      type: Object,
      default: () => ({}),
    },
    // feed：推荐/关注流（非本人且未关注时显示关注入口）
    // partner：人物动态；mine：我的动态（均不显示关注入口）
    mode: {
      type: String,
      default: 'feed',
    },
  });

  const emit = defineEmits([
    'open-detail',
    'open-member',
    'follow',
    'like',
    'comment',
    'share',
    'more',
  ]);

  const item = computed(() => normalizeMomentItem(props.item));
  const layout = computed(() => momentLayout(item.value.imageUrls.length));

  function openDetail() {
    emit('open-detail', item.value);
  }
  function openMember() {
    emit('open-member', item.value);
  }
  function follow() {
    emit('follow', item.value);
  }
  function like() {
    emit('like', item.value);
  }
  function comment() {
    emit('comment', item.value);
  }
  function share() {
    emit('share', item.value);
  }
  function openMore() {
    emit('more', item.value);
  }
</script>

<style scoped>
  .moment-item {
    padding: 30rpx 30rpx 6rpx;
    background: #ffffff;
  }

  .moment-author {
    display: flex;
    align-items: center;
  }
  .moment-avatar {
    width: 82rpx;
    height: 82rpx;
    flex: none;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    color: #ffffff;
    font-size: 30rpx;
    background: #d7a2ad;
    border-radius: 50%;
  }
  .moment-avatar__image {
    width: 100%;
    height: 100%;
  }
  .moment-author__copy {
    flex: 1;
    min-width: 0;
    margin-left: 18rpx;
  }
  .moment-author__name-row {
    display: flex;
    align-items: center;
    gap: 10rpx;
  }
  .moment-author__name {
    color: #493638;
    font-size: 29rpx;
    font-weight: 700;
  }
  .moment-author__verified {
    flex: none;
    padding: 4rpx 10rpx;
    color: #bd6a7e;
    font-size: 19rpx;
    background: #fff0f3;
    border-radius: 10rpx;
  }
  .moment-author__time {
    display: block;
    margin-top: 7rpx;
    color: #ae9c9c;
    font-size: 22rpx;
  }
  .moment-more {
    flex: none;
    padding: 10rpx 12rpx;
    color: #9d8d8d;
    font-size: 34rpx;
    line-height: 1;
  }
  .moment-follow {
    flex: none;
    margin-left: 16rpx;
    padding: 10rpx 22rpx;
    color: #ffffff;
    font-size: 22rpx;
    background: linear-gradient(135deg, #ec6d96, #df4f83);
    border-radius: 999rpx;
  }

  .moment-content {
    display: block;
    margin-top: 24rpx;
    color: #3f3233;
    font-size: 29rpx;
    line-height: 1.7;
    white-space: pre-wrap;
  }

  .moment-images {
    display: flex;
    flex-wrap: wrap;
    gap: 8rpx;
    margin-top: 20rpx;
  }
  .moment-images--single .moment-image {
    width: 74%;
    height: 400rpx;
  }
  .moment-images--pair .moment-image {
    width: calc((100% - 8rpx) / 2);
    height: 240rpx;
  }
  .moment-images--grid .moment-image {
    width: calc((100% - 16rpx) / 3);
    height: 200rpx;
  }
  .moment-image {
    border-radius: 14rpx;
  }

  .moment-actions {
    display: flex;
    justify-content: flex-end;
    gap: 38rpx;
    margin-top: 26rpx;
    padding: 22rpx 0 8rpx;
    border-top: 1rpx solid #edf1f5;
  }
  .moment-action {
    display: flex;
    align-items: center;
    gap: 7rpx;
    color: #867475;
    font-size: 24rpx;
  }
  .moment-action--liked {
    color: #df4f83;
  }
</style>
