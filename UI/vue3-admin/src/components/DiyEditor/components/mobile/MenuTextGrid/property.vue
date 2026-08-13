<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="80px" :model="formData" class="m-t-8px">
      <el-form-item label="横向滑动" prop="scrollable">
        <el-switch v-model="formData.scrollable" />
      </el-form-item>
      <template v-if="formData.scrollable !== true">
        <el-form-item label="行数" prop="row">
          <el-radio-group v-model="formData.row">
            <el-radio :value="1">1行</el-radio>
            <el-radio :value="2">2行</el-radio>
            <el-radio :value="3">3行</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
      <el-form-item :label="formData.scrollable === true ? '每屏显示' : '列数'" prop="column">
        <el-radio-group v-model="formData.column">
          <el-radio :value="3">{{ formData.scrollable === true ? '3个' : '3列' }}</el-radio>
          <el-radio :value="4">{{ formData.scrollable === true ? '4个' : '4列' }}</el-radio>
          <el-radio :value="5">{{ formData.scrollable === true ? '5个' : '5列' }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="文字颜色" prop="textColor">
        <ColorInput v-model="formData.textColor" />
      </el-form-item>
      <el-form-item label="文字大小" prop="fontSize">
        <el-slider v-model="formData.fontSize" :min="10" :max="20" show-input />
      </el-form-item>

      <el-card header="菜单设置" class="property-group" shadow="never">
        <el-text type="info" size="small">
          {{
            formData.scrollable === true
              ? '拖动左侧小圆点可调整顺序，按每屏显示数量单行横向滑动'
              : '拖动左侧小圆点可调整顺序，超出每页格数自动分页'
          }}
        </el-text>
        <Draggable
          v-model="formData.list"
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
import {
  EMPTY_MENU_TEXT_GRID_ITEM_PROPERTY,
  MenuTextGridProperty
} from '@/components/DiyEditor/components/mobile/MenuTextGrid/config'

/** 文字分类导航区属性面板 */
defineOptions({ name: 'MenuTextGridProperty' })

const props = defineProps<{ modelValue: MenuTextGridProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>

<style scoped lang="scss"></style>
