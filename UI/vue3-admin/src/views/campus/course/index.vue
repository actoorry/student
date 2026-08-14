<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="课程编号" prop="courseCode">
        <el-input
          v-model="queryParams.courseCode"
          placeholder="请输入课程编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="课程名" prop="courseName">
        <el-input
          v-model="queryParams.courseName"
          placeholder="请输入课程名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="授课教师" prop="teacherId">
        <el-input-number
          v-model="queryParams.teacherId"
          :min="1"
          controls-position="right"
          placeholder="请输入教师ID"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['campus:course:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="课程编号" align="center" prop="courseCode" />
      <el-table-column label="课程名" align="center" prop="courseName" />
      <el-table-column label="学分" align="center" prop="credit" />
      <el-table-column label="授课教师" align="center" prop="teacherName">
        <template #default="scope">
          {{ scope.row.teacherName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="及格人数" align="center" prop="passCount">
        <template #default="scope">
          {{ scope.row.passCount != null ? scope.row.passCount : '-' }}
        </template>
      </el-table-column>
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
            v-hasPermi="['campus:course:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['campus:course:delete']"
          >
            删除
          </el-button>
          <el-button
            link
            type="success"
            @click="openSelectDialog(scope.row.id)"
            v-hasPermi="['campus:course:select']"
          >
            选课
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
  <CourseForm ref="formRef" @success="getList" />

  <!-- 选课弹窗 -->
  <Dialog title="学生选课" v-model="selectDialogVisible">
    <el-form
      ref="selectFormRef"
      :model="selectData"
      :rules="selectRules"
      label-width="100px"
      v-loading="selectLoading"
    >
      <el-form-item label="学生ID" prop="studentId">
        <el-input-number
          v-model="selectData.studentId"
          :min="1"
          controls-position="right"
          placeholder="请输入学生ID"
          class="!w-full"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitSelect" type="primary" :disabled="selectLoading">确 定</el-button>
      <el-button @click="selectDialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { CourseApi, Course, CoursePageReqVO } from '@/api/campus/course'
import CourseForm from './CourseForm.vue'

/** 课程 列表 */
defineOptions({ name: 'CampusCourse' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<Course[]>([])
const total = ref(0)
const queryParams = reactive<CoursePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  courseCode: undefined,
  courseName: undefined,
  teacherId: undefined,
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CourseApi.getCoursePage(queryParams)
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
    await CourseApi.deleteCourse(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 选课弹窗 */
const selectDialogVisible = ref(false)
const selectLoading = ref(false)
const selectFormRef = ref()
const currentCourseId = ref<number>()
const selectData = reactive<{ studentId?: number }>({
  studentId: undefined,
})
const selectRules = reactive({
  studentId: [{ required: true, message: '学生ID不能为空', trigger: 'blur' }],
})
const openSelectDialog = (courseId: number) => {
  currentCourseId.value = courseId
  selectData.studentId = undefined
  selectDialogVisible.value = true
  selectFormRef.value?.resetFields()
}
const submitSelect = async () => {
  await selectFormRef.value.validate()
  if (selectData.studentId == null || currentCourseId.value == null) return
  selectLoading.value = true
  try {
    await CourseApi.selectCourse(selectData.studentId, currentCourseId.value)
    message.success('选课成功')
    selectDialogVisible.value = false
    await getList()
  } finally {
    selectLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
