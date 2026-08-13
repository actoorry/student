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
      <el-form-item label="手机号" prop="employeeMobile">
        <el-input
          v-model="queryParams.employeeMobile"
          placeholder="请输入手机号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input
          v-model="queryParams.idCard"
          placeholder="请输入身份证号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="员工工号" prop="employeeNo">
        <el-input
          v-model="queryParams.employeeNo"
          placeholder="请输入员工工号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="岗位" prop="postId">
        <el-select v-model="queryParams.postId" placeholder="请选择岗位" clearable class="!w-240px">
          <el-option
            v-for="item in postList"
            :key="item.id"
            :label="item.name"
            :value="item.id!"
          />
        </el-select>
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
      <el-form-item label="在职状态" prop="employmentStatus">
        <el-select
          v-model="queryParams.employmentStatus"
          placeholder="请选择在职状态"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_EMPLOYMENT_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['hr:employee:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['hr:employee:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['hr:employee:delete']"
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
      <el-table-column label="姓名" align="center" prop="name" min-width="100px" />
      <el-table-column label="手机号" align="center" prop="employeeMobile" min-width="120px" />
      <el-table-column label="身份证号" align="center" prop="idCard" min-width="180px" />
      <el-table-column label="员工工号" align="center" prop="employeeNo" />
      <el-table-column label="部门" align="center" prop="deptName" min-width="120px">
        <template #default="scope">
          {{ scope.row.deptName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="人员类别" align="center" prop="personnelCategory" min-width="100px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.HR_PERSONNEL_CATEGORY" :value="scope.row.personnelCategory" />
        </template>
      </el-table-column>
      <el-table-column label="岗位" align="center" prop="postName" min-width="100px">
        <template #default="scope">
          {{ scope.row.postName || scope.row.position || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="职称" align="center" prop="professionalTitle" min-width="100px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.HR_PROFESSIONAL_TITLE" :value="scope.row.professionalTitle" />
        </template>
      </el-table-column>
      <el-table-column label="在职状态" align="center" prop="employmentStatus" min-width="100px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.HR_EMPLOYMENT_STATUS" :value="scope.row.employmentStatus" />
        </template>
      </el-table-column>
      <el-table-column
        label="加入单位时间"
        align="center"
        prop="hireDate"
        min-width="120px"
        :formatter="dateFormatter2"
      />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="120px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['hr:employee:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['hr:employee:delete']"
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
  <EmployeeForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { isEmpty } from '@/utils/is'
import { dateFormatter, dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
import { EmployeeApi, type Employee } from '@/api/hr/employee'
import * as PostApi from '@/api/system/post'
import EmployeeForm from './EmployeeForm.vue'

/** 员工列表 */
defineOptions({ name: 'HrEmployee' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<Employee[]>([])
const total = ref(0)
const postList = ref([] as PostApi.PostVO[])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  employeeMobile: undefined,
  idCard: undefined,
  employeeNo: undefined,
  postId: undefined,
  personnelCategory: undefined,
  employmentStatus: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EmployeeApi.getEmployeePage(queryParams)
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

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EmployeeApi.deleteEmployee(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 批量删除员工 */
const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    await EmployeeApi.deleteEmployeeList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Employee[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await EmployeeApi.exportEmployee(queryParams)
    download.excel(data, '员工.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 */
onMounted(async () => {
  postList.value = await PostApi.getSimplePostList()
  getList()
})
</script>
