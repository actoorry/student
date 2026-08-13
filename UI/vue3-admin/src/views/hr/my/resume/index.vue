<template>
  <ContentWrap>
    <!-- 搜索工作栏：职工端无姓名搜索 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="78px"
    >
      <el-form-item>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['hr:my:resume:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增履历
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="所在部门" align="center" prop="deptName" min-width="120px" />
      <el-table-column label="论文" align="center" prop="papers" min-width="160px" :show-overflow-tooltip="true" />
      <el-table-column label="奖惩情况" align="center" prop="awardsPunishments" min-width="160px" :show-overflow-tooltip="true" />
      <el-table-column label="年度考核" align="center" prop="annualReview" min-width="160px" :show-overflow-tooltip="true" />
      <el-table-column label="籍贯" align="center" min-width="140px">
        <template #default="scope">
          {{ [scope.row.province, scope.row.city, scope.row.county].filter(Boolean).join(' / ') || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" min-width="160px" :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" min-width="120px" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['hr:my:resume:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['hr:my:resume:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <HrMyResumeForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { MyResumeApi } from '@/api/hr/my/resume'
import type { Resume } from '@/api/hr/resume'
import HrMyResumeForm from './HrMyResumeForm.vue'

/** 职工我的履历 列表 */
defineOptions({ name: 'HrMyResume' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<Resume[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await MyResumeApi.getMyResumePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await MyResumeApi.deleteMyResume(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(getList)
</script>
