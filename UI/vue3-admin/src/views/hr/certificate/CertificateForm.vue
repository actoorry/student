<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="680px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <el-form-item label="关联员工" prop="partnerId">
        <el-select
          v-model="formData.partnerId"
          placeholder="请搜索并选择员工"
          filterable
          remote
          :remote-method="loadEmployeeList"
          :loading="employeeLoading"
          clearable
          class="!w-full"
          @change="handleEmployeeChange"
        >
          <el-option
            v-for="item in employeeOptions"
            :key="item.partnerId"
            :label="formatEmployeeLabel(item)"
            :value="item.partnerId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="工号">
        <el-input v-model="formData.employeeNo" placeholder="选员工后自动带出" disabled />
      </el-form-item>
      <el-form-item label="科室" prop="dept">
        <el-tree-select
          v-model="formData.dept"
          :data="deptList"
          :props="defaultProps"
          check-strictly
          node-key="id"
          placeholder="选员工后自动带出"
          class="!w-full"
        />
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
          placeholder="选择到期日期（督查核心字段）"
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
import { CertificateApi, Certificate } from '@/api/hr/certificate'
import { EmployeeApi, Employee } from '@/api/hr/employee'
import * as DeptApi from '@/api/system/dept'
import { defaultProps, handleTree } from '@/utils/tree'
import { UploadFile } from '@/components/UploadFile'

/** 人员证书 表单 */
defineOptions({ name: 'CertificateForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const employeeLoading = ref(false)
const employeeOptions = ref<Employee[]>([])
const deptList = ref<Tree[]>([])

const formatEmployeeLabel = (item: Employee) => {
  const extra = item.employeeNo || item.employeeMobile
  return extra ? `${item.name}（${extra}）` : item.name || ''
}

const loadEmployeeList = async (query: string) => {
  if (!query) {
    employeeOptions.value = []
    return
  }
  employeeLoading.value = true
  try {
    const data = await EmployeeApi.getEmployeePage({ name: query, pageNo: 1, pageSize: 20 })
    employeeOptions.value = data.list || []
  } finally {
    employeeLoading.value = false
  }
}

const handleEmployeeChange = (partnerId: number) => {
  const employee = employeeOptions.value.find((item) => item.partnerId === partnerId)
  if (employee) {
    formData.value.employeeNo = employee.employeeNo
    formData.value.dept = employee.dept
  }
}

const formData = ref({
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
})
const formRules = reactive({
  partnerId: [{ required: true, message: '关联员工不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  deptList.value = handleTree(await DeptApi.getSimpleDeptList())
  if (id) {
    formLoading.value = true
    try {
      formData.value = await CertificateApi.getCertificate(id)
      if (formData.value.partnerId && formData.value.name) {
        employeeOptions.value = [{
          partnerId: formData.value.partnerId,
          name: formData.value.name,
          employeeNo: formData.value.employeeNo
        } as Employee]
      }
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
    if (Array.isArray(data.attachment)) {
      data.attachment = data.attachment.join(',')
    }
    if (formType.value === 'create') {
      await CertificateApi.createCertificate(data)
      message.success(t('common.createSuccess'))
    } else {
      await CertificateApi.updateCertificate(data)
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
  employeeOptions.value = []
  formRef.value?.resetFields()
}
</script>
