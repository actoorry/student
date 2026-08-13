<template>
  <goods-list-page ref="listRef" list-mode="new" />
</template>

<script setup>
  import { ref } from 'vue';
  import { onLoad, onReady, onReachBottom } from '@dcloudio/uni-app';
  import GoodsListPage from './components/list/goods-list-page.vue';

  const listRef = ref(null);
  let pageOptions = {};

  onLoad((options) => {
    pageOptions = options;
  });

  onReady(() => {
    listRef.value?.init(pageOptions);
  });

  onReachBottom(() => {
    listRef.value?.loadMore();
  });
</script>
