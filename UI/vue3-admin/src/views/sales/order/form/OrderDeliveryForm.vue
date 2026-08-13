<template>
  <Dialog v-model="dialogVisible" title="订单发货" width="35%">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="发货方式">
        <el-radio-group v-model="expressType" @change="handleExpressTypeChange">
          <el-radio border value="express">快递物流</el-radio>
          <el-radio border value="none">无需发货</el-radio>
        </el-radio-group>
      </el-form-item>
      <template v-if="expressType === 'express'">
        <el-form-item label="发货模式">
          <el-radio-group v-model="waybillMode" @change="handleWaybillModeChange">
            <el-radio border value="manual">手工单号</el-radio>
            <el-radio border value="waybill" v-hasPermi="['sales:sales_electronic_waybill:create']">
              电子面单
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 手工单号模式 -->
        <template v-if="waybillMode === 'manual'">
          <el-form-item label="物流公司" prop="logisticsId">
            <el-select v-model="formData.logisticsId" placeholder="请选择" style="width: 100%">
              <el-option v-for="item in deliveryExpressList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="物流单号" prop="logisticsNo">
            <el-input v-model="formData.logisticsNo" placeholder="请输入物流单号" />
          </el-form-item>
        </template>
        <!-- 电子面单模式 -->
        <template v-else>
          <el-form-item label="快递公司" prop="waybillExpressId">
            <el-select v-model="waybillExpressId" placeholder="请选择快递公司" style="width: 100%" @change="handleExpressChange">
              <el-option v-for="item in deliveryExpressList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="电子面单账户" prop="accountId">
            <el-select
              v-model="formData.accountId"
              placeholder="请选择电子面单账户"
              style="width: 100%"
              :disabled="!waybillExpressId"
              @change="handleAccountChange"
            >
              <el-option v-for="item in waybillAccountList" :key="item.id!" :label="item.name" :value="item.id!" />
            </el-select>
          </el-form-item>
          <el-form-item label="寄件地址" prop="addressId">
            <el-select
              v-model="formData.addressId"
              placeholder="请选择寄件地址"
              style="width: 100%"
              :disabled="senderAddressList.length === 0"
            >
              <el-option
                v-for="item in senderAddressList"
                :key="item.id"
                :label="`${item.name} ${item.areaName || ''} ${item.detailAddress}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="打印类型" prop="printType">
            <el-select v-model="formData.printType" placeholder="请选择打印类型" style="width: 100%">
              <el-option label="HTML 面单" value="HTML" />
              <el-option label="图片面单" value="IMAGE" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="waybillExpressId && waybillAccountList.length === 0">
            <span style="font-size: 12px; color: #e6a23c">该快递公司暂无启用的电子面单账户，请先在「电子面单账户」中配置</span>
          </el-form-item>
        </template>
      </template>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as DeliveryExpressApi from '@/api/sales/delivery/express'
import * as WaybillAccountApi from '@/api/sales/delivery/waybillAccount'
import * as TradeOrderApi from '@/api/sales/order'
import { copyValueToTarget } from '@/utils'

defineOptions({ name: 'OrderDeliveryForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const expressType = ref('express') // 如果值是 express，则是快递；none 则是无
const waybillMode = ref('manual') // manual - 手工单号；waybill - 电子面单
const waybillExpressId = ref<number | undefined>() // 电子面单模式下的快递公司（仅用于过滤账户，不随请求提交）
const formData = ref<any>({
  id: undefined, // 订单编号
  logisticsId: null, // 物流公司编号（手工模式）
  logisticsNo: '', // 物流编号（手工模式）
  accountId: undefined, // 电子面单账户编号
  addressId: undefined, // 寄件地址编号
  printType: 'HTML' // 打印类型
})
const formRef = ref() // 表单 Ref
const waybillAccountList = ref<WaybillAccountApi.WaybillAccountVO[]>([]) // 电子面单账户列表
const senderAddressList = ref<WaybillAccountApi.SenderAddressVO[]>([]) // 商户寄件地址列表

const formRules = reactive({
  logisticsId: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
  logisticsNo: [
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (expressType.value === 'express' && waybillMode.value === 'manual' && (value == null || value.trim() === '')) {
          callback(new Error('请填写物流单号'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  waybillExpressId: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  accountId: [{ required: true, message: '请选择电子面单账户', trigger: 'change' }],
  addressId: [{ required: true, message: '请选择寄件地址', trigger: 'change' }],
  printType: [{ required: true, message: '请选择打印类型', trigger: 'change' }]
})

/** 切换发货方式时清理不适用字段 */
const handleExpressTypeChange = (type: string) => {
  if (type === 'none') {
    // 无需发货：清空快递分支字段，提交时以 0 + 空串为准
    formData.value.logisticsId = null
    formData.value.logisticsNo = ''
    formData.value.accountId = undefined
    formData.value.addressId = undefined
  }
  formRef.value?.clearValidate()
}

/** 切换发货模式时清理字段与校验 */
const handleWaybillModeChange = async () => {
  formData.value.logisticsId = null
  formData.value.logisticsNo = ''
  formData.value.accountId = undefined
  formData.value.addressId = undefined
  waybillExpressId.value = undefined
  waybillAccountList.value = []
  if (waybillMode.value === 'waybill' && senderAddressList.value.length === 0) {
    // 懒加载寄件地址，避免手工模式用户触发 403
    try {
      senderAddressList.value = await WaybillAccountApi.getSenderAddressList()
    } catch {
      senderAddressList.value = []
    }
  }
  formRef.value?.clearValidate()
}

/** 选择快递公司后加载匹配的电子面单账户 */
const handleExpressChange = async (expressId: number) => {
  waybillAccountList.value = await WaybillAccountApi.getWaybillAccountListByExpress(expressId)
  formData.value.accountId = undefined
  formData.value.addressId = undefined
  formRef.value?.clearValidate()
}

/** 选择账户后默认选中其默认寄件地址 */
const handleAccountChange = (accountId: number) => {
  const account = waybillAccountList.value.find((item) => item.id === accountId)
  if (account?.defaultAddressId) {
    formData.value.addressId = account.defaultAddressId
  } else {
    formData.value.addressId = undefined
  }
  formRef.value?.clearValidate()
}

/** 打开弹窗 */
const open = async (row: TradeOrderApi.OrderVO) => {
  resetForm()
  // 设置数据
  copyValueToTarget(formData.value, row)
  if (row.logisticsId === 0) {
    expressType.value = 'none'
  }
  dialogVisible.value = true
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  if (expressType.value === 'express' && (waybillMode.value === 'manual' || waybillMode.value === 'waybill')) {
    // 快递分支必须先通过必填校验
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
      return
    }
  }
  // 提交请求
  formLoading.value = true
  try {
    const data = unref(formData)
    if (expressType.value === 'none') {
      // 无需发货的情况
      data.logisticsId = 0
      data.logisticsNo = ''
      await TradeOrderApi.deliveryOrder(data)
    } else if (waybillMode.value === 'manual') {
      // 手工单号发货：仅提交快递字段，不携带电子面单字段
      await TradeOrderApi.deliveryOrder({
        id: data.id,
        logisticsId: data.logisticsId,
        logisticsNo: data.logisticsNo
      })
    } else {
      // 电子面单发货
      await TradeOrderApi.deliveryOrderByWaybill({
        id: data.id,
        accountId: data.accountId,
        addressId: data.addressId,
        printType: data.printType
      })
    }
    message.success(t('common.updateSuccess'))
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success', true)
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined, // 订单编号
    logisticsId: null, // 物流公司编号
    logisticsNo: '', // 物流编号
    accountId: undefined,
    addressId: undefined,
    printType: 'HTML'
  }
  waybillMode.value = 'manual'
  waybillExpressId.value = undefined
  waybillAccountList.value = []
  formRef.value?.resetFields()
}
const deliveryExpressList = ref<DeliveryExpressApi.DeliveryExpressVO[]>([])
onMounted(async () => {
  deliveryExpressList.value = await DeliveryExpressApi.getSimpleDeliveryExpressList()
})
</script>
