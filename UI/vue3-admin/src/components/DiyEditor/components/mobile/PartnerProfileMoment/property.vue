<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="96px" :model="formData">
      <el-card header="TA 的动态" class="property-group" shadow="never">
        <el-form-item label="标题"><el-input v-model="formData.title" :maxlength="12" show-word-limit /></el-form-item>
        <el-form-item label="显示全部动态"><el-switch v-model="formData.showAllMoments" /></el-form-item>
        <el-form-item label="正文最大长度"><el-input-number v-model="formData.contentMaxLength" :min="20" :max="200" controls-position="right" /></el-form-item>
        <el-form-item label="最多图片数"><el-input-number v-model="formData.maxImages" :min="0" :max="3" controls-position="right" /></el-form-item>
        <el-form-item label="标题颜色"><ColorInput v-model="formData.titleColor" /></el-form-item>
        <el-form-item label="全部动态颜色"><ColorInput v-model="formData.allMomentsColor" /></el-form-item>
        <el-form-item label="正文颜色"><ColorInput v-model="formData.contentColor" /></el-form-item>
        <el-form-item label="元数据颜色"><ColorInput v-model="formData.metaColor" /></el-form-item>
        <el-form-item label="卡片背景"><ColorInput v-model="formData.cardBackgroundColor" /></el-form-item>
        <el-form-item label="卡片圆角"><el-input-number v-model="formData.cardRadius" :min="0" :max="80" controls-position="right" /> rpx</el-form-item>
      </el-card>
      <el-card header="运行时说明" class="property-group" shadow="never">
        <div class="text-12px text-gray-500 leading-20px">该组件只在人物推荐页面读取当前人物的最新已发布公开动态；普通页面无上下文时会安全隐藏内容。</div>
        <div class="text-12px text-gray-400 leading-20px mt-8px">“全部动态”是固定编译期入口，人物 ID、动态内容、图片、接口与路由均不可配置或保存到装修 JSON。</div>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>
<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import type { PartnerProfileMomentProperty } from './config'
defineOptions({ name: 'PartnerProfileMomentProperty' })
const props = defineProps<{ modelValue: PartnerProfileMomentProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>
<style scoped lang="scss">.property-group { margin-bottom: 16px; &:last-child { margin-bottom: 0; } }</style>
