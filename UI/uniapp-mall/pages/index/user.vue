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
        <s-block-item :type="item.id" :data="item.property || {}" :styles="item.property?.style || {}" />
      </s-block>
    </template>
    <!-- 装修未配置时，默认展示登录/退出 -->
    <s-user-auth-button v-if="!hasAuthButton" />
  </s-layout>
</template>

<script setup>
  import { computed } from 'vue';
  import { onShow, onPageScroll, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';

  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  const template = computed(() => sheep.$store('app').template.user);
  const hasAuthButton = computed(() =>
    (template.value?.components || []).some((item) => item.id === 'UserAuthButton'),
  );

  onShow(() => {
    sheep.$store('user').updateUserData();
  });

  onPullDownRefresh(() => {
    sheep.$store('user').updateUserData();
    setTimeout(function () {
      uni.stopPullDownRefresh();
    }, 800);
  });

  onPageScroll(() => {});
</script>

<style></style>
