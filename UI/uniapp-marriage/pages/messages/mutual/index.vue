<template>
  <s-layout title="互相关注" navbar="normal" showLeftButton :bgStyle="pageBackground">
    <scroll-view class="list-scroll" scroll-y refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="loadContacts">
      <view class="list-content">
        <view class="intro-card">
          <text class="intro-title">可以开始聊天的人</text>
          <text class="intro-description">互相关注后即可直接发送私信。</text>
        </view>
        <view v-if="!isLoggedIn" class="login-card">
          <text class="login-title">登录后查看互相关注</text>
          <text class="login-description">互相关注的人会在登录后展示。</text>
          <view class="primary-button" @tap="openLogin">立即登录</view>
        </view>
        <template v-else>
          <view v-if="loading" class="state-card">联系人加载中...</view>
          <view v-else-if="errorText" class="state-card" @tap="loadContacts">{{ errorText }}，点击重试</view>
          <view v-else-if="contacts.length === 0" class="state-card">暂无互相关注联系人</view>
          <view v-else class="contact-list">
            <view v-for="item in contacts" :key="item.partnerId" class="contact-row" @tap="openChat(item)">
              <view class="avatar">
                <image v-if="item.avatar" class="avatar-image" :src="item.avatar" mode="aspectFill" />
                <text v-else>{{ String(item.nickname || '缘').slice(0, 1) }}</text>
              </view>
              <view class="contact-main">
                <text class="contact-name">{{ item.nickname || '处佳缘用户' }}</text>
                <text class="contact-meta">{{ memberMeta(item) }}</text>
              </view>
              <view class="chat-action">聊天</view>
            </view>
          </view>
        </template>
      </view>
    </scroll-view>
  </s-layout>
</template>

<script setup>
  import { computed, ref, watch } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { showAuthModal } from '@/sheep/hooks/useModal';
  import InteractionApi, { INTERACTION_SCENES } from '@/sheep/api/marriage/interaction';
  import { getPageData, getResponseData, parsePositiveId } from '@/sheep/helper/marriage';

  const pageBackground = { backgroundColor: '#fff8f7' };
  const isLoggedIn = computed(() => sheep.$store('user').isLogin);
  const contacts = ref([]);
  const loading = ref(false);
  const refreshing = ref(false);
  const errorText = ref('');

  // 未登录时不再自动弹出登录弹窗，改为页面内展示“立即登录”入口，避免反复弹窗。
  async function loadContacts() {
    if (!isLoggedIn.value) {
      contacts.value = [];
      return;
    }
    loading.value = true;
    refreshing.value = true;
    errorText.value = '';
    try {
      const page = getPageData(await InteractionApi.getMutualFollowPage(), '联系人加载失败');
      contacts.value = page.list.filter((item) => item.mutual);
      getResponseData(await InteractionApi.readScene(INTERACTION_SCENES.MUTUAL_FOLLOW), '标记已读失败');
      sheep.$store('social').refreshUnread();
    } catch (error) {
      errorText.value = error.message || '联系人加载失败';
    } finally {
      loading.value = false;
      refreshing.value = false;
    }
  }

  function memberMeta(item) {
    return [Number(item.age) > 0 ? `${item.age}岁` : '', item.city, item.bio].filter(Boolean).join(' · ') || '资料待完善';
  }

  function openChat(item) {
    const peerId = parsePositiveId(item.partnerId);
    if (!peerId) return uni.showToast({ title: '聊天对象不可用', icon: 'none' });
    sheep.$router.go('/pages/messages/chat/index', {
      peerId,
      nickname: encodeURIComponent(item.nickname || ''),
      avatar: encodeURIComponent(item.avatar || ''),
    });
  }

  function openLogin() {
    showAuthModal();
  }

  onShow(() => loadContacts());

  // 从页面内“立即登录”完成登录后，自动加载联系人。
  watch(isLoggedIn, (loggedIn, wasLoggedIn) => {
    if (loggedIn && !wasLoggedIn) loadContacts();
  });
</script>

<style scoped>
  .list-scroll { height: calc(100vh - 90rpx); background: #fff8f7; }
  .list-content { padding: 24rpx; }
  .intro-card, .state-card, .contact-list, .login-card { background: #fff; border: 1rpx solid rgba(194,132,145,.13); border-radius: 28rpx; }
  .intro-card { padding: 32rpx; }
  .intro-title { display: block; color: #4e3a3c; font-size: 32rpx; font-weight: 760; }
  .intro-description { display: block; margin-top: 12rpx; color: #9f888b; font-size: 24rpx; }
  .login-card { margin-top: 20rpx; padding: 54rpx 38rpx; text-align: center; }
  .login-title { display: block; color: #4f393c; font-size: 32rpx; font-weight: 750; }
  .login-description { display: block; margin: 16rpx 0 28rpx; color: #9b8588; font-size: 25rpx; }
  .primary-button { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 42rpx; color: #fff; background: linear-gradient(135deg,#ec6d96,#df4f83); border-radius: 999rpx; }
  .state-card { margin-top: 20rpx; padding: 76rpx 30rpx; color: #9d8789; text-align: center; }
  .contact-list { margin-top: 20rpx; overflow: hidden; }
  .contact-row { display: flex; align-items: center; padding: 26rpx; border-bottom: 1rpx solid #f5eded; }
  .contact-row:last-child { border-bottom: 0; }
  .avatar { width: 88rpx; height: 88rpx; display: flex; align-items: center; justify-content: center; flex: none; overflow: hidden; color: #fff; background: #94ac98; border-radius: 28rpx; }
  .avatar-image { width: 100%; height: 100%; }
  .contact-main { flex: 1; min-width: 0; margin-left: 19rpx; }
  .contact-name { display: block; color: #4f3d3f; font-size: 28rpx; font-weight: 700; }
  .contact-meta { display: block; margin-top: 8rpx; overflow: hidden; color: #978487; font-size: 22rpx; white-space: nowrap; text-overflow: ellipsis; }
  .chat-action { padding: 12rpx 22rpx; color: #fff; font-size: 22rpx; background: #83a188; border-radius: 999rpx; }
</style>
