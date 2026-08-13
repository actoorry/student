<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="680px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <!-- 本人信息只读展示 -->
      <el-form-item label="姓名">
        <el-input :model-value="employeeLabel" disabled />
        <div v-if="selfEmployee" class="mt-5px text-12px text-gray-500">
          工号：{{ selfEmployee.employeeNo || '-' }} ｜ 部门：{{ selfEmployee.deptName || '-' }}
        </div>
      </el-form-item>

      <el-form-item label="外出类型" prop="recordType">
        <el-select v-model="formData.recordType" placeholder="请选择外出类型" class="!w-full">
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_OUTBOUND_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="地点">
        <el-cascader
          v-model="areaIds"
          :options="areaTree"
          :props="areaCascaderProps"
          class="!w-full"
          clearable
          filterable
          placeholder="请选择省 / 市 / 区/县"
          show-all-levels
        />
      </el-form-item>

      <el-form-item :label="orgLabel" prop="organization">
        <el-input v-model="formData.organization" :placeholder="`请输入${orgLabel}`" />
      </el-form-item>

      <el-form-item v-if="formData.recordType !== 'rural_support'" label="进修/培训名称" prop="practiceName">
        <el-input v-model="formData.practiceName" placeholder="请输入进修/培训名称" />
      </el-form-item>

      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
          v-model="formData.startDate"
          type="date"
          value-format="x"
          placeholder="选择开始日期"
          class="!w-full"
        />
      </el-form-item>

      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker
          v-model="formData.endDate"
          type="date"
          value-format="x"
          placeholder="选择结束日期"
          class="!w-full"
        />
      </el-form-item>

      <el-form-item v-if="formData.recordType === 'rural_support'" label="预计服务年限">
        <el-tag type="primary">{{ previewSupportYears }} 年</el-tag>
        <span class="ml-10px text-12px text-gray-500">天数 / 365，计入汇总统计</span>
      </el-form-item>

      <el-form-item
        v-if="formData.recordType && formData.recordType !== 'rural_support'"
        label="继教学分"
        prop="continuingEducationCredit"
      >
        <el-input-number
          v-model="formData.continuingEducationCredit"
          :min="0"
          :precision="1"
          :step="0.5"
          placeholder="请输入继教学分"
          class="!w-full"
        />
      </el-form-item>

      <el-form-item label="总结" prop="summary">
        <el-input v-model="formData.summary" type="textarea" :rows="3" placeholder="请输入总结" />
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
import { MyOutboundApi } from '@/api/hr/my/outbound'
import type { OutboundVO } from '@/api/hr/outbound'
import { useMySelfEmployee } from '@/views/hr/my/composables/useMySelfEmployee'
import { useUserStore } from '@/store/modules/user'
import { getAreaTree } from '@/api/system/area'

/** 职工我的外出申请 表单 */
defineOptions({ name: 'HrMyOutboundForm' })

const { t } = useI18n()
const message = useMessage()
const userStore = useUserStore()

interface AreaNode {
  id: number
  name: string
  children?: AreaNode[]
}

const areaCascaderProps = {
  children: 'children',
  label: 'name',
  value: 'id',
  checkStrictly: true,
  emitPath: true
}

const areaTree = ref<AreaNode[]>([])
const areaIds = ref<number[]>()

const resolveAreaNames = (ids?: number[]) => {
  const names = ['', '', '']
  if (!ids?.length) {
    return { province: '', city: '', county: '' }
  }
  let nodes = areaTree.value
  for (let i = 0; i < ids.length && i < 3; i++) {
    const node = nodes.find((n) => n.id === ids[i])
    if (!node) {
      break
    }
    names[i] = node.name
    nodes = node.children || []
  }
  return { province: names[0], city: names[1], county: names[2] }
}

const findAreaIdsByNames = (province?: string, city?: string, county?: string): number[] | undefined => {
  if (!province && !city && !county) {
    return undefined
  }
  const result: number[] = []
  let nodes = areaTree.value
  if (province) {
    const p = nodes.find((n) => n.name === province)
    if (!p) return result.length ? result : undefined
    result.push(p.id)
    nodes = p.children || []
  }
  if (city) {
    const c = nodes.find((n) => n.name === city)
    if (!c) return result
    result.push(c.id)
    nodes = c.children || []
  }
  if (county) {
    const d = nodes.find((n) => n.name === county)
    if (d) result.push(d.id)
  }
  return result.length ? result : undefined
}

const loadAreaTree = async () => {
  areaTree.value = (await getAreaTree()) || []
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formRef = ref()
const { selfEmployee, employeeLabel, loadSelfEmployee, resetSelfEmployee } = useMySelfEmployee()

const formData = ref<Partial<OutboundVO>>({
  id: undefined,
  partnerId: undefined,
  recordType: undefined,
  province: undefined,
  city: undefined,
  county: undefined,
  organization: undefined,
  practiceName: undefined,
  startDate: undefined,
  endDate: undefined,
  continuingEducationCredit: undefined,
  summary: undefined
})
const formRules = reactive({
  recordType: [{ required: true, message: '外出类型不能为空', trigger: 'change' }],
  startDate: [{ required: true, message: '开始日期不能为空', trigger: 'change' }],
  endDate: [{ required: true, message: '结束日期不能为空', trigger: 'change' }]
})

const orgLabel = computed(() => {
  return formData.value.recordType === 'rural_support' ? '支援单位' : '进修/培训单位'
})

const previewSupportYears = computed(() => {
  const { startDate, endDate } = formData.value
  if (!startDate || !endDate) return '0.0'
  const start = new Date(Number(startDate))
  const end = new Date(Number(endDate))
  const days = Math.floor((end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000)) + 1
  return (days / 365).toFixed(1)
})

const resetForm = () => {
  formData.value = {
    id: undefined,
    partnerId: undefined,
    recordType: undefined,
    province: undefined,
    city: undefined,
    county: undefined,
    organization: undefined,
    practiceName: undefined,
    startDate: undefined,
    endDate: undefined,
    continuingEducationCredit: undefined,
    summary: undefined
  }
  resetSelfEmployee()
  areaIds.value = undefined
  formRef.value?.resetFields()
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  await loadAreaTree()
  const userId = userStore.user?.id
  if (userId) {
    formData.value.partnerId = userId
    await loadSelfEmployee()
  }
  if (id) {
    formLoading.value = true
    try {
      const data = await MyOutboundApi.getMyOutbound(id)
      formData.value = data
      areaIds.value = findAreaIdsByNames(data.province, data.city, data.county)
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
    const data = { ...formData.value } as unknown as OutboundVO
    data.partnerId = userStore.user?.id
    const areaNames = resolveAreaNames(areaIds.value)
    data.province = areaNames.province || undefined
    data.city = areaNames.city || undefined
    data.county = areaNames.county || undefined
    if (formType.value === 'create') {
      await MyOutboundApi.createMyOutbound(data)
      message.success(t('common.createSuccess'))
    } else {
      await MyOutboundApi.updateMyOutbound(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
