<template>
  <s-layout :title="peerName || '私信'" navbar="normal" showLeftButton :bgStyle="pageBackground">
    <view class="chat-page">
      <view v-if="unavailable" class="state-card">
        <text>{{ errorText || '聊天对象不可用' }}</text>
        <view class="back-button" @tap="safeBack">返回</view>
      </view>
      <template v-else>
        <scroll-view
          class="message-scroll"
          scroll-y
          :scroll-top="scrollTop"
          upper-threshold="80"
          @scrolltoupper="loadOlder"
        >
          <view class="message-content">
            <view v-if="olderError" class="history-action failed" @tap="loadOlder">{{ olderError }}，点击重试</view>
            <view v-else-if="hasMore" class="history-action" @tap="loadOlder">
              {{ loadingOlder ? '加载中...' : '查看更早消息' }}
            </view>
            <view v-else-if="messages.length" class="history-action">没有更早消息了</view>
            <view v-if="loading && messages.length === 0" class="empty-state">聊天记录加载中...</view>
            <view v-else-if="errorText && messages.length === 0" class="empty-state" @tap="loadInitial">
              {{ errorText }}，点击重试
            </view>
            <view v-else-if="messages.length === 0" class="empty-state">发一条消息，开始认识彼此吧</view>

            <view
              v-for="item in messages"
              :key="item.clientMessageId || item.id"
              class="message-row"
              :class="{ mine: isMine(item) }"
            >
              <view v-if="!isMine(item)" class="chat-avatar" @tap="openMember">
                <image v-if="peerAvatar" class="avatar-image" :src="peerAvatar" mode="aspectFill" />
                <text v-else>{{ String(peerName || '聊').slice(0, 1) }}</text>
              </view>
              <view class="bubble-wrap">
                <text class="message-time">{{ formatMarriageTime(item.sendTime) }}</text>
                <view class="message-bubble" :class="{ recalled: Number(item.type) === IM_MESSAGE_TYPES.RECALL }">
                  {{ messageText(item) }}
                </view>
                <text v-if="isMine(item) && item.pending" class="send-status">发送中...</text>
                <text v-else-if="isMine(item) && item.failed" class="send-status failed" @tap="retryMessage(item)">发送失败，点击重试</text>
                <text v-else-if="isMine(item) && item.read" class="send-status">已读</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="chat-editor">
          <input
            v-model="draft"
            class="chat-input"
            maxlength="1000"
            confirm-type="send"
            placeholder="输入消息..."
            @confirm="sendMessage"
          />
          <view class="send-button" :class="{ disabled: !draft.trim() }" @tap="sendMessage">发送</view>
        </view>
      </template>
    </view>
  </s-layout>
</template>

<script setup>
  import { nextTick, ref } from 'vue';
  import { onHide, onLoad, onShow, onUnload } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { showAuthModal } from '@/sheep/hooks/useModal';
  import { INTERACTION_GUARD_SCENES, requireRealNameInteraction } from '@/sheep/hooks/useRealNameInteractionGuard';
  import MessageApi, { createClientMessageId, IM_MESSAGE_TYPES } from '@/sheep/api/marriage/message';
  import {
    addImMessageListener,
    connectImWebSocket,
    removeImMessageListener,
  } from '@/sheep/api/marriage/imSocket';
  import { subscribeRealtimeConnection, unsubscribeRealtimeConnection } from '@/sheep/api/infra/realtime';
  import {
    dedupeBy,
    formatMarriageTime,
    parseMessageText,
    parsePositiveId,
  } from '@/sheep/helper/marriage';

  const pageBackground = { backgroundColor: '#f8f2f3' };
  const peerId = ref(0);
  const peerName = ref('私信');
  const peerAvatar = ref('');
  const messages = ref([]);
  const draft = ref('');
  const loading = ref(false);
  const loadingOlder = ref(false);
  const hasMore = ref(true);
  const unavailable = ref(false);
  const errorText = ref('');
  const olderError = ref('');
  const scrollTop = ref(0);
  const pageSize = 30;
  const RECONCILE_INTERVAL = 5000;
  const receivedEventKeys = new Set();
  let listenerKey = '';
  let reconcileTimer = null;
  let latestReadMessageId = 0;

  const toast = (title) => uni.showToast({ title, icon: 'none' });

  function decodeOption(value) {
    try {
      return decodeURIComponent(value || '');
    } catch (error) {
      return String(value || '');
    }
  }

  function sortMessages(list) {
    return [...list].sort((left, right) => Number(left.id || 0) - Number(right.id || 0));
  }

  function mergeMessages(list, prepend = false) {
    const merged = prepend ? list.concat(messages.value) : messages.value.concat(list);
    messages.value = sortMessages(
      dedupeBy(merged, (item) => (Number(item.id) > 0 ? `id-${item.id}` : item.clientMessageId)),
    );
  }

  async function loadInitial() {
    if (!peerId.value || loading.value) return;
    loading.value = true;
    errorText.value = '';
    try {
      const list = await MessageApi.getPrivateList({ receiverId: peerId.value, limit: pageSize });
      messages.value = sortMessages(dedupeBy(list || [], (item) => item.clientMessageId || `id-${item.id}`));
      hasMore.value = (list || []).length >= pageSize;
      await markReadIfNeeded();
      scrollToBottom();
    } catch (error) {
      errorText.value = error.message || '聊天记录加载失败';
    } finally {
      loading.value = false;
    }
  }

  async function loadOlder() {
    if (!hasMore.value || loadingOlder.value || messages.value.length === 0) return;
    const maxId = messages.value.reduce((minimum, item) => {
      const id = Number(item.id);
      if (id <= 0) return minimum;
      return minimum === 0 ? id : Math.min(minimum, id);
    }, 0);
    if (!maxId) return;
    loadingOlder.value = true;
    olderError.value = '';
    try {
      const list = await MessageApi.getPrivateList({ receiverId: peerId.value, maxId, limit: pageSize });
      mergeMessages(list || [], true);
      // Keep the first previously visible row anchored after older history is prepended.
      scrollTop.value += Math.max(1, (list || []).length) * 96;
      hasMore.value = (list || []).length >= pageSize;
    } catch (error) {
      olderError.value = error.message || '更早消息加载失败';
    } finally {
      loadingOlder.value = false;
    }
  }

  function isMine(message) {
    return Number(message.receiverId) === peerId.value && Number(message.senderId) !== peerId.value;
  }

  function messageText(message) {
    if (Number(message.type) === IM_MESSAGE_TYPES.RECALL) return '这条消息已撤回';
    return parseMessageText(message.content) || '[暂不支持的消息]';
  }

  async function sendMessage() {
    const text = draft.value.trim();
    if (!text) return;
    if (!(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.MESSAGE))) return;
    draft.value = '';
    const clientMessageId = createClientMessageId();
    const optimistic = {
      id: -Date.now(),
      clientMessageId,
      senderId: Number(sheep.$store('user').userInfo?.id) || 0,
      receiverId: peerId.value,
      type: IM_MESSAGE_TYPES.TEXT,
      content: JSON.stringify({ text }),
      sendTime: new Date().toISOString(),
      pending: true,
    };
    mergeMessages([optimistic]);
    scrollToBottom();
    await submitMessage(optimistic);
  }

  async function reconcileLatest() {
    if (!peerId.value || loading.value || loadingOlder.value) return;
    try {
      const list = await MessageApi.getPrivateList({ receiverId: peerId.value, limit: pageSize });
      const previousSize = messages.value.length;
      mergeMessages(list || []);
      if (messages.value.length > previousSize) scrollToBottom();
      await markReadIfNeeded();
    } catch (error) {
      // The reconnect event and the next interval will retry; preserve the current chat UI.
      console.warn('[marriage-chat] latest-message reconciliation failed', error);
    }
  }

  function startReconcileTimer() {
    if (reconcileTimer) return;
    reconcileTimer = setInterval(reconcileLatest, RECONCILE_INTERVAL);
  }

  function stopReconcileTimer() {
    if (!reconcileTimer) return;
    clearInterval(reconcileTimer);
    reconcileTimer = null;
  }

  async function submitMessage(message) {
    if (message.sending) return;
    replaceOptimistic(message.clientMessageId, { ...message, pending: true, failed: false, sending: true });
    try {
      const sent = await MessageApi.sendPrivateText({
        receiverId: peerId.value,
        text: messageText(message),
        clientMessageId: message.clientMessageId,
      });
      replaceOptimistic(message.clientMessageId, { ...sent, pending: false, failed: false, sending: false });
      sheep.$store('social').refreshUnread();
    } catch (error) {
      replaceOptimistic(message.clientMessageId, { ...message, pending: false, failed: true, sending: false });
      toast(error.message || '发送失败');
    }
  }

  function replaceOptimistic(clientMessageId, replacement) {
    const index = messages.value.findIndex((item) => item.clientMessageId === clientMessageId);
    if (index >= 0) messages.value[index] = replacement;
    else mergeMessages([replacement]);
  }

  async function retryMessage(message) {
    if (!(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.MESSAGE))) return;
    submitMessage(message);
  }

  async function markReadIfNeeded() {
    const maxIncomingId = messages.value.reduce(
      (maximum, item) => (Number(item.senderId) === peerId.value ? Math.max(maximum, Number(item.id) || 0) : maximum),
      0,
    );
    if (!maxIncomingId || maxIncomingId <= latestReadMessageId) return;
    try {
      await MessageApi.readPrivate({ receiverId: peerId.value, messageId: maxIncomingId });
      latestReadMessageId = maxIncomingId;
      sheep.$store('social').refreshUnread();
    } catch (error) {
      console.warn('[marriage-chat] read cursor update failed', error);
    }
  }

  function handleSocketMessage(message) {
    if (
      Number(message?.senderId) !== peerId.value &&
      Number(message?.receiverId) !== peerId.value
    ) {
      return;
    }
    const eventKey = `${message.type}-${message.id || 0}-${message.clientMessageId || ''}`;
    if (receivedEventKeys.has(eventKey)) return;
    receivedEventKeys.add(eventKey);
    if (receivedEventKeys.size > 300) receivedEventKeys.clear();

    const type = Number(message.type);
    if (type === IM_MESSAGE_TYPES.RECALL) {
      const index = messages.value.findIndex((item) => Number(item.id) === Number(message.id));
      if (index >= 0) messages.value[index] = { ...messages.value[index], type };
      return;
    }
    if (type === IM_MESSAGE_TYPES.RECEIPT || type === IM_MESSAGE_TYPES.READ) {
      messages.value = messages.value.map((item) =>
        isMine(item) && Number(item.id) <= Number(message.id) ? { ...item, read: true } : item,
      );
      sheep.$store('social').refreshUnread();
      return;
    }

    const optimistic = messages.value.find((item) => item.clientMessageId && item.clientMessageId === message.clientMessageId);
    if (optimistic) replaceOptimistic(message.clientMessageId, { ...message, pending: false, failed: false, sending: false });
    else mergeMessages([{ ...message, pending: false, failed: false, sending: false }]);
    if (Number(message.senderId) === peerId.value) markReadIfNeeded();
    scrollToBottom();
  }

  function registerListener() {
    if (listenerKey) removeImMessageListener(listenerKey);
    listenerKey = `marriage-chat-${peerId.value}`;
    addImMessageListener(listenerKey, handleSocketMessage);
    subscribeRealtimeConnection(listenerKey, loadInitial);
    connectImWebSocket();
  }

  function scrollToBottom() {
    nextTick(() => {
      scrollTop.value += 100000;
    });
  }

  function openMember() {
    sheep.$router.go('/pages/member-detail/index', { id: peerId.value });
  }

  function safeBack() {
    if (sheep.$router.hasHistory()) sheep.$router.back();
    else sheep.$router.go('/pages/messages/index');
  }

  onLoad((options) => {
    peerId.value = parsePositiveId(options?.peerId);
    peerName.value = decodeOption(options?.nickname) || '私信';
    peerAvatar.value = decodeOption(options?.avatar);
    unavailable.value = !peerId.value;
    if (unavailable.value) errorText.value = '缺少有效的聊天对象编号';
    if (!sheep.$store('user').isLogin) {
      unavailable.value = true;
      errorText.value = '登录后才能查看私信';
      showAuthModal();
      return;
    }
    registerListener();
    loadInitial();
  });

  onShow(() => {
    if (!unavailable.value && peerId.value) {
      registerListener();
      // Foreground recovery must reconcile even if a platform did not deliver a
      // WebSocket close event while the Mini Program was backgrounded.
      loadInitial();
      startReconcileTimer();
    }
  });

  onHide(() => {
    stopReconcileTimer();
  });

  onUnload(() => {
    stopReconcileTimer();
    if (listenerKey) removeImMessageListener(listenerKey);
    if (listenerKey) unsubscribeRealtimeConnection(listenerKey);
  });
</script>

<style scoped>
  .chat-page { height: calc(100vh - 90rpx); display: flex; flex-direction: column; background: #f8f2f3; }
  .state-card { margin: 30rpx 24rpx; padding: 90rpx 34rpx; color: #8e7779; text-align: center; background: #fff; border-radius: 30rpx; }
  .back-button { display: inline-flex; margin-top: 28rpx; padding: 14rpx 34rpx; color: #fff; background: #df5c88; border-radius: 999rpx; }
  .message-scroll { flex: 1; min-height: 0; }
  .message-content { padding: 24rpx 24rpx 40rpx; }
  .history-action, .empty-state { padding: 24rpx; color: #a18d90; text-align: center; font-size: 22rpx; }
  .history-action.failed { color: #d65a72; }
  .empty-state { margin-top: 24rpx; padding: 70rpx 24rpx; background: rgba(255,255,255,.74); border-radius: 26rpx; }
  .message-row { display: flex; align-items: flex-end; margin-top: 26rpx; }
  .message-row.mine { justify-content: flex-end; }
  .chat-avatar { width: 70rpx; height: 70rpx; display: flex; align-items: center; justify-content: center; flex: none; overflow: hidden; color: #fff; background: #bd909b; border-radius: 22rpx; }
  .avatar-image { width: 100%; height: 100%; }
  .bubble-wrap { max-width: 72%; margin-left: 14rpx; }
  .mine .bubble-wrap { display: flex; flex-direction: column; align-items: flex-end; margin: 0; }
  .message-time { display: block; margin-bottom: 7rpx; color: #b3a2a4; font-size: 18rpx; }
  .message-bubble { padding: 19rpx 23rpx; color: #4f4143; font-size: 27rpx; line-height: 1.55; word-break: break-all; background: #fff; border-radius: 8rpx 24rpx 24rpx 24rpx; box-shadow: 0 8rpx 20rpx rgba(83,51,58,.06); }
  .mine .message-bubble { color: #fff; background: linear-gradient(135deg,#ec729a,#df5586); border-radius: 24rpx 8rpx 24rpx 24rpx; }
  .message-bubble.recalled, .mine .message-bubble.recalled { color: #a18f91; font-size: 22rpx; background: #ece5e6; }
  .send-status { margin-top: 6rpx; color: #aa979a; font-size: 18rpx; }
  .send-status.failed { color: #d65a72; }
  .chat-editor { display: flex; align-items: center; gap: 14rpx; padding: 18rpx 22rpx calc(18rpx + env(safe-area-inset-bottom)); background: rgba(255,255,255,.97); box-shadow: 0 -8rpx 25rpx rgba(80,50,55,.07); }
  .chat-input { flex: 1; height: 76rpx; padding: 0 24rpx; color: #4d3f41; background: #f6eff0; border-radius: 999rpx; }
  .send-button { padding: 17rpx 25rpx; color: #fff; font-size: 24rpx; background: #df5a87; border-radius: 999rpx; }
  .send-button.disabled { opacity: .45; }
</style>
