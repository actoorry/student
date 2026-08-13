<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="680px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <!-- 本人信息只读展示 -->
      <el-form-item label="姓名">
        <el-input :model-value="employeeLabel" disabled />
        <div v-if="selfEmployee" class="mt-5px text-12px text-gray-500">
          工号：{{ selfEmployee.employeeNo || '-' }} ｜ 科室：{{ selfEmployee.deptName || '-' }}
        </div>
      </el-form-item>
      <el-form-item label="证书类型" prop="certificateType">
        <el-select v-model="formData.certificateType" placeholder="请选择证书类型" clearable class="!w-full">
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_CERTIFICATE_TYPE)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="证书名称" prop="certificateName">
        <el-input v-model="formData.certificateName" placeholder="请输入证书名称（可覆盖字典默认名）" />
      </el-form-item>
      <el-form-item label="证书编号" prop="certificateNo">
        <el-input v-model="formData.certificateNo" placeholder="请输入证书编号" />
      </el-form-item>
      <el-form-item label="发证机关" prop="issuingAuthority">
        <el-input v-model="formData.issuingAuthority" placeholder="请输入发证机关" />
      </el-form-item>
      <el-form-item label="发证日期" prop="issueDate">
        <el-date-picker
          v-model="formData.issueDate"
          type="date"
          value-format="x"
          placeholder="选择发证日期"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="到期日期" prop="expireDate">
        <el-date-picker
          v-model="formData.expireDate"
          type="date"
          value-format="x"
          placeholder="选择到期日期"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="上次考核日" prop="lastAssessmentDate">
        <el-date-picker
          v-model="formData.lastAssessmentDate"
          type="date"
          value-format="x"
          placeholder="选择上次考核日"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="下次考核日" prop="nextAssessmentDate">
        <el-date-picker
          v-model="formData.nextAssessmentDate"
          type="date"
          value-format="x"
          placeholder="选择下次考核日"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="证书文件" prop="attachment">
        <UploadFile
          v-model="formData.attachment"
          :limit="1"
          :file-type="['jpg', 'jpeg', 'png', 'pdf']"
          :file-size="10"
        />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { MyCertificateApi } from '@/api/hr/my/certificate'
import type { Certificate } from '@/api/hr/certificate'
import { useMySelfEmployee } from '@/views/hr/my/composables/useMySelfEmployee'
import { useUserStore } from '@/store/modules/user'
import { UploadFile } from '@/components/UploadFile'

/** 职工我的证书 表单 */
defineOptions({ name: 'HrMyCertificateForm' })

const { t } = useI18n()
const message = useMessage()
const userStore = useUserStore()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const { selfEmployee, employeeLabel, loadSelfEmployee, resetSelfEmployee } = useMySelfEmployee()

const formData = ref({
  id: undefined,
  partnerId: undefined as number | undefined,
  employeeNo: undefined as string | undefined,
  dept: undefined as number | undefined,
  certificateType: undefined,
  certificateName: undefined,
  certificateNo: undefined,
  issuingAuthority: undefined,
  issueDate: undefined,
  expireDate: undefined,
  lastAssessmentDate: undefined,
  nextAssessmentDate: undefined,
  attachment: '',
  remark: undefined
})
const formRules = reactive({})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  const userId = userStore.user?.id
  if (userId) {
    formData.value.partnerId = userId
    const employee = await loadSelfEmployee()
    if (employee) {
      formData.value.employeeNo = employee.employeeNo
      formData.value.dept = employee.dept
    }
  }
  if (id) {
    formLoading.value = true
    try {
      formData.value = await MyCertificateApi.getMyCertificate(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = { ...formData.value } as unknown as Certificate
    data.partnerId = userStore.user?.id
    if (Array.isArray(data.attachment)) {
      data.attachment = (data.attachment as unknown as string[]).join(',')
    }
    if (formType.value === 'create') {
      await MyCertificateApi.createMyCertificate(data)
      message.success(t('common.createSuccess'))
    } else {
      await MyCertificateApi.updateMyCertificate(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    partnerId: undefined,
    employeeNo: undefined,
    dept: undefined,
    certificateType: undefined,
    certificateName: undefined,
    certificateNo: undefined,
    issuingAuthority: undefined,
    issueDate: undefined,
    expireDate: undefined,
    lastAssessmentDate: undefined,
    nextAssessmentDate: undefined,
    attachment: '',
    remark: undefined
  }
  resetSelfEmployee()
  formRef.value?.resetFields()
}
</script>
