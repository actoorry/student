<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="720px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
      <el-form-item label="员工" prop="partnerId">
        <el-select
          v-model="formData.partnerId"
          filterable remote reserve-keyword clearable
          placeholder="请输入员工姓名搜索"
          :remote-method="searchEmployee"
          :loading="employeeLoading"
          class="!w-full"
          @change="onEmployeeChange"
        >
          <el-option
            v-for="item in employeeOptions"
            :key="item.partnerId"
            :label="`${item.name}${item.employeeNo ? '（' + item.employeeNo + ')' : ''}`"
            :value="item.partnerId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="异动类型" prop="changeType">
        <el-select v-model="formData.changeType" placeholder="请选择" class="!w-full">
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_POSITION_CHANGE_TYPE)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="原部门">
        <el-tree-select v-model="formData.fromDept" :data="deptList" :props="defaultProps" check-strictly node-key="id" placeholder="默认取员工当前部门" class="!w-full" />
      </el-form-item>
      <el-form-item label="新部门" prop="toDept">
        <el-tree-select v-model="formData.toDept" :data="deptList" :props="defaultProps" check-strictly node-key="id" placeholder="请选择新部门" class="!w-full" />
      </el-form-item>
      <el-form-item label="原岗位">
        <el-select v-model="formData.fromPostId" placeholder="默认取员工当前岗位" clearable filterable class="!w-full">
          <el-option
            v-for="item in postList"
            :key="item.id"
            :label="item.name"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="新岗位" prop="toPostId">
        <el-select v-model="formData.toPostId" placeholder="请选择新岗位" clearable filterable class="!w-full">
          <el-option
            v-for="item in postList"
            :key="item.id"
            :label="item.name"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker v-model="formData.startDate" type="date" value-format="x" placeholder="选择开始日期" class="!w-full" />
      </el-form-item>
      <el-form-item label="结束日期">
        <el-date-picker v-model="formData.endDate" type="date" value-format="x" placeholder="可选" class="!w-full" />
      </el-form-item>
      <el-form-item label="变动原因">
        <el-input v-model="formData.reason" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="批文附件">
        <UploadFile v-model="formData.docAttachment" :limit="5" />
      </el-form-item>
      <el-form-item label="同步员工档案">
        <el-switch v-model="formData.syncEmployee" :active-value="1" :inactive-value="0" />
        <span class="ml-10px text-12px text-gray-500">开启后将更新员工档案部门/岗位</span>
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
import { defaultProps, handleTree } from '@/utils/tree'
import * as DeptApi from '@/api/system/dept'
import * as PostApi from '@/api/system/post'
import { PositionChangeApi, PositionChangeVO } from '@/api/hr/positionChange'
import { EmployeeApi, Employee } from '@/api/hr/employee'

defineOptions({ name: 'PositionChangeForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formRef = ref()
const deptList = ref<any[]>([])
const postList = ref<PostApi.PostVO[]>([])

const formData = ref<Partial<PositionChangeVO>>({
  partnerId: undefined,
  changeType: undefined,
  fromDept: undefined,
  toDept: undefined,
  fromPostId: undefined,
  toPostId: undefined,
  startDate: undefined,
  endDate: undefined,
  reason: undefined,
  docAttachment: '',
  syncEmployee: 0,
  remark: undefined
})

const formRules = reactive({
  partnerId: [{ required: true, message: '员工不能为空', trigger: 'change' }],
  changeType: [{ required: true, message: '异动类型不能为空', trigger: 'change' }],
  toPostId: [{ required: true, message: '新岗位不能为空', trigger: 'change' }],
  startDate: [{ required: true, message: '开始日期不能为空', trigger: 'change' }]
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

const onEmployeeChange = (partnerId?: number) => {
  const emp = employeeOptions.value.find((e) => e.partnerId === partnerId)
  if (emp) {
    formData.value.fromDept = emp.dept
    formData.value.fromPostId = emp.postId
  }
}

const loadDeptList = async () => {
  const data = await DeptApi.getSimpleDeptList()
  deptList.value = handleTree(data)
}

const resetForm = () => {
  formData.value = {
    partnerId: undefined,
    changeType: undefined,
    fromDept: undefined,
    toDept: undefined,
    fromPostId: undefined,
    toPostId: undefined,
    startDate: undefined,
    endDate: undefined,
    reason: undefined,
    docAttachment: '',
    syncEmployee: 0,
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
  await loadDeptList()
  postList.value = await PostApi.getSimplePostList()
  if (id) {
    formLoading.value = true
    try {
      const data = await PositionChangeApi.getPositionChange(id)
      formData.value = { ...data, docAttachment: data.docAttachment || '' }
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
    const data = { ...formData.value } as PositionChangeVO
    const doc = data.docAttachment
    data.docAttachment = Array.isArray(doc) ? doc.join(',') : (doc || '')
    if (formType.value === 'create') {
      await PositionChangeApi.createPositionChange(data)
      message.success(t('common.createSuccess'))
    } else {
      await PositionChangeApi.updatePositionChange(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
