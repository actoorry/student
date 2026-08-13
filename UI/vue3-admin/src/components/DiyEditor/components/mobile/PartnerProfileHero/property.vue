<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="80px" :model="formData">
      <el-card header="人物主图" class="property-group" shadow="never">
        <el-form-item label="组件高度" prop="height">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.height"
            :min="640"
            :max="1400"
          />
          rpx
        </el-form-item>
        <el-form-item label="头像尺寸" prop="avatarSize">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.avatarSize"
            :min="64"
            :max="160"
          />
          rpx
        </el-form-item>
        <el-form-item label="姓名颜色" prop="nameColor">
          <ColorInput v-model="formData.nameColor" />
        </el-form-item>
        <el-form-item label="年龄颜色" prop="ageColor">
          <ColorInput v-model="formData.ageColor" />
        </el-form-item>
      </el-card>

      <el-card header="运行时说明" class="property-group" shadow="never">
        <div class="text-12px text-gray-500 leading-20px">
          该组件的真实运行时内容依赖“人物推荐页面上下文”。请仅在已启用人物推荐模式的首页使用，否则组件内部将不展示任何人物资料。
        </div>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import { PartnerProfileHeroProperty } from './config'

/** 人物主图属性面板 */
defineOptions({ name: 'PartnerProfileHeroProperty' })

const props = defineProps<{ modelValue: PartnerProfileHeroProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>

<style scoped lang="scss"></style>
