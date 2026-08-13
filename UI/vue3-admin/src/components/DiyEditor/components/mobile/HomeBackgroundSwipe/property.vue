<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="80px" :model="formData">
      <el-card header="样式设置" class="property-group" shadow="never">
        <el-form-item label="组件高度" prop="height">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.height"
            :min="200"
            :max="1600"
          />
          rpx
        </el-form-item>
        <el-form-item label="左侧露出" prop="previousMargin">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.previousMargin"
            :min="0"
            :max="200"
          />
          rpx
        </el-form-item>
        <el-form-item label="右侧露出" prop="nextMargin">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.nextMargin"
            :min="0"
            :max="200"
          />
          rpx
        </el-form-item>
        <el-form-item label="初始索引" prop="initialIndex">
          <el-input-number
            class="!w-50% mr-10px"
            controls-position="right"
            v-model="formData.initialIndex"
            :min="0"
            :max="Math.max(0, (formData.slides?.length || 1) - 1)"
          />
        </el-form-item>
        <el-form-item label="指示器" prop="indicator">
          <el-radio-group v-model="formData.indicator">
            <el-radio value="dot">小圆点</el-radio>
            <el-radio value="number">数字</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-card>

      <el-card header="场景设置" class="property-group" shadow="never">
        <Draggable
          v-model="formData.slides"
          :empty-item="cloneDeep(EMPTY_HOME_BACKGROUND_SWIPE_ITEM_PROPERTY)"
          :limit="5"
          :min="1"
        >
          <template #default="{ element, index: slideIndex }">
            <el-form-item
              label="背景颜色"
              prop="backgroundColor"
              class="m-b-8px!"
              label-width="70px"
            >
              <ColorInput v-model="element.backgroundColor" />
            </el-form-item>
            <el-form-item
              label="背景图片"
              prop="backgroundImage"
              class="m-b-8px!"
              label-width="70px"
            >
              <UploadImg
                v-model="element.backgroundImage"
                draggable="false"
                height="80px"
                width="100%"
              >
                <template #tip> 建议尺寸：750 * 1334 </template>
              </UploadImg>
            </el-form-item>
            <el-form-item
              label="前景图片"
              prop="foregroundImage"
              class="m-b-8px!"
              label-width="70px"
            >
              <UploadImg
                v-model="element.foregroundImage"
                draggable="false"
                height="80px"
                width="100%"
              >
                <template #tip> 建议尺寸：300 * 300 </template>
              </UploadImg>
            </el-form-item>
            <el-form-item label="标题" prop="title" class="m-b-8px!" label-width="70px">
              <el-input v-model="element.title" placeholder="请输入标题" />
            </el-form-item>
            <el-form-item label="副标题" prop="subtitle" class="m-b-8px!" label-width="70px">
              <el-input v-model="element.subtitle" placeholder="请输入副标题" />
            </el-form-item>
            <el-form-item label="标签" prop="tags" class="m-b-8px!" label-width="70px">
              <el-tag
                v-for="(tag, tagIndex) in element.tags"
                :key="tagIndex"
                closable
                class="m-r-8px"
                @close="handleRemoveTag(element, tagIndex)"
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-if="element.tags.length < 3"
                v-model="inputTagMap[slideIndex]"
                placeholder="输入后回车"
                class="w-120px"
                size="small"
                @keyup.enter="handleAddTag(element, slideIndex)"
              />
            </el-form-item>
          </template>
        </Draggable>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useVModel } from '@vueuse/core'
import { cloneDeep } from 'lodash-es'
import { EMPTY_HOME_BACKGROUND_SWIPE_ITEM_PROPERTY, HomeBackgroundSwipeProperty } from './config'

/** 历史静态场景轮播属性面板 */
defineOptions({ name: 'HomeBackgroundSwipeProperty' })

const props = defineProps<{ modelValue: HomeBackgroundSwipeProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)

const inputTagMap = reactive<Record<string, string>>({})

const handleAddTag = (element: any, index: number) => {
  const tag = inputTagMap[index]?.trim()
  if (!tag) return
  if (!element.tags) element.tags = []
  if (element.tags.length >= 3) return
  if (!element.tags.includes(tag)) {
    element.tags.push(tag)
  }
  inputTagMap[index] = ''
}

const handleRemoveTag = (element: any, index: number) => {
  element.tags.splice(index, 1)
}
</script>

<style scoped lang="scss"></style>
