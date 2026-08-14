<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="学生ID" prop="studentId">
        <el-input-number
          v-model="formData.studentId"
          :min="1"
          controls-position="right"
          placeholder="请输入学生ID"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="课程ID" prop="courseId">
        <el-input-number
          v-model="formData.courseId"
          :min="1"
          controls-position="right"
          placeholder="请输入课程ID"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="学年" prop="schoolYear">
        <el-input v-model="formData.schoolYear" placeholder="如 2026-2027" />
      </el-form-item>
      <el-form-item label="学期" prop="semester">
        <el-select v-model="formData.semester" placeholder="请选择学期" clearable class="!w-full">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CAMPUS_SEMESTER)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="分数" prop="score">
        <el-input-number
          v-model="formData.score"
          :min="0"
          :max="100"
          :precision="1"
          :step="0.5"
          controls-position="right"
          placeholder="请输入分数"
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
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { ExamRecordApi, ExamRecord } from '@/api/campus/examRecord'

/** 考试记录 表单 */
defineOptions({ name: 'ExamRecordForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  studentId: undefined,
  courseId: undefined,
  schoolYear: undefined,
  semester: undefined,
  score: undefined,
})
const formRules = reactive({
  studentId: [{ required: true, message: '学生ID不能为空', trigger: 'blur' }],
  courseId: [{ required: true, message: '课程ID不能为空', trigger: 'blur' }],
  schoolYear: [{ required: true, message: '学年不能为空', trigger: 'blur' }],
  semester: [{ required: true, message: '学期不能为空', trigger: 'change' }],
  score: [{ required: true, message: '分数不能为空', trigger: 'blur' }],
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
      formData.value = await ExamRecordApi.getExamRecord(id)
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
    const data = formData.value as unknown as ExamRecord
    if (formType.value === 'create') {
      await ExamRecordApi.createExamRecord(data)
      message.success(t('common.createSuccess'))
    } else {
      await ExamRecordApi.updateExamRecord(data)
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
    studentId: undefined,
    courseId: undefined,
    schoolYear: undefined,
    semester: undefined,
    score: undefined,
  }
  formRef.value?.resetFields()
}
</script>
