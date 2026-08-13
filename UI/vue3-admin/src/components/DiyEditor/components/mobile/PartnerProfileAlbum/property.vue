<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="80px" :model="formData">
      <el-card header="相册" class="property-group" shadow="never">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="formData.title"
            :maxlength="12"
            show-word-limit
            placeholder="相册"
          />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input
            v-model="formData.subtitle"
            :maxlength="20"
            show-word-limit
            placeholder="真实生活照片（留空隐藏）"
          />
        </el-form-item>
        <el-form-item label="标题颜色" prop="titleColor">
          <ColorInput v-model="formData.titleColor" />
        </el-form-item>
        <el-form-item label="副标题色" prop="subtitleColor">
          <ColorInput v-model="formData.subtitleColor" />
        </el-form-item>
        <el-form-item label="图片高度" prop="imageHeight">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.imageHeight"
            :min="120"
            :max="480"
          />
          rpx
        </el-form-item>
        <el-form-item label="图片圆角" prop="imageRadius">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.imageRadius"
            :min="0"
            :max="64"
          />
          rpx
        </el-form-item>
      </el-card>

      <el-card header="运行时说明" class="property-group" shadow="never">
        <div class="text-12px text-gray-500 leading-20px">
          该组件的真实运行时内容依赖"人物推荐页面上下文"。请仅在已启用人物推荐模式的首页使用，否则组件内部将不展示任何内容。
        </div>
        <div class="text-12px text-gray-400 leading-20px mt-8px">
          运行特性：仅展示最多 9 张人物生活相册图片（albumImages），人物主图不会被纳入相册；已登录用户点击已成功加载的相册图片时会同时记录"谁看过我"关系；图片全部加载失败或无上下文时组件内部自动隐藏。
        </div>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import type { PartnerProfileAlbumProperty } from './config'

/** 相册属性面板 */
defineOptions({ name: 'PartnerProfileAlbumProperty' })

const props = defineProps<{ modelValue: PartnerProfileAlbumProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>

<style scoped lang="scss">
.property-group {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}
</style>
