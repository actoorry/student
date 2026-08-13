<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <el-form-item label="单位名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入单位名称" />
      </el-form-item>
      <el-form-item label="单位类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择单位类型" class="w-100%">
          <el-option
            v-for="dict in unitTypeOptions"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="换算系数" prop="relativeFactor">
        <el-input v-model="formData.relativeFactor" placeholder="请输入换算到基础单位的系数，例如 1000" />
        <el-text v-if="selectedBaseUnitName" type="info" class="mt-6px">
          当前类型基础单位：{{ selectedBaseUnitName }}
        </el-text>
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
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import * as ProductUnitApi from '@/api/product/unit'

defineOptions({ name: 'ProductUnitForm' })

const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<ProductUnitApi.UnitVO>({
  id: undefined,
  name: '',
  status: CommonStatusEnum.ENABLE,
  type: undefined as unknown as number,
  relativeFactor: undefined
})
const formRef = ref()

const unitTypeOptions = computed(() =>
  getIntDictOptions(DICT_TYPE.PRODUCT_UNIT_TYPE).filter((dict) => dict.value !== 0)
)

const baseUnitNameMap: Record<string, string> = {
  数量型: '个',
  重量型: '克',
  长度型: '毫米',
  体积型: '毫升',
  时间型: '秒',
  次数型: '次'
}

const selectedBaseUnitName = computed(() => {
  const typeLabel = unitTypeOptions.value.find((dict) => dict.value === formData.value.type)?.label
  return typeLabel ? baseUnitNameMap[typeLabel] : ''
})

const validateRelativeFactor = (_rule: any, value: string, callback: any) => {
  const numberValue = Number(value)
  if (!value || Number.isNaN(numberValue) || numberValue <= 0) {
    callback(new Error('换算系数必须大于 0'))
    return
  }
  callback()
}

const formRules = reactive({
  name: [{ required: true, message: '单位名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '单位类型不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
  relativeFactor: [{ validator: validateRelativeFactor, trigger: 'blur' }]
})

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ProductUnitApi.getUnit(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = { ...formData.value }
    if (formType.value === 'create') {
      await ProductUnitApi.createUnit(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductUnitApi.updateUnit(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    status: CommonStatusEnum.ENABLE,
    type: undefined as unknown as number,
    relativeFactor: undefined
  }
  formRef.value?.resetFields()
}
</script>
