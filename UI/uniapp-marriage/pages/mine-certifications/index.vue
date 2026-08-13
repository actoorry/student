<template>
  <view class="certification-page">
    <su-navbar title="我的认证" statusBar />
    <view v-if="!state.loggedIn" class="certification-page__state">
      <image src="/static/certification/lock.svg" mode="aspectFit" />
      <text>请先登录后查看认证信息</text><button @tap="goMine">返回我的页面登录</button>
    </view>
    <view v-else-if="state.loading" class="certification-page__state">认证信息加载中…</view>
    <view v-else-if="state.error" class="certification-page__state"><text>{{ state.error }}</text><button @tap="loadProfile">重新加载</button></view>
    <view v-else class="certification-page__cards">
      <view class="certification-page__intro"><text>认证中心</text><text>认证信息仅供当前账号查看</text></view>
      <view class="certification-page__card" @tap="goRealName"><image src="/static/certification/shield.svg" /><view><text>实名认证</text><text>{{ status.realVerified ? '已认证，可查看实名信息' : '完成实名认证，保障账号真实可靠' }}</text></view><text>{{ status.realAction }} ›</text></view>
      <view class="certification-page__card" :class="{ disabled: !status.realVerified }" @tap="goMarriage"><image src="/static/certification/heart.svg" /><view><text>婚姻认证</text><text>{{ status.marriageVerified ? '已认证，可查看最近结果' : (status.realVerified ? '完成婚姻状态核验' : '请先完成实名认证') }}</text></view><text>{{ status.marriageAction }} ›</text></view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import sheep from '@/sheep';
import MarriageProfileApi from '@/sheep/api/marriage/profile';
import { certificationState } from './partner-certification';

const state = reactive({ loggedIn: false, loading: false, error: '', profile: null, requestId: 0 });
const status = computed(() => certificationState(state.profile));
function goMine() { sheep.$router.go('/pages/index/user'); }
function goRealName() { if (state.loggedIn) sheep.$router.go('/pages/mine-profile/nameCheck/index'); }
function goMarriage() { if (status.value.realVerified) sheep.$router.go('/pages/mine-profile/marriageCheck/index'); else uni.showToast({ title: '请先完成实名认证', icon: 'none' }); }
async function loadProfile() {
  const requestId = ++state.requestId;
  state.loggedIn = sheep.$store('user').isLogin === true;
  state.error = '';
  state.profile = null;
  if (!state.loggedIn) return;
  state.loading = true;
  const result = await MarriageProfileApi.getMyProfile();
  if (requestId !== state.requestId || !sheep.$store('user').isLogin) return;
  state.loading = false;
  if (result?.code === 0 && result.data) state.profile = result.data;
  else state.error = result?.msg || '认证信息加载失败，请重试';
}
onShow(loadProfile);
</script>

<style scoped lang="scss">
.certification-page { min-height:100vh; background:var(--marriage-soft); color:var(--marriage-text); padding:0 28rpx; &__header { height:104rpx; display:flex; align-items:center; justify-content:center; font-size:34rpx; font-weight:600; position:relative; > text:first-child { position:absolute; left:0; font-size:58rpx; font-weight:300; } } &__state { min-height:55vh; display:flex; flex-direction:column; align-items:center; justify-content:center; gap:28rpx; color:var(--marriage-muted); image { width:130rpx; height:130rpx; } button { background:var(--marriage-primary); color:#fff; border-radius:999rpx; font-size:28rpx; } } &__intro { display:flex; flex-direction:column; gap:10rpx; margin:30rpx 0; text:first-child { font-size:42rpx; font-weight:700; } text:last-child { font-size:26rpx; color:var(--marriage-muted); } } &__card { display:flex; align-items:center; gap:20rpx; margin-bottom:22rpx; padding:30rpx 24rpx; border-radius:24rpx; background:var(--marriage-surface); box-shadow:var(--marriage-shadow); image { width:76rpx; height:76rpx; } view { display:flex; flex:1; flex-direction:column; gap:8rpx; text:first-child { font-size:32rpx; font-weight:600; } text:last-child { color:var(--marriage-muted); font-size:25rpx; } } > text { color:var(--marriage-primary); font-size:25rpx; } &.disabled { opacity:.58; } } }
</style>
