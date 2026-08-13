<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="客商" prop="partnerId">
        <el-input
          v-model="selectedPartnerName"
          placeholder="请选择客商"
          readonly
          clearable
          class="!w-240px"
          @clear="clearPartner"
        >
          <template #append>
            <el-button @click="openPartnerSelect">选择</el-button>
          </template>
        </el-input>
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
        <el-button type="primary" @click="openCreateAccount">
          <Icon icon="ep:plus" class="mr-5px" /> 新建账户
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="客商编号" align="center" prop="partnerId" />
      <el-table-column label="余额" align="center" prop="balance">
        <template #default="{ row }"> {{ fenToYuan(row.balance) }} 元</template>
      </el-table-column>
      <el-table-column label="累计支出" align="center" prop="totalExpense">
        <template #default="{ row }"> {{ fenToYuan(row.totalExpense) }} 元</template>
      </el-table-column>
      <el-table-column label="累计充值" align="center" prop="totalRecharge">
        <template #default="{ row }"> {{ fenToYuan(row.totalRecharge) }} 元</template>
      </el-table-column>
      <el-table-column label="冻结金额" align="center" prop="freezePrice">
        <template #default="{ row }"> {{ fenToYuan(row.freezePrice) }} 元</template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="openForm(scope.row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 弹窗 -->
  <AccountForm ref="formRef" />
  <PartnerSelectDialog ref="partnerSelectDialogRef" :multiple="false" @selected="handlePartnerSelected" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { fenToYuan } from '@/utils'
import * as WalletApi from '@/api/accountant/account'
import * as PartnerApi from '@/api/partner/partner'
import AccountForm from './AccountForm.vue'
import PartnerSelectDialog from '@/views/partner/partner/components/PartnerSelectDialog.vue'

defineOptions({ name: 'WalletBalance' })

const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  partnerId: null as number | null,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const selectedPartnerName = ref('')

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await WalletApi.getAccountPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  selectedPartnerName.value = ''
  queryParams.partnerId = null
  handleQuery()
}

/** 选择客商 */
const partnerSelectDialogRef = ref()
const isCreateAccount = ref(false)
const openPartnerSelect = () => {
  isCreateAccount.value = false
  partnerSelectDialogRef.value.open(queryParams.partnerId ? [queryParams.partnerId] : [])
}
const openCreateAccount = () => {
  isCreateAccount.value = true
  partnerSelectDialogRef.value.open(queryParams.partnerId ? [queryParams.partnerId] : [])
}
const handlePartnerSelected = async (rows: PartnerApi.PartnerVO[]) => {
  const partner = rows[0]
  queryParams.partnerId = partner.id
  selectedPartnerName.value = partner.nickname || String(partner.id)
  if (isCreateAccount.value) {
    await WalletApi.getAccount({ partnerId: partner.id })
    await handleQuery()
  }
  isCreateAccount.value = false
}
const clearPartner = () => {
  queryParams.partnerId = null
  selectedPartnerName.value = ''
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (id?: number) => {
  formRef.value.open(id)
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
