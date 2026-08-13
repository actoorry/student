<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
    <el-form ref="formRef" :model="formData" label-width="90px">
      <el-form-item label="审核备注">
        <el-input v-model="formData.remark" type="textarea" :rows="4" placeholder="请输入审核备注（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as WarriorApi from '@/api/rongjh/warrior'

defineOptions({ name: 'RongjhWarriorAuditForm' })

const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const auditType = ref<'approve' | 'reject' | 'blacklist'>('approve')
const formData = ref({ id: 0, remark: '' })

const open = (type: 'approve' | 'reject' | 'blacklist', id: number) => {
  auditType.value = type
  formData.value = { id, remark: '' }
  dialogTitle.value = type === 'approve' ? '审核通过' : type === 'reject' ? '审核驳回' : '拉黑成员'
  dialogVisible.value = true
}

defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  formLoading.value = true
  try {
    const data = { id: formData.value.id, remark: formData.value.remark }
    if (auditType.value === 'approve') {
      await WarriorApi.approveWarrior(data)
      message.success('审核通过')
    } else if (auditType.value === 'reject') {
      await WarriorApi.rejectWarrior(data)
      message.success('已驳回')
    } else {
      await WarriorApi.blacklistWarrior(data)
      message.success('已拉黑')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
