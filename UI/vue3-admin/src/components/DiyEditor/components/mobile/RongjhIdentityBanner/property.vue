<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="100px" :model="formData" class="mt-12px">
      <el-form-item label="跳转路径" prop="applyUrl">
        <AppLinkInput v-model="formData.applyUrl" />
      </el-form-item>
      <el-form-item label="未认证文案" prop="textNone">
        <el-input v-model="formData.textNone" placeholder="未认证时展示的提示文字" />
      </el-form-item>
      <el-form-item label="认证中文案" prop="textPending">
        <el-input v-model="formData.textPending" placeholder="审核中展示的提示文字" />
      </el-form-item>
      <el-form-item label="已认证文案" prop="textApproved">
        <el-input v-model="formData.textApproved" placeholder="认证通过后展示的提示文字" />
      </el-form-item>
      <el-form-item label="未认证按钮" prop="actionText">
        <el-input v-model="formData.actionText" placeholder="未认证时右侧按钮文字" />
      </el-form-item>
      <el-form-item label="被拒按钮" prop="actionTextRejected">
        <el-input v-model="formData.actionTextRejected" placeholder="认证被拒时右侧按钮文字" />
      </el-form-item>
      <el-form-item label="说明">
        <span class="text-12px color-gray">认证状态由接口自动读取，文案与跳转路径可在上方自定义。</span>
      </el-form-item>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { RongjhIdentityBannerProperty } from './config'
import { useVModel } from '@vueuse/core'

defineOptions({ name: 'RongjhIdentityBannerProperty' })

const props = defineProps<{ modelValue: RongjhIdentityBannerProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>
