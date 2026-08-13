<template>
  <view class="certification-form-page"><su-navbar title="实名认证" statusBar />
    <view v-if="state.unauthorized" class="certification-form-page__state"><text>登录状态已失效，请重新登录</text><button @tap="goCenter">返回认证中心</button></view>
    <view v-else-if="state.loading" class="certification-form-page__state">资料加载中…</view>
    <view v-else class="certification-form-page__body"><view v-if="state.error" class="certification-form-page__error"><text>{{ state.error }}</text><text @tap="loadProfile">重试</text></view>
      <view v-if="state.verified" class="certification-form-page__result"><image src="/static/certification/shield.svg" /><text>实名认证已通过</text><text>{{ state.profile.name }} {{ state.profile.maskedIdCard }}</text></view>
      <template v-else><view class="certification-form-page__hint">请填写与身份证一致的本人信息。身份证号仅用于本次认证，不会保存到本地。</view><input v-model="form.name" maxlength="20" placeholder="真实姓名" /><input v-model="form.idCard" maxlength="18" placeholder="18 位身份证号" /><text v-if="!state.profile.mobile" class="certification-form-page__warning">请先绑定手机号后再认证</text><button :disabled="state.submitting || !state.profile.mobile" @tap="submit">{{ state.submitting ? '认证中…' : '提交认证' }}</button></template>
    </view></view>
</template>
<script setup>
import { onHide, onLoad } from '@dcloudio/uni-app';
import { reactive } from 'vue';
import sheep from '@/sheep';
import MarriageProfileApi from '@/sheep/api/marriage/profile';
import PartnerCertificationApi from '@/sheep/api/partner/certification';
import {
  clearSensitiveForm,
  isVerified,
  validateRealName,
} from '../partner-certification';
const form = reactive({ name: '', idCard: '' }); const state = reactive({ loading:true, submitting:false, unauthorized:false, verified:false, error:'', profile:{}, requestId:0 });
function goCenter() { sheep.$router.go('/pages/mine-certifications/index'); } function safeBack() { if (sheep.$router.hasHistory()) sheep.$router.back(); else goCenter(); }
async function loadProfile() { const requestId=++state.requestId; state.unauthorized=sheep.$store('user').isLogin !== true; state.error=''; if (state.unauthorized) { state.loading=false; clearSensitiveForm(form); return; } state.loading=true; const result=await MarriageProfileApi.getMyProfile(); if (requestId!==state.requestId || !sheep.$store('user').isLogin) return; state.loading=false; if (result?.code===0 && result.data) { state.profile=result.data; state.verified=isVerified(result.data.realVerified); } else state.error=result?.msg || '资料加载失败，请重试'; }
async function submit() { if (state.submitting || state.verified) return; if (!sheep.$store('user').isLogin) { state.unauthorized=true; clearSensitiveForm(form); return; } const check=validateRealName(form.name, form.idCard); if (!check.valid) { uni.showToast({ title:check.message, icon:'none' }); return; } state.submitting=true; const result=await PartnerCertificationApi.verifyRealName({ name:check.name, idCard:check.idCard }); state.submitting=false; if (!sheep.$store('user').isLogin) { state.unauthorized=true; clearSensitiveForm(form); return; } if (result?.code===0) { uni.showToast({ title:result.data?.passed ? (result.data?.reused ? '已复用历史认证结果' : '认证通过') : (result.data?.reason || '认证未通过'), icon:'none' }); if (result.data?.passed) { clearSensitiveForm(form); loadProfile(); } } else uni.showToast({ title:result?.msg || '认证失败，请重试', icon:'none' }); }
onLoad(loadProfile); onHide(() => clearSensitiveForm(form));
</script>
<style scoped lang="scss">.certification-form-page { min-height:100vh; padding:0 28rpx; background:var(--marriage-soft); color:var(--marriage-text); &__header { height:104rpx; display:flex; align-items:center; justify-content:center; position:relative; font-size:34rpx; font-weight:600; > text:first-child { position:absolute; left:0; font-size:58rpx; } } &__body { padding-top:35rpx; } input, button, &__hint, &__result, &__error { box-sizing:border-box; width:100%; margin-bottom:22rpx; padding:28rpx; border-radius:16rpx; background:var(--marriage-surface); font-size:28rpx; } input { border:1rpx solid var(--marriage-line); } button { background:var(--marriage-primary); color:#fff; } button[disabled] { opacity:.45; } &__hint, &__error, &__warning { display:block; color:var(--marriage-muted); font-size:25rpx; } &__error { display:flex; justify-content:space-between; color:var(--marriage-primary-pressed); } &__result { display:flex; flex-direction:column; align-items:center; gap:20rpx; box-shadow:var(--marriage-shadow); image { width:120rpx; height:120rpx; } } &__state { min-height:55vh; display:flex; flex-direction:column; align-items:center; justify-content:center; gap:28rpx; button { background:var(--marriage-primary); color:#fff; } } }</style>
