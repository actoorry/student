<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="88px" :model="formData">
      <el-card header="实名认证标志" class="property-group" shadow="never">
        <el-form-item label="卡片背景" prop="badgeBackgroundColor">
          <ColorInput v-model="formData.badgeBackgroundColor" />
        </el-form-item>
        <el-form-item label="强调色" prop="accentColor">
          <ColorInput v-model="formData.accentColor" />
        </el-form-item>
        <el-form-item label="标题颜色" prop="titleColor">
          <ColorInput v-model="formData.titleColor" />
        </el-form-item>
        <el-form-item label="说明颜色" prop="descriptionColor">
          <ColorInput v-model="formData.descriptionColor" />
        </el-form-item>
      </el-card>

      <el-card header="运行时说明" class="property-group" shadow="never">
        <div class="text-12px text-gray-500 leading-20px">
          该组件的真实内容依赖“人物推荐页面上下文”，仅在当前人物已完成实名认证时显示固定文案“实名认证 / 身份证前四位：XXXX”。<br />
          可信文案、显示条件和数据来源由代码持有，不可通过装修属性配置。
        </div>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import { PartnerRealNameVerificationBadgeProperty } from './config'

/** 实名认证标志属性面板 */
defineOptions({ name: 'PartnerRealNameVerificationBadgeProperty' })

const props = defineProps<{ modelValue: PartnerRealNameVerificationBadgeProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>

<style scoped lang="scss"></style>
