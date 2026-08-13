<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="680px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
      <el-form-item label="员工" prop="partnerId">
        <el-select
          v-model="formData.partnerId"
          filterable remote reserve-keyword clearable
          placeholder="请输入员工姓名搜索"
          :remote-method="searchEmployee"
          :loading="employeeLoading"
          class="!w-full"
        >
          <el-option
            v-for="item in employeeOptions"
            :key="item.partnerId"
            :label="`${item.name}${item.employeeNo ? '（' + item.employeeNo + ')' : ''}`"
            :value="item.partnerId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="岗位类别" prop="postCategory">
        <el-select v-model="formData.postCategory" placeholder="请选择" class="!w-full" @change="formData.postLevel = undefined">
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_POST_CATEGORY)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="岗位等级" prop="postLevel">
        <el-select v-model="formData.postLevel" placeholder="请选择" class="!w-full">
          <el-option v-for="dict in filteredPostLevels" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="聘任岗位" prop="postId">
        <el-select v-model="formData.postId" placeholder="请选择岗位" clearable filterable class="!w-full">
          <el-option
            v-for="item in postList"
            :key="item.id"
            :label="item.name"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="起始日" prop="startDate">
        <el-date-picker v-model="formData.startDate" type="date" value-format="x" placeholder="选择起始日" class="!w-full" />
      </el-form-item>
      <el-form-item label="聘期(年)" prop="termYears">
        <el-input-number v-model="formData.termYears" :min="1" :max="10" class="!w-full" />
      </el-form-item>
      <el-form-item label="到期日" prop="endDate">
        <el-date-picker v-model="formData.endDate" type="date" value-format="x" placeholder="可留空自动计算" class="!w-full" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择" class="!w-full">
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_APPOINTMENT_STATUS)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="当前聘任" prop="isCurrent">
        <el-radio-group v-model="formData.isCurrent">
          <el-radio :value="1">是</el-radio>
          <el-radio :value="0">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="聘任文件">
        <UploadFile v-model="formData.appointmentDoc" :limit="5" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="2" />
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
import { AppointmentApi, AppointmentVO } from '@/api/hr/appointment'
import { EmployeeApi, Employee } from '@/api/hr/employee'
import * as PostApi from '@/api/system/post'

defineOptions({ name: 'AppointmentForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formRef = ref()

const formData = ref<Partial<AppointmentVO>>({
  partnerId: undefined,
  postCategory: undefined,
  postLevel: undefined,
  postId: undefined,
  startDate: undefined,
  endDate: undefined,
  termYears: 3,
  appointmentDoc: '',
  status: 'active',
  isCurrent: 1,
  remark: undefined
})

const formRules = reactive({
  partnerId: [{ required: true, message: '员工不能为空', trigger: 'change' }],
  postCategory: [{ required: true, message: '岗位类别不能为空', trigger: 'change' }],
  postLevel: [{ required: true, message: '岗位等级不能为空', trigger: 'change' }],
  postId: [{ required: true, message: '聘任岗位不能为空', trigger: 'change' }],
  startDate: [{ required: true, message: '起始日不能为空', trigger: 'change' }]
})

const postList = ref<PostApi.PostVO[]>([])

const allPostLevels = getStrDictOptions(DICT_TYPE.HR_POST_LEVEL)
const filteredPostLevels = computed(() => {
  if (!formData.value.postCategory) return allPostLevels
  return allPostLevels.filter((d) => !d.remark || d.remark === formData.value.postCategory)
})

const employeeOptions = ref<Employee[]>([])
const employeeLoading = ref(false)
const searchEmployee = async (name: string) => {
  if (!name) {
    employeeOptions.value = []
    return
  }
  employeeLoading.value = true
  try {
    const data = await EmployeeApi.getEmployeePage({ pageNo: 1, pageSize: 20, name })
    employeeOptions.value = data.list
  } finally {
    employeeLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    partnerId: undefined,
    postCategory: undefined,
    postLevel: undefined,
    postId: undefined,
    startDate: undefined,
    endDate: undefined,
    termYears: 3,
    appointmentDoc: '',
    status: 'active',
    isCurrent: 1,
    remark: undefined
  }
  employeeOptions.value = []
  formRef.value?.resetFields()
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  postList.value = await PostApi.getSimplePostList()
  if (id) {
    formLoading.value = true
    try {
      const data = await AppointmentApi.getAppointment(id)
      formData.value = { ...data, appointmentDoc: data.appointmentDoc || '' }
      if (data.partnerId && data.partnerName) {
        employeeOptions.value = [{ partnerId: data.partnerId, name: data.partnerName, employeeNo: data.employeeNo } as Employee]
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = { ...formData.value } as AppointmentVO
    const doc = data.appointmentDoc
    data.appointmentDoc = Array.isArray(doc) ? doc.join(',') : (doc || '')
    if (formType.value === 'create') {
      await AppointmentApi.createAppointment(data)
      message.success(t('common.createSuccess'))
    } else {
      await AppointmentApi.updateAppointment(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
