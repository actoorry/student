<template>
  <s-layout title="我的动态" navbar="normal" :bgStyle="{ backgroundColor: '#f6f8fa' }">
    <scroll-view
      class="page"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
      @scrolltolower="loadMore"
    >
      <view class="intro">展示我发布过的全部动态</view>
      <view v-if="!loggedIn" class="state" @tap="showAuthModal">登录后查看我的动态</view>
      <view v-else-if="loading && !moments.length" class="state">动态加载中...</view>
      <view v-else-if="errorText && !moments.length" class="state" @tap="refresh">{{ errorText }}，点击重试</view>
      <view v-else-if="!moments.length" class="state">还没有发布动态\n去动态页发布第一条生活片段吧</view>
      <view v-for="item in moments" :key="item.id" class="moment-row">
        <s-moment-item
          :item="item"
          mode="mine"
          @open-detail="openMomentDetail"
          @open-member="openMember"
          @like="toggleLike"
          @comment="openMomentDetail"
          @share="shareMoment"
          @more="openMoreMenu"
        />
      </view>
      <view v-if="loggedIn && moments.length" class="state small">{{ loading ? '加载中...' : hasMore ? '上拉加载更多' : '已展示全部' }}</view>
    </scroll-view>
  </s-layout>
</template>
<script setup>
  import { computed, ref } from 'vue';
  import { onShow, onUnload } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { showAuthModal, showShareModal } from '@/sheep/hooks/useModal';
  import { INTERACTION_GUARD_SCENES, requireRealNameInteraction } from '@/sheep/hooks/useRealNameInteractionGuard';
  import MomentApi from '@/sheep/api/marriage/moment';
  import { mergeMomentChange } from '@/sheep/helper/marriage-moment';
  import { dedupeBy, getPageData, getResponseData, parsePositiveId } from '@/sheep/helper/marriage';

  const moments = ref([]);
  const loading = ref(false);
  const refreshing = ref(false);
  const hasMore = ref(true);
  const errorText = ref('');
  const pageNo = ref(1);
  const pageSize = 10;
  let generation = 0;

  const loggedIn = computed(() => sheep.$store('user').isLogin);
  const toast = (title) => uni.showToast({ title, icon: 'none' });

  async function load({ reset = false, refresh = false } = {}) {
    if (!loggedIn.value || (loading.value && !reset)) return false;
    const id = ++generation;
    const requestedPage = reset ? 1 : pageNo.value;
    loading.value = true;
    refreshing.value = refresh;
    errorText.value = '';
    try {
      const page = getPageData(
        await MomentApi.getMyPage({ pageNo: requestedPage, pageSize }),
        '动态加载失败',
      );
      if (id !== generation) return false;
      moments.value = dedupeBy(reset ? page.list : moments.value.concat(page.list), (item) => item.id);
      pageNo.value = requestedPage;
      hasMore.value = moments.value.length < page.total && page.list.length > 0;
      return true;
    } catch (error) {
      if (id === generation) errorText.value = error.message || '动态加载失败';
      return false;
    } finally {
      if (id === generation) {
        loading.value = false;
        refreshing.value = false;
      }
    }
  }

  function refresh() {
    hasMore.value = true;
    load({ reset: true, refresh: true });
  }

  async function loadMore() {
    if (loading.value || !hasMore.value) return;
    const before = pageNo.value;
    pageNo.value += 1;
    if (!(await load())) pageNo.value = before;
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
    if (loggedIn.value && !moments.value.length) load({ reset: true });
  });

  onUnload(() => {
    generation += 1;
  });
</script>
<style scoped>
  .page { height: 100vh; padding: 24rpx 0; box-sizing: border-box; background: #f6f8fa; }
  .intro { padding: 12rpx 28rpx 24rpx; color: #947d7f; font-size: 26rpx; }
  .state { margin: 80rpx 24rpx 0; padding: 48rpx 24rpx; color: #9d8787; text-align: center; white-space: pre-line; background: #fff; border-radius: 28rpx; }
  .small { margin: 24rpx; padding: 18rpx; background: transparent; }
  .moment-row { border-bottom: 16rpx solid #f6f8fa; }
</style>
