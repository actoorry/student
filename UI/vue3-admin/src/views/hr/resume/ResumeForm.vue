<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="720px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-divider content-position="left">基本信息</el-divider>
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
      <el-form-item label="员工工号" prop="employeeNo">
        <el-input v-model="formData.employeeNo" placeholder="选择员工后自动带出" disabled />
      </el-form-item>
      <el-form-item label="所在部门" prop="dept">
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

      <el-divider content-position="left">工作经历</el-divider>
      <div v-for="(row, idx) in workRows" :key="idx" class="mb-10px flex items-center">
        <el-input
          v-model="row.startYear"
          placeholder="起年"
          class="!w-90px"
        />
        <span class="mx-6px text-gray-500">-</span>
        <el-input
          v-model="row.endYear"
          placeholder="止年/至今"
          class="!w-110px"
        />
        <el-input
          v-model="row.unit"
          placeholder="单位 / 科室"
          class="flex-1 mx-8px"
        />
        <el-input
          v-model="row.position"
          placeholder="职务 / 岗位"
          class="!w-160px"
        />
        <el-button
          link
          type="danger"
          class="ml-6px"
          :disabled="workRows.length <= 1"
          @click="removeWorkRow(idx)"
        >
          <Icon icon="ep:delete" />
        </el-button>
      </div>
      <el-form-item>
        <el-button link type="primary" @click="addWorkRow">
          <Icon icon="ep:plus" class="mr-4px" /> 添加工作经历
        </el-button>
      </el-form-item>

      <el-divider content-position="left">其他履历</el-divider>
      <el-form-item label="奖惩情况" prop="awardsPunishments">
        <el-input v-model="formData.awardsPunishments" type="textarea" :rows="3" placeholder="请输入奖惩情况" />
      </el-form-item>
      <el-form-item label="论文" prop="papers">
        <el-input v-model="formData.papers" type="textarea" :rows="3" placeholder="请输入论文" />
      </el-form-item>
      <el-form-item label="年度考核" prop="annualReview">
        <el-input v-model="formData.annualReview" type="textarea" :rows="3" placeholder="请输入年度考核情况" />
      </el-form-item>

      <el-divider content-position="left">证书（来自证书管理）</el-divider>
      <el-form-item label="持证情况">
        <div class="w-full">
          <el-table
            v-if="certificateList.length"
            :data="certificateList"
            size="small"
            border
            max-height="240"
          >
            <el-table-column label="证书类型" align="center" min-width="120">
              <template #default="{ row }">
                <DictTag :type="DICT_TYPE.HR_CERTIFICATE_TYPE" :value="row.certificateType" />
              </template>
            </el-table-column>
            <el-table-column label="证书名称" align="center" prop="certificateName" min-width="120" show-overflow-tooltip />
            <el-table-column label="编号" align="center" prop="certificateNo" width="140" show-overflow-tooltip />
            <el-table-column label="发证日期" align="center" prop="issueDate" width="110" :formatter="dateFormatter2" />
            <el-table-column label="到期日期" align="center" prop="expireDate" width="110" :formatter="dateFormatter2" />
          </el-table>
          <el-empty
            v-else
            :description="formData.partnerId ? '该员工暂无证书，请在「证书管理」中维护' : '请先选择关联员工'"
            :image-size="50"
          />
        </div>
      </el-form-item>

      <el-divider content-position="left">籍贯与备注</el-divider>
      <el-form-item label="籍贯">
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
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { ResumeApi, Resume } from '@/api/hr/resume'
import { EmployeeApi, Employee } from '@/api/hr/employee'
import { CertificateApi, Certificate } from '@/api/hr/certificate'
import { getAreaTree } from '@/api/system/area'
import * as DeptApi from '@/api/system/dept'
import { defaultProps, handleTree } from '@/utils/tree'
import { dateFormatter2 } from '@/utils/formatTime'

/** 履历 表单 */
defineOptions({ name: 'ResumeForm' })

const { t } = useI18n()
const message = useMessage()

interface WorkRow {
  startYear: string
  endYear: string
  unit: string
  position: string
}

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
  emitPath: true,
}

const emptyWorkRow = (): WorkRow => ({ startYear: '', endYear: '', unit: '', position: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const employeeLoading = ref(false)
const employeeOptions = ref<Employee[]>([])
const deptList = ref<Tree[]>([])
const workRows = ref<WorkRow[]>([emptyWorkRow()])
const certificateList = ref<Certificate[]>([])
const areaTree = ref<AreaNode[]>([])
const areaIds = ref<number[]>()

/** 地区 ID 路径 → 省/市/区县名称（仍存 partner_resume 原字段，兼容 OA 迁移） */
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

/** 编辑回显：按已存名称反查地区 ID 路径 */
const findAreaIdsByNames = (province?: string, city?: string, county?: string): number[] | undefined => {
  if (!province && !city && !county) {
    return undefined
  }
  const result: number[] = []
  let nodes = areaTree.value
  if (province) {
    const p = nodes.find((n) => n.name === province)
    if (!p) {
      return result.length ? result : undefined
    }
    result.push(p.id)
    nodes = p.children || []
  }
  if (city) {
    const c = nodes.find((n) => n.name === city)
    if (!c) {
      return result
    }
    result.push(c.id)
    nodes = c.children || []
  }
  if (county) {
    const d = nodes.find((n) => n.name === county)
    if (d) {
      result.push(d.id)
    }
  }
  return result.length ? result : undefined
}

const loadAreaTree = async () => {
  areaTree.value = (await getAreaTree()) || []
}

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
    if (employee.dept) {
      formData.value.dept = employee.dept
    }
  }
  loadCertificates(partnerId)
}

const loadCertificates = async (partnerId?: number) => {
  certificateList.value = []
  if (!partnerId) {
    return
  }
  try {
    const data = await CertificateApi.getCertificatePage({ partnerId, pageNo: 1, pageSize: 100 })
    certificateList.value = data.list || []
  } catch {
    certificateList.value = []
  }
}

const addWorkRow = () => workRows.value.push(emptyWorkRow())
const removeWorkRow = (idx: number) => workRows.value.splice(idx, 1)

/** resume_content JSON ↔ 工作经历行 互转 */
const parseWorkRows = (content?: string): WorkRow[] => {
  if (!content) {
    return [emptyWorkRow()]
  }
  try {
    const parsed = JSON.parse(content)
    if (Array.isArray(parsed) && parsed.length) {
      return parsed.map((r: any) => ({
        startYear: r.startYear ?? '',
        endYear: r.endYear ?? '',
        unit: r.unit ?? '',
        position: r.position ?? '',
      }))
    }
    if (Array.isArray(parsed)) {
      return [emptyWorkRow()]
    }
  } catch {
    // 兼容旧自由文本：整段塞进单位字段
    return [{ startYear: '', endYear: '', unit: content, position: '' }]
  }
  return [emptyWorkRow()]
}

const serializeWorkRows = (): string => {
  const rows = workRows.value.filter((r) => r.startYear || r.endYear || r.unit || r.position)
  return rows.length ? JSON.stringify(rows) : ''
}

const formData = ref({
  id: undefined,
  partnerId: undefined,
  employeeNo: undefined,
  dept: undefined,
  resumeContent: undefined,
  awardsPunishments: undefined,
  certificates: undefined,
  papers: undefined,
  annualReview: undefined,
  remark: undefined,
  province: undefined,
  city: undefined,
  county: undefined,
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
  await loadAreaTree()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ResumeApi.getResume(id)
      workRows.value = parseWorkRows(formData.value.resumeContent)
      if (formData.value.partnerId && formData.value.name) {
        employeeOptions.value = [{
          partnerId: formData.value.partnerId,
          name: formData.value.name,
          employeeNo: formData.value.employeeNo,
        } as Employee]
      }
      if (formData.value.dept) {
        const deptId = Number(formData.value.dept)
        if (!Number.isNaN(deptId)) {
          ;(formData.value as Record<string, unknown>).dept = deptId
        }
      }
      await loadCertificates(formData.value.partnerId)
      areaIds.value = findAreaIdsByNames(formData.value.province, formData.value.city, formData.value.county)
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
    const data = { ...formData.value } as unknown as Resume
    data.resumeContent = serializeWorkRows()
    const areaNames = resolveAreaNames(areaIds.value)
    data.province = areaNames.province || undefined
    data.city = areaNames.city || undefined
    data.county = areaNames.county || undefined
    if (data.dept !== undefined && data.dept !== null) {
      data.dept = String(data.dept)
    }
    if (formType.value === 'create') {
      await ResumeApi.createResume(data)
      message.success(t('common.createSuccess'))
    } else {
      await ResumeApi.updateResume(data)
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
    employeeNo: undefined,
    dept: undefined,
    resumeContent: undefined,
    awardsPunishments: undefined,
    certificates: undefined,
    papers: undefined,
    annualReview: undefined,
    remark: undefined,
    province: undefined,
    city: undefined,
    county: undefined,
  }
  employeeOptions.value = []
  workRows.value = [emptyWorkRow()]
  certificateList.value = []
  areaIds.value = undefined
  formRef.value?.resetFields()
}
</script>
