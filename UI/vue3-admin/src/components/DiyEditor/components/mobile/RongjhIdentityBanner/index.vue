<template>
  <div
    class="mx-12px flex items-center justify-between rounded-10px px-12px py-10px"
    style="background: linear-gradient(90deg, #fff8f2 0%, #fff3ea 100%)"
  >
    <div class="flex flex-1 items-center gap-8px">
      <Icon icon="mdi:star" color="#F97316" :size="18" />
      <span class="text-13px" style="color: #9a4b1f">{{ display.textNone }}</span>
    </div>
    <div class="flex shrink-0 items-center gap-2px">
      <span class="text-13px font-600" style="color: #f97316">{{ display.actionText }}</span>
      <Icon icon="ep:arrow-right" color="#F97316" :size="12" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DEFAULT_RONGJH_IDENTITY_BANNER, RongjhIdentityBannerProperty } from './config'

defineOptions({ name: 'RongjhIdentityBanner' })

const props = defineProps<{ property: RongjhIdentityBannerProperty }>()

const display = computed(() => ({
  ...DEFAULT_RONGJH_IDENTITY_BANNER,
  ...props.property
}))
</script>
