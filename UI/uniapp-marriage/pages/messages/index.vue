<template>
  <s-layout
    title="消息"
    tabbar="/pages/messages/index"
    navbar="normal"
    :bgStyle="pageBackground"
    fit-viewport
  >
    <scroll-view
      class="messages-scroll"
      :style="viewportLayout.scroll"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="loadMessages"
    >
      <view class="messages-content">
        <view class="message-hero">
          <text class="hero-eyebrow">处佳缘 · 消息中心</text>
          <text class="hero-title">不错过每一次心动</text>
          <text class="hero-description">关注、访客和私信都集中在这里。</text>
        </view>

        <view v-if="!isLoggedIn" class="login-card">
          <text class="login-title">登录后查看专属消息</text>
          <text class="login-description">你的私信和互动提醒仅在登录后展示。</text>
          <view class="primary-button" @tap="openLogin">立即登录</view>
        </view>

        <template v-else>
          <view class="section-heading">
            <text class="section-title">互动提醒</text>
            <text v-if="interactionUnread > 0" class="read-action" @tap="markAllInteractionRead">全部已读</text>
          </view>
          <view class="quick-grid">
            <view v-for="entry in quickEntries" :key="entry.path" class="quick-card" @tap="openPath(entry.path)">
              <view class="quick-icon" :style="{ background: entry.color }">{{ entry.icon }}</view>
              <text class="quick-label">{{ entry.label }}</text>
              <text class="quick-hint">{{ entry.hint }}</text>
              <text v-if="entry.badge > 0" class="badge">{{ entry.badge > 99 ? '99+' : entry.badge }}</text>
            </view>
          </view>

          <view class="section-heading conversation-heading">
            <text class="section-title">最近私信</text>
            <text class="section-hint">{{ privateUnread > 0 ? `${privateUnread} 条未读` : '保持联系' }}</text>
          </view>

          <view v-if="loading && conversations.length === 0" class="state-card">消息加载中...</view>
          <view v-else-if="errorText && conversations.length === 0" class="state-card" @tap="loadMessages">
            {{ errorText }}，点击重试
          </view>
          <view v-else-if="conversations.length === 0" class="state-card" @tap="openMutual">
            暂时还没有私信，去互相关注列表开启聊天
          </view>
          <view v-else class="conversation-list">
            <view v-for="conversation in conversations" :key="conversation.peerId" class="conversation-row" @tap="openConversation(conversation)">
              <view class="conversation-avatar">
                <image v-if="conversation.peerAvatar" class="avatar-image" :src="conversation.peerAvatar" mode="aspectFill" />
                <text v-else>{{ avatarText(conversation.peerNickname) }}</text>
              </view>
              <view class="conversation-main">
                <view class="conversation-top">
                  <text class="conversation-name">{{ conversation.peerNickname || '处佳缘用户' }}</text>
                  <text class="conversation-time">{{ formatMarriageTime(conversation.lastSendTime) }}</text>
                </view>
                <view class="conversation-bottom">
                  <text class="conversation-preview">{{ parseMessageText(conversation.lastMessage) || '点击开始聊天' }}</text>
                  <text v-if="conversation.unreadCount > 0" class="conversation-badge">
                    {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
                  </text>
                </view>
              </view>
            </view>
          </view>
          <view v-if="errorText && conversations.length" class="inline-error" @tap="loadMessages">
            {{ errorText }}，点击重试
          </view>
        </template>
      </view>
    </scroll-view>
  </s-layout>
</template>

<script setup>
  import { computed, ref, watch } from 'vue';
  import { onShow, onUnload } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { showAuthModal } from '@/sheep/hooks/useModal';
  import InteractionApi, { INTERACTION_SCENES } from '@/sheep/api/marriage/interaction';
  import MessageApi from '@/sheep/api/marriage/message';
  import {
    addImMessageListener,
    addInteractionNotificationListener,
    connectImWebSocket,
    removeImMessageListener,
    removeInteractionNotificationListener,
  } from '@/sheep/api/marriage/imSocket';
  import {
    dedupeBy,
    formatMarriageTime,
    getResponseData,
    parseMessageText,
    parsePositiveId,
  } from '@/sheep/helper/marriage';
  import { getViewportLayoutStyles } from '@/sheep/helper/viewport-layout.mjs';

  uni.hideTabBar({ fail: () => {} });
  const pageBackground = { backgroundColor: '#fff8f7' };
  const viewportLayout = getViewportLayoutStyles(true);
  const loading = ref(false);
  const refreshing = ref(false);
  const errorText = ref('');
  const unreadGroups = ref([]);
  const conversations = ref([]);
  const privateUnread = ref(0);
  const receivedMessageIds = new Set();

  const isLoggedIn = computed(() => sheep.$store('user').isLogin);
  const interactionUnread = computed(() =>
    unreadGroups.value.reduce((sum, item) => sum + Math.max(0, Number(item?.unreadCount) || 0), 0),
  );
  const countScene = (scene) =>
    Number(unreadGroups.value.find((item) => item.scene === scene)?.unreadCount) || 0;
  const quickEntries = computed(() => [
    {
      label: '关注提醒',
      hint: '谁关注了你',
      icon: '关',
      color: '#f9dfe6',
      badge: countScene(INTERACTION_SCENES.FOLLOW),
      path: '/pages/messages/follow/index',
    },
    {
      label: '互相关注',
      hint: '可以开始聊天',
      icon: '缘',
      color: '#e5efe7',
      badge: countScene(INTERACTION_SCENES.MUTUAL_FOLLOW),
      path: '/pages/messages/mutual/index',
    },
    {
      label: '谁看过我',
      hint: '看看新的访客',
      icon: '访',
      color: '#eee9f5',
      badge: countScene(INTERACTION_SCENES.VIEW_ME_SUMMARY),
      path: '/pages/mine-view/index?mode=view-me',
    },
  ]);

  const toast = (title) => uni.showToast({ title, icon: 'none' });

  async function loadMessages() {
    if (!isLoggedIn.value) {
      unreadGroups.value = [];
      conversations.value = [];
      privateUnread.value = 0;
      sheep.$store('social').setUnreadCounts(0, 0);
      return;
    }
    loading.value = true;
    refreshing.value = true;
    errorText.value = '';
    try {
      const [groupResponse, unreadResponse, conversationResponse] = await Promise.all([
        InteractionApi.getUnreadCountGroup(),
        MessageApi.getUnreadCount(),
        MessageApi.getConversationList(),
      ]);
      unreadGroups.value = getResponseData(groupResponse, '互动提醒加载失败') || [];
      privateUnread.value = Number(getResponseData(unreadResponse, '私信未读加载失败')) || 0;
      conversations.value = dedupeBy(
        getResponseData(conversationResponse, '会话加载失败') || [],
        (item) => item.peerId,
      );
      sheep.$store('social').setUnreadCounts(interactionUnread.value, privateUnread.value);
    } catch (error) {
      errorText.value = error.message || '消息加载失败';
    } finally {
      loading.value = false;
      refreshing.value = false;
    }
  }

  async function markAllInteractionRead() {
    try {
      getResponseData(await InteractionApi.readAll(), '操作失败');
      unreadGroups.value = unreadGroups.value.map((item) => ({ ...item, unreadCount: 0 }));
      sheep.$store('social').setUnreadCounts(0, privateUnread.value);
      toast('已全部标记为已读');
    } catch (error) {
      toast(error.message || '操作失败');
    }
  }

  function openLogin() {
    showAuthModal();
  }

  function openPath(path) {
    sheep.$router.go(path);
  }

  function openMutual() {
    openPath('/pages/messages/mutual/index');
  }

  function openConversation(conversation) {
    const peerId = parsePositiveId(conversation.peerId);
    if (!peerId) return toast('聊天对象不可用');
    sheep.$router.go('/pages/messages/chat/index', {
      peerId,
      nickname: encodeURIComponent(conversation.peerNickname || ''),
      avatar: encodeURIComponent(conversation.peerAvatar || ''),
    });
  }

  function avatarText(name) {
    return String(name || '聊').slice(0, 1);
  }

  function handlePrivateEvent(message) {
    if (message?.id && receivedMessageIds.has(`${message.type}-${message.id}`)) return;
    if (message?.id) {
      receivedMessageIds.add(`${message.type}-${message.id}`);
      if (receivedMessageIds.size > 200) receivedMessageIds.clear();
    }
    loadMessages();
  }

  function registerRealtime() {
    addImMessageListener('marriage-messages-page', handlePrivateEvent);
    addInteractionNotificationListener('marriage-messages-page', (notification) => {
      if (notification?.bizType === 'marriage') loadMessages();
    });
    connectImWebSocket();
  }

  watch(isLoggedIn, (loggedIn, wasLoggedIn) => {
    if (!loggedIn || wasLoggedIn) return;
    sheep.$store('social').startRealtime();
    registerRealtime();
    loadMessages();
  });

  onShow(() => {
    if (isLoggedIn.value) {
      sheep.$store('social').startRealtime();
      registerRealtime();
    }
    loadMessages();
  });

  onUnload(() => {
    removeImMessageListener('marriage-messages-page');
    removeInteractionNotificationListener('marriage-messages-page');
  });
</script>

<style scoped>
  .messages-scroll { width: 100%; background: #fff8f7; }
  .messages-content { padding: 24rpx 24rpx 160rpx; }
  .message-hero { padding: 38rpx 34rpx; color: #fff; background: linear-gradient(135deg,#a96779,#e46a92); border-radius: 32rpx; box-shadow: 0 18rpx 40rpx rgba(159,73,101,.18); }
  .hero-eyebrow { display: block; font-size: 22rpx; opacity: .82; }
  .hero-title { display: block; margin-top: 12rpx; font-size: 39rpx; font-weight: 800; }
  .hero-description { display: block; margin-top: 12rpx; font-size: 25rpx; opacity: .9; }
  .login-card, .state-card, .conversation-list { margin-top: 22rpx; background: #fff; border: 1rpx solid rgba(194,132,145,.13); border-radius: 28rpx; }
  .login-card { padding: 54rpx 38rpx; text-align: center; }
  .login-title { display: block; color: #4f393c; font-size: 32rpx; font-weight: 750; }
  .login-description { display: block; margin: 16rpx 0 28rpx; color: #9b8588; font-size: 25rpx; }
  .primary-button { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 42rpx; color: #fff; background: linear-gradient(135deg,#ec6d96,#df4f83); border-radius: 999rpx; }
  .section-heading { display: flex; align-items: center; justify-content: space-between; margin: 34rpx 8rpx 18rpx; }
  .section-title { color: #4e3a3c; font-size: 31rpx; font-weight: 760; }
  .read-action, .section-hint { color: #a87884; font-size: 23rpx; }
  .quick-grid { display: flex; gap: 14rpx; }
  .quick-card { position: relative; flex: 1; min-width: 0; padding: 25rpx 16rpx; text-align: center; background: #fff; border: 1rpx solid rgba(194,132,145,.13); border-radius: 24rpx; }
  .quick-icon { display: flex; align-items: center; justify-content: center; width: 72rpx; height: 72rpx; margin: 0 auto; color: #795660; font-size: 27rpx; font-weight: 800; border-radius: 24rpx; }
  .quick-label { display: block; margin-top: 17rpx; color: #554244; font-size: 25rpx; font-weight: 700; }
  .quick-hint { display: block; margin-top: 7rpx; overflow: hidden; color: #ab999b; font-size: 19rpx; white-space: nowrap; text-overflow: ellipsis; }
  .badge, .conversation-badge { display: flex; align-items: center; justify-content: center; min-width: 34rpx; height: 34rpx; padding: 0 8rpx; box-sizing: border-box; color: #fff; font-size: 18rpx; background: #e65382; border-radius: 999rpx; }
  .badge { position: absolute; top: 14rpx; right: 14rpx; }
  .conversation-heading { margin-top: 38rpx; }
  .state-card { padding: 70rpx 30rpx; color: #9d8789; text-align: center; }
  .conversation-list { overflow: hidden; }
  .conversation-row { display: flex; align-items: center; padding: 26rpx; border-bottom: 1rpx solid #f5eded; }
  .conversation-row:last-child { border-bottom: 0; }
  .conversation-avatar { width: 88rpx; height: 88rpx; display: flex; align-items: center; justify-content: center; flex: none; overflow: hidden; color: #fff; background: #c597a2; border-radius: 28rpx; }
  .avatar-image { width: 100%; height: 100%; }
  .conversation-main { flex: 1; min-width: 0; margin-left: 20rpx; }
  .conversation-top, .conversation-bottom { display: flex; align-items: center; justify-content: space-between; gap: 16rpx; }
  .conversation-name { flex: 1; color: #4d3b3d; font-size: 28rpx; font-weight: 700; }
  .conversation-time { color: #b19fa1; font-size: 20rpx; }
  .conversation-bottom { margin-top: 10rpx; }
  .conversation-preview { flex: 1; overflow: hidden; color: #968487; font-size: 23rpx; white-space: nowrap; text-overflow: ellipsis; }
  .inline-error { padding: 24rpx; color: #b06e7e; text-align: center; font-size: 23rpx; }
</style>
