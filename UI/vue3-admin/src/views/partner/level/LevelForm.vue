<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
      <el-form-item label="等级名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入等级名称" />
      </el-form-item>
      <el-form-item label="等级值" prop="level">
        <el-input-number v-model="formData.level" :min="0" placeholder="请输入等级值" />
      </el-form-item>
      <el-form-item label="升级经验" prop="experience">
        <el-input-number v-model="formData.experience" :min="0" placeholder="请输入升级经验" />
      </el-form-item>
      <el-form-item label="折扣百分比" prop="discountPercent">
        <el-input-number v-model="formData.discountPercent" :min="0" :max="100" placeholder="0-100" />
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <el-input v-model="formData.icon" placeholder="请输入图标URL" />
      </el-form-item>
      <el-form-item label="背景图" prop="backgroundUrl">
        <el-input v-model="formData.backgroundUrl" placeholder="请输入背景图URL" />
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
import * as PartnerLevelApi from '@/api/partner/level'
import { DICT_TYPE } from '@/utils/dict'
import { getIntDictOptions } from '@/utils/dict'
import { useMessage } from '@/hooks/web/useMessage'

const message = useMessage()
const { t } = useI18n()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formType = ref('')
const formData = reactive({ id: undefined, name: '', level: 0, experience: 0, discountPercent: 100, icon: '', backgroundUrl: '', status: 0 })
const formRules = reactive({
  name: [{ required: true, message: '等级名称不能为空', trigger: 'blur' }],
  level: [{ required: true, message: '等级值不能为空', trigger: 'blur' }],
  experience: [{ required: true, message: '升级经验不能为空', trigger: 'blur' }]
})
const formRef = ref()
const emit = defineEmits(['success'])

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    const data = await PartnerLevelApi.getMemberLevel(id)
    Object.assign(formData, data)
  }
}
const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.level = 0
  formData.experience = 0
  formData.discountPercent = 100
  formData.icon = ''
  formData.backgroundUrl = ''
  formData.status = 0
  formRef.value?.resetFields()
}
const submitForm = async () => {
  await formRef.value.validate()
  if (formType.value === 'create') {
    await PartnerLevelApi.createMemberLevel(formData)
    message.success(t('common.createSuccess'))
  } else {
    await PartnerLevelApi.updateMemberLevel(formData)
    message.success(t('common.updateSuccess'))
  }
  dialogVisible.value = false
  emit('success')
}
defineExpose({ open })
</script>
