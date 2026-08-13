<template>
  <scroll-view class="chat-scroll-view" scroll-y :scroll-top="scrollTop" upper-threshold="80" @scrolltoupper="loadMoreHistory">
    <view v-if="initialLoading && !messageList.length" class="state">聊天记录加载中...</view>
    <view v-else-if="initialError && !messageList.length" class="state failed" @tap="refreshMessageList">{{ initialError }}，点击重试</view>
    <view v-else-if="!messageList.length" class="state">还没有客服消息</view>
    <view v-if="incrementalError" class="history failed" @tap="loadMoreHistory">{{ incrementalError }}，点击重试</view>
    <view v-else-if="hasMore && messageList.length" class="history" @tap="loadMoreHistory">{{ isLoading ? '加载中...' : '查看更早消息' }}</view>
    <view v-for="(item, index) in messageList" :key="item.id || item.localId" class="message-item">
      <MessageListItem :message="item" :message-index="index" :message-list="messageList" @retry="emit('retry', item)" />
    </view>
  </scroll-view>
  <su-fixed bottom><slot name="bottom" /></su-fixed>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import MessageListItem from '@/pages/chat/components/messageListItem.vue';
  import KeFuApi from '@/sheep/api/promotion/kefu';
  import { mergeByMessageId, getOldestCursor } from '@/sheep/helper/realtime-messaging';

  const emit = defineEmits(['retry']);
  const props = defineProps({ conversationId: { type: [Number, String], default: undefined } });
  const messageList = ref([]);
  const isLoading = ref(false);
  const initialLoading = ref(false);
  const initialError = ref('');
  const incrementalError = ref('');
  const hasMore = ref(true);
  const scrollTop = ref(0);
  const limit = 20;

  function merge(messages, prepend = false) {
    messageList.value = mergeByMessageId(messageList.value, messages, { prepend });
  }

  async function loadPage({ older = false } = {}) {
    if (isLoading.value || (older && !hasMore.value)) return;
    isLoading.value = true;
    if (!older) { initialLoading.value = true; initialError.value = ''; }
    else incrementalError.value = '';
    try {
      const createTime = older ? getOldestCursor(messageList.value.filter((item) => Number(item.id) > 0), 'createTime') : undefined;
      const data = await KeFuApi.getKefuMessageList({
        ...(props.conversationId ? { conversationId: props.conversationId } : {}), createTime, limit,
      });
      merge(data || [], older);
      hasMore.value = (data || []).length >= limit;
      if (older) scrollTop.value += Math.max(1, (data || []).length) * 80;
    } catch (error) {
      if (older) incrementalError.value = error.message || '更早消息加载失败';
      else initialError.value = error.message || '客服记录加载失败';
    } finally {
      isLoading.value = false;
      initialLoading.value = false;
    }
  }

  const loadMoreHistory = () => loadPage({ older: true });
  async function refreshMessageList() {
    hasMore.value = true;
    await loadPage();
    scrollToBottom();
  }
  function scrollToBottom() { scrollTop.value += 100000; }
  function appendMessage(message) { merge([message]); scrollToBottom(); }
  function replaceMessage(localId, message) {
    const index = messageList.value.findIndex((item) => item.localId === localId);
    if (index >= 0) messageList.value[index] = { ...message, localId };
    else appendMessage(message);
  }
  async function markRead() {
    const conversationId = props.conversationId || messageList.value.find((item) => item.conversationId)?.conversationId;
    if (conversationId) await KeFuApi.updateReadStatus(conversationId);
  }
  defineExpose({ appendMessage, replaceMessage, refreshMessageList, markRead, loadMoreHistory });
  onMounted(refreshMessageList);
</script>

<style scoped lang="scss">
  .chat-scroll-view { height: calc(100vh - 150px); padding: 20rpx 24rpx 150rpx; box-sizing: border-box; background: #f8f8f8; }
  .message-item { margin-bottom: 10rpx; }
  .state, .history { padding: 28rpx; color: #999; text-align: center; font-size: 24rpx; }
  .failed { color: #d65a72; }
</style>
