<template>
  <s-layout title="动态" tabbar="/pages/dynamics/index" navbar="normal" :bgStyle="pageBackground">
    <view class="dynamics-page">
      <view class="feed-header">
        <view class="filter-tabs">
          <view
            v-for="(label, index) in filters"
            :key="label"
            class="filter-tab"
            :class="{ active: activeFilter === index }"
            @tap="changeFilter(index)"
          >
            {{ label }}
          </view>
        </view>
        <view class="publish-button" @tap="openPublish">发布</view>
      </view>

      <scroll-view
        class="feed-scroll"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="refreshFeed"
        @scrolltolower="loadMore"
      >
        <view class="feed-content">
          <view v-if="loading && moments.length === 0" class="state-card">动态加载中...</view>
          <view v-else-if="errorText && moments.length === 0" class="state-card" @tap="refreshFeed">
            {{ errorText }}，点击重试
          </view>
          <view v-else-if="moments.length === 0" class="state-card">
            {{ activeFilter === 0 ? '暂时还没有动态' : '关注的人还没有发布动态' }}
          </view>

          <view v-for="item in moments" :key="item.id" class="moment-row">
            <s-moment-item
              :item="item"
              mode="feed"
              @open-detail="openMomentDetail"
              @open-member="openMember"
              @follow="followAuthor"
              @like="toggleLike"
              @comment="openMomentDetail"
              @share="shareMoment"
              @more="openMoreMenu"
            />
          </view>

          <view v-if="moments.length" class="load-state">
            {{ loading ? '加载中...' : hasMore ? '上拉加载更多' : '已展示全部' }}
          </view>
          <view v-if="errorText && moments.length" class="inline-error" @tap="loadMore">
            {{ errorText }}，点击重试
          </view>
        </view>
      </scroll-view>
    </view>

    <view v-if="publishVisible" class="modal-mask" @tap="closePublish">
      <view class="compose-panel" @tap.stop>
        <view class="panel-title-row">
          <text class="panel-title">发布动态</text>
          <text class="panel-close" @tap="closePublish">关闭</text>
        </view>
        <textarea
          v-model="publishContent"
          class="compose-input"
          maxlength="500"
          placeholder="分享此刻的心情和故事..."
        />
        <view class="compose-images">
          <view v-for="(image, index) in publishImages" :key="image" class="compose-image-wrap">
            <image class="compose-image" :src="image" mode="aspectFill" />
            <view class="remove-image" @tap="publishImages.splice(index, 1)">×</view>
          </view>
          <view v-if="publishImages.length < 9" class="add-image" @tap="choosePublishImages">+ 图片</view>
        </view>
        <button class="submit-button" :loading="publishing" :disabled="publishing" @tap="submitPublish">
          发布
        </button>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref } from 'vue';
  import { onShow, onUnload } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { showAuthModal, showShareModal } from '@/sheep/hooks/useModal';
  import { INTERACTION_GUARD_SCENES, requireRealNameInteraction } from '@/sheep/hooks/useRealNameInteractionGuard';
  import MomentApi from '@/sheep/api/marriage/moment';
  import InteractionApi from '@/sheep/api/marriage/interaction';
  import FileApi from '@/sheep/api/infra/file';
  import { mergeMomentChange } from '@/sheep/helper/marriage-moment';
  import {
    dedupeBy,
    getPageData,
    getResponseData,
    parsePositiveId,
  } from '@/sheep/helper/marriage';

  uni.hideTabBar({ fail: () => {} });

  const pageBackground = { backgroundColor: '#fff8f7' };
  const filters = ['推荐', '关注'];
  const activeFilter = ref(0);
  const moments = ref([]);
  const pageNo = ref(1);
  const pageSize = 10;
  const total = ref(0);
  const loading = ref(false);
  const refreshing = ref(false);
  const hasMore = ref(true);
  const errorText = ref('');
  let feedRequestId = 0;

  const publishVisible = ref(false);
  const publishContent = ref('');
  const publishImages = ref([]);
  const publishing = ref(false);

  const isLoggedIn = () => sheep.$store('user').isLogin;
  const toast = (title) => uni.showToast({ title, icon: 'none' });

  function requireLogin() {
    if (isLoggedIn()) return true;
    showAuthModal();
    return false;
  }

  async function loadFeed({ reset = false, refresh = false } = {}) {
    if (loading.value && !reset) return;
    const requestId = ++feedRequestId;
    const requestedFilter = activeFilter.value;
    const requestedPage = reset ? 1 : pageNo.value;
    loading.value = true;
    if (refresh) refreshing.value = true;
    errorText.value = '';
    try {
      const page = getPageData(
        await MomentApi.getPage({
          pageNo: requestedPage,
          pageSize,
          following: requestedFilter === 1,
        }),
        '动态加载失败',
      );
      if (requestId !== feedRequestId || requestedFilter !== activeFilter.value) return;
      const nextList = reset ? page.list : moments.value.concat(page.list);
      moments.value = dedupeBy(nextList, (item) => item.id);
      pageNo.value = requestedPage;
      total.value = page.total;
      hasMore.value = moments.value.length < page.total && page.list.length > 0;
      return true;
    } catch (error) {
      if (requestId !== feedRequestId) return;
      errorText.value = error.message || '动态加载失败';
      return false;
    } finally {
      if (requestId === feedRequestId) {
        loading.value = false;
        refreshing.value = false;
      }
    }
  }

  function refreshFeed() {
    hasMore.value = true;
    loadFeed({ reset: true, refresh: true });
  }

  async function loadMore() {
    if (loading.value || !hasMore.value) return;
    const previousPage = pageNo.value;
    pageNo.value += 1;
    const loaded = await loadFeed();
    if (!loaded) pageNo.value = previousPage;
  }

  function changeFilter(index) {
    if (index === activeFilter.value) return;
    if (index === 1 && !requireLogin()) return;
    activeFilter.value = index;
    moments.value = [];
    pageNo.value = 1;
    total.value = 0;
    hasMore.value = true;
    loadFeed({ reset: true });
  }

  async function followAuthor(item) {
    if (!(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.FOLLOW))) return;
    try {
      getResponseData(await InteractionApi.follow(item.partnerId), '关注失败');
      moments.value = moments.value.map((moment) =>
        moment.id === item.id ? { ...moment, followed: true } : moment,
      );
      toast('已关注');
    } catch (error) {
      toast(error.message || '关注失败');
    }
  }

  async function toggleLike(item) {
    if (!item.liked && !(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.LIKE))) return;
    try {
      const data = getResponseData(await MomentApi.toggleLike(item.id), '点赞失败');
      moments.value = moments.value.map((moment) =>
        moment.id === item.id
          ? { ...moment, liked: !!data.liked, likeCount: Number(data.likeCount) || 0 }
          : moment,
      );
    } catch (error) {
      toast(error.message || '点赞失败');
    }
  }

  function removeMoment(item) {
    uni.showModal({
      title: '删除动态',
      content: '删除后无法恢复，确定继续吗？',
      success: async ({ confirm }) => {
        if (!confirm) return;
        try {
          getResponseData(await MomentApi.delete(item.id), '删除失败');
          moments.value = mergeMomentChange(moments.value, { id: item.id, removed: true });
          total.value = Math.max(0, total.value - 1);
          toast('已删除');
        } catch (error) {
          toast(error.message || '删除失败');
        }
      },
    });
  }

  function openMoreMenu(item) {
    uni.showActionSheet({
      itemList: ['删除'],
      success: ({ tapIndex }) => {
        if (tapIndex !== 0) return;
        removeMoment(item);
      },
      fail: () => {},
    });
  }

  async function openPublish() {
    if (!(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.PUBLISH))) return;
    publishVisible.value = true;
  }

  function closePublish() {
    if (!publishing.value) publishVisible.value = false;
  }

  function choosePublishImages() {
    uni.chooseImage({
      count: 9 - publishImages.value.length,
      sizeType: ['compressed'],
      success: ({ tempFilePaths }) => {
        publishImages.value = publishImages.value.concat(tempFilePaths || []).slice(0, 9);
      },
    });
  }

  async function uploadImages(paths) {
    const urls = [];
    for (const path of paths) {
      const result = await FileApi.uploadFile(path, 'marriage/moment');
      urls.push(getResponseData(result, '图片上传失败'));
    }
    return urls;
  }

  async function submitPublish() {
    if (publishing.value) return;
    const content = publishContent.value.trim();
    if (!content && publishImages.value.length === 0) {
      toast('请输入内容或选择图片');
      return;
    }
    if (!(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.PUBLISH))) return;
    publishing.value = true;
    try {
      const imageUrls = await uploadImages(publishImages.value);
      getResponseData(await MomentApi.create({ content, imageUrls }), '发布失败');
      publishContent.value = '';
      publishImages.value = [];
      publishVisible.value = false;
      activeFilter.value = 0;
      await loadFeed({ reset: true });
      toast('发布成功');
    } catch (error) {
      toast(error.message || '发布失败');
    } finally {
      publishing.value = false;
    }
  }

  function openMomentDetail(item) {
    const id = parsePositiveId(item?.id);
    if (!id) return toast('该动态暂不可查看');
    uni.navigateTo({
      url: `/pages/moment-detail/index?id=${id}`,
      events: {
        'moment-detail-change': handleDetailChange,
      },
      success: (res) => {
        res.eventChannel.emit('moment-snapshot', { moment: item });
      },
    });
  }

  function handleDetailChange(data) {
    const next = mergeMomentChange(moments.value, data);
    if (next !== moments.value) moments.value = next;
  }

  function openMember(item) {
    const memberId = parsePositiveId(item?.partnerId);
    if (!memberId) return toast('该用户暂不可查看');
    sheep.$router.go('/pages/member-detail/index', { id: memberId });
  }

  function shareMoment() {
    showShareModal();
  }

  onShow(() => {
    if (activeFilter.value === 1 && !isLoggedIn()) activeFilter.value = 0;
    if (moments.value.length === 0) loadFeed({ reset: true });
  });

  onUnload(() => {
    feedRequestId += 1;
  });
</script>

<style scoped>
  .dynamics-page { height: calc(100vh - 90rpx); display: flex; flex-direction: column; background: #f6f8fa; }
  .feed-header { display: flex; align-items: center; justify-content: space-between; padding: 24rpx 28rpx 18rpx; background: rgba(255,255,255,.96); }
  .filter-tabs { display: flex; gap: 34rpx; }
  .filter-tab { position: relative; padding: 12rpx 4rpx; color: #9b8585; font-size: 30rpx; font-weight: 600; }
  .filter-tab.active { color: #5e3b3d; }
  .filter-tab.active::after { content: ''; position: absolute; left: 20%; right: 20%; bottom: 0; height: 5rpx; border-radius: 5rpx; background: #e65f8e; }
  .publish-button { padding: 13rpx 28rpx; color: #fff; font-size: 25rpx; background: linear-gradient(135deg,#ec6d96,#df4f83); border-radius: 999rpx; }
  .feed-scroll { flex: 1; min-height: 0; }
  .feed-content { padding: 16rpx 0 150rpx; }
  .moment-row { border-bottom: 16rpx solid #f6f8fa; }
  .state-card, .inline-error { padding: 70rpx 30rpx; color: #9d8787; text-align: center; background: #fff; border-radius: 28rpx; margin: 16rpx 24rpx; }
  .inline-error { margin-top: 16rpx; padding: 24rpx; }
  .load-state { padding: 24rpx; color: #ab9696; text-align: center; font-size: 23rpx; }
  .modal-mask { position: fixed; z-index: 1000; inset: 0; display: flex; align-items: flex-end; background: rgba(31,20,23,.46); }
  .compose-panel { width: 100%; padding: 30rpx 28rpx calc(30rpx + env(safe-area-inset-bottom)); box-sizing: border-box; background: #fff; border-radius: 36rpx 36rpx 0 0; }
  .panel-title-row { display: flex; align-items: center; justify-content: space-between; }
  .panel-title { color: #4e393b; font-size: 32rpx; font-weight: 700; }
  .panel-close { color: #a48f90; font-size: 25rpx; }
  .compose-input { width: 100%; height: 220rpx; margin-top: 24rpx; padding: 22rpx; box-sizing: border-box; color: #4f4243; font-size: 28rpx; background: #fff8f8; border-radius: 22rpx; }
  .compose-images { display: flex; flex-wrap: wrap; gap: 14rpx; margin-top: 20rpx; }
  .compose-image-wrap, .add-image { position: relative; width: 150rpx; height: 150rpx; }
  .compose-image { width: 100%; height: 100%; border-radius: 18rpx; }
  .remove-image { position: absolute; top: -10rpx; right: -10rpx; width: 38rpx; height: 38rpx; color: #fff; text-align: center; line-height: 36rpx; background: #554245; border-radius: 50%; }
  .add-image { display: flex; align-items: center; justify-content: center; color: #a07f84; font-size: 25rpx; background: #fff3f5; border: 1rpx dashed #d7abb4; border-radius: 18rpx; }
  .submit-button { margin-top: 28rpx; color: #fff; font-size: 28rpx; background: linear-gradient(135deg,#ec6d96,#df4f83); border: 0; border-radius: 999rpx; }
</style>
