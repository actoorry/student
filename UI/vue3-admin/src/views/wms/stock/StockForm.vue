<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="产品SKU ID，FK -> product_sku.id" prop="skuId">
        <el-input v-model="formData.skuId" placeholder="请输入产品SKU ID，FK -> product_sku.id" />
      </el-form-item>
      <el-form-item label="所属实体仓库ID，FK -> wms_warehouse.id（parent_id=0）" prop="warehouseId">
        <el-input v-model="formData.warehouseId" placeholder="请输入所属实体仓库ID，FK -> wms_warehouse.id（parent_id=0）" />
      </el-form-item>
      <el-form-item label="存储库位ID，FK -> wms_warehouse.id（叶子节点）" prop="locationId">
        <el-input v-model="formData.locationId" placeholder="请输入存储库位ID，FK -> wms_warehouse.id（叶子节点）" />
      </el-form-item>
      <el-form-item label="库存管控方式：0=不管理 1=批次 2=序列号（冗余自product_spu）" prop="stockMode">
        <el-input v-model="formData.stockMode" placeholder="请输入库存管控方式：0=不管理 1=批次 2=序列号（冗余自product_spu）" />
      </el-form-item>
      <el-form-item label="批次号。stock_mode=0时为NULL；stock_mode=1时用户录入；stock_mode=2时存序列号" prop="batchNo">
        <el-input v-model="formData.batchNo" placeholder="请输入批次号。stock_mode=0时为NULL；stock_mode=1时用户录入；stock_mode=2时存序列号" />
      </el-form-item>
      <el-form-item label="生产日期，批次管理时使用" prop="productionDate">
        <el-date-picker
          v-model="formData.productionDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择生产日期，批次管理时使用"
        />
      </el-form-item>
      <el-form-item label="有效期至" prop="expiryDate">
        <el-date-picker
          v-model="formData.expiryDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择有效期至"
        />
      </el-form-item>
      <el-form-item label="库存数量（最小单位）。正数，允许0（仅记录库位-产品关系）" prop="quantity">
        <el-input v-model="formData.quantity" placeholder="请输入库存数量（最小单位）。正数，允许0（仅记录库位-产品关系）" />
      </el-form-item>
      <el-form-item label="最近入库单价（移动平均），仅入库类单据更新" prop="unitPrice">
        <el-input v-model="formData.unitPrice" placeholder="请输入最近入库单价（移动平均），仅入库类单据更新" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { StockApi, Stock } from '@/api/wms/stock'

/** 库存快照 表单 */
defineOptions({ name: 'StockForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  skuId: undefined,
  warehouseId: undefined,
  locationId: undefined,
  stockMode: undefined,
  batchNo: undefined,
  productionDate: undefined,
  expiryDate: undefined,
  quantity: undefined,
  unitPrice: undefined,
})
const formRules = reactive({
  skuId: [{ required: true, message: '产品SKU ID，FK -> product_sku.id不能为空', trigger: 'blur' }],
  warehouseId: [{ required: true, message: '所属实体仓库ID，FK -> wms_warehouse.id（parent_id=0）不能为空', trigger: 'blur' }],
  locationId: [{ required: true, message: '存储库位ID，FK -> wms_warehouse.id（叶子节点）不能为空', trigger: 'blur' }],
  stockMode: [{ required: true, message: '库存管控方式：0=不管理 1=批次 2=序列号（冗余自product_spu）不能为空', trigger: 'blur' }],
  quantity: [{ required: true, message: '库存数量（最小单位）。正数，允许0（仅记录库位-产品关系）不能为空', trigger: 'blur' }],
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await StockApi.getStock(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as Stock
    if (formType.value === 'create') {
      await StockApi.createStock(data)
      message.success(t('common.createSuccess'))
    } else {
      await StockApi.updateStock(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    skuId: undefined,
    warehouseId: undefined,
    locationId: undefined,
    stockMode: undefined,
    batchNo: undefined,
    productionDate: undefined,
    expiryDate: undefined,
    quantity: undefined,
    unitPrice: undefined,
  }
  formRef.value?.resetFields()
}
</script>