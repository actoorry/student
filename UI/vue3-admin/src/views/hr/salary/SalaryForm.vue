<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
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
      <el-form-item label="人员编号" prop="employeeNo">
        <el-input v-model="formData.employeeNo" placeholder="请输入人员编号" />
      </el-form-item>
      <el-form-item label="人员类别" prop="personnelCategory">
        <el-select
          v-model="formData.personnelCategory"
          placeholder="请选择人员类别"
          clearable
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
      <el-form-item label="年份" prop="year">
        <el-input v-model="formData.year" placeholder="请输入年份" />
      </el-form-item>
      <el-form-item label="月份" prop="month">
        <el-input v-model="formData.month" placeholder="请输入月份" />
      </el-form-item>
      <el-form-item label="岗位工资" prop="basicSalary">
        <el-input v-model="formData.basicSalary" placeholder="请输入岗位工资" />
      </el-form-item>
      <el-form-item label="薪级工资" prop="salaryGrade">
        <el-input v-model="formData.salaryGrade" placeholder="请输入薪级工资" />
      </el-form-item>
      <el-form-item label="单位职补" prop="unitAllowance">
        <el-input v-model="formData.unitAllowance" placeholder="请输入单位职补" />
      </el-form-item>
      <el-form-item label="岗位津贴" prop="postAllowance">
        <el-input v-model="formData.postAllowance" placeholder="请输入岗位津贴" />
      </el-form-item>
      <el-form-item label="独生子女" prop="onlyChildAllowance">
        <el-input v-model="formData.onlyChildAllowance" placeholder="请输入独生子女" />
      </el-form-item>
      <el-form-item label="回民补贴" prop="huiEthnicAllowance">
        <el-input v-model="formData.huiEthnicAllowance" placeholder="请输入回民补贴" />
      </el-form-item>
      <el-form-item label="计生兼职" prop="familyPlanningAllowance">
        <el-input v-model="formData.familyPlanningAllowance" placeholder="请输入计生兼职" />
      </el-form-item>
      <el-form-item label="福利费" prop="welfareFee">
        <el-input v-model="formData.welfareFee" placeholder="请输入福利费" />
      </el-form-item>
      <el-form-item label="公务交通补贴" prop="officialTransportAllowance">
        <el-input v-model="formData.officialTransportAllowance" placeholder="请输入公务交通补贴" />
      </el-form-item>
      <el-form-item label="提租补贴" prop="rentAllowance">
        <el-input v-model="formData.rentAllowance" placeholder="请输入提租补贴" />
      </el-form-item>
      <el-form-item label="反聘费" prop="rehireFee">
        <el-input v-model="formData.rehireFee" placeholder="请输入反聘费" />
      </el-form-item>
      <el-form-item label="补发工资" prop="backPay">
        <el-input v-model="formData.backPay" placeholder="请输入补发工资" />
      </el-form-item>
      <el-form-item label="其他工资" prop="otherWage">
        <el-input v-model="formData.otherWage" placeholder="请输入其他工资" />
      </el-form-item>
      <el-form-item label="基础性绩效" prop="basicPerformance">
        <el-input v-model="formData.basicPerformance" placeholder="请输入基础性绩效" />
      </el-form-item>
      <el-form-item label="绩效工资" prop="performanceSalary">
        <el-input v-model="formData.performanceSalary" placeholder="请输入绩效工资" />
      </el-form-item>
      <el-form-item label="应发合计" prop="grossSalaryTotal">
        <el-input v-model="formData.grossSalaryTotal" placeholder="请输入应发合计" />
      </el-form-item>
      <el-form-item label="社保基金" prop="socialSecurity">
        <el-input v-model="formData.socialSecurity" placeholder="请输入社保基金" />
      </el-form-item>
      <el-form-item label="医保金" prop="medicalInsurance">
        <el-input v-model="formData.medicalInsurance" placeholder="请输入医保金" />
      </el-form-item>
      <el-form-item label="职业年金" prop="occupationalAnnuity">
        <el-input v-model="formData.occupationalAnnuity" placeholder="请输入职业年金" />
      </el-form-item>
      <el-form-item label="失业金" prop="unemploymentInsurance">
        <el-input v-model="formData.unemploymentInsurance" placeholder="请输入失业金" />
      </el-form-item>
      <el-form-item label="住房公积金" prop="housingFund">
        <el-input v-model="formData.housingFund" placeholder="请输入住房公积金" />
      </el-form-item>
      <el-form-item label="工会经费" prop="unionFee">
        <el-input v-model="formData.unionFee" placeholder="请输入工会经费" />
      </el-form-item>
      <el-form-item label="房租费用" prop="rentFee">
        <el-input v-model="formData.rentFee" placeholder="请输入房租费用" />
      </el-form-item>
      <el-form-item label="病事假" prop="sickLeaveDeduction">
        <el-input v-model="formData.sickLeaveDeduction" placeholder="请输入病事假" />
      </el-form-item>
      <el-form-item label="代扣所得税" prop="incomeTax">
        <el-input v-model="formData.incomeTax" placeholder="请输入代扣所得税" />
      </el-form-item>
      <el-form-item label="其他扣款" prop="otherDeduction">
        <el-input v-model="formData.otherDeduction" placeholder="请输入其他扣款" />
      </el-form-item>
      <el-form-item label="扣款合计" prop="totalDeduction">
        <el-input v-model="formData.totalDeduction" placeholder="请输入扣款合计" />
      </el-form-item>
      <el-form-item label="实发合计" prop="netSalaryTotal">
        <el-input v-model="formData.netSalaryTotal" placeholder="请输入实发合计" />
      </el-form-item>
      <el-form-item label="所得基数" prop="taxBase">
        <el-input v-model="formData.taxBase" placeholder="请输入所得基数" />
      </el-form-item>
      <el-form-item label="子女教育" prop="childEducation">
        <el-input v-model="formData.childEducation" placeholder="请输入子女教育" />
      </el-form-item>
      <el-form-item label="继续教育" prop="continuingEducation">
        <el-input v-model="formData.continuingEducation" placeholder="请输入继续教育" />
      </el-form-item>
      <el-form-item label="住房贷款利息" prop="housingLoanInterest">
        <el-input v-model="formData.housingLoanInterest" placeholder="请输入住房贷款利息" />
      </el-form-item>
      <el-form-item label="住房租金" prop="housingRent">
        <el-input v-model="formData.housingRent" placeholder="请输入住房租金" />
      </el-form-item>
      <el-form-item label="老人赡养费" prop="elderlySupport">
        <el-input v-model="formData.elderlySupport" placeholder="请输入老人赡养费" />
      </el-form-item>
      <el-form-item label="其他合法扣除" prop="otherLegalDeduction">
        <el-input v-model="formData.otherLegalDeduction" placeholder="请输入其他合法扣除" />
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
import { SalaryApi, Salary } from '@/api/hr/salary'
import * as DeptApi from '@/api/system/dept'
import { defaultProps, handleTree } from '@/utils/tree'

/** 薪资 表单 */
defineOptions({ name: 'SalaryForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  partnerId: undefined,
  dept: undefined,
  employeeNo: undefined,
  personnelCategory: undefined,
  year: undefined,
  month: undefined,
  basicSalary: undefined,
  salaryGrade: undefined,
  unitAllowance: undefined,
  postAllowance: undefined,
  onlyChildAllowance: undefined,
  huiEthnicAllowance: undefined,
  familyPlanningAllowance: undefined,
  welfareFee: undefined,
  officialTransportAllowance: undefined,
  rentAllowance: undefined,
  rehireFee: undefined,
  backPay: undefined,
  otherWage: undefined,
  basicPerformance: undefined,
  performanceSalary: undefined,
  grossSalaryTotal: undefined,
  socialSecurity: undefined,
  medicalInsurance: undefined,
  occupationalAnnuity: undefined,
  unemploymentInsurance: undefined,
  housingFund: undefined,
  unionFee: undefined,
  rentFee: undefined,
  sickLeaveDeduction: undefined,
  incomeTax: undefined,
  otherDeduction: undefined,
  totalDeduction: undefined,
  netSalaryTotal: undefined,
  taxBase: undefined,
  childEducation: undefined,
  continuingEducation: undefined,
  housingLoanInterest: undefined,
  housingRent: undefined,
  elderlySupport: undefined,
  otherLegalDeduction: undefined,
})
const formRules = reactive({
})
const formRef = ref()
const deptList = ref<Tree[]>([])

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  deptList.value = handleTree(await DeptApi.getSimpleDeptList())
  if (id) {
    formLoading.value = true
    try {
      formData.value = await SalaryApi.getSalary(id)
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
    const data = formData.value as unknown as Salary
    if (formType.value === 'create') {
      await SalaryApi.createSalary(data)
      message.success(t('common.createSuccess'))
    } else {
      await SalaryApi.updateSalary(data)
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
    dept: undefined,
    employeeNo: undefined,
    personnelCategory: undefined,
    year: undefined,
    month: undefined,
    basicSalary: undefined,
    salaryGrade: undefined,
    unitAllowance: undefined,
    postAllowance: undefined,
    onlyChildAllowance: undefined,
    huiEthnicAllowance: undefined,
    familyPlanningAllowance: undefined,
    welfareFee: undefined,
    officialTransportAllowance: undefined,
    rentAllowance: undefined,
    rehireFee: undefined,
    backPay: undefined,
    otherWage: undefined,
    basicPerformance: undefined,
    performanceSalary: undefined,
    grossSalaryTotal: undefined,
    socialSecurity: undefined,
    medicalInsurance: undefined,
    occupationalAnnuity: undefined,
    unemploymentInsurance: undefined,
    housingFund: undefined,
    unionFee: undefined,
    rentFee: undefined,
    sickLeaveDeduction: undefined,
    incomeTax: undefined,
    otherDeduction: undefined,
    totalDeduction: undefined,
    netSalaryTotal: undefined,
    taxBase: undefined,
    childEducation: undefined,
    continuingEducation: undefined,
    housingLoanInterest: undefined,
    housingRent: undefined,
    elderlySupport: undefined,
    otherLegalDeduction: undefined,
  }
  formRef.value?.resetFields()
}
</script>
