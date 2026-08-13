<template>
  <s-layout :title="pageTitle" navbar="normal" showLeftButton :bgStyle="pageBackground">
    <scroll-view
      class="list-scroll"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="reload"
      @scrolltolower="loadMore"
    >
      <view class="list-content">
        <view class="intro-card">
          <text class="intro-title">{{ summaryTitle }}</text>
          <text class="intro-description">点击头像或名称查看公开资料。</text>
        </view>
        <view v-if="!isLoggedIn" class="login-card">
          <text class="login-title">登录后查看谁看过你</text>
          <text class="login-description">访客记录与互动提醒仅在登录后展示。</text>
          <view class="primary-button" @tap="openLogin">立即登录</view>
        </view>
        <template v-else>
          <view v-if="loading && members.length === 0" class="state-card">{{ loadingText }}</view>
          <view v-else-if="errorText && members.length === 0" class="state-card" @tap="reload">
            {{ errorText }}，点击重试
          </view>
          <view v-else-if="members.length === 0" class="state-card">{{ emptyText }}</view>
          <view v-else class="member-list">
            <view v-for="(item, index) in members" :key="item.partnerId" class="member-row">
              <view class="avatar" @tap="openMember(item)">
                <image v-if="item.avatar" class="avatar-image" :src="item.avatar" mode="aspectFill" />
                <text v-else>{{ avatarText(item) }}</text>
              </view>
              <view class="member-main" @tap="openMember(item)">
                <view class="name-row">
                  <text class="member-name">{{ memberName(item) }}</text>
                  <text v-if="item.mutual" class="mutual-tag">互相关注</text>
                </view>
                <text class="member-meta">{{ memberMeta(item) }}</text>
                <text class="view-time">{{ formatMarriageTime(item.createTime) }}</text>
              </view>
              <view v-if="item.followed" class="followed-button">已关注</view>
              <view v-else class="follow-button" @tap.stop="follow(item, index)">{{ isViewMe ? '回关' : '关注' }}</view>
            </view>
          </view>
          <view v-if="members.length" class="load-state">{{ loading ? '加载中...' : hasMore ? '上拉加载更多' : '已展示全部' }}</view>
          <view v-if="errorText && members.length" class="inline-error" @tap="retryFailedRequest">
            {{ errorText }}，点击重试
          </view>
        </template>
      </view>
    </scroll-view>
  </s-layout>
</template>

<script setup>
  import { computed, ref, watch } from 'vue';
  import { onLoad, onShow, onUnload } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { showAuthModal } from '@/sheep/hooks/useModal';
  import { INTERACTION_GUARD_SCENES, requireRealNameInteraction } from '@/sheep/hooks/useRealNameInteractionGuard';
  import InteractionApi, { INTERACTION_SCENES } from '@/sheep/api/marriage/interaction';
  import { dedupeBy, formatMarriageTime, getPageData, getResponseData, parsePositiveId } from '@/sheep/helper/marriage';

  const VIEW_ME = 'view-me';
  const MY_VIEW = 'my-view';
  const pageBackground = { backgroundColor: '#fff8f7' };
  const mode = ref(VIEW_ME);
  const members = ref([]);
  const pageNo = ref(1);
  const pageSize = 20;
  const total = ref(0);
  const loading = ref(false);
  const refreshing = ref(false);
  const hasMore = ref(true);
  const errorText = ref('');
  let requestSequence = 0;

  const isViewMe = computed(() => mode.value === VIEW_ME);
  const isLoggedIn = computed(() => sheep.$store('user').isLogin);
  const pageTitle = computed(() => (isViewMe.value ? '看过我的' : '我看过的'));
  const summaryTitle = computed(() => (isViewMe.value ? `${total.value} 人看过我` : `我看过 ${total.value} 人`));
  const loadingText = computed(() => (isViewMe.value ? '加载看过我的列表中...' : '加载我看过的列表中...'));
  const emptyText = computed(() => (isViewMe.value ? '暂时还没有人看过你' : '你还没有看过任何人'));
  const toast = (title) => uni.showToast({ title, icon: 'none' });

  // 未登录时不再自动弹出登录弹窗，改为页面内展示“立即登录”入口，避免反复弹窗。
  function requireLogin() {
    return sheep.$store('user').isLogin;
  }

  function openLogin() {
    showAuthModal();
  }

  async function load(reset = false, refresh = false) {
    if (!requireLogin()) {
      refreshing.value = false;
      return false;
    }
    if (loading.value && !reset) return false;
    const requestId = ++requestSequence;
    const requestedMode = mode.value;
    const requestedPage = reset ? 1 : pageNo.value;
    loading.value = true;
    if (refresh) refreshing.value = true;
    errorText.value = '';
    try {
      const response =
        requestedMode === VIEW_ME
          ? await InteractionApi.getViewMePage({ pageNo: requestedPage, pageSize })
          : await InteractionApi.getMyViewPage({ pageNo: requestedPage, pageSize });
      const page = getPageData(response, `${pageTitle.value}列表加载失败`);
      if (requestId !== requestSequence || requestedMode !== mode.value) return false;
      const nextMembers = reset ? page.list : members.value.concat(page.list);
      members.value = dedupeBy(nextMembers, (item) => parsePositiveId(item?.partnerId));
      total.value = page.total;
      pageNo.value = requestedPage;
      hasMore.value = members.value.length < page.total && page.list.length > 0;
      return true;
    } catch (error) {
      if (requestId !== requestSequence || requestedMode !== mode.value) return false;
      errorText.value = error.message || `${pageTitle.value}列表加载失败`;
      return false;
    } finally {
      if (requestId === requestSequence) {
        loading.value = false;
        refreshing.value = false;
      }
    }
  }

  function reload() {
    hasMore.value = true;
    load(true, true);
  }

  async function loadMore() {
    if (loading.value || !hasMore.value) return;
    const previousPage = pageNo.value;
    pageNo.value += 1;
    const loaded = await load();
    if (!loaded && requestSequence > 0) pageNo.value = previousPage;
  }

  function retryFailedRequest() {
    if (pageNo.value <= 1) reload();
    else loadMore();
  }

  async function follow(item, index) {
    const partnerId = parsePositiveId(item?.partnerId);
    if (!partnerId) return toast('该用户暂不可关注');
    if (!(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.FOLLOW))) return;
    try {
      const result = getResponseData(await InteractionApi.follow(partnerId), '关注失败');
      members.value[index] = { ...item, followed: true, mutual: !!result?.mutual };
      toast(result?.mutual ? '已互相关注' : isViewMe.value ? '已回关' : '已关注');
    } catch (error) {
      toast(error.message || '关注失败');
    }
  }

  function markViewMeSummaryRead() {
    if (!isViewMe.value || !sheep.$store('user').isLogin) return;
    InteractionApi.readScene(INTERACTION_SCENES.VIEW_ME_SUMMARY)
      .then((response) => getResponseData(response, '标记浏览提醒已读失败'))
      .then(() => sheep.$store('social').refreshUnread())
      .catch(() => {});
  }

  function openMember(item) {
    const id = parsePositiveId(item?.partnerId);
    if (id) sheep.$router.go('/pages/member-detail/index', { id });
  }

  const memberName = (item) => String(item?.nickname || '').trim() || '处佳缘用户';
  const avatarText = (item) => memberName(item).slice(0, 1);
  const memberMeta = (item) =>
    [Number(item?.age) > 0 ? `${item.age}岁` : '', item?.city, item?.bio].filter(Boolean).join(' · ') || '资料待完善';

  onLoad((options) => {
    mode.value = options?.mode === MY_VIEW ? MY_VIEW : VIEW_ME;
  });
  onShow(() => {
    markViewMeSummaryRead();
    reload();
  });
  onUnload(() => {
    requestSequence += 1;
  });

  // 从页面内“立即登录”完成登录后，自动加载列表。
  watch(isLoggedIn, (loggedIn, wasLoggedIn) => {
    if (loggedIn && !wasLoggedIn) reload();
  });
</script>

<style scoped>
  .list-scroll { height: calc(100vh - 90rpx); background: #fff8f7; }
  .list-content { padding: 24rpx; }
  .intro-card, .state-card, .member-list, .inline-error, .login-card { background: #fff; border: 1rpx solid rgba(194,132,145,.13); border-radius: 28rpx; }
  .intro-card { padding: 32rpx; }
  .intro-title { display: block; color: #4e3a3c; font-size: 32rpx; font-weight: 760; }
  .intro-description { display: block; margin-top: 12rpx; color: #9f888b; font-size: 24rpx; }
  .login-card { margin-top: 20rpx; padding: 54rpx 38rpx; text-align: center; }
  .login-title { display: block; color: #4f393c; font-size: 32rpx; font-weight: 750; }
  .login-description { display: block; margin: 16rpx 0 28rpx; color: #9b8588; font-size: 25rpx; }
  .primary-button { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 42rpx; color: #fff; background: linear-gradient(135deg,#ec6d96,#df4f83); border-radius: 999rpx; }
  .state-card { margin-top: 20rpx; padding: 76rpx 30rpx; color: #9d8789; text-align: center; }
  .member-list { margin-top: 20rpx; overflow: hidden; }
  .member-row { display: flex; align-items: center; padding: 26rpx; border-bottom: 1rpx solid #f5eded; }
  .member-row:last-child { border-bottom: 0; }
  .avatar { width: 92rpx; height: 92rpx; display: flex; align-items: center; justify-content: center; flex: none; overflow: hidden; color: #fff; background: #b997ac; border-radius: 28rpx; }
  .avatar-image { width: 100%; height: 100%; }
  .member-main { flex: 1; min-width: 0; margin-left: 18rpx; }
  .name-row { display: flex; align-items: center; gap: 10rpx; }
  .member-name { color: #4f3d3f; font-size: 27rpx; font-weight: 700; }
  .mutual-tag { padding: 4rpx 9rpx; color: #9870a8; font-size: 18rpx; background: #f2eafa; border-radius: 9rpx; }
  .member-meta, .view-time { display: block; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
  .member-meta { margin-top: 8rpx; color: #927f82; font-size: 22rpx; }
  .view-time { margin-top: 7rpx; color: #b2a0a2; font-size: 19rpx; }
  .follow-button, .followed-button { margin-left: 14rpx; padding: 12rpx 20rpx; font-size: 21rpx; border-radius: 999rpx; }
  .follow-button { color: #fff; background: linear-gradient(135deg,#ec6d96,#df4f83); }
  .followed-button { color: #9e8b8e; background: #f5eff0; }
  .load-state { padding: 26rpx; color: #a79295; text-align: center; font-size: 22rpx; }
  .inline-error { margin-top: 16rpx; padding: 24rpx; color: #9d8789; text-align: center; font-size: 22rpx; }
</style>
