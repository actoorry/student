<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
      <el-form-item v-if="auditType === 'approve'" label="批准金额" prop="actualAmount">
        <el-input-number
          v-model="formData.actualAmount"
          :min="0.01"
          :precision="2"
          :step="100"
          class="!w-full"
          placeholder="请输入批准金额"
        />
      </el-form-item>
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
import type { FormRules } from 'element-plus'
import * as HelpApi from '@/api/rongjh/help'

defineOptions({ name: 'RongjhHelpAuditForm' })

const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const auditType = ref<'approve' | 'reject'>('approve')
const formRef = ref()
const formData = ref({ id: 0, actualAmount: undefined as number | undefined, remark: '' })

const rules = reactive<FormRules>({
  actualAmount: [{ required: true, message: '批准金额不能为空', trigger: 'blur' }]
})

const open = (type: 'approve' | 'reject', id: number) => {
  auditType.value = type
  formData.value = { id, actualAmount: undefined, remark: '' }
  dialogTitle.value = type === 'approve' ? '审核通过' : '审核驳回'
  dialogVisible.value = true
}

defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (auditType.value === 'approve') {
    await formRef.value?.validate()
  }
  formLoading.value = true
  try {
    if (auditType.value === 'approve') {
      await HelpApi.approveHelp({
        id: formData.value.id,
        actualAmount: Number(formData.value.actualAmount),
        remark: formData.value.remark
      })
      message.success('审核通过')
    } else {
      await HelpApi.rejectHelp({ id: formData.value.id, remark: formData.value.remark })
      message.success('已驳回')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
