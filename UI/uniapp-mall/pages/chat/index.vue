<template>
  <s-layout
    class="chat-wrap"
    title="在线客服"
    navbar="inner"
  >
    <!--  覆盖头部导航栏背景颜色  -->
    <view class="page-bg" :style="{ height: sys_navBar + 'px' }"></view>
    <!--  聊天区域  -->
    <MessageList ref="messageListRef" @retry="retryMessage">
      <template #bottom>
        <message-input
          v-model="chat.msg"
          @on-tools="onTools"
          @send-message="onSendMessage"
          :auto-focus="false"
          :show-char-count="true"
          :max-length="500"
        ></message-input>
      </template>
    </MessageList>
    <!--  聊天工具  -->
    <tools-popup
      :show-tools="chat.showTools"
      :tools-mode="chat.toolsMode"
      @close="handleToolsClose"
      @on-emoji="onEmoji"
      @image-select="onSelect"
      @on-show-select="onShowSelect"
    >
      <message-input
        v-model="chat.msg"
        @on-tools="onTools"
        @send-message="onSendMessage"
        :auto-focus="false"
        :show-char-count="true"
        :max-length="500"
      ></message-input>
    </tools-popup>
    <!--  商品订单选择  -->
    <SelectPopup
      :mode="chat.selectMode"
      :show="chat.showSelect"
      @select="onSelect"
      @close="chat.showSelect = false"
    />
  </s-layout>
</template>

<script setup>
  import MessageList from '@/pages/chat/components/messageList.vue';
  import { reactive, ref } from 'vue';
  import { onShow, onUnload } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import ToolsPopup from '@/pages/chat/components/toolsPopup.vue';
  import MessageInput from '@/pages/chat/components/messageInput.vue';
  import SelectPopup from '@/pages/chat/components/select-popup.vue';
  import {
    KeFuMessageContentTypeEnum,
    WebSocketMessageTypeConstants,
  } from '@/pages/chat/util/constants';
  import FileApi from '@/sheep/api/infra/file';
  import KeFuApi from '@/sheep/api/promotion/kefu';
  import { subscribeRealtime, unsubscribeRealtime, subscribeRealtimeConnection, unsubscribeRealtimeConnection, ensureRealtimeConnection } from '@/sheep/api/infra/realtime';

  const sys_navBar = sheep.$platform.navbar;

  const chat = reactive({
    msg: '',
    scrollInto: '',
    showTools: false,
    toolsMode: '',
    showSelect: false,
    selectMode: '',
  });
  const sending = new Set();
  const listenerKey = 'mall-kefu-chat';
  let localSequence = 0;

  const toast = (title) => sheep.$helper.toast(title);
  const newLocalId = () => `kefu-${Date.now()}-${++localSequence}`;

  // 发送消息
  async function onSendMessage() {
    const text = chat.msg.trim();
    if (!text) return;
    const sent = await sendKefuMessage({ contentType: KeFuMessageContentTypeEnum.TEXT, content: JSON.stringify({ text }) });
    if (sent) chat.msg = '';
  }

  const messageListRef = ref();

  //======================= 聊天工具相关 start =======================

  function handleToolsClose() {
    chat.showTools = false;
    chat.toolsMode = '';
  }

  function onEmoji(item) {
    chat.msg += item.name;
  }

  // 点击工具栏开关
  function onTools(mode) {
    if (isReconnecting.value) {
      sheep.$helper.toast('您已掉线！请返回重试');
      return;
    }

    // 第二次点击关闭
    if (chat.showTools && chat.toolsMode === mode) {
      handleToolsClose();
      return;
    }
    // 切换工具栏
    if (chat.showTools && chat.toolsMode !== mode) {
      chat.showTools = false;
      chat.toolsMode = '';
    }
    // 延迟打开等一下过度效果
    setTimeout(() => {
      chat.toolsMode = mode;
      chat.showTools = true;
    }, 200);
  }

  function onShowSelect(mode) {
    chat.showTools = false;
    chat.showSelect = true;
    chat.selectMode = mode;
  }

  async function onSelect({ type, data }) {
    let msg;
    switch (type) {
      case 'image':
        const res = await FileApi.uploadFile(data.tempFiles[0].path);
        msg = {
          contentType: KeFuMessageContentTypeEnum.IMAGE,
          content: JSON.stringify({ picUrl: res.data }),
        };
        break;
      case 'goods':
        msg = {
          contentType: KeFuMessageContentTypeEnum.PRODUCT,
          content: JSON.stringify(data),
        };
        break;
      case 'order':
        msg = {
          contentType: KeFuMessageContentTypeEnum.ORDER,
          content: JSON.stringify(data),
        };
        break;
    }
    if (msg) {
      // 发送消息
      // scrollBottom();
      const sent = await sendKefuMessage(msg);
      if (sent) {
        chat.showTools = false;
        chat.showSelect = false;
        chat.selectMode = '';
      }
    }
  }

  //======================= 聊天工具相关 end =======================
  async function sendKefuMessage(data, retrying = null) {
    if (!sheep.$store('user').isLogin) return toast('请先登录');
    const localId = retrying?.localId || newLocalId();
    if (sending.has(localId)) return false;
    const optimistic = retrying || {
      localId, id: -Date.now(), senderType: 1, contentType: data.contentType, content: data.content,
      createTime: new Date().toISOString(), pending: true, failed: false,
    };
    sending.add(localId);
    messageListRef.value?.replaceMessage(localId, { ...optimistic, pending: true, failed: false });
    try {
      const id = await KeFuApi.sendKefuMessage(data);
      messageListRef.value?.replaceMessage(localId, { ...optimistic, id: Number(id) || optimistic.id, pending: false, failed: false });
      await messageListRef.value?.refreshMessageList();
      return true;
    } catch (error) {
      messageListRef.value?.replaceMessage(localId, { ...optimistic, pending: false, failed: true });
      toast(error.message || '发送失败');
      return false;
    } finally {
      sending.delete(localId);
    }
  }

  async function retryMessage(message) {
    // 客服没有 clientMessageId：先从服务端对账，避免响应中断时重复发送。
    await messageListRef.value?.refreshMessageList();
    await sendKefuMessage({ contentType: message.contentType, content: message.content }, message);
  }

  function onKefuMessage(message) { messageListRef.value?.appendMessage(message); }
  async function onReadStatus(message) {
    await messageListRef.value?.markRead().catch(() => {});
    if (message?.conversationId) toast('客服已读您的消息');
  }
  function registerRealtime() {
    subscribeRealtime(WebSocketMessageTypeConstants.KEFU_MESSAGE_TYPE, listenerKey, onKefuMessage);
    subscribeRealtime(WebSocketMessageTypeConstants.KEFU_MESSAGE_ADMIN_READ, listenerKey, onReadStatus);
    subscribeRealtimeConnection(listenerKey, () => messageListRef.value?.refreshMessageList());
    ensureRealtimeConnection();
  }
  onShow(async () => { registerRealtime(); await messageListRef.value?.refreshMessageList(); });
  onUnload(() => {
    unsubscribeRealtime(WebSocketMessageTypeConstants.KEFU_MESSAGE_TYPE, listenerKey);
    unsubscribeRealtime(WebSocketMessageTypeConstants.KEFU_MESSAGE_ADMIN_READ, listenerKey);
    unsubscribeRealtimeConnection(listenerKey);
  });
</script>

<style scoped lang="scss">
  .chat-wrap {
    .page-bg {
      width: 100%;
      position: absolute;
      top: 0;
      left: 0;
      background-color: var(--ui-BG-Main);
      z-index: 1;
    }

    .status {
      position: relative;
      box-sizing: border-box;
      z-index: 3;
      height: 70rpx;
      padding: 0 30rpx;
      background: var(--ui-BG-Main-opacity-1);
      display: flex;
      align-items: center;
      font-size: 30rpx;
      font-weight: 400;
      color: var(--ui-BG-Main);
    }
  }
</style>
