<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="分组名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分组名称" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入备注" type="textarea" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as PartnerGroupApi from '@/api/partner/group'
import { DICT_TYPE } from '@/utils/dict'
import { getIntDictOptions } from '@/utils/dict'
import { useMessage } from '@/hooks/web/useMessage'

const message = useMessage()
const { t } = useI18n()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formType = ref('')
const formData = reactive({ id: undefined, name: '', remark: '', status: 0 })
const formRules = reactive({ name: [{ required: true, message: '分组名称不能为空', trigger: 'blur' }] })
const formRef = ref()
const emit = defineEmits(['success'])

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    const data = await PartnerGroupApi.getMemberGroup(id)
    Object.assign(formData, data)
  }
}
const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.remark = ''
  formData.status = 0
  formRef.value?.resetFields()
}
const submitForm = async () => {
  await formRef.value.validate()
  if (formType.value === 'create') {
    await PartnerGroupApi.createMemberGroup(formData)
    message.success(t('common.createSuccess'))
  } else {
    await PartnerGroupApi.updateMemberGroup(formData)
    message.success(t('common.updateSuccess'))
  }
  dialogVisible.value = false
  emit('success')
}
defineExpose({ open })
</script>
