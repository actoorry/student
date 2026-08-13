<template>
  <div class="mx-12px">
    <div
      class="flex h-40px items-center justify-center rounded-20px text-14px font-500 text-white"
      style="background: linear-gradient(135deg, #d62d24, #b81d18)"
    >
      {{ display.logoutText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DEFAULT_USER_AUTH_BUTTON, UserAuthButtonProperty } from './config'

defineOptions({ name: 'UserAuthButton' })

const props = defineProps<{ property: UserAuthButtonProperty }>()

const display = computed(() => ({
  ...DEFAULT_USER_AUTH_BUTTON,
  ...props.property
}))
</script>
