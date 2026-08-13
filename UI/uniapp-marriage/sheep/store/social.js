import { defineStore } from 'pinia';
import app from './app';
import user from './user';
import InteractionApi from '@/sheep/api/marriage/interaction';
import MessageApi from '@/sheep/api/marriage/message';
import {
  addImMessageListener,
  addInteractionNotificationListener,
  connectImWebSocket,
  disconnectImWebSocket,
  removeImMessageListener,
  removeInteractionNotificationListener,
} from '@/sheep/api/marriage/imSocket';
import { getResponseData } from '@/sheep/helper/marriage';
import { subscribeRealtimeConnection, unsubscribeRealtimeConnection } from '@/sheep/api/infra/realtime';

const STORE_LISTENER_KEY = 'marriage-social-store';

const social = defineStore('social', {
  state: () => ({
    interactionUnread: 0,
    privateUnread: 0,
    unreadTotal: 0,
    refreshing: false,
  }),
  actions: {
    setUnreadCounts(interactionUnread = 0, privateUnread = 0) {
      this.interactionUnread = Math.max(0, Number(interactionUnread) || 0);
      this.privateUnread = Math.max(0, Number(privateUnread) || 0);
      this.unreadTotal = this.interactionUnread + this.privateUnread;
      this.applyMessageTabBadge();
    },
    applyMessageTabBadge() {
      const items = app().template?.basic?.tabbar?.items;
      if (!Array.isArray(items)) return;
      const messageItem = items.find((item) => item?.url === '/pages/messages/index');
      if (!messageItem) return;
      messageItem.badge = this.unreadTotal > 0 ? (this.unreadTotal > 99 ? '99+' : this.unreadTotal) : null;
      messageItem.dot = false;
    },
    async refreshUnread() {
      if (!user().isLogin || this.refreshing) {
        if (!user().isLogin) this.setUnreadCounts(0, 0);
        return;
      }
      this.refreshing = true;
      try {
        const [groupsResponse, privateResponse] = await Promise.all([
          InteractionApi.getUnreadCountGroup(),
          MessageApi.getUnreadCount(),
        ]);
        const groups = getResponseData(groupsResponse, '互动未读加载失败') || [];
        const interactionUnread = groups.reduce(
          (sum, item) => sum + Math.max(0, Number(item?.unreadCount) || 0),
          0,
        );
        const privateUnread = getResponseData(privateResponse, '私信未读加载失败') || 0;
        this.setUnreadCounts(interactionUnread, privateUnread);
      } catch (error) {
        console.warn('[marriage-social] unread refresh failed', error);
      } finally {
        this.refreshing = false;
      }
    },
    startRealtime() {
      if (!user().isLogin) {
        this.stopRealtime();
        disconnectImWebSocket();
        this.setUnreadCounts(0, 0);
        return;
      }
      addImMessageListener(STORE_LISTENER_KEY, () => this.refreshUnread());
      addInteractionNotificationListener(STORE_LISTENER_KEY, (notification) => {
        if (notification?.bizType === 'marriage') this.refreshUnread();
      });
      subscribeRealtimeConnection(STORE_LISTENER_KEY, () => this.refreshUnread());
      connectImWebSocket();
      this.refreshUnread();
    },
    stopRealtime() {
      removeImMessageListener(STORE_LISTENER_KEY);
      removeInteractionNotificationListener(STORE_LISTENER_KEY);
      unsubscribeRealtimeConnection(STORE_LISTENER_KEY);
    },
  },
});

export default social;
