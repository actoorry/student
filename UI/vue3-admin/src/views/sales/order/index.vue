<template>
  <doc-alert title="【交易】交易订单" url="https://doc.appap.vip/mall/trade-order/" />
  <doc-alert title="【交易】购物车" url="https://doc.appap.vip/mall/trade-cart/" />

  <!-- 搜索 -->
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TRADE_ORDER_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支付方式" prop="payChannelCode">
        <el-select
          v-model="queryParams.payChannelCode"
          class="!w-280px"
          clearable
          placeholder="全部"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.PAY_CHANNEL_CODE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-280px"
          end-placeholder="自定义时间"
          start-placeholder="自定义时间"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="订单来源" prop="terminal">
        <el-select v-model="queryParams.terminal" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TERMINAL)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="订单类型" prop="type">
        <el-select v-model="queryParams.type" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TRADE_ORDER_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="配送方式" prop="deliveryType">
        <el-select v-model="queryParams.deliveryType" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TRADE_DELIVERY_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="queryParams.deliveryType === DeliveryTypeEnum.EXPRESS.type"
        label="快递公司"
        prop="logisticsId"
      >
        <el-select v-model="queryParams.logisticsId" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="item in deliveryExpressList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="queryParams.deliveryType === DeliveryTypeEnum.PICK_UP.type"
        label="自提门店"
        prop="pickUpStoreId"
      >
        <el-select
          v-model="queryParams.pickUpStoreId"
          class="!w-280px"
          clearable
          multiple
          placeholder="全部"
        >
          <el-option
            v-for="item in pickUpStoreList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="queryParams.deliveryType === DeliveryTypeEnum.PICK_UP.type"
        label="核销码"
        prop="pickUpVerifyCode"
      >
        <el-input
          v-model="queryParams.pickUpVerifyCode"
          class="!w-280px"
          clearable
          placeholder="请输入自提核销码"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="聚合搜索">
        <el-input
          v-show="true"
          v-model="queryParams[queryType.queryParam]"
          :type="queryType.queryParam === 'userId' ? 'number' : 'text'"
          class="!w-280px"
          clearable
          placeholder="请输入"
        >
          <template #prepend>
            <el-select
              v-model="queryType.queryParam"
              class="!w-110px"
              clearable
              placeholder="全部"
              @change="inputChangeSelect"
            >
              <el-option
                v-for="dict in dynamicSearchList"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <!-- 添加 row-key="id" 解决列数据中的 table#header 数据不刷新的问题  -->
    <el-table v-loading="loading" :data="list" row-key="id">
      <OrderTableColumn :list="list" :pick-up-store-list="pickUpStoreList">
        <template #default="{ row }">
          <div class="flex items-center justify-center">
            <el-button
              v-hasPermi="['trade:order:query']"
              link
              type="primary"
              @click="openDetail(row.id)"
            >
              <Icon icon="ep:notification" />
              详情
            </el-button>
            <!-- 电子面单操作：仅对有效电子面单展示 -->
            <el-button
              v-if="row.waybill && row.waybill.status === 1 && row.waybill.labelUrl"
              v-hasPermi="['sales:sales_electronic_waybill:reprint']"
              link
              type="primary"
              @click="openWaybillPrint(row)"
            >
              <Icon icon="ep:printer" />
              面单打印
            </el-button>
            <el-button
              v-if="row.waybill && row.waybill.status === 1"
              v-hasPermi="['sales:sales_electronic_waybill:reprint']"
              link
              type="primary"
              @click="handleWaybillReprint(row)"
            >
              <Icon icon="ep:refresh-right" />
              复打
            </el-button>
            <el-button
              v-if="row.waybill && row.waybill.status === 1"
              v-hasPermi="['sales:sales_electronic_waybill:cancel']"
              link
              type="danger"
              @click="handleWaybillCancel(row)"
            >
              <Icon icon="ep:close" />
              取消面单
            </el-button>
            <el-dropdown
              v-hasPermi="['trade:order:update']"
              @command="(command) => handleCommand(command, row)"
            >
              <el-button link type="primary">
                <Icon icon="ep:d-arrow-right" />
                更多
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <!-- 如果是【快递】，并且【未发货】，则展示【发货】按钮 -->
                  <el-dropdown-item
                    v-if="
                      row.deliveryType === DeliveryTypeEnum.EXPRESS.type &&
                      row.status === TradeOrderStatusEnum.UNDELIVERED.status
                    "
                    command="delivery"
                  >
                    <Icon icon="ep:takeaway-box" />
                    发货
                  </el-dropdown-item>
                  <el-dropdown-item command="remark">
                    <Icon icon="ep:chat-line-square" />
                    备注
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </OrderTableColumn>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 各种操作的弹窗 -->
  <OrderDeliveryForm ref="deliveryFormRef" @success="getList" />
  <OrderUpdateRemarkForm ref="updateRemarkForm" @success="getList" />
</template>

<script lang="ts" setup>
import type { FormInstance } from 'element-plus'
import OrderDeliveryForm from '@/views/sales/order/form/OrderDeliveryForm.vue'
import OrderUpdateRemarkForm from '@/views/sales/order/form/OrderUpdateRemarkForm.vue'
import * as TradeOrderApi from '@/api/sales/order'
import * as PickUpStoreApi from '@/api/sales/delivery/pickUpStore'
import { DICT_TYPE, getIntDictOptions, getStrDictOptions } from '@/utils/dict'
import * as DeliveryExpressApi from '@/api/sales/delivery/express'
import { DeliveryTypeEnum, TradeOrderStatusEnum } from '@/utils/constants'
import { OrderTableColumn } from './components'

defineOptions({ name: 'TradeOrder' })

const { currentRoute, push } = useRouter() // 路由跳转
const loading = ref(true) // 列表的加载中
const total = ref(2) // 列表的总页数
const list = ref<TradeOrderApi.OrderVO[]>([]) // 列表的数据
const queryFormRef = ref<FormInstance>() // 搜索的表单
// 表单搜索
const queryParams = ref({
  pageNo: 1, // 页数
  pageSize: 10, // 每页显示数量
  status: undefined, // 订单状态
  payChannelCode: undefined, // 支付方式
  createTime: undefined, // 创建时间
  terminal: undefined, // 订单来源
  type: undefined, // 订单类型
  deliveryType: undefined, // 配送方式
  logisticsId: undefined, // 快递公司
  pickUpStoreId: undefined, // 自提门店
  pickUpVerifyCode: undefined // 自提核销码
})
const queryType = reactive({ queryParam: '' }) // 订单搜索类型 queryParam

const message = useMessage() // 消息弹窗

// 订单聚合搜索 select 类型配置（动态搜索）
const dynamicSearchList = ref([
  { value: 'no', label: '订单号' },
  { value: 'userId', label: '用户UID' },
  { value: 'userNickname', label: '用户昵称' },
  { value: 'userMobile', label: '用户电话' }
])
/**
 * 聚合搜索切换查询对象时触发
 * @param val
 */
const inputChangeSelect = (val: string) => {
  dynamicSearchList.value
    .filter((item) => item.value !== val)
    ?.forEach((item1) => {
      // 清除集合搜索无用属性
      if (queryParams.value.hasOwnProperty(item1.value)) {
        delete queryParams.value[item1.value]
      }
    })
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await TradeOrderApi.getOrderPage(unref(queryParams))
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = async () => {
  queryParams.value.pageNo = 1
  await getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.value = {
    pageNo: 1, // 页数
    pageSize: 10, // 每页显示数量
    status: undefined, // 订单状态
    payChannelCode: undefined, // 支付方式
    createTime: undefined, // 创建时间
    terminal: undefined, // 订单来源
    type: undefined, // 订单类型
    deliveryType: undefined, // 配送方式
    logisticsId: undefined, // 快递公司
    pickUpStoreId: undefined, // 自提门店
    pickUpVerifyCode: undefined // 自提核销码
  }
  handleQuery()
}

/** 查看订单详情 */
const openDetail = (id: number) => {
  push({ name: 'TradeOrderDetail', params: { id } })
}

/** 操作分发 */
const deliveryFormRef = ref()
const updateRemarkForm = ref()
const handleCommand = (command: string, row: TradeOrderApi.OrderVO) => {
  switch (command) {
    case 'remark':
      updateRemarkForm.value?.open(row)
      break
    case 'delivery':
      deliveryFormRef.value?.open(row)
      break
  }
}

/** 面单打印：浏览器本地打印 HTML/图片面单 */
const openWaybillPrint = (row: TradeOrderApi.OrderVO) => {
  if (row.waybill?.labelUrl) {
    window.open(row.waybill.labelUrl, '_blank')
  }
}

/** 电子面单复打：不创建新单号，返回可打印面单并打开 */
const handleWaybillReprint = async (row: TradeOrderApi.OrderVO) => {
  if (!row.id) {
    return
  }
  try {
    const waybill = await TradeOrderApi.reprintElectronicWaybill(row.id)
    if (waybill?.labelUrl) {
      window.open(waybill.labelUrl, '_blank')
    }
    message.success('复打成功')
    await getList()
  } catch {}
}

/** 电子面单取消：取消成功后订单回到待发货 */
const handleWaybillCancel = async (row: TradeOrderApi.OrderVO) => {
  if (!row.id) {
    return
  }
  try {
    await message.confirm('确认取消该订单的电子面单？取消成功后订单将回到待发货状态，可重新发货')
    await TradeOrderApi.cancelElectronicWaybill({ id: row.id, reason: '管理员取消' })
    message.success('取消成功')
    await getList()
  } catch {}
}

// 监听路由变化更新列表，解决订单保存/更新后，列表不刷新的问题。
watch(
  () => currentRoute.value,
  () => {
    getList()
  }
)

const pickUpStoreList = ref<PickUpStoreApi.DeliveryPickUpStoreVO[]>([]) // 自提门店精简列表
const deliveryExpressList = ref<DeliveryExpressApi.DeliveryExpressVO[]>([]) // 物流公司
/** 初始化 **/
onMounted(async () => {
  await getList()
  pickUpStoreList.value = await PickUpStoreApi.getSimpleDeliveryPickUpStoreList()
  deliveryExpressList.value = await DeliveryExpressApi.getSimpleDeliveryExpressList()
})
</script>
