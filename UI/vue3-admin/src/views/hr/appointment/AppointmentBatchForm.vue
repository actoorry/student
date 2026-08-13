<template>
  <Dialog title="批量岗位聘任" v-model="dialogVisible" width="680px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
      <el-form-item label="员工" prop="partnerIds">
        <el-select
          v-model="formData.partnerIds"
          filterable remote multiple reserve-keyword
          placeholder="请输入姓名搜索并多选"
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
import { AppointmentApi, AppointmentBatchVO } from '@/api/hr/appointment'
import { EmployeeApi, Employee } from '@/api/hr/employee'
import * as PostApi from '@/api/system/post'

defineOptions({ name: 'AppointmentBatchForm' })

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()

const formData = ref<Partial<AppointmentBatchVO>>({
  partnerIds: [],
  postCategory: undefined,
  postLevel: undefined,
  postId: undefined,
  startDate: undefined,
  termYears: 3,
  appointmentDoc: '',
  remark: undefined
})

const formRules = reactive({
  partnerIds: [{ required: true, message: '请至少选择一名员工', trigger: 'change' }],
  postCategory: [{ required: true, message: '岗位类别不能为空', trigger: 'change' }],
  postLevel: [{ required: true, message: '岗位等级不能为空', trigger: 'change' }],
  postId: [{ required: true, message: '聘任岗位不能为空', trigger: 'change' }],
  startDate: [{ required: true, message: '起始日不能为空', trigger: 'change' }],
  termYears: [{ required: true, message: '聘期不能为空', trigger: 'change' }]
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
  if (!name) return
  employeeLoading.value = true
  try {
    const data = await EmployeeApi.getEmployeePage({ pageNo: 1, pageSize: 30, name })
    employeeOptions.value = data.list
  } finally {
    employeeLoading.value = false
  }
}

const open = async () => {
  dialogVisible.value = true
  postList.value = await PostApi.getSimplePostList()
  formData.value = {
    partnerIds: [],
    postCategory: undefined,
    postLevel: undefined,
    postId: undefined,
    startDate: undefined,
    termYears: 3,
    appointmentDoc: '',
    remark: undefined
  }
  employeeOptions.value = []
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = { ...formData.value } as AppointmentBatchVO
    const doc = data.appointmentDoc
    data.appointmentDoc = Array.isArray(doc) ? doc.join(',') : (doc || '')
    const count = await AppointmentApi.batchCreateAppointment(data)
    message.success(`已成功聘任 ${count} 人`)
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
