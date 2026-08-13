<template>
  <slot />
</template>

<script setup>
  import { computed, provide } from 'vue';

  const props = defineProps({
    partnerId: {
      type: Number,
      required: true,
    },
    profile: {
      type: Object,
      default: null,
    },
    index: {
      type: Number,
      default: 0,
    },
    active: {
      type: Boolean,
      default: false,
    },
    /** 页面拥有的悬浮互动层可见性（只读），仅由活动人物 scope 传入 */
    floatingActionsVisible: {
      type: Boolean,
      default: true,
    },
    /** 页面配置、模板、身份或人物切换时递增，用于隔离互动异步结果。 */
    interactionGeneration: {
      type: Number,
      default: 0,
    },
  });

  const context = computed(() => ({
    partnerId: props.partnerId,
    profile: props.profile,
    index: props.index,
    active: props.active,
    floatingActionsVisible: props.floatingActionsVisible,
    interactionGeneration: props.interactionGeneration,
  }));

  provide('partnerRecommendationContext', context);
</script>
