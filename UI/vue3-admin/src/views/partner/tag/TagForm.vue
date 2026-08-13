<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="标签名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入标签名称" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as PartnerTagApi from '@/api/partner/tag'
import { useMessage } from '@/hooks/web/useMessage'

const message = useMessage()
const { t } = useI18n()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formType = ref('')
const formData = reactive({ id: undefined, name: '' })
const formRules = reactive({ name: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }] })
const formRef = ref()
const emit = defineEmits(['success'])

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    const data = await PartnerTagApi.getMemberTag(id)
    Object.assign(formData, data)
  }
}
const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formRef.value?.resetFields()
}
const submitForm = async () => {
  await formRef.value.validate()
  if (formType.value === 'create') {
    await PartnerTagApi.createMemberTag(formData)
    message.success(t('common.createSuccess'))
  } else {
    await PartnerTagApi.updateMemberTag(formData)
    message.success(t('common.updateSuccess'))
  }
  dialogVisible.value = false
  emit('success')
}
defineExpose({ open })
</script>
