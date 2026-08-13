<template>
  <s-layout title="关注提醒" navbar="normal" showLeftButton :bgStyle="pageBackground">
    <scroll-view class="list-scroll" scroll-y refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="reload" @scrolltolower="loadMore">
      <view class="list-content">
        <view class="intro-card">
          <text class="intro-title">谁关注了你</text>
          <text class="intro-description">查看关注提醒，回关后即可进一步认识彼此。</text>
        </view>
        <view v-if="loading && notifications.length === 0" class="state-card">关注提醒加载中...</view>
        <view v-else-if="errorText && notifications.length === 0" class="state-card" @tap="reload">
          {{ errorText }}，点击重试
        </view>
        <view v-else-if="notifications.length === 0" class="state-card">暂时没有关注提醒</view>
        <view v-else class="notification-list">
          <view v-for="(item, index) in notifications" :key="item.id" class="notification-row">
            <view class="avatar" @tap="openMember(item)">
              <image v-if="avatarUrl(item)" class="avatar-image" :src="avatarUrl(item)" mode="aspectFill" />
              <text v-else>{{ avatarText(item) }}</text>
            </view>
            <view class="notification-main" @tap="openMember(item)">
              <view class="row-top">
                <text class="member-name">{{ nickname(item) }}</text>
                <text class="event-time">{{ formatMarriageTime(item.eventTime) }}</text>
              </view>
              <text class="notification-copy">{{ item.content || '关注了你' }}</text>
            </view>
            <view class="follow-back" @tap="followBack(item, index)">回关</view>
          </view>
        </view>
        <view v-if="notifications.length" class="load-state">{{ loading ? '加载中...' : hasMore ? '上拉加载更多' : '已展示全部' }}</view>
      </view>
    </scroll-view>
  </s-layout>
</template>

<script setup>
  import { ref } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import InteractionApi, { INTERACTION_SCENES } from '@/sheep/api/marriage/interaction';
  import { dedupeBy, formatMarriageTime, getPageData, getResponseData, parsePositiveId } from '@/sheep/helper/marriage';

  const pageBackground = { backgroundColor: '#fff8f7' };
  const notifications = ref([]);
  const pageNo = ref(1);
  const pageSize = 20;
  const loading = ref(false);
  const refreshing = ref(false);
  const hasMore = ref(true);
  const errorText = ref('');

  const partnerId = (item) => parsePositiveId(item?.payload?.fromPartnerId || item?.actorId);
  const nickname = (item) => item?.payload?.nickname || '关注你的人';
  const avatarUrl = (item) => item?.payload?.avatar || '';
  const avatarText = (item) => nickname(item).slice(0, 1);
  const toast = (title) => uni.showToast({ title, icon: 'none' });

  async function load(reset = false) {
    if (loading.value) return;
    loading.value = true;
    errorText.value = '';
    const requestedPage = reset ? 1 : pageNo.value;
    try {
      const page = getPageData(
        await InteractionApi.getNotificationPage({
          pageNo: requestedPage,
          pageSize,
          scene: INTERACTION_SCENES.FOLLOW,
        }),
        '关注提醒加载失败',
      );
      notifications.value = dedupeBy(reset ? page.list : notifications.value.concat(page.list), (item) => item.id);
      pageNo.value = requestedPage;
      hasMore.value = notifications.value.length < page.total && page.list.length > 0;
      getResponseData(await InteractionApi.readScene(INTERACTION_SCENES.FOLLOW), '标记已读失败');
      sheep.$store('social').refreshUnread();
      return true;
    } catch (error) {
      errorText.value = error.message || '关注提醒加载失败';
      return false;
    } finally {
      loading.value = false;
      refreshing.value = false;
    }
  }

  function reload() {
    refreshing.value = true;
    hasMore.value = true;
    load(true);
  }

  async function loadMore() {
    if (loading.value || !hasMore.value) return;
    const previousPage = pageNo.value;
    pageNo.value += 1;
    const loaded = await load();
    if (!loaded) pageNo.value = previousPage;
  }

  async function followBack(item, index) {
    const id = partnerId(item);
    if (!id) return toast('缺少关注方信息');
    try {
      const result = getResponseData(await InteractionApi.follow(id), '回关失败');
      notifications.value[index] = { ...item, followedBack: true };
      toast(result?.mutual ? '已互相关注' : '已回关');
    } catch (error) {
      toast(error.message || '回关失败');
    }
  }

  function openMember(item) {
    const id = partnerId(item);
    if (!id) return toast('该用户暂不可查看');
    sheep.$router.go('/pages/member-detail/index', { id });
  }

  onShow(() => reload());
</script>

<style scoped>
  .list-scroll { height: calc(100vh - 90rpx); background: #fff8f7; }
  .list-content { padding: 24rpx; }
  .intro-card, .state-card, .notification-list { background: #fff; border: 1rpx solid rgba(194,132,145,.13); border-radius: 28rpx; }
  .intro-card { padding: 32rpx; }
  .intro-title { display: block; color: #4e3a3c; font-size: 32rpx; font-weight: 760; }
  .intro-description { display: block; margin-top: 12rpx; color: #9f888b; font-size: 24rpx; line-height: 1.6; }
  .state-card { margin-top: 20rpx; padding: 76rpx 30rpx; color: #9d8789; text-align: center; }
  .notification-list { margin-top: 20rpx; overflow: hidden; }
  .notification-row { display: flex; align-items: center; padding: 26rpx; border-bottom: 1rpx solid #f5eded; }
  .notification-row:last-child { border-bottom: 0; }
  .avatar { width: 84rpx; height: 84rpx; display: flex; align-items: center; justify-content: center; flex: none; overflow: hidden; color: #fff; background: #c697a2; border-radius: 50%; }
  .avatar-image { width: 100%; height: 100%; }
  .notification-main { flex: 1; min-width: 0; margin-left: 18rpx; }
  .row-top { display: flex; align-items: center; justify-content: space-between; gap: 10rpx; }
  .member-name { color: #4f3d3f; font-size: 27rpx; font-weight: 700; }
  .event-time { color: #b39fa2; font-size: 19rpx; }
  .notification-copy { display: block; margin-top: 9rpx; overflow: hidden; color: #927f82; font-size: 23rpx; white-space: nowrap; text-overflow: ellipsis; }
  .follow-back { margin-left: 16rpx; padding: 12rpx 22rpx; color: #fff; font-size: 22rpx; background: linear-gradient(135deg,#ec6d96,#df4f83); border-radius: 999rpx; }
  .load-state { padding: 26rpx; color: #a79295; text-align: center; font-size: 22rpx; }
</style>
