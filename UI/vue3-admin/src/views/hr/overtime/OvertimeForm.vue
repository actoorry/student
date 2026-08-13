<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="720px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <!-- 基本信息 -->
      <el-form-item label="员工" prop="partnerId">
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
        <div v-if="selectedEmployee" class="mt-5px text-12px text-gray-500">
          工号：{{ selectedEmployee.employeeNo || '-' }} ｜ 所属科室：{{ selectedEmployee.deptName || '-' }}
        </div>
      </el-form-item>

      <!-- 加班信息 -->
      <el-form-item label="加班类型" prop="overtimeType">
        <el-select v-model="formData.overtimeType" placeholder="请选择加班类型" clearable class="!w-full">
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_OVERTIME_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="加班原因" prop="overtimeReason">
        <el-select v-model="formData.overtimeReason" placeholder="请选择加班原因" clearable class="!w-full">
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_OVERTIME_REASON)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="加班日期" prop="overtimeDate">
        <el-date-picker
          v-model="formData.overtimeDate"
          type="date"
          value-format="x"
          placeholder="选择加班日期"
          class="!w-full"
        />
        <div v-if="isNightShift" class="mt-5px text-12px text-gray-500">
          填班次所属日期，用于按月统计
        </div>
      </el-form-item>

      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker
          v-model="formData.startTime"
          type="datetime"
          value-format="x"
          placeholder="选择加班开始时间"
          class="!w-full"
        />
      </el-form-item>

      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker
          v-model="formData.endTime"
          type="datetime"
          value-format="x"
          placeholder="选择加班结束时间"
          class="!w-full"
        />
        <div v-if="isNightShift" class="mt-5px text-12px text-gray-500">
          夜班常跨日（如 22:00~次日06:00），结束时间可早于开始时间的日历日
        </div>
      </el-form-item>

      <el-form-item label="预计时长">
        <el-tag type="primary">{{ previewDuration }}</el-tag>
        <span class="ml-10px text-12px text-gray-500">小时（保存时由后端按时间差计算）</span>
      </el-form-item>

      <!-- 值班信息 -->
      <el-form-item label="值班科室" prop="workDeptId">
        <el-tree-select
          v-model="formData.workDeptId"
          :data="deptList"
          :props="defaultProps"
          check-strictly
          node-key="id"
          :placeholder="formData.overtimeType === 'emergency' ? '请选择值班科室' : '可选，急诊加班必填'"
          clearable
          class="!w-full"
        />
      </el-form-item>

      <el-form-item label="值班地点" prop="workLocation">
        <el-input v-model="formData.workLocation" placeholder="如「急诊科」「内一病区」" />
      </el-form-item>

      <el-form-item v-if="isLegalHolidayDuty" label="节假日名称" prop="holidayName">
        <el-input v-model="formData.holidayName" placeholder="如「2026年春节」" />
      </el-form-item>

      <!-- 说明 -->
      <el-form-item label="工作内容简述" prop="workSummary">
        <el-input
          v-model="formData.workSummary"
          type="textarea"
          :rows="3"
          placeholder="请简述本次加班具体工作内容"
        />
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="2"
          :placeholder="formData.overtimeReason === 'other' ? '选择「其他」原因时请在此补充说明' : '请输入备注'"
        />
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
import { OvertimeApi, Overtime } from '@/api/hr/overtime'
import { EmployeeApi, Employee } from '@/api/hr/employee'
import * as DeptApi from '@/api/system/dept'
import { defaultProps, handleTree } from '@/utils/tree'

/** 加班登记 表单 */
defineOptions({ name: 'OvertimeForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const employeeLoading = ref(false)
const employeeOptions = ref<Employee[]>([])
const selectedEmployee = ref<Employee | null>(null)
const deptList = ref<Tree[]>([])

const NIGHT_SHIFT_TYPES = ['big_night_shift', 'small_night_shift', 'night_shift']
const isNightShift = computed(() => NIGHT_SHIFT_TYPES.includes(formData.value.overtimeType || ''))
const isLegalHolidayDuty = computed(() =>
  ['legal_holiday_duty', 'holiday_duty'].includes(formData.value.overtimeType || '')
)

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
  selectedEmployee.value = employeeOptions.value.find((item) => item.partnerId === partnerId) || null
}

const formData = ref({
  id: undefined,
  partnerId: undefined,
  overtimeType: undefined,
  overtimeReason: undefined,
  workSummary: undefined,
  overtimeDate: undefined,
  startTime: undefined,
  endTime: undefined,
  workDeptId: undefined,
  workLocation: undefined,
  holidayName: undefined,
  remark: undefined
})

/** 急诊加班：值班科室、值班地点必填；法定节假日值班：节假日名称必填 */
const formRules = computed(() => ({
  partnerId: [{ required: true, message: '员工不能为空', trigger: 'change' }],
  overtimeType: [{ required: true, message: '加班类型不能为空', trigger: 'change' }],
  overtimeReason: [{ required: true, message: '加班原因不能为空', trigger: 'change' }],
  workSummary: [{ required: true, message: '工作内容简述不能为空', trigger: 'blur' }],
  overtimeDate: [{ required: true, message: '加班日期不能为空', trigger: 'change' }],
  startTime: [{ required: true, message: '开始时间不能为空', trigger: 'change' }],
  endTime: [{ required: true, message: '结束时间不能为空', trigger: 'change' }],
  workDeptId:
    formData.value.overtimeType === 'emergency'
      ? [{ required: true, message: '急诊加班请选择值班科室', trigger: 'change' }]
      : [],
  workLocation:
    formData.value.overtimeType === 'emergency'
      ? [{ required: true, message: '急诊加班请填写值班地点', trigger: 'blur' }]
      : [],
  holidayName: isLegalHolidayDuty.value
    ? [{ required: true, message: '法定节假日值班请填写节假日名称', trigger: 'blur' }]
    : []
}))
const formRef = ref()

/** 预计时长（小时，1 位小数） */
const previewDuration = computed(() => {
  const start = Number(formData.value.startTime)
  const end = Number(formData.value.endTime)
  if (!start || !end || end <= start) return '0.0'
  return (Math.round(((end - start) / 3600000) * 10) / 10).toFixed(1)
})

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  deptList.value = handleTree(await DeptApi.getSimpleDeptList())
  if (id) {
    formLoading.value = true
    try {
      const data = await OvertimeApi.getOvertime(id)
      formData.value = {
        id: data.id,
        partnerId: data.partnerId,
        overtimeType: data.overtimeType,
        overtimeReason: data.overtimeReason,
        workSummary: data.workSummary,
        overtimeDate: data.overtimeDate,
        startTime: data.startTime,
        endTime: data.endTime,
        workDeptId: data.workDeptId,
        workLocation: data.workLocation,
        holidayName: data.holidayName,
        remark: data.remark
      }
      if (data.partnerId && data.name) {
        employeeOptions.value = [
          {
            partnerId: data.partnerId,
            name: data.name,
            employeeNo: data.employeeNo,
            deptName: data.deptName
          } as Employee
        ]
        selectedEmployee.value = employeeOptions.value[0]
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
    const data = { ...formData.value } as unknown as Overtime
    if (formType.value === 'create') {
      await OvertimeApi.createOvertime(data)
      message.success(t('common.createSuccess'))
    } else {
      await OvertimeApi.updateOvertime(data)
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
    overtimeType: undefined,
    overtimeReason: undefined,
    workSummary: undefined,
    overtimeDate: undefined,
    startTime: undefined,
    endTime: undefined,
    workDeptId: undefined,
    workLocation: undefined,
    holidayName: undefined,
    remark: undefined
  }
  selectedEmployee.value = null
  employeeOptions.value = []
  formRef.value?.resetFields()
}
</script>
