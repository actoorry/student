<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
      <el-form-item label="类型名称" prop="name">
        <el-input v-model="formData.name" placeholder="例如：采购入库、销售出库" maxlength="50" />
      </el-form-item>

      <el-form-item label="库存影响方向" prop="stockImpact">
        <el-radio-group v-model="formData.stockImpact">
          <el-radio :value="1">入库（增加库存）</el-radio>
          <el-radio :value="2">出库（减少库存）</el-radio>
          <el-radio :value="3">内部调拨</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="默认来源" prop="fromLocationId">
            <el-select v-model="formData.fromLocationId" placeholder="选择来源" clearable class="!w-full">
              <el-option label="用户自选（实体库位）" :value="0" />
              <el-option v-for="(name, id) in VIRTUAL_WAREHOUSE" :key="id" :label="name" :value="Number(id)" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="默认去向" prop="toLocationId">
            <el-select v-model="formData.toLocationId" placeholder="选择去向" clearable class="!w-full">
              <el-option label="用户自选（实体库位）" :value="0" />
              <el-option v-for="(name, id) in VIRTUAL_WAREHOUSE" :key="id" :label="name" :value="Number(id)" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="强制供应商" prop="needSupplier">
            <el-radio-group v-model="formData.needSupplier">
              <el-radio :value="1">是</el-radio>
              <el-radio :value="0">否</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="强制客户" prop="needCustomer">
            <el-radio-group v-model="formData.needCustomer">
              <el-radio :value="1">是</el-radio>
              <el-radio :value="0">否</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="状态" prop="active">
            <el-radio-group v-model="formData.active">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="formData.sort" :min="0" :max="999" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="备注" type="textarea" :rows="2" maxlength="500" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { OrderTypeApi, OrderType } from '@/api/wms/ordertype'
import { VIRTUAL_WAREHOUSE } from '../enums'

/** 单据类型配置表单弹窗 */
defineOptions({ name: 'WmsOrderTypeForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')

const formData = ref({
  id: undefined,
  name: undefined,
  fromLocationId: 0,
  toLocationId: 0,
  stockImpact: 1,
  needSupplier: 0,
  needCustomer: 0,
  active: 1,
  sort: 0,
  remark: undefined
})

const formRules = reactive({
  name: [{ required: true, message: '类型名称不能为空', trigger: 'blur' }],
  stockImpact: [{ required: true, message: '库存影响方向不能为空', trigger: 'change' }],
  fromLocationId: [{ required: true, message: '请选择默认来源', trigger: 'change' }],
  toLocationId: [{ required: true, message: '请选择默认去向', trigger: 'change' }],
  needSupplier: [{ required: true, message: '请选择', trigger: 'change' }],
  needCustomer: [{ required: true, message: '请选择', trigger: 'change' }],
  active: [{ required: true, message: '请选择状态', trigger: 'change' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
})

const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增单据类型' : '编辑单据类型'
  formType.value = type
  resetForm()

  if (id) {
    formLoading.value = true
    try {
      const data = await OrderTypeApi.getOrderType(id)
      formData.value = { ...formData.value, ...data }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as OrderType
    if (formType.value === 'create') {
      await OrderTypeApi.createOrderType(data)
      message.success(t('common.createSuccess'))
    } else {
      await OrderTypeApi.updateOrderType(data)
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
    name: undefined,
    fromLocationId: 0,
    toLocationId: 0,
    stockImpact: 1,
    needSupplier: 0,
    needCustomer: 0,
    active: 1,
    sort: 0,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>