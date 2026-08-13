<!-- 自定义页面：支持装修 -->
<template>
  <s-layout
    :title="state.name"
    navbar="custom"
    :bgStyle="state.page"
    :navbarStyle="state.navigationBar"
    onShareAppMessage
    showLeftButton
  >
    <template v-for="(item, index) in state.components" :key="index">
      <template v-if="item && item.id">
        <su-sticky
          v-if="item.id === 'StickySearchMenuHeader' && item.property?.sticky !== false"
          :offsetTop="0"
          :customNavHeight="0"
          bgColor="transparent"
        >
          <s-block :styles="item.property?.style || {}">
            <s-block-item
              :type="item.id"
              :data="item.property || {}"
              :styles="item.property?.style || {}"
            />
          </s-block>
        </su-sticky>
        <s-block v-else :styles="item.property?.style || {}">
          <s-block-item
            :type="item.id"
            :data="item.property || {}"
            :styles="item.property?.style || {}"
          />
        </s-block>
      </template>
    </template>
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad, onPageScroll } from '@dcloudio/uni-app';
  import DiyApi from '@/sheep/api/promotion/diy';
  import $share from '@/sheep/platform/share';

  const state = reactive({
    name: '',
    id: 0,
    components: [],
    navigationBar: {},
    page: {},
  });
  onLoad(async (options) => {
    let id = options.id

    // #ifdef MP
    // 小程序预览自定义页面
    if (options.scene) {
      const sceneParams = decodeURIComponent(options.scene).split('=');
      if (sceneParams[0] === 'bindUserId') {
        // 二维码分销邀请场景：与 SPM 推广人共用统一待绑定上下文
        $share.captureBindUserId(sceneParams[1]);
      } else {
        id = sceneParams[1];
      }
    }
    // #endif

    const { code, data } = await DiyApi.getDiyPage(id);
    if (code === 0) {
      state.id = Number(id) || 0;
      state.name = data.name;
      state.components = data.property?.components;
      state.navigationBar = data.property?.navigationBar;
      state.page = data.property?.page;
    }
  });

  onPageScroll(() => {});
</script>

<style></style>
