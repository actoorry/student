<template>
  <view class="mine-points-page">
    <su-navbar title="我的积分" statusBar />
    <view v-if="state.unauthorized" class="mine-points-page__state"><text>登录状态已失效，请返回我的页面登录</text><button @tap="goMine">返回我的页面</button></view>
    <view v-else class="mine-points-page__body">
      <view class="mine-points-page__balance"><text>当前积分</text><text>{{ state.totalPoint }}</text><text>{{ state.total }} 条记录</text></view>
      <view v-if="state.loading && state.records.length === 0" class="mine-points-page__state">积分记录加载中…</view>
      <view v-else-if="state.error && state.records.length === 0" class="mine-points-page__state"><text>{{ state.error }}</text><button @tap="retry">重新加载</button></view>
      <template v-else>
        <view v-if="state.error" class="mine-points-page__error"><text>{{ state.error }}</text><text @tap="retry">重试</text></view>
        <view v-if="state.records.length === 0" class="mine-points-page__empty">暂无积分记录</view>
        <view v-else class="mine-points-page__records">
          <view v-for="record in state.records" :key="record.id" class="mine-points-page__record">
            <view><text>{{ record.title || '积分变动' }}</text><text>{{ record.description || `变动后 ${record.totalPoint} 积分` }}</text><text>{{ formatTime(record.createTime) }}</text></view>
            <text :class="record.point > 0 ? 'plus' : (record.point < 0 ? 'minus' : 'neutral')">{{ pointText(record.point) }}</text>
          </view>
        </view>
        <view v-if="state.hasMore" class="mine-points-page__load-more" @tap="loadMore">{{ state.loading ? '加载中…' : (state.error ? '重试加载更多' : '加载更多') }}</view>
        <view v-else-if="state.records.length > 0" class="mine-points-page__load-more muted">已展示全部记录</view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { reactive } from 'vue';
import { onReachBottom, onShow } from '@dcloudio/uni-app';
import sheep from '@/sheep';
import PointApi from '@/sheep/api/partner/point';
import {
  appendUniquePointRecords,
  canLoadMorePointRecords,
  isActiveMineRequest,
  nextPointPage,
  normalizePointPage,
  responseMessage,
} from './helper';

const quietAuthRequest = { auth: true, showLoading: false, showError: false };
const pageSize = 20;
const state = reactive({ loading: false, unauthorized: false, error: '', totalPoint: 0, total: 0, records: [], confirmedPage: 0, failedPage: 0, hasMore: true, requestId: 0 });
function goMine() { sheep.$router.go('/pages/index/user'); }
function safeBack() { if (sheep.$router.hasHistory()) sheep.$router.back(); else goMine(); }
function isCurrent(requestId) { return isActiveMineRequest(requestId, state.requestId, sheep.$store('user').isLogin === true); }
function markUnauthorized() { state.unauthorized = true; state.loading = false; state.error = ''; state.totalPoint = 0; state.total = 0; state.records = []; state.confirmedPage = 0; state.failedPage = 0; state.hasMore = false; }
function formatTime(value) { return value ? sheep.$helper.timeFormat(value, 'yyyy-mm-dd hh:MM:ss') : ''; }
function pointText(point) { return Number(point) > 0 ? `+${point}` : String(point); }

async function loadPage(pageNo) {
  if (state.loading) return;
  state.unauthorized = sheep.$store('user').isLogin !== true;
  if (state.unauthorized) { markUnauthorized(); return; }
  const requestId = ++state.requestId;
  state.loading = true;
  state.error = '';
  try {
    const result = await PointApi.getPointRecordPage({ pageNo, pageSize }, quietAuthRequest);
    if (!isCurrent(requestId)) return;
    if (result?.code !== 0 || !result.data) {
      state.error = responseMessage(result, '积分记录加载失败，请重试');
      state.failedPage = pageNo;
      return;
    }
    const payload = normalizePointPage(result.data);
    state.totalPoint = payload.totalPoint;
    state.total = payload.records.total;
    state.records = pageNo === 1 ? payload.records.list : appendUniquePointRecords(state.records, payload.records.list);
    state.confirmedPage = pageNo;
    state.failedPage = 0;
    state.hasMore = state.records.length < state.total;
  } catch (error) {
    if (requestId === state.requestId) {
      if (sheep.$store('user').isLogin !== true) markUnauthorized();
      else { state.error = responseMessage(error, '积分记录加载失败，请重试'); state.failedPage = pageNo; }
    }
  } finally {
    if (requestId === state.requestId) {
      if (sheep.$store('user').isLogin !== true) markUnauthorized();
      else state.loading = false;
    }
  }
}

function reload() {
  state.records = [];
  state.totalPoint = 0;
  state.total = 0;
  state.confirmedPage = 0;
  state.failedPage = 0;
  state.hasMore = true;
  loadPage(1);
}
function loadMore() {
  if (!canLoadMorePointRecords({ isLoggedIn: sheep.$store('user').isLogin === true, loading: state.loading, hasMore: state.hasMore })) return;
  loadPage(nextPointPage(state));
}
function retry() { loadPage(nextPointPage(state)); }

onShow(reload);
onReachBottom(loadMore);
</script>

<style scoped lang="scss">
.mine-points-page { min-height: 100vh; padding: 0 28rpx; background: #fff7f5; color: #422f31; &__header { position: relative; display: flex; align-items: center; justify-content: center; height: 104rpx; font-size: 34rpx; font-weight: 600; > text:first-child { position: absolute; left: 0; font-size: 58rpx; font-weight: 300; } } &__body { padding: 24rpx 0 56rpx; } &__balance { display: flex; flex-direction: column; gap: 10rpx; padding: 42rpx 34rpx; color: #fff; border-radius: 28rpx; background: linear-gradient(145deg, #c75460, #e58877); > text:first-child { font-size: 26rpx; opacity: .9; } > text:nth-child(2) { font-size: 62rpx; font-weight: 700; } > text:last-child { font-size: 24rpx; opacity: .88; } } &__state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 45vh; gap: 28rpx; color: #806c68; button { color: #fff; border-radius: 40rpx; background: #c75460; } } &__error { display: flex; justify-content: space-between; margin-top: 22rpx; padding: 22rpx; color: #b6535a; border-radius: 16rpx; background: #fff; font-size: 26rpx; } &__empty { margin-top: 22rpx; padding: 80rpx 0; color: #806c68; text-align: center; border-radius: 22rpx; background: #fff; } &__records { margin-top: 22rpx; overflow: hidden; border-radius: 22rpx; background: #fff; } &__record { display: flex; align-items: center; justify-content: space-between; gap: 20rpx; padding: 28rpx 24rpx; border-bottom: 1rpx solid #f2e5e3; > view { display: flex; flex: 1; flex-direction: column; gap: 8rpx; min-width: 0; > text:first-child { overflow: hidden; font-size: 29rpx; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; } > text:nth-child(2), > text:last-child { color: #8f7774; font-size: 24rpx; } } > text { flex: 0 0 auto; font-size: 30rpx; font-weight: 600; } } &__load-more { padding: 30rpx 0; color: #c75460; text-align: center; font-size: 26rpx; &.muted { color: #a3908d; } } }
.plus { color: #c75460; } .minus { color: #4e7069; } .neutral { color: #806c68; }
</style>
