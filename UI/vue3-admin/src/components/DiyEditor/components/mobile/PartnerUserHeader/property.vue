<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-card header="运行时说明" shadow="never">
      <div class="text-12px text-gray-500 leading-20px">
        该组件用于当前登录用户的“我的”页面。背景、头像、昵称、二维码和编辑动作均由已编译客户端提供，不会保存到装修配置；无自定义背景时显示默认占位图。
      </div>
      <div class="text-12px text-gray-400 leading-20px mt-8px">
        登录用户可点击背景查看或更换图片。在首页或独立页面使用时，组件会安全显示匿名状态；请仅配置公共容器样式。
      </div>
    </el-card>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import type { PartnerUserHeaderProperty } from './config'

defineOptions({ name: 'PartnerUserHeaderProperty' })

const props = defineProps<{ modelValue: PartnerUserHeaderProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>
