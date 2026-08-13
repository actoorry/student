<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入姓名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="部门" prop="dept">
        <el-tree-select
          v-model="queryParams.dept"
          :data="deptList"
          :props="defaultProps"
          check-strictly
          node-key="id"
          placeholder="请选择部门"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="人员编号" prop="employeeNo">
        <el-input
          v-model="queryParams.employeeNo"
          placeholder="请输入人员编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="人员类别" prop="personnelCategory">
        <el-select
          v-model="queryParams.personnelCategory"
          placeholder="请选择人员类别"
          clearable
          class="!w-240px"
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
        <el-input
          v-model="queryParams.year"
          placeholder="请输入年份"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="月份" prop="month">
        <el-input
          v-model="queryParams.month"
          placeholder="请输入月份"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['hr:salary:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="warning"
          plain
          @click="openImportForm"
          v-hasPermi="['hr:salary:import']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导入
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['hr:salary:export']"
        >
          <Icon icon="ep:upload" class="mr-5px" /> 导出
        </el-button>
        <el-button
            type="danger"
            plain
            :disabled="isEmpty(checkedIds)"
            @click="handleDeleteBatch"
            v-hasPermi="['hr:salary:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
        row-key="id"
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        @selection-change="handleRowCheckboxChange"
    >
    <el-table-column type="selection" width="55" />
      <el-table-column label="姓名" align="center" prop="name" />
      <el-table-column label="部门" align="center" prop="deptName" min-width="120px">
        <template #default="scope">
          {{ scope.row.deptName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="人员编号" align="center" prop="employeeNo" />
      <el-table-column label="人员类别" align="center" prop="personnelCategory" min-width="100px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.HR_PERSONNEL_CATEGORY" :value="scope.row.personnelCategory" />
        </template>
      </el-table-column>
      <el-table-column label="年份" align="center" prop="year" />
      <el-table-column label="月份" align="center" prop="month" />
      <el-table-column label="岗位工资" align="center" prop="basicSalary" />
      <el-table-column label="薪级工资" align="center" prop="salaryGrade" />
      <el-table-column label="单位职补" align="center" prop="unitAllowance" />
      <el-table-column label="岗位津贴" align="center" prop="postAllowance" />
      <el-table-column label="应发合计" align="center" prop="grossSalaryTotal" />
      <el-table-column label="扣款合计" align="center" prop="totalDeduction" />
      <el-table-column label="实发合计" align="center" prop="netSalaryTotal" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['hr:salary:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['hr:salary:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <SalaryForm ref="formRef" @success="getList" />

  <!-- 表单弹窗：导入 -->
  <SalaryImportForm ref="importFormRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { SalaryApi, Salary } from '@/api/hr/salary'
import SalaryForm from './SalaryForm.vue'
import SalaryImportForm from './SalaryImportForm.vue'
import * as DeptApi from '@/api/system/dept'
import { defaultProps, handleTree } from '@/utils/tree'

/** 薪资 列表 */
defineOptions({ name: 'HrSalary' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<Salary[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  partnerId: undefined,
  name: undefined,
  dept: undefined,
  employeeNo: undefined,
  personnelCategory: undefined,
  year: undefined,
  month: undefined,
  createTime: [],
})
const queryFormRef = ref()
const exportLoading = ref(false)
const deptList = ref<Tree[]>([])

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await SalaryApi.getSalaryPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 导入操作 */
const importFormRef = ref()
const openImportForm = () => {
  importFormRef.value.open()
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await SalaryApi.deleteSalary(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 批量删除薪资 */
const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    await SalaryApi.deleteSalaryList(checkedIds.value);
    checkedIds.value = [];
    message.success(t('common.delSuccess'))
    await getList();
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Salary[]) => {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await SalaryApi.exportSalary(queryParams)
    download.excel(data, '薪资.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(async () => {
  deptList.value = handleTree(await DeptApi.getSimpleDeptList())
  getList()
})
</script>
