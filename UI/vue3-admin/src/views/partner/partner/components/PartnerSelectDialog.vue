<!--
  客商弹窗选择器（支持单选/多选）

  高复用公共组件，可通过 props 传入初始筛选条件（如 isSupplier: true 只展示供应商）

  Props:
    multiple — true 多选（checkbox），false 单选（radio）；默认 true
    isCustomer — 初始筛选：是否客户
    isSupplier — 初始筛选：是否供应商
    isCompany — 初始筛选：是否公司
  Events:
    selected(rows: PartnerVO[]) — 确认选择后触发，单选时数组长度为 1
  Expose:
    open(selectedIds?: number[]) — 打开弹窗，可传入已选 ID 用于预选高亮
-->
<template>
  <el-dialog
    v-model="dialogVisible"
    title="客商选择"
    width="70%"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    :destroy-on-close="false"
    append-to-body
    draggable
  >
    <ContentWrap>
      <el-form :inline="true" :model="queryParams" label-width="85px">
        <el-form-item label="客商名称">
          <el-input
            v-model="queryParams.nickname"
            placeholder="请输入客商名称"
            clearable
            @keyup.enter="handleQuery"
            class="!w-220px"
          />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input
            v-model="queryParams.mobile"
            placeholder="请输入手机号码"
            clearable
            @keyup.enter="handleQuery"
            class="!w-220px"
          />
        </el-form-item>
        <el-form-item label="客商类型">
          <el-select
            v-model="partnerKind"
            placeholder="请选择客商类型"
            clearable
            class="!w-220px"
          >
            <el-option label="客户" :value="1" />
            <el-option label="供应商" :value="2" />
            <el-option label="公司" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>
    <!-- 数据表格：单选 radio / 多选 checkbox 统一在一个 table 内 -->
    <ContentWrap>
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        row-key="id"
        :highlight-current-row="!multiple"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        @row-dblclick="handleRowDblClick"
      >
        <!-- 多选：checkbox（reserve-selection 保证跨页勾选不丢失） -->
        <el-table-column
          v-if="multiple"
          type="selection"
          :reserve-selection="true"
          width="50"
          align="center"
        />
        <!-- 单选：radio -->
        <el-table-column v-else width="50" align="center">
          <template #default="{ row }">
            <el-radio
              v-model="selectedRadioId"
              :value="row.id"
              class="radio-no-label"
              @change="handleRadioChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="客商名称" align="left" prop="nickname" min-width="150" />
        <el-table-column label="手机号码" align="center" prop="mobile" width="130" />
        <el-table-column label="邮箱" align="center" prop="email" min-width="180" />
        <el-table-column label="客户" align="center" prop="isCustomer" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.isCustomer" type="success" size="small">是</el-tag>
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="供应商" align="center" prop="isSupplier" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.isSupplier" type="success" size="small">是</el-tag>
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="公司" align="center" prop="isCompany" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.isCompany" type="success" size="small">是</el-tag>
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="80">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>
    <template #footer>
      <el-button type="primary" @click="confirmSelect">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import * as PartnerApi from '@/api/partner/partner'

defineOptions({ name: 'PartnerSelectDialog' })

const props = withDefaults(
  defineProps<{
    multiple?: boolean // true 多选（checkbox），false 单选（radio）
    isCustomer?: boolean // 初始筛选：是否客户
    isSupplier?: boolean // 初始筛选：是否供应商
    isCompany?: boolean // 初始筛选：是否公司
  }>(),
  {
    multiple: true,
    isCustomer: undefined,
    isSupplier: undefined,
    isCompany: undefined
  }
)

const message = useMessage()
const emit = defineEmits<{
  selected: [rows: PartnerApi.PartnerVO[]]
}>()

const dialogVisible = ref(false) // 弹窗是否展示
const loading = ref(false) // 列表加载中
const list = ref<PartnerApi.PartnerVO[]>([]) // 客商列表
const total = ref(0) // 总条数

// ==================== 选中状态 ====================
const tableRef = ref() // 表格 Ref
const selectedRows = ref<PartnerApi.PartnerVO[]>([]) // 多选模式：选中行
const selectedRadioId = ref<number>() // 单选模式：选中 ID
const currentRadioRow = ref<PartnerApi.PartnerVO>() // 单选模式：选中行对象
const preSelectedIds = ref<number[]>([]) // 打开弹窗时传入的已选 ID

// ==================== 客商类型筛选 ====================
const partnerKind = ref<number | undefined>(undefined) // 1=客户 2=供应商 3=公司

/** 多选：checkbox 变化 */
const handleSelectionChange = (rows: PartnerApi.PartnerVO[]) => {
  if (props.multiple) {
    selectedRows.value = rows
  }
}

/** 单选：radio 变化 */
const handleRadioChange = (row: PartnerApi.PartnerVO) => {
  currentRadioRow.value = row
}

/** 单击行：单选模式下点击整行即选中 */
const handleRowClick = (row: PartnerApi.PartnerVO) => {
  if (props.multiple) {
    return
  }
  selectedRadioId.value = row.id
  currentRadioRow.value = row
}

/** 双击行：多选模式切换勾选，单选模式直接确认 */
const handleRowDblClick = (row: PartnerApi.PartnerVO) => {
  if (props.multiple) {
    tableRef.value?.toggleRowSelection(row)
    return
  }
  selectedRadioId.value = row.id
  currentRadioRow.value = row
  confirmSelect()
}

// ==================== 客商查询 ====================
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  nickname: undefined as string | undefined,
  mobile: undefined as string | undefined,
  customer: undefined as boolean | undefined,
  supplier: undefined as boolean | undefined,
  company: undefined as boolean | undefined,
  status: CommonStatusEnum.ENABLE as number | undefined
})

/** 根据 partnerKind 设置查询参数 */
const applyPartnerKindFilter = () => {
  queryParams.customer = undefined
  queryParams.supplier = undefined
  queryParams.company = undefined
  if (partnerKind.value === 1) {
    queryParams.customer = true
  } else if (partnerKind.value === 2) {
    queryParams.supplier = true
  } else if (partnerKind.value === 3) {
    queryParams.company = true
  }
}

/** 查询客商列表 */
const getList = async () => {
  loading.value = true
  try {
    applyPartnerKindFilter()
    const data = await PartnerApi.getPartnerPage(queryParams)
    list.value = data.list
    total.value = data.total
    await nextTick()
    applyPreSelection()
  } finally {
    loading.value = false
  }
}

/** 恢复预选状态（当前页可见范围内） */
const applyPreSelection = () => {
  if (preSelectedIds.value.length === 0) {
    return
  }
  if (props.multiple) {
    const table = tableRef.value
    if (!table) {
      return
    }
    list.value.forEach((row) => {
      if (preSelectedIds.value.includes(row.id)) {
        table.toggleRowSelection(row, true)
      }
    })
  } else {
    const match = list.value.find((row) => preSelectedIds.value.includes(row.id))
    if (match) {
      selectedRadioId.value = match.id
      currentRadioRow.value = match
    }
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置查询条件 */
const resetQuery = () => {
  queryParams.nickname = undefined
  queryParams.mobile = undefined
  partnerKind.value = undefined
  queryParams.status = CommonStatusEnum.ENABLE
  handleQuery()
}

/** 确认选择 */
const confirmSelect = () => {
  if (props.multiple) {
    if (selectedRows.value.length === 0) {
      message.warning('请至少选择一条数据')
      return
    }
    emit('selected', selectedRows.value)
  } else {
    if (!currentRadioRow.value) {
      message.warning('请选择一条数据')
      return
    }
    emit('selected', [currentRadioRow.value])
  }
  dialogVisible.value = false
}

// ==================== 打开弹窗 ====================

/** 打开弹窗，可传入已选 ID 用于预选高亮 */
const open = async (selectedIds?: number[]) => {
  dialogVisible.value = true
  // 重置查询条件 + 页码
  queryParams.nickname = undefined
  queryParams.mobile = undefined
  partnerKind.value = undefined
  queryParams.status = CommonStatusEnum.ENABLE
  queryParams.pageNo = 1
  // 应用 props 传入的初始筛选条件
  if (props.isCustomer !== undefined) {
    queryParams.customer = props.isCustomer
    partnerKind.value = 1
  }
  if (props.isSupplier !== undefined) {
    queryParams.supplier = props.isSupplier
    partnerKind.value = 2
  }
  if (props.isCompany !== undefined) {
    queryParams.company = props.isCompany
    partnerKind.value = 3
  }
  // 清空上一次的选中状态
  selectedRows.value = []
  selectedRadioId.value = undefined
  currentRadioRow.value = undefined
  preSelectedIds.value = selectedIds ?? []
  // 多选模式清空跨页缓存的勾选
  await nextTick()
  tableRef.value?.clearSelection()
  await getList()
}
defineExpose({ open })
</script>

<style lang="scss" scoped>
/* 隐藏 radio 的 label 文字，只保留圆圈 */
.radio-no-label {
  :deep(.el-radio__label) {
    display: none;
  }
}
</style>
