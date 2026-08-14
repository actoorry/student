<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="formData.studentNo" placeholder="请输入学号" />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="formData.gender" placeholder="请选择性别" clearable class="!w-full">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CAMPUS_GENDER)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="班级" prop="className">
        <el-input v-model="formData.className" placeholder="请输入班级" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="formData.mobile" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="登录账号ID" prop="userId">
        <el-input-number
          v-model="formData.userId"
          :min="0"
          controls-position="right"
          placeholder="请输入关联登录账号ID"
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
import { StudentApi, Student } from '@/api/campus/student'

/** 学生 表单 */
defineOptions({ name: 'StudentForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  studentNo: undefined,
  name: undefined,
  gender: undefined,
  className: undefined,
  mobile: undefined,
  userId: undefined,
})
const formRules = reactive({
  studentNo: [{ required: true, message: '学号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
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
      formData.value = await StudentApi.getStudent(id)
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
    const data = formData.value as unknown as Student
    if (formType.value === 'create') {
      await StudentApi.createStudent(data)
      message.success(t('common.createSuccess'))
    } else {
      await StudentApi.updateStudent(data)
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
    studentNo: undefined,
    name: undefined,
    gender: undefined,
    className: undefined,
    mobile: undefined,
    userId: undefined,
  }
  formRef.value?.resetFields()
}
</script>
