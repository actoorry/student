<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="86px" :model="formData">
      <el-card header="资料项显示" class="property-group" shadow="never">
        <el-form-item
          v-for="field in BASIC_INFO_FIELDS"
          :key="field.toggleKey"
          :label="field.label"
          :prop="field.toggleKey"
        >
          <el-switch
            :model-value="getFieldVisibility(field.toggleKey)"
            active-text="显示"
            inactive-text="隐藏"
            @update:model-value="setFieldVisibility(field.toggleKey, $event)"
          />
        </el-form-item>
      </el-card>

      <el-card header="运行时说明" class="property-group" shadow="never">
        <div class="text-12px text-gray-500 leading-20px">
          该组件依赖“人物推荐页面上下文”，运行时只展示当前人物已填写且开关开启的资料。未提供人物上下文时，组件内部不会展示内容。
        </div>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import { BASIC_INFO_FIELDS, isBasicInfoFieldVisible } from './config'
import type {
  PartnerProfileBasicInfoProperty,
  PartnerProfileBasicInfoToggleKey
} from './config'

/** 基本资料属性面板 */
defineOptions({ name: 'PartnerProfileBasicInfoProperty' })

const props = defineProps<{ modelValue: PartnerProfileBasicInfoProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)

const getFieldVisibility = (key: PartnerProfileBasicInfoToggleKey) =>
  isBasicInfoFieldVisible(formData.value[key])

const setFieldVisibility = (
  key: PartnerProfileBasicInfoToggleKey,
  value: string | number | boolean
) => {
  formData.value[key] = value === true
}
</script>

<style scoped lang="scss"></style>
