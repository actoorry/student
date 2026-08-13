<template>
  <view class="mine-signin-page">
    <su-navbar title="我的签到" statusBar />
    <view v-if="state.unauthorized" class="mine-signin-page__state"><text>登录状态已失效，请返回我的页面登录</text><button @tap="goMine">返回我的页面</button></view>
    <view v-else-if="state.loading" class="mine-signin-page__state">签到状态加载中…</view>
    <view v-else-if="state.error && !hasConfigs" class="mine-signin-page__state"><text>{{ state.error }}</text><button @tap="loadSummary">重新加载</button></view>
    <view v-else class="mine-signin-page__body">
      <view class="mine-signin-page__status-card">
        <text class="mine-signin-page__kicker">{{ state.summary.signedToday ? '今日已签到' : '今日待签到' }}</text>
        <text class="mine-signin-page__days">连续 {{ state.summary.currentDay }} 天</text>
        <text class="mine-signin-page__reward">{{ rewardDescription }}</text>
        <button :disabled="!canSign" @tap="submit">{{ state.submitting ? '签到中…' : (state.summary.signedToday ? '已签到' : '立即签到') }}</button>
      </view>
      <view v-if="state.error" class="mine-signin-page__error"><text>{{ state.error }}</text><text @tap="loadSummary">刷新</text></view>
      <view class="mine-signin-page__rail-card">
        <view class="mine-signin-page__section-head"><text>连续奖励</text><text>按运营配置发放</text></view>
        <view v-if="!hasConfigs" class="mine-signin-page__empty"><text>暂无可用签到奖励</text><text @tap="loadSummary">重新加载</text></view>
        <view v-else class="mine-signin-page__reward-list">
          <view v-for="item in state.summary.configs" :key="item.id || item.day" class="mine-signin-page__reward-item" :class="{ active: item.day <= state.summary.currentDay }">
            <text>第 {{ item.day }} 天</text><text>+{{ item.point }} 积分</text><text>经验 +{{ item.experience }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import sheep from '@/sheep';
import SignInApi from '@/sheep/api/partner/signin';
import {
  canCreateSignIn,
  emptySignInSummary,
  isActiveMineRequest,
  normalizeSignInResult,
  normalizeSignInSummary,
  responseMessage,
} from './helper';

const quietAuthRequest = { auth: true, showLoading: false, showError: false };
const state = reactive({ loading: true, submitting: false, unauthorized: false, error: '', summary: emptySignInSummary(), requestId: 0, actionId: 0 });
const hasConfigs = computed(() => state.summary.configs.length > 0);
const canSign = computed(() => canCreateSignIn({ isLoggedIn: sheep.$store('user').isLogin === true, loading: state.loading, submitting: state.submitting, signedToday: state.summary.signedToday, hasConfigs: hasConfigs.value }));
const rewardDescription = computed(() => {
  const record = state.summary.todayRecord;
  if (state.summary.signedToday && record) return `今日获得 +${record.point} 积分，经验 +${record.experience}`;
  return `签到可得 +${state.summary.nextRewardPoint} 积分，经验 +${state.summary.nextRewardExperience}`;
});

function goMine() { sheep.$router.go('/pages/index/user'); }
function safeBack() { if (sheep.$router.hasHistory()) sheep.$router.back(); else goMine(); }
function isCurrent(requestId) { return isActiveMineRequest(requestId, state.requestId, sheep.$store('user').isLogin === true); }
function markUnauthorized() { state.unauthorized = true; state.loading = false; state.submitting = false; state.error = ''; state.summary = emptySignInSummary(); }

async function loadSummary() {
  const requestId = ++state.requestId;
  state.unauthorized = sheep.$store('user').isLogin !== true;
  state.error = '';
  state.summary = emptySignInSummary();
  if (state.unauthorized) { markUnauthorized(); return; }
  state.loading = true;
  try {
    const result = await SignInApi.getSignInRecordSummary(quietAuthRequest);
    if (!isCurrent(requestId)) return;
    if (result?.code === 0 && result.data) state.summary = normalizeSignInSummary(result.data);
    else state.error = responseMessage(result, '签到状态加载失败，请重试');
  } catch (error) {
    if (requestId === state.requestId) {
      if (sheep.$store('user').isLogin !== true) markUnauthorized();
      else state.error = responseMessage(error, '签到状态加载失败，请重试');
    }
  } finally {
    if (requestId === state.requestId) {
      if (sheep.$store('user').isLogin !== true) markUnauthorized();
      else state.loading = false;
    }
  }
}

async function submit() {
  if (!canSign.value) {
    if (sheep.$store('user').isLogin !== true) state.unauthorized = true;
    return;
  }
  const actionId = ++state.actionId;
  state.submitting = true;
  try {
    const result = await SignInApi.createSignInRecord(quietAuthRequest);
    if (actionId !== state.actionId) return;
    if (sheep.$store('user').isLogin !== true) { markUnauthorized(); return; }
    const signInResult = normalizeSignInResult(result?.data);
    if (result?.code === 0 && signInResult) {
      uni.showToast({ title: `签到成功，积分 +${signInResult.point}`, icon: 'success' });
      await loadSummary();
    } else {
      uni.showToast({ title: responseMessage(result, '签到失败，请稍后重试'), icon: 'none' });
      if (result?.msg === '今日已签到') await loadSummary();
    }
  } catch (error) {
    if (actionId === state.actionId) {
      if (sheep.$store('user').isLogin !== true) markUnauthorized();
      else uni.showToast({ title: responseMessage(error, '签到失败，请稍后重试'), icon: 'none' });
    }
  } finally {
    if (actionId === state.actionId) state.submitting = false;
  }
}

onShow(loadSummary);
</script>

<style scoped lang="scss">
.mine-signin-page { min-height: 100vh; padding: 0 28rpx; background: #fff7f5; color: #422f31; &__header { position: relative; display: flex; align-items: center; justify-content: center; height: 104rpx; font-size: 34rpx; font-weight: 600; > text:first-child { position: absolute; left: 0; font-size: 58rpx; font-weight: 300; } } &__state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 55vh; gap: 28rpx; color: #806c68; button { color: #fff; border-radius: 40rpx; background: #c75460; } } &__body { padding: 26rpx 0 56rpx; } &__status-card, &__rail-card { box-sizing: border-box; width: 100%; border-radius: 28rpx; background: #fff; box-shadow: 0 12rpx 32rpx rgb(180 109 113 / 12%); } &__status-card { display: flex; flex-direction: column; align-items: center; padding: 52rpx 30rpx; background: linear-gradient(145deg, #c75460, #e58877); color: #fff; button { width: 100%; margin-top: 36rpx; color: #b34f5c; border-radius: 46rpx; background: #fff; } button[disabled] { opacity: .58; } } &__kicker { font-size: 28rpx; opacity: .9; } &__days { margin: 12rpx 0; font-size: 48rpx; font-weight: 700; } &__reward { font-size: 26rpx; text-align: center; } &__error { display: flex; justify-content: space-between; margin: 22rpx 0; padding: 22rpx; color: #b6535a; border-radius: 16rpx; background: #fff; font-size: 26rpx; } &__rail-card { padding: 30rpx 24rpx; } &__section-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24rpx; > text:first-child { font-size: 32rpx; font-weight: 600; } > text:last-child { color: #8f7774; font-size: 24rpx; } } &__reward-list { display: flex; flex-wrap: wrap; gap: 16rpx; } &__reward-item { display: flex; flex: 1; flex-direction: column; min-width: 170rpx; gap: 8rpx; padding: 22rpx 16rpx; color: #806c68; border-radius: 18rpx; background: #fff6f3; font-size: 24rpx; &.active { color: #fff; background: #c75460; } } &__empty { display: flex; align-items: center; justify-content: space-between; padding: 30rpx 12rpx; color: #806c68; font-size: 26rpx; } }
</style>
