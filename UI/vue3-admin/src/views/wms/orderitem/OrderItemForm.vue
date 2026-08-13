<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="700px" :close-on-click-modal="false">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" v-loading="formLoading">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="SKU ID" prop="skuId">
            <el-input v-model="formData.skuId" placeholder="SKU ID" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="来源库位" prop="fromLocationId">
            <el-select v-model="formData.fromLocationId" placeholder="来源" clearable class="!w-full">
              <el-option v-for="w in locationList" :key="w.id" :label="w.name" :value="w.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="去向库位" prop="toLocationId">
            <el-select v-model="formData.toLocationId" placeholder="去向" clearable class="!w-full">
              <el-option v-for="w in locationList" :key="w.id" :label="w.name" :value="w.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="数量" prop="quantity">
            <el-input-number v-model="formData.quantity" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="批次号" prop="batchNo">
            <el-input v-model="formData.batchNo" placeholder="批次号" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="单价" prop="unitPrice">
            <el-input-number v-model="formData.unitPrice" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="生产日期" prop="productionDate">
            <el-date-picker v-model="formData.productionDate" type="date" value-format="YYYY-MM-DD" placeholder="生产日期" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="有效期至" prop="expiryDate">
            <el-date-picker v-model="formData.expiryDate" type="date" value-format="YYYY-MM-DD" placeholder="有效期至" class="!w-full" />
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
import { OrderItemApi } from '@/api/wms/order'
import { WarehouseApi } from '@/api/wms/warehouse'
import { isVirtualWarehouse } from '../enums'

/** 出入库单据明细行表单弹窗（嵌入OrderForm使用） */
defineOptions({ name: 'WmsOrderItemForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const orderId = ref<number | undefined>(undefined)
const locationList = ref<any[]>([])

const formData = ref({
  id: undefined,
  orderId: undefined,
  skuId: undefined,
  fromLocationId: undefined,
  toLocationId: undefined,
  quantity: 0,
  batchNo: undefined,
  productionDate: undefined,
  expiryDate: undefined,
  unitPrice: undefined,
  remark: undefined
})

const formRules = reactive({
  skuId: [{ required: true, message: 'SKU ID不能为空', trigger: 'blur' }],
  quantity: [{ required: true, message: '数量不能为空', trigger: 'blur' }]
})

const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number, parentOrderId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增明细行' : '编辑明细行'
  formType.value = type
  orderId.value = parentOrderId
  resetForm()

  // 加载库位列表
  try {
    const res = await WarehouseApi.getWarehousePage({ pageSize: 200 })
    locationList.value = (res.list || []).filter((w: any) => !isVirtualWarehouse(w.id))
  } catch { }

  if (id) {
    formLoading.value = true
    try {
      const data = await OrderItemApi.getOrderItem(id)
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
    const data = { ...formData.value, orderId: orderId.value } as any
    if (formType.value === 'create') {
      await OrderItemApi.createOrderItem(data)
      message.success(t('common.createSuccess'))
    } else {
      await OrderItemApi.updateOrderItem(data)
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
    orderId: undefined,
    skuId: undefined,
    fromLocationId: undefined,
    toLocationId: undefined,
    quantity: 0,
    batchNo: undefined,
    productionDate: undefined,
    expiryDate: undefined,
    unitPrice: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>