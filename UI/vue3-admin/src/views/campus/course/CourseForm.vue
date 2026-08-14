<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="课程编号" prop="courseCode">
        <el-input v-model="formData.courseCode" placeholder="请输入课程编号" />
      </el-form-item>
      <el-form-item label="课程名" prop="courseName">
        <el-input v-model="formData.courseName" placeholder="请输入课程名" />
      </el-form-item>
      <el-form-item label="学分" prop="credit">
        <el-input-number
          v-model="formData.credit"
          :min="0"
          :precision="2"
          controls-position="right"
          placeholder="请输入学分"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="授课教师" prop="teacherId">
        <el-input-number
          v-model="formData.teacherId"
          :min="0"
          controls-position="right"
          placeholder="请输入授课教师ID"
          class="!w-full"
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
import { CourseApi, Course } from '@/api/campus/course'

/** 课程 表单 */
defineOptions({ name: 'CourseForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  courseCode: undefined,
  courseName: undefined,
  credit: undefined,
  teacherId: undefined,
})
const formRules = reactive({
  courseCode: [{ required: true, message: '课程编号不能为空', trigger: 'blur' }],
  courseName: [{ required: true, message: '课程名不能为空', trigger: 'blur' }],
})
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await CourseApi.getCourse(id)
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
    const data = formData.value as unknown as Course
    if (formType.value === 'create') {
      await CourseApi.createCourse(data)
      message.success(t('common.createSuccess'))
    } else {
      await CourseApi.updateCourse(data)
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
    courseCode: undefined,
    courseName: undefined,
    credit: undefined,
    teacherId: undefined,
  }
  formRef.value?.resetFields()
}
</script>
