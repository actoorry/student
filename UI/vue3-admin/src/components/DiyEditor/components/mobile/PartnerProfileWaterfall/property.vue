<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="110px">
      <el-divider content-position="left">人物推荐规则</el-divider>
      <el-form-item label="仅实名">
        <el-switch v-model="formData.rule.realVerifiedOnly" />
      </el-form-item>
      <el-form-item label="必须有背景图">
        <el-switch v-model="formData.rule.backgroundImageRequired" />
      </el-form-item>
      <el-form-item label="仅异性">
        <el-switch v-model="formData.rule.oppositeSexOnly" />
      </el-form-item>
      <el-alert
        class="mb-16px"
        title="规则仅作用于当前人物信息卡瀑布流组件；默认全部关闭。"
        type="info"
        :closable="false"
      />

      <el-divider content-position="left">显示设置</el-divider>
      <el-form-item label="显示姓名">
        <el-switch v-model="formData.fields.name.show" />
      </el-form-item>
      <el-form-item label="显示联系按钮">
        <el-switch v-model="formData.contactButton.show" />
      </el-form-item>
      <el-form-item label="显示资料按钮">
        <el-switch v-model="formData.profileButton.show" />
      </el-form-item>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { useVModel } from '@vueuse/core'
import type { PartnerProfileWaterfallProperty } from './config'

defineOptions({ name: 'PartnerProfileWaterfallProperty' })

const props = defineProps<{ modelValue: PartnerProfileWaterfallProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)

if (!formData.value.rule) {
  formData.value.rule = {
    realVerifiedOnly: false,
    backgroundImageRequired: false,
    oppositeSexOnly: false
  }
}
</script>
