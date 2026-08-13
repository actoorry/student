<!-- 个人中心：支持装修 -->
<template>
  <s-layout
    title="我的"
    tabbar="/pages/index/user"
    navbar="custom"
    :bgStyle="template.page"
    :navbarStyle="template.navigationBar"
    onShareAppMessage
  >
    <template v-for="(item, index) in template.components" :key="index">
      <s-block v-if="item && item.id" :styles="item.property?.style || {}">
        <s-block-item :type="item.id" :data="item.property || {}" :styles="item.property?.style || {}" :source-context="{ source: 'template', id: templateId, slot: 'user', index }" />
      </s-block>
    </template>
    <!-- 装修未配置时，默认展示登录/退出 -->
    <s-user-auth-button v-if="!hasAuthButton" />
  </s-layout>
</template>

<script setup>
  import { computed, provide, reactive, readonly, watch } from 'vue';
  import { onShow, onPageScroll, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import MarriageProfileApi from '@/sheep/api/marriage/profile';
  import {
    beginPartnerUserProfileRefresh,
    shouldRefreshPartnerUserProfile,
  } from '@/sheep/helper/partner-user-session.mjs';

  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  const template = computed(() => sheep.$store('app').template.user);
  const templateId = computed(() => sheep.$store('app').template.templateId);
  const hasAuthButton = computed(() =>
    (template.value?.components || []).some((item) => item.id === 'UserAuthButton'),
  );

  const partnerUserProfileState = reactive({
    authenticated: false,
    loading: false,
    error: '',
    profile: null,
    generation: 0,
  });
  provide('partnerUserProfileRuntimeContext', {
    state: readonly(partnerUserProfileState),
    refresh: refreshPartnerUserProfile,
  });

  async function refreshPartnerUserProfile() {
    const authenticated = sheep.$store('user').isLogin === true;
    const { generation, shouldLoad } = beginPartnerUserProfileRefresh(
      partnerUserProfileState,
      authenticated,
    );
    if (!shouldLoad) return;
    const result = await MarriageProfileApi.getLoginUser();
    if (generation !== partnerUserProfileState.generation) return;
    partnerUserProfileState.loading = false;
    if (result && result.code === 0 && result.data) {
      partnerUserProfileState.profile = result.data;
      return;
    }
    partnerUserProfileState.error = result?.msg || '资料加载失败，请下拉刷新重试';
  }

  onShow(() => {
    sheep.$store('user').updateUserData();
    refreshPartnerUserProfile();
  });

  function handleLoginSuccess() {
    sheep.$store('user').updateUserData();
    refreshPartnerUserProfile();
  }

  // 未登录时不再自动弹出登录面板，由用户点击“登录”按钮后主动登录；
  // 登录态切换时立即同步个人中心运行时资料，避免退出后继续显示旧头像和昵称。
  const isLoggedIn = computed(() => sheep.$store('user').isLogin);
  watch(isLoggedIn, (loggedIn, wasLoggedIn) => {
    if (!shouldRefreshPartnerUserProfile(loggedIn, wasLoggedIn)) return;
    if (loggedIn) {
      handleLoginSuccess();
      return;
    }
    refreshPartnerUserProfile();
  });

  onPullDownRefresh(() => {
    sheep.$store('user').updateUserData();
    refreshPartnerUserProfile();
    setTimeout(function () {
      uni.stopPullDownRefresh();
    }, 800);
  });

  onPageScroll(() => {});
</script>

<style></style>
