<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="formData.mobile" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="用户昵称" prop="nickname">
        <el-input v-model="formData.nickname" placeholder="请输入用户昵称" />
      </el-form-item>
      <el-form-item label="头像" prop="avatar">
        <UploadImg v-model="formData.avatar" :limit="1" :is-show-tip="false" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="formData.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="用户性别" prop="sex">
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
      <el-form-item label="所在地" prop="areaId">
        <el-tree-select
          v-model="formData.areaId"
          :data="areaList"
          :props="defaultProps"
          :render-after-expand="true"
        />
      </el-form-item>
      <el-form-item label="是否客户" prop="isCustomer">
        <el-checkbox v-model="formData.isCustomer" />
      </el-form-item>
      <el-form-item label="是否供应商" prop="isSupplier">
        <el-checkbox v-model="formData.isSupplier" />
      </el-form-item>
      <el-form-item label="是否公司" prop="isCompany">
        <el-checkbox v-model="formData.isCompany" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input type="textarea" v-model="formData.remark" placeholder="请输入备注" />
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
import * as PartnerApi from '@/api/partner/partner'
import * as AreaApi from '@/api/system/area'
import { defaultProps } from '@/utils/tree'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  mobile: undefined,
  status: undefined,
  nickname: undefined,
  avatar: undefined,
  email: undefined,
  sex: undefined,
  areaId: undefined,
  birthday: undefined,
  isCustomer: undefined,
  isSupplier: undefined,
  isCompany: undefined,
  remark: undefined
})
const formRules = reactive({
  mobile: [{ required: true, message: '手机号不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }]
})
const formRef = ref()
const areaList = ref([])

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await PartnerApi.getPartner(id)
    } finally {
      formLoading.value = false
    }
  }
  areaList.value = await AreaApi.getAreaTree()
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = formData.value as unknown as PartnerApi.PartnerVO
    if (formType.value === 'create') {
      await PartnerApi.createPartner(data)
      message.success(t('common.createSuccess'))
    } else {
      await PartnerApi.updatePartner(data)
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
    mobile: undefined,
    status: undefined,
    nickname: undefined,
    avatar: undefined,
    email: undefined,
    sex: undefined,
    areaId: undefined,
    birthday: undefined,
    isCustomer: undefined,
    isSupplier: undefined,
    isCompany: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
