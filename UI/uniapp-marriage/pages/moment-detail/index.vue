<template>
  <s-layout title="详情" navbar="normal" :bgStyle="pageBackground">
    <view v-if="!snapshot" class="detail-error">
      <text class="detail-error__text">动态内容暂时无法打开，请返回列表重试</text>
      <view class="detail-error__back" @tap="goBack">返回</view>
    </view>

    <template v-else>
      <scroll-view class="detail-scroll" scroll-y @scrolltolower="loadMoreComments">
        <view class="detail-body">
          <view class="detail-author" @tap="openMember">
            <view class="detail-avatar">
              <image
                v-if="snapshot.avatar"
                class="detail-avatar__image"
                :src="snapshot.avatar"
                mode="aspectFill"
              />
              <text v-else class="detail-avatar__text">{{ avatarText(snapshot.nickname) }}</text>
            </view>
            <view class="detail-author__copy">
              <view class="detail-author__name-row">
                <text class="detail-author__name">{{ snapshot.nickname }}</text>
                <text v-if="snapshot.verifiedLabel" class="detail-author__verified">
                  {{ snapshot.verifiedLabel }}
                </text>
              </view>
              <text class="detail-author__time">{{ formatMarriageTime(snapshot.publishTime) }}</text>
            </view>
            <view v-if="snapshot.mine" class="detail-more" @tap="openMoreMenu">⋯</view>
          </view>

          <text v-if="snapshot.content" class="detail-content">{{ snapshot.content }}</text>

          <view v-if="snapshot.imageUrls.length" class="detail-images" :class="layout.className">
            <image
              v-for="(image, imageIndex) in snapshot.imageUrls"
              :key="`${snapshot.id}-${imageIndex}`"
              class="detail-image"
              :src="image"
              mode="aspectFill"
              @tap="previewImage(imageIndex)"
            />
          </view>

          <view class="detail-stats">
            {{ formatMomentCount(snapshot.likeCount) }} 赞 · {{ formatMomentCount(snapshot.commentCount) }} 评论
          </view>

          <view class="detail-actions">
            <view class="detail-action" @tap="shareMoment">分享</view>
            <view
              class="detail-action"
              :class="{ 'detail-action--liked': snapshot.liked }"
              @tap="toggleLike"
            >
              {{ snapshot.liked ? '已赞' : '点赞' }}
            </view>
            <view class="detail-action" @tap="focusComment">评论</view>
          </view>
        </view>

        <view class="comments">
          <view class="comments-title">全部评论（{{ commentTotal }}）</view>
          <view v-if="commentLoading && comments.length === 0" class="comments-state">评论加载中...</view>
          <view
            v-else-if="commentError && comments.length === 0"
            class="comments-state"
            @tap="reloadComments"
          >
            {{ commentError }}，点击重试
          </view>
          <view v-else-if="comments.length === 0" class="comments-state">还没有评论，来聊聊吧</view>
          <view
            v-for="comment in comments"
            :key="comment.id"
            class="comment-row"
            @tap="setReplyTarget(comment)"
          >
            <text class="comment-author">{{ comment.nickname || '用户' }}</text>
            <text class="comment-copy">
              <text v-if="comment.replyToNickname">回复 {{ comment.replyToNickname }}：</text>
              {{ comment.content }}
            </text>
          </view>
          <view v-if="comments.length && !commentHasMore" class="comments-state">已展示全部评论</view>
          <view v-if="comments.length && commentHasMore" class="comments-state">
            {{ commentLoading ? '加载中...' : '上拉加载更多' }}
          </view>
        </view>
      </scroll-view>

      <view class="composer">
        <view v-if="replyTargetName" class="reply-hint" @tap="clearReplyTarget">
          回复 {{ replyTargetName }} · 点击取消
        </view>
        <view class="composer-row">
          <input
            v-model="commentContent"
            class="composer-input"
            maxlength="200"
            placeholder="说点什么..."
            :focus="composerFocus"
            confirm-type="send"
            @confirm="submitComment"
          />
          <view class="composer-send" :class="{ 'composer-send--disabled': submitting }" @tap="submitComment">
            {{ submitting ? '发送中' : '发送' }}
          </view>
        </view>
      </view>
    </template>
  </s-layout>
</template>

<script setup>
  import { computed, getCurrentInstance, ref } from 'vue';
  import { onLoad, onUnload } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { showShareModal } from '@/sheep/hooks/useModal';
  import { INTERACTION_GUARD_SCENES, requireRealNameInteraction } from '@/sheep/hooks/useRealNameInteractionGuard';
  import MomentApi from '@/sheep/api/marriage/moment';
  import {
    avatarText,
    formatMomentCount,
    momentLayout,
    validateMomentSnapshot,
  } from '@/sheep/helper/marriage-moment';
  import { dedupeBy, formatMarriageTime, getPageData, getResponseData, parsePositiveId } from '@/sheep/helper/marriage';

  const pageBackground = { backgroundColor: '#f6f8fa' };
  const toast = (title) => uni.showToast({ title, icon: 'none' });

  const instance = getCurrentInstance();
  let eventChannel = null;
  try {
    const proxy = instance?.proxy;
    eventChannel =
      proxy?.$scope?.getOpenerEventChannel?.() || proxy?.getOpenerEventChannel?.() || null;
  } catch (error) {
    eventChannel = null;
  }

  const routeId = ref(0);
  const snapshot = ref(null);
  const layout = computed(() => momentLayout(snapshot.value?.imageUrls.length || 0));

  const comments = ref([]);
  const commentLoading = ref(false);
  const commentError = ref('');
  const commentPageNo = ref(1);
  const commentTotal = ref(0);
  const commentHasMore = ref(true);
  const commentPageSize = 20;
  let commentRequestId = 0;

  const commentContent = ref('');
  const composerFocus = ref(false);
  const submitting = ref(false);
  let likeInFlight = false;

  const replyParentId = ref(0);
  const replyToPartnerId = ref(0);
  const replyTargetName = ref('');

  async function loadComments(reset = false) {
    if (!snapshot.value || commentLoading.value) return false;
    if (reset) {
      commentPageNo.value = 1;
      commentHasMore.value = true;
    }
    const requestId = ++commentRequestId;
    const requestedPage = reset ? 1 : commentPageNo.value;
    commentLoading.value = true;
    commentError.value = '';
    try {
      const page = getPageData(
        await MomentApi.getCommentPage({
          momentId: snapshot.value.id,
          pageNo: requestedPage,
          pageSize: commentPageSize,
        }),
        '评论加载失败',
      );
      if (requestId !== commentRequestId || !snapshot.value) return false;
      comments.value = dedupeBy(reset ? page.list : comments.value.concat(page.list), (item) => item.id);
      commentPageNo.value = requestedPage;
      commentTotal.value = page.total;
      commentHasMore.value = comments.value.length < page.total && page.list.length > 0;
      return true;
    } catch (error) {
      if (requestId !== commentRequestId) return false;
      commentError.value = error.message || '评论加载失败';
      return false;
    } finally {
      if (requestId === commentRequestId) commentLoading.value = false;
    }
  }

  function reloadComments() {
    loadComments(true);
  }

  async function loadMoreComments() {
    if (!commentHasMore.value || commentLoading.value) return;
    const before = commentPageNo.value;
    commentPageNo.value += 1;
    const loaded = await loadComments();
    if (!loaded) commentPageNo.value = before;
  }

  async function toggleLike() {
    const item = snapshot.value;
    if (!item) return;
    if (likeInFlight) return;
    if (!item.liked && !(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.LIKE))) return;
    likeInFlight = true;
    try {
      const data = getResponseData(await MomentApi.toggleLike(item.id), '点赞失败');
      snapshot.value = {
        ...item,
        liked: !!data.liked,
        likeCount: Number(data.likeCount) || 0,
      };
      emitChange({
        id: item.id,
        liked: snapshot.value.liked,
        likeCount: snapshot.value.likeCount,
      });
    } catch (error) {
      toast(error.message || '点赞失败');
    } finally {
      likeInFlight = false;
    }
  }

  function setReplyTarget(comment) {
    replyParentId.value = parsePositiveId(comment.parentId) || parsePositiveId(comment.id);
    replyToPartnerId.value = parsePositiveId(comment.partnerId);
    replyTargetName.value = comment.nickname || '用户';
    composerFocus.value = true;
  }

  function clearReplyTarget() {
    replyParentId.value = 0;
    replyToPartnerId.value = 0;
    replyTargetName.value = '';
  }

  async function submitComment() {
    if (submitting.value || !snapshot.value) return;
    if (!(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.COMMENT))) return;
    const content = commentContent.value.trim();
    if (!content) return toast('请输入评论内容');
    submitting.value = true;
    try {
      getResponseData(
        await MomentApi.createComment({
          momentId: snapshot.value.id,
          parentId: replyParentId.value,
          replyToPartnerId: replyToPartnerId.value,
          content,
        }),
        '评论失败',
      );
      commentContent.value = '';
      clearReplyTarget();
      await loadComments(true);
      const count = Number(snapshot.value.commentCount || 0) + 1;
      snapshot.value = { ...snapshot.value, commentCount: count };
      commentTotal.value = count;
      emitChange({ id: snapshot.value.id, commentCount: count });
    } catch (error) {
      toast(error.message || '评论失败');
    } finally {
      submitting.value = false;
    }
  }

  function focusComment() {
    composerFocus.value = true;
  }

  function openMoreMenu() {
    const item = snapshot.value;
    if (!item?.mine) return;
    uni.showActionSheet({
      itemList: ['删除'],
      success: ({ tapIndex }) => {
        if (tapIndex !== 0) return;
        uni.showModal({
          title: '删除动态',
          content: '删除后无法恢复，确定继续吗？',
          success: async ({ confirm }) => {
            if (!confirm) return;
            try {
              getResponseData(await MomentApi.delete(item.id), '删除失败');
              emitChange({ id: item.id, removed: true });
              toast('已删除');
              goBack();
            } catch (error) {
              toast(error.message || '删除失败');
            }
          },
        });
      },
      fail: () => {},
    });
  }

  function emitChange(change) {
    eventChannel?.emit('moment-detail-change', change);
  }

  function previewImage(index) {
    const urls = snapshot.value?.imageUrls || [];
    if (!urls.length) return;
    uni.previewImage({ urls, current: urls[index] });
  }

  function openMember() {
    const memberId = parsePositiveId(snapshot.value?.partnerId);
    if (!memberId) return toast('该用户暂不可查看');
    sheep.$router.go('/pages/member-detail/index', { id: memberId });
  }

  function shareMoment() {
    showShareModal();
  }

  function goBack() {
    uni.navigateBack({
      fail: () => {
        uni.switchTab({ url: '/pages/dynamics/index' });
      },
    });
  }

  onLoad((query) => {
    routeId.value = parsePositiveId(query?.id);
    eventChannel?.on('moment-snapshot', (data) => {
      snapshot.value = validateMomentSnapshot(routeId.value, data?.moment);
      if (snapshot.value) {
        commentTotal.value = snapshot.value.commentCount;
        loadComments(true);
      }
    });
  });

  onUnload(() => {
    commentRequestId += 1;
  });
</script>

<style scoped>
  .detail-error {
    padding: 160rpx 60rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 40rpx;
  }
  .detail-error__text {
    color: #9d8787;
    font-size: 28rpx;
    text-align: center;
  }
  .detail-error__back {
    padding: 18rpx 60rpx;
    color: #ffffff;
    font-size: 26rpx;
    background: linear-gradient(135deg, #ec6d96, #df4f83);
    border-radius: 999rpx;
  }

  .detail-scroll {
    height: calc(100vh - env(safe-area-inset-bottom));
  }
  .detail-body {
    padding: 30rpx 30rpx 10rpx;
    background: #ffffff;
  }
  .detail-author {
    display: flex;
    align-items: center;
  }
  .detail-avatar {
    width: 88rpx;
    height: 88rpx;
    flex: none;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    color: #ffffff;
    font-size: 32rpx;
    background: #d7a2ad;
    border-radius: 50%;
  }
  .detail-avatar__image {
    width: 100%;
    height: 100%;
  }
  .detail-author__copy {
    flex: 1;
    min-width: 0;
    margin-left: 18rpx;
  }
  .detail-author__name-row {
    display: flex;
    align-items: center;
    gap: 10rpx;
  }
  .detail-author__name {
    color: #493638;
    font-size: 31rpx;
    font-weight: 700;
  }
  .detail-author__verified {
    flex: none;
    padding: 4rpx 10rpx;
    color: #bd6a7e;
    font-size: 19rpx;
    background: #fff0f3;
    border-radius: 10rpx;
  }
  .detail-author__time {
    display: block;
    margin-top: 8rpx;
    color: #ae9c9c;
    font-size: 22rpx;
  }
  .detail-more {
    flex: none;
    padding: 10rpx 12rpx;
    color: #9d8d8d;
    font-size: 36rpx;
    line-height: 1;
  }

  .detail-content {
    display: block;
    margin-top: 26rpx;
    color: #3f3233;
    font-size: 30rpx;
    line-height: 1.75;
    white-space: pre-wrap;
  }

  .detail-images {
    display: flex;
    flex-wrap: wrap;
    gap: 8rpx;
    margin-top: 22rpx;
  }
  .detail-images--single .detail-image {
    width: 74%;
    height: 400rpx;
  }
  .detail-images--pair .detail-image {
    width: calc((100% - 8rpx) / 2);
    height: 240rpx;
  }
  .detail-images--grid .detail-image {
    width: calc((100% - 16rpx) / 3);
    height: 200rpx;
  }
  .detail-image {
    border-radius: 14rpx;
  }

  .detail-stats {
    margin-top: 24rpx;
    color: #ae9c9c;
    font-size: 23rpx;
  }
  .detail-actions {
    display: flex;
    justify-content: flex-end;
    gap: 38rpx;
    margin-top: 24rpx;
    padding: 22rpx 0 8rpx;
    border-top: 1rpx solid #edf1f5;
  }
  .detail-action {
    color: #867475;
    font-size: 24rpx;
  }
  .detail-action--liked {
    color: #df4f83;
  }

  .comments {
    margin-top: 16rpx;
    padding: 24rpx 30rpx 40rpx;
    background: #ffffff;
  }
  .comments-title {
    color: #493638;
    font-size: 28rpx;
    font-weight: 700;
  }
  .comments-state {
    padding: 36rpx 0;
    color: #a08d8e;
    text-align: center;
    font-size: 24rpx;
  }
  .comment-row {
    padding: 22rpx 4rpx;
    border-bottom: 1rpx solid #f3f5f8;
  }
  .comment-author {
    color: #9c6370;
    font-size: 24rpx;
    font-weight: 700;
  }
  .comment-copy {
    display: block;
    margin-top: 8rpx;
    color: #554748;
    font-size: 26rpx;
    line-height: 1.55;
  }

  .composer {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
    background: #ffffff;
    border-top: 1rpx solid #edf1f5;
  }
  .reply-hint {
    padding: 4rpx 8rpx 12rpx;
    color: #bd6a7e;
    font-size: 24rpx;
  }
  .composer-row {
    display: flex;
    gap: 16rpx;
    align-items: center;
  }
  .composer-input {
    flex: 1;
    height: 80rpx;
    padding: 0 26rpx;
    color: #554748;
    font-size: 26rpx;
    background: #f5f7f9;
    border-radius: 999rpx;
  }
  .composer-send {
    flex: none;
    padding: 20rpx 30rpx;
    color: #ffffff;
    font-size: 25rpx;
    background: linear-gradient(135deg, #ec6d96, #df4f83);
    border-radius: 999rpx;
  }
  .composer-send--disabled {
    opacity: 0.6;
  }
</style>
