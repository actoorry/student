<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="90px" :model="formData" class="m-t-8px">
      <el-card header="头部背景" class="property-group" shadow="never">
        <el-form-item label="背景图片" prop="background.image">
          <UploadImg
            v-model="formData.background.image"
            draggable="false"
            height="90px"
            width="100%"
          >
            <template #tip>建议宽度 750px，重要内容放在图片中央安全区</template>
          </UploadImg>
        </el-form-item>
        <el-form-item label="背景颜色" prop="background.color">
          <ColorInput v-model="formData.background.color" />
        </el-form-item>
        <el-form-item label="展示高度" prop="background.height">
          <div class="slider-with-value">
            <el-slider v-model="formData.background.height" :min="320" :max="800" />
            <span class="slider-value">{{ formData.background.height }} rpx</span>
          </div>
        </el-form-item>
      </el-card>

      <el-card header="头部布局" class="property-group" shadow="never">
        <el-form-item label="是否吸顶" prop="sticky">
          <el-switch
            :model-value="formData.sticky !== false"
            @change="formData.sticky = Boolean($event)"
          />
        </el-form-item>
        <el-form-item label="搜索导航间距" prop="spacing.searchMenuGap">
          <div class="slider-with-value">
            <el-slider v-model="formData.spacing.searchMenuGap" :min="0" :max="40" />
            <span class="slider-value">{{ formData.spacing.searchMenuGap }} rpx</span>
          </div>
        </el-form-item>
        <el-form-item label="导航底部间距" prop="spacing.menuBottomGap">
          <div class="slider-with-value">
            <el-slider v-model="formData.spacing.menuBottomGap" :min="0" :max="40" />
            <span class="slider-value">{{ formData.spacing.menuBottomGap }} rpx</span>
          </div>
        </el-form-item>
      </el-card>

      <el-card header="搜索栏" class="property-group" shadow="never">
        <el-form-item label="提示文字" prop="search.placeholder">
          <el-input v-model="formData.search.placeholder" />
        </el-form-item>
        <el-form-item label="文本位置" prop="search.placeholderPosition">
          <el-radio-group v-model="formData.search.placeholderPosition">
            <el-radio value="left">居左</el-radio>
            <el-radio value="center">居中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="扫一扫" prop="search.showScan">
          <el-switch v-model="formData.search.showScan" />
        </el-form-item>
        <el-form-item label="框体高度" prop="search.height">
          <el-slider v-model="formData.search.height" :min="28" :max="50" show-input />
        </el-form-item>
        <el-form-item label="框体圆角" prop="search.borderRadius">
          <el-slider v-model="formData.search.borderRadius" :min="0" :max="30" show-input />
        </el-form-item>
        <el-form-item label="框体颜色" prop="search.backgroundColor">
          <ColorInput v-model="formData.search.backgroundColor" />
        </el-form-item>
        <el-form-item label="文本颜色" prop="search.textColor">
          <ColorInput v-model="formData.search.textColor" />
        </el-form-item>
        <el-card header="搜索热词" shadow="never">
          <Draggable v-model="formData.search.hotKeywords" :empty-item="''" :min="0">
            <template #default="{ index }">
              <el-input v-model="formData.search.hotKeywords[index]" placeholder="请输入热词" />
            </template>
          </Draggable>
        </el-card>
      </el-card>

      <el-card header="文字分类导航" class="property-group" shadow="never">
        <el-form-item label="每屏显示" prop="menu.column">
          <el-radio-group v-model="formData.menu.column">
            <el-radio :value="3">3个</el-radio>
            <el-radio :value="4">4个</el-radio>
            <el-radio :value="5">5个</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文字颜色" prop="menu.textColor">
          <ColorInput v-model="formData.menu.textColor" />
        </el-form-item>
        <el-form-item label="文字大小" prop="menu.fontSize">
          <el-slider v-model="formData.menu.fontSize" :min="10" :max="20" show-input />
        </el-form-item>
        <el-text type="info" size="small">
          固定为单行横向滑动；拖动左侧小圆点调整菜单顺序。
        </el-text>
        <Draggable
          v-model="formData.menu.list"
          :empty-item="cloneDeep(EMPTY_MENU_TEXT_GRID_ITEM_PROPERTY)"
        >
          <template #default="{ element }">
            <el-form-item label="标题" prop="title">
              <InputWithColor v-model="element.title" v-model:color="element.titleColor" />
            </el-form-item>
            <el-form-item label="链接" prop="url">
              <AppLinkInput v-model="element.url" />
            </el-form-item>
          </template>
        </Draggable>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import { cloneDeep } from 'lodash-es'
import { EMPTY_MENU_TEXT_GRID_ITEM_PROPERTY } from '@/components/DiyEditor/components/mobile/MenuTextGrid/config'
import { component, StickySearchMenuHeaderProperty } from './config'

defineOptions({ name: 'StickySearchMenuHeaderProperty' })

const props = defineProps<{ modelValue: StickySearchMenuHeaderProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)

// 兼容保存过但字段不完整的装修 JSON，避免属性面板因新增子字段而无法编辑。
formData.value.background = {
  ...cloneDeep(component.property.background),
  ...(formData.value.background || {})
}
formData.value.spacing = {
  ...cloneDeep(component.property.spacing),
  ...(formData.value.spacing || {})
}
formData.value.search = {
  ...cloneDeep(component.property.search),
  ...(formData.value.search || {}),
  style: {
    ...cloneDeep(component.property.search.style),
    ...(formData.value.search?.style || {})
  }
}
formData.value.menu = {
  ...cloneDeep(component.property.menu),
  ...(formData.value.menu || {}),
  style: {
    ...cloneDeep(component.property.menu.style),
    ...(formData.value.menu?.style || {})
  }
}

watchEffect(() => {
  formData.value.menu.scrollable = true
  formData.value.menu.row = 1
})
</script>

<style scoped lang="scss">
.slider-with-value {
  display: flex;
  width: 100%;
  min-width: 0;
  align-items: center;
  gap: 12px;

  :deep(.el-slider) {
    min-width: 0;
    flex: 1;
  }
}

.slider-value {
  min-width: 68px;
  color: var(--el-text-color-regular);
  text-align: right;
  white-space: nowrap;
}
</style>
