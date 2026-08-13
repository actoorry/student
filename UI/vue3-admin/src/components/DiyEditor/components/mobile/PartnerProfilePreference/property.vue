<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="88px" :model="formData">
      <el-card header="择偶条件" class="property-group" shadow="never">
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
        <el-form-item label="强调线颜色" prop="accentColor">
          <ColorInput v-model="formData.accentColor" />
        </el-form-item>
        <el-form-item label="标签文字色" prop="tagTextColor">
          <ColorInput v-model="formData.tagTextColor" />
        </el-form-item>
        <el-form-item label="标签背景色" prop="tagBackgroundColor">
          <ColorInput v-model="formData.tagBackgroundColor" />
        </el-form-item>
        <el-form-item label="标签圆角" prop="tagRadius">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.tagRadius"
            :min="TAG_RADIUS_RANGE.min"
            :max="TAG_RADIUS_RANGE.max"
          />
          rpx
        </el-form-item>
      </el-card>

      <el-card header="运行时说明" class="property-group" shadow="never">
        <div class="text-12px text-gray-500 leading-20px">
          标签来自人物推荐页面当前人物的真实择偶条件（服务端生成），装修属性只能控制区块标题和标签视觉，不能修改人物择偶内容。请仅在已启用人物推荐模式的首页使用。
        </div>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import { TAG_RADIUS_RANGE, TITLE_MAX_LENGTH } from './config'
import type { PartnerProfilePreferenceProperty } from './config'

/** 择偶条件属性面板 */
defineOptions({ name: 'PartnerProfilePreferenceProperty' })

const props = defineProps<{ modelValue: PartnerProfilePreferenceProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>

<style scoped lang="scss"></style>
