<template>
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="单位名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入单位名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="单位类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择单位类型" clearable class="!w-240px">
          <el-option
            v-for="dict in unitTypeOptions"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['product:unit:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" row-key="id">
      <el-table-column label="单位名称" min-width="140" prop="name" />
      <el-table-column label="单位类型" min-width="120" prop="type">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.PRODUCT_UNIT_TYPE" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="换算关系" min-width="220">
        <template #default="scope">
          <span>1 {{ scope.row.name }} = {{ scope.row.relativeFactor }} {{ getBaseUnitLabel(scope.row.type) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button
            v-if="isEditableUnit(scope.row)"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['product:unit:update']"
          >
            编辑
          </el-button>
          <el-button
            v-if="isEditableUnit(scope.row)"
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['product:unit:delete']"
          >
            删除
          </el-button>
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

  <UnitForm ref="formRef" @success="handleFormSuccess" />
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as ProductUnitApi from '@/api/product/unit'
import UnitForm from './UnitForm.vue'

defineOptions({ name: 'ProductUnit' })

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const total = ref(0)
const list = ref<ProductUnitApi.UnitVO[]>([])
const queryParams = reactive<ProductUnitApi.UnitPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  status: undefined,
  type: undefined,
  createTime: []
})
const queryFormRef = ref()
const unitTypeOptions = computed(() =>
  getIntDictOptions(DICT_TYPE.PRODUCT_UNIT_TYPE).filter((dict) => dict.value !== 0)
)

const getList = async () => {
  loading.value = true
  try {
    const data = await ProductUnitApi.getUnitPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getBaseUnitLabel = (type?: number) => {
  const typeLabel = unitTypeOptions.value.find((dict) => dict.value === type)?.label
  return typeLabel ? `${typeLabel}基础单位` : '基础单位'
}

const isEditableUnit = (unit: ProductUnitApi.UnitVO) => {
  if (unit.type === 0 || !unit.relativeFactor) {
    return false
  }
  return Number(unit.relativeFactor) !== 1
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleFormSuccess = async () => {
  await getList()
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ProductUnitApi.deleteUnit(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
