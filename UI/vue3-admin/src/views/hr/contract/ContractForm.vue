<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
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
      <el-form-item label="合同编号">
        <el-input v-model="formData.contractNo" placeholder="系统自动生成" disabled />
      </el-form-item>
      <el-form-item label="甲方" prop="partyA">
        <el-input v-model="formData.partyA" placeholder="请输入甲方" />
      </el-form-item>
      <el-form-item label="部门" prop="dept">
        <el-tree-select
          v-model="formData.dept"
          :data="deptList"
          :props="defaultProps"
          check-strictly
          node-key="id"
          placeholder="请选择部门"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="合同开始" prop="startTime">
        <el-date-picker
          v-model="formData.startTime"
          type="date"
          value-format="x"
          placeholder="选择合同开始时间"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="合同结束" prop="endTime">
        <el-date-picker
          v-model="formData.endTime"
          type="date"
          value-format="x"
          placeholder="选择合同结束时间"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="甲方签订日" prop="signDateA">
        <el-date-picker
          v-model="formData.signDateA"
          type="date"
          value-format="x"
          placeholder="选择甲方签订日期"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="乙方签订日" prop="signDateB">
        <el-date-picker
          v-model="formData.signDateB"
          type="date"
          value-format="x"
          placeholder="选择乙方签订日期"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="合同文件" prop="contractFile">
        <UploadFile v-model="formData.contractFile" />
      </el-form-item>
      <el-form-item label="可视化图片" prop="photo">
        <UploadImg v-model="formData.photo" :limit="1" />
      </el-form-item>
      <el-form-item label="合同状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择合同状态" clearable class="!w-full">
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_WORKING_TYPE)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
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
import { ContractApi, Contract } from '@/api/hr/contract'
import { EmployeeApi, Employee } from '@/api/hr/employee'
import * as DeptApi from '@/api/system/dept'
import { defaultProps, handleTree } from '@/utils/tree'
import { UploadImg } from '@/components/UploadFile'

/** 劳动合同 表单 */
defineOptions({ name: 'ContractForm' })

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
  if (employee?.dept) {
    formData.value.dept = employee.dept
  }
}

const formData = ref({
  id: undefined,
  partnerId: undefined,
  contractNo: undefined,
  partyA: undefined,
  dept: undefined,
  startTime: undefined,
  endTime: undefined,
  signDateA: undefined,
  signDateB: undefined,
  contractFile: '',
  photo: undefined,
  status: undefined,
})
const formRules = reactive({
  partnerId: [{ required: true, message: '关联员工不能为空', trigger: 'change' }],
})
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  deptList.value = handleTree(await DeptApi.getSimpleDeptList())
  if (type === 'create') {
    formData.value.contractNo = await ContractApi.previewContractNo()
  }
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ContractApi.getContract(id)
      if (formData.value.partnerId && formData.value.name) {
        employeeOptions.value = [{
          partnerId: formData.value.partnerId,
          name: formData.value.name,
        } as Employee]
      }
      if (formData.value.dept) {
        const deptId = Number(formData.value.dept)
        if (!Number.isNaN(deptId)) {
          ;(formData.value as Record<string, unknown>).dept = deptId
        }
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = { ...formData.value } as unknown as Contract
    if (formType.value === 'create') {
      delete data.contractNo
    }
    if (data.dept !== undefined && data.dept !== null) {
      data.dept = String(data.dept)
    }
    if (Array.isArray(data.contractFile)) {
      data.contractFile = data.contractFile.join(',')
    }
    if (formType.value === 'create') {
      await ContractApi.createContract(data)
      message.success(t('common.createSuccess'))
    } else {
      await ContractApi.updateContract(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    partnerId: undefined,
    contractNo: undefined,
    partyA: undefined,
    dept: undefined,
    startTime: undefined,
    endTime: undefined,
    signDateA: undefined,
    signDateB: undefined,
    contractFile: '',
    photo: undefined,
    status: undefined,
  }
  employeeOptions.value = []
  formRef.value?.resetFields()
}
</script>
