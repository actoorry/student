<!-- 首页，支持店铺装修 -->
<template>
  <view v-if="template">
    <s-layout
      title="首页"
      navbar="custom"
      tabbar="/pages/index/index"
      :bgStyle="template.page"
      :navbarStyle="template.navigationBar"
      onShareAppMessage
    >
      <template v-for="(item, index) in template.components" :key="index">
        <s-block v-if="item && item.id" :styles="item.property?.style || {}">
          <s-block-item
            :type="item.id"
            :data="item.property || {}"
            :styles="item.property?.style || {}"
            :source-context="{ source: 'template', id: templateId, slot: 'home', index }"
          />
        </s-block>
      </template>
    </s-layout>
  </view>
</template>

<script setup>
  import { computed } from 'vue';
  import { onLoad, onShow, onPageScroll, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import $share from '@/sheep/platform/share';

  uni.hideTabBar({ fail: () => {} });

  const template = computed(() => sheep.$store('app').template?.home);
  const templateId = computed(() => sheep.$store('app').template?.templateId);

  onLoad((options) => {
    // #ifdef MP
    if (options.scene) {
      const sceneParams = decodeURIComponent(options.scene).split('=');
      options[sceneParams[0]] = sceneParams[1];
    }
    // #endif
    if (options.templateId) sheep.$store('app').init(options.templateId);
    if (options.spm) $share.decryptSpm(options.spm);
    if (options.page) sheep.$router.go(decodeURIComponent(options.page));
  });

  onShow(async () => {
    // #ifdef APP-PLUS
    if (sheep.$platform.os === 'ios' && (await sheep.$platform.checkNetwork())) {
      await sheep.$store('app').init();
    }
    // #endif
  });

  onPullDownRefresh(() => {
    sheep.$store('app').init();
    setTimeout(() => uni.stopPullDownRefresh(), 800);
  });

  onPageScroll(() => {});
</script>

<style></style>
