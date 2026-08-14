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
      <el-form-item label="学生ID" prop="studentId">
        <el-input-number
          v-model="queryParams.studentId"
          :min="1"
          controls-position="right"
          placeholder="请输入学生ID"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="课程ID" prop="courseId">
        <el-input-number
          v-model="queryParams.courseId"
          :min="1"
          controls-position="right"
          placeholder="请输入课程ID"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="学年" prop="schoolYear">
        <el-input
          v-model="queryParams.schoolYear"
          placeholder="如 2026-2027"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="学期" prop="semester">
        <el-select
          v-model="queryParams.semester"
          placeholder="请选择学期"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CAMPUS_SEMESTER)"
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
          v-hasPermi="['campus:exam-record:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="学生姓名" align="center" prop="studentName" />
      <el-table-column label="课程名称" align="center" prop="courseName" />
      <el-table-column label="学年" align="center" prop="schoolYear" />
      <el-table-column label="学期" align="center" prop="semester">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.CAMPUS_SEMESTER" :value="scope.row.semester" />
        </template>
      </el-table-column>
      <el-table-column label="分数" align="center" prop="score" />
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
            v-hasPermi="['campus:exam-record:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['campus:exam-record:delete']"
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
  <ExamRecordForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { ExamRecordApi, ExamRecord, ExamRecordPageReqVO } from '@/api/campus/examRecord'
import ExamRecordForm from './ExamRecordForm.vue'

/** 考试记录 列表 */
defineOptions({ name: 'CampusExamRecord' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<ExamRecord[]>([])
const total = ref(0)
const queryParams = reactive<ExamRecordPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  studentId: undefined,
  courseId: undefined,
  schoolYear: undefined,
  semester: undefined,
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ExamRecordApi.getExamRecordPage(queryParams)
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
    await ExamRecordApi.deleteExamRecord(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
