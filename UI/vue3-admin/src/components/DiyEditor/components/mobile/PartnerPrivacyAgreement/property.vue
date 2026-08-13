<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="88px" :model="formData">
      <el-card header="入口设置" class="property-group" shadow="never">
        <el-form-item label="入口标题"><el-input v-model="formData.entryTitle" :maxlength="40" show-word-limit /></el-form-item>
        <el-form-item label="本地图标"><el-select v-model="formData.entryIcon"><el-option v-for="icon in PRIVACY_AGREEMENT_ICON_VALUES" :key="icon" :label="icon" :value="icon" /></el-select></el-form-item>
      </el-card>
      <el-card header="详情设置" class="property-group" shadow="never">
        <el-form-item label="详情标题"><el-input v-model="formData.detailTitle" :maxlength="40" show-word-limit /></el-form-item>
        <el-form-item label="导语"><el-input v-model="formData.detailSubtitle" type="textarea" :maxlength="200" show-word-limit /></el-form-item>
        <el-form-item label="协议章节"><el-button text type="primary" :disabled="formData.sections.length >= 10" @click="addSection">新增章节</el-button></el-form-item>
        <el-card v-for="(section, index) in formData.sections" :key="index" class="section-card" shadow="never">
          <div class="section-actions"><span>第 {{ index + 1 }} 节</span><el-button text type="danger" :disabled="formData.sections.length <= 1" @click="removeSection(index)">删除</el-button></div>
          <el-form-item label="标题"><el-input v-model="section.title" :maxlength="40" show-word-limit /></el-form-item>
          <el-form-item label="正文"><el-input v-model="section.content" type="textarea" :rows="4" :maxlength="2000" show-word-limit /></el-form-item>
        </el-card>
        <el-button plain @click="previewVisible = true">预览详情页</el-button>
      </el-card>
      <el-card header="治理说明" shadow="never"><div class="text-12px text-gray-500 leading-20px">该区块仅展示运营配置的说明文字，不代表登录授权、协议版本、用户同意或审计记录；固定详情页不会读取文章或任意路由。</div></el-card>
    </el-form>
    <el-dialog v-model="previewVisible" title="隐私协议详情预览" width="375px"><section class="detail-preview"><h1>{{ normalized.detailTitle }}</h1><p v-if="normalized.detailSubtitle">{{ normalized.detailSubtitle }}</p><article v-for="(section, index) in normalized.sections" :key="index"><h2>{{ section.title }}</h2><div>{{ section.content }}</div></article><el-empty v-if="!normalized.sections.length" description="协议内容暂不可用" /></section></el-dialog>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useVModel } from '@vueuse/core'
import { PRIVACY_AGREEMENT_ICON_VALUES, normalizePartnerPrivacyAgreementProperty, type PartnerPrivacyAgreementProperty } from './config'

defineOptions({ name: 'PartnerPrivacyAgreementProperty' })
const props = defineProps<{ modelValue: PartnerPrivacyAgreementProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
const previewVisible = ref(false)
const normalized = computed(() => normalizePartnerPrivacyAgreementProperty(formData.value))
function addSection() { if (formData.value.sections.length < 10) formData.value.sections.push({ title: '', content: '' }) }
function removeSection(index: number) { if (formData.value.sections.length > 1) formData.value.sections.splice(index, 1) }
</script>

<style scoped lang="scss">
.property-group { margin-bottom: 16px; }.section-card { margin: 0 0 12px; }.section-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }.detail-preview { color: #241818; }.detail-preview h1 { margin: 0 0 4px; font-size: 24px; line-height: 32px; }.detail-preview > p { margin: 0 0 16px; color: #705c58; line-height: 22px; }.detail-preview article { margin-bottom: 14px; padding: 16px; border: 1px solid rgba(214, 198, 190, .62); border-radius: 16px; background: #fffbf8; color: #705c58; white-space: pre-wrap; }.detail-preview h2 { margin: 0 0 8px; color: #241818; font-size: 17px; }
</style>
