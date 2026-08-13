<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="100px" :model="formData" class="mt-12px">
      <el-form-item label="登录文案" prop="loginText">
        <el-input v-model="formData.loginText" placeholder="未登录时展示的按钮文字" />
      </el-form-item>
      <el-form-item label="退出文案" prop="logoutText">
        <el-input v-model="formData.logoutText" placeholder="已登录时展示的按钮文字" />
      </el-form-item>
      <el-form-item label="退出提示" prop="logoutConfirmText">
        <el-input v-model="formData.logoutConfirmText" placeholder="点击退出时的确认提示" />
      </el-form-item>
      <el-form-item label="说明">
        <span class="text-12px color-gray">未登录显示登录按钮，已登录显示退出按钮；状态由系统自动判断。</span>
      </el-form-item>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { UserAuthButtonProperty } from './config'
import { useVModel } from '@vueuse/core'

defineOptions({ name: 'UserAuthButtonProperty' })

const props = defineProps<{ modelValue: UserAuthButtonProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>
