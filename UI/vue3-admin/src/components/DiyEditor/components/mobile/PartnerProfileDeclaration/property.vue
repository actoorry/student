<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="88px" :model="formData">
      <el-card header="择偶宣言" class="property-group" shadow="never">
        <el-form-item label="区块标题" prop="title">
          <el-input
            v-model="formData.title"
            :maxlength="TITLE_MAX_LENGTH"
            show-word-limit
            placeholder="请输入区块标题"
          />
        </el-form-item>
        <el-form-item label="标题颜色" prop="titleColor">
          <ColorInput v-model="formData.titleColor" />
        </el-form-item>
        <el-form-item label="正文颜色" prop="contentColor">
          <ColorInput v-model="formData.contentColor" />
        </el-form-item>
        <el-form-item label="正文背景" prop="contentBackgroundColor">
          <ColorInput v-model="formData.contentBackgroundColor" />
        </el-form-item>
        <el-form-item label="正文圆角" prop="contentRadius">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.contentRadius"
            :min="CONTENT_RADIUS_RANGE.min"
            :max="CONTENT_RADIUS_RANGE.max"
          />
          rpx
        </el-form-item>
      </el-card>

      <el-card header="运行时说明" class="property-group" shadow="never">
        <div class="text-12px text-gray-500 leading-20px">
          正文来自人物推荐页面当前人物的真实资料，装修属性不能修改人物宣言。请仅在已启用人物推荐模式的首页使用。
        </div>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import { CONTENT_RADIUS_RANGE, TITLE_MAX_LENGTH } from './config'
import type { PartnerProfileDeclarationProperty } from './config'

/** 择偶宣言属性面板 */
defineOptions({ name: 'PartnerProfileDeclarationProperty' })

const props = defineProps<{ modelValue: PartnerProfileDeclarationProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>

<style scoped lang="scss"></style>
