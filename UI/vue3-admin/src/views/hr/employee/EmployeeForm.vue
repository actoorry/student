<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-divider content-position="left">基础信息</el-divider>
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入姓名"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.name"
        />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input
          v-model="formData.idCard"
          placeholder="请输入身份证号"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.idCard"
        />
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="formData.sex">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_USER_SEX)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="出生日期" prop="birthday">
        <el-date-picker
          v-model="formData.birthday"
          type="date"
          value-format="x"
          placeholder="选择出生日期"
        />
      </el-form-item>

      <el-divider content-position="left">工作信息</el-divider>
      <el-form-item label="员工工号" prop="employeeNo">
        <el-input
          v-model="formData.employeeNo"
          placeholder="请输入员工工号（字母/数字，最大30位）"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.employeeNo"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="employeeMobile">
        <el-input
          v-model="formData.employeeMobile"
          placeholder="请输入11位手机号"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.employeeMobile"
        />
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
      <el-form-item label="岗位" prop="postId">
        <el-select v-model="formData.postId" placeholder="请选择岗位" clearable class="!w-full">
          <el-option
            v-for="item in postList"
            :key="item.id"
            :label="item.name"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="职务" prop="duty">
        <el-input
          v-model="formData.duty"
          placeholder="请输入职务"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.text100"
        />
      </el-form-item>
      <el-form-item label="人员类别" prop="personnelCategory">
        <el-select
          v-model="formData.personnelCategory"
          clearable
          placeholder="请选择人员类别"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_PERSONNEL_CATEGORY)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item prop="professionalTitle">
        <template #label>
          <Tooltip message="聘用职员等级、卫生技术等级" title="职称" />
        </template>
        <el-select
          v-model="formData.professionalTitle"
          clearable
          filterable
          placeholder="请选择职称"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_PROFESSIONAL_TITLE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="进入单位时间" prop="hireDate">
        <el-date-picker
          v-model="formData.hireDate"
          type="date"
          value-format="x"
          placeholder="选择进入单位时间"
        />
      </el-form-item>
      <el-form-item label="在职状态" prop="employmentStatus">
        <el-select
          v-model="formData.employmentStatus"
          clearable
          placeholder="请选择在职状态"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_EMPLOYMENT_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-divider content-position="left">档案信息</el-divider>
      <el-form-item label="政治面貌" prop="politicalStatus">
        <el-select
          v-model="formData.politicalStatus"
          clearable
          placeholder="请选择政治面貌"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_POLITICAL_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item prop="partyJoinDate">
        <template #label>
          <Tooltip message="政治面貌为中共党员时填写" title="入党时间" />
        </template>
        <el-date-picker
          v-model="formData.partyJoinDate"
          type="date"
          value-format="x"
          placeholder="选择入党时间"
        />
      </el-form-item>
      <el-form-item label="参加工作时间" prop="careerStartDate">
        <el-date-picker
          v-model="formData.careerStartDate"
          type="date"
          value-format="x"
          placeholder="选择参加工作时间"
        />
      </el-form-item>
      <el-form-item prop="appointmentDate">
        <template #label>
          <Tooltip message="职务聘任时填写" title="任职时间" />
        </template>
        <el-date-picker
          v-model="formData.appointmentDate"
          type="date"
          value-format="x"
          placeholder="选择任职时间"
        />
      </el-form-item>
      <el-form-item label="最高学历" prop="highestEducation">
        <el-select
          v-model="formData.highestEducation"
          clearable
          placeholder="请选择最高学历"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_EDUCATION)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="全日制学历" prop="fullTimeEducation">
        <el-input
          v-model="formData.fullTimeEducation"
          placeholder="请输入全日制学历"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.text100"
        />
      </el-form-item>
      <el-form-item label="是否在编" prop="establishmentStatus">
        <el-select
          v-model="formData.establishmentStatus"
          clearable
          placeholder="请选择是否在编"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_ESTABLISHMENT_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="人员身份" prop="personnelIdentity">
        <el-select
          v-model="formData.personnelIdentity"
          clearable
          placeholder="请选择人员身份"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_PERSONNEL_IDENTITY)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="人员来源" prop="recruitmentSource">
        <el-select
          v-model="formData.recruitmentSource"
          clearable
          placeholder="请选择人员来源"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_RECRUITMENT_SOURCE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="进入形式" prop="entryMode">
        <el-select
          v-model="formData.entryMode"
          clearable
          placeholder="请选择进入形式"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_ENTRY_MODE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="民族" prop="ethnicity">
        <el-select
          v-model="formData.ethnicity"
          clearable
          filterable
          placeholder="请选择民族"
          class="!w-full"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_ETHNICITY)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="籍贯">
        <el-cascader
          v-model="nativeAreaIds"
          :options="nativeAreaTree"
          :props="nativeAreaCascaderProps"
          class="!w-full"
          clearable
          filterable
          placeholder="请选择省 / 市"
          show-all-levels
        />
      </el-form-item>
      <el-form-item label="银行卡卡号" prop="bankCard">
        <el-input
          v-model="formData.bankCard"
          placeholder="请输入银行卡卡号"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.text100"
        />
      </el-form-item>
      <el-form-item label="毕业院校及专业" prop="schoolMajor">
        <el-input
          v-model="formData.schoolMajor"
          placeholder="请输入毕业院校及专业"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.text100"
        />
      </el-form-item>
      <el-form-item label="家庭信息" prop="homeInformation">
        <el-input
          v-model="formData.homeInformation"
          placeholder="请输入家庭信息"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.text100"
        />
      </el-form-item>
      <el-form-item label="姓名简写" prop="nameAbbreviation">
        <el-input
          v-model="formData.nameAbbreviation"
          placeholder="请输入姓名简写"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.text100"
        />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          placeholder="请输入备注"
          :maxlength="HR_EMPLOYEE_FIELD_LIMIT.remark"
        />
      </el-form-item>
    </el-form>

    <!-- 外出汇总：仅编辑模式且存在职工档案时展示 -->
    <el-collapse
      v-if="formType === 'update' && formData.partnerId"
      v-model="activeOutboundCollapse"
      class="mt-10px"
    >
      <el-collapse-item name="outbound" title="外出记录汇总">
        <div v-loading="outboundLoading">
          <el-descriptions :column="2" border size="small" class="mb-10px">
            <el-descriptions-item label="累计下乡支援年限">
              <el-tag type="primary">{{ outboundSummary.supportYears }} 年</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="累计继教学分">
              <el-tag type="success">{{ outboundSummary.continuingEducationCredit }} 分</el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <el-table :data="outboundList" size="small" :stripe="true" max-height="240">
            <el-table-column label="外出类型" prop="recordType" width="100px">
              <template #default="scope">
                <dict-tag :type="DICT_TYPE.HR_OUTBOUND_TYPE" :value="scope.row.recordType" />
              </template>
            </el-table-column>
            <el-table-column label="单位" prop="organization" min-width="120px" show-overflow-tooltip />
            <el-table-column label="地点" min-width="140px" show-overflow-tooltip>
              <template #default="scope">
                {{
                  [scope.row.province, scope.row.city, scope.row.county]
                    .filter(Boolean)
                    .join(' / ') || '-'
                }}
              </template>
            </el-table-column>
            <el-table-column label="开始日期" prop="startDate" width="110px" />
            <el-table-column label="结束日期" prop="endDate" width="110px" />
            <el-table-column label="天数" prop="durationDays" width="70px" />
            <el-table-column label="服务年限" prop="supportYears" width="90px">
              <template #default="scope">
                <span v-if="scope.row.recordType === 'rural_support'">
                  {{ scope.row.supportYears }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="继教学分" prop="continuingEducationCredit" width="90px">
              <template #default="scope">
                <span v-if="scope.row.recordType !== 'rural_support'">
                  {{ scope.row.continuingEducationCredit }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="mt-10px">
            <el-button
              type="primary"
              plain
              size="small"
              @click="handleAddOutbound"
              v-hasPermi="['hr:outbound:create']"
            >
              <Icon icon="ep:plus" class="mr-5px" /> 新增外出申请
            </el-button>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions, getStrDictOptions } from '@/utils/dict'
import { defaultProps, handleTree } from '@/utils/tree'
import * as DeptApi from '@/api/system/dept'
import * as PostApi from '@/api/system/post'
import { EmployeeApi, type Employee } from '@/api/hr/employee'
import { OutboundApi, type OutboundVO } from '@/api/hr/outbound'
import { getAreaTree } from '@/api/system/area'
import {
  HR_EMPLOYEE_EMPLOYEE_NO_PATTERN,
  HR_EMPLOYEE_FIELD_LIMIT,
  HR_EMPLOYEE_MOBILE_PATTERN
} from './employeeValidation'
import type { FormItemRule } from 'element-plus'

/** 员工表单 */
defineOptions({ name: 'EmployeeForm' })

const { t } = useI18n()
const message = useMessage()

interface AreaNode {
  id: number
  name: string
  children?: AreaNode[]
}

/** 籍贯只需要省/市两级，去掉区县子节点 */
const toNativeAreaTree = (nodes: AreaNode[]): AreaNode[] =>
  nodes.map((province) => ({
    ...province,
    children: province.children?.map((city) => ({ ...city, children: undefined }))
  }))

const nativeAreaCascaderProps = {
  children: 'children',
  label: 'name',
  value: 'id',
  checkStrictly: true,
  emitPath: true
}

const nativeAreaTree = ref<AreaNode[]>([])
const nativeAreaIds = ref<number[]>()

const resolveNativeAreaNames = (ids?: number[]) => {
  const names = ['', '']
  if (!ids?.length) {
    return { nativeProvince: '', nativeCity: '' }
  }

  let nodes = nativeAreaTree.value
  for (let i = 0; i < ids.length && i < 2; i++) {
    const node = nodes.find((item) => item.id === ids[i])
    if (!node) {
      break
    }
    names[i] = node.name
    nodes = node.children || []
  }
  return { nativeProvince: names[0], nativeCity: names[1] }
}

const findNativeAreaIdsByNames = (province?: string, city?: string): number[] | undefined => {
  if (!province && !city) {
    return undefined
  }

  const result: number[] = []
  let nodes = nativeAreaTree.value
  if (province) {
    const provinceNode = nodes.find((item) => item.name === province)
    if (!provinceNode) {
      return undefined
    }
    result.push(provinceNode.id)
    nodes = provinceNode.children || []
  }
  if (city) {
    const cityNode = nodes.find((item) => item.name === city)
    if (cityNode) {
      result.push(cityNode.id)
    }
  }
  return result.length ? result : undefined
}

const loadNativeAreaTree = async () => {
  nativeAreaTree.value = toNativeAreaTree((await getAreaTree()) || [])
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<any>({
  id: undefined,
  partnerId: undefined,
  name: undefined,
  idCard: undefined,
  sex: undefined,
  birthday: undefined,
  avatar: undefined,
  detailAddress: undefined,
  remark: undefined,
  employeeNo: undefined,
  employeeMobile: undefined,
  dept: undefined,
  personnelCategory: undefined,
  professionalTitle: undefined,
  politicalStatus: undefined,
  partyJoinDate: undefined,
  careerStartDate: undefined,
  hireDate: undefined,
  postId: undefined,
  position: undefined,
  duty: undefined,
  appointmentDate: undefined,
  highestEducation: undefined,
  fullTimeEducation: undefined,
  establishmentStatus: undefined,
  personnelIdentity: undefined,
  recruitmentSource: undefined,
  entryMode: undefined,
  employmentStatus: undefined,
  ethnicity: undefined,
  nativeProvince: undefined,
  nativeCity: undefined,
  bankCard: undefined,
  homeInformation: undefined,
  schoolMajor: undefined,
  professionalCategory: undefined,
  nameAbbreviation: undefined
})

const maxLenRule = (max: number, label: string): FormItemRule => ({
  max,
  message: `${label}长度不能超过 ${max} 个字符`,
  trigger: 'blur'
})

const formRules = reactive({
  name: [
    { required: true, message: '姓名不能为空', trigger: 'blur' },
    maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.name, '姓名')
  ],
  idCard: [maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.idCard, '身份证号')],
  employeeNo: [
    maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.employeeNo, '员工工号'),
    {
      pattern: HR_EMPLOYEE_EMPLOYEE_NO_PATTERN,
      message: '员工工号只能包含字母和数字',
      trigger: 'blur'
    }
  ],
  employeeMobile: [
    {
      validator: (_rule: unknown, value: string | undefined, callback: (error?: Error) => void) => {
        if (!value || HR_EMPLOYEE_MOBILE_PATTERN.test(String(value))) {
          callback()
        } else {
          callback(new Error('员工手机号格式不正确'))
        }
      },
      trigger: 'blur'
    }
  ],
  duty: [maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.text100, '职务')],
  fullTimeEducation: [maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.text100, '全日制学历')],
  bankCard: [maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.text100, '银行卡卡号')],
  homeInformation: [maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.text100, '家庭信息')],
  schoolMajor: [maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.text100, '毕业院校及专业')],
  nameAbbreviation: [maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.text100, '姓名简写')],
  remark: [maxLenRule(HR_EMPLOYEE_FIELD_LIMIT.remark, '备注')]
})

const formRef = ref()
const deptList = ref<Tree[]>([])
const postList = ref([] as PostApi.PostVO[])

const outboundSummary = ref({ supportYears: 0, continuingEducationCredit: 0 })
const outboundList = ref<OutboundVO[]>([])
const outboundLoading = ref(false)
const router = useRouter()
const activeOutboundCollapse = ref<string[]>([])

const loadOutboundSummary = async (partnerId: number) => {
  outboundLoading.value = true
  try {
    const [summary, page] = await Promise.all([
      OutboundApi.getOutboundSummary(partnerId),
      OutboundApi.getOutboundPage({ pageNo: 1, pageSize: 100, partnerId, effective: 1 })
    ])
    outboundSummary.value = summary
    outboundList.value = page.list
  } finally {
    outboundLoading.value = false
  }
}

const handleAddOutbound = () => {
  router.push({ path: '/hr/outbound', query: { partnerId: formData.value.partnerId } })
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  deptList.value = handleTree(await DeptApi.getSimpleDeptList())
  postList.value = await PostApi.getSimplePostList()
  await loadNativeAreaTree()

  if (id) {
    formLoading.value = true
    try {
      formData.value = await EmployeeApi.getEmployee(id)
      nativeAreaIds.value = findNativeAreaIdsByNames(
        formData.value.nativeProvince,
        formData.value.nativeCity
      )
      if (formData.value.partnerId) {
        await loadOutboundSummary(formData.value.partnerId)
      }
    } finally {
      formLoading.value = false
    }
  } else {
    outboundSummary.value = { supportYears: 0, continuingEducationCredit: 0 }
    outboundList.value = []
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = { ...formData.value } as Employee
    const areaNames = resolveNativeAreaNames(nativeAreaIds.value)
    data.nativeProvince = areaNames.nativeProvince || undefined
    data.nativeCity = areaNames.nativeCity || undefined
    if (formType.value === 'create') {
      await EmployeeApi.createEmployee(data)
      message.success(t('common.createSuccess'))
    } else {
      await EmployeeApi.updateEmployee(data)
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
    name: undefined,
    idCard: undefined,
    sex: undefined,
    birthday: undefined,
    avatar: undefined,
    detailAddress: undefined,
    remark: undefined,
    employeeNo: undefined,
    employeeMobile: undefined,
    dept: undefined,
    personnelCategory: undefined,
    professionalTitle: undefined,
    politicalStatus: undefined,
    partyJoinDate: undefined,
    careerStartDate: undefined,
    hireDate: undefined,
    postId: undefined,
    position: undefined,
    duty: undefined,
    appointmentDate: undefined,
    highestEducation: undefined,
    fullTimeEducation: undefined,
    establishmentStatus: undefined,
    personnelIdentity: undefined,
    recruitmentSource: undefined,
    entryMode: undefined,
    employmentStatus: undefined,
    ethnicity: undefined,
    nativeProvince: undefined,
    nativeCity: undefined,
    bankCard: undefined,
    homeInformation: undefined,
    schoolMajor: undefined,
    professionalCategory: undefined,
    nameAbbreviation: undefined
  }
  formRef.value?.resetFields()
  nativeAreaIds.value = undefined
  outboundSummary.value = { supportYears: 0, continuingEducationCredit: 0 }
  outboundList.value = []
}
</script>
