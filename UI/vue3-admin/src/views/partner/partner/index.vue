<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          class="!w-240px"
          clearable
          placeholder="请输入手机号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          class="!w-240px"
          clearable
          placeholder="请输入昵称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="注册时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
          end-placeholder="结束日期"
          start-placeholder="开始日期"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="是否客户" prop="customer">
        <el-select v-model="queryParams.customer" class="!w-240px" clearable placeholder="请选择">
          <el-option label="是" :value="true" />
          <el-option label="否" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否供应商" prop="supplier">
        <el-select v-model="queryParams.supplier" class="!w-240px" clearable placeholder="请选择">
          <el-option label="是" :value="true" />
          <el-option label="否" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否公司" prop="company">
        <el-select v-model="queryParams.company" class="!w-240px" clearable placeholder="请选择">
          <el-option label="是" :value="true" />
          <el-option label="否" :value="false" />
        </el-select>
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
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['partner:user:create']">
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="list"
      :show-overflow-tooltip="true"
      :stripe="true"
    >
      <el-table-column align="center" label="用户编号" prop="id" width="100px" />
      <el-table-column align="center" label="头像" prop="avatar" width="80px">
        <template #default="scope">
          <img v-if="scope.row.avatar" :src="scope.row.avatar" style="width: 40px" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="手机号" prop="mobile" width="120px" />
      <el-table-column align="center" label="昵称" prop="nickname" width="100px" />
      <el-table-column align="center" label="性别" prop="sex" width="80px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_USER_SEX" :value="scope.row.sex" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="邮箱" prop="email" width="160px" />
      <el-table-column align="center" label="客户" prop="isCustomer" width="80px">
        <template #default="scope">
          <el-tag v-if="scope.row.isCustomer" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="供应商" prop="isSupplier" width="80px">
        <template #default="scope">
          <el-tag v-if="scope.row.isSupplier" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="公司" prop="isCompany" width="80px">
        <template #default="scope">
          <el-tag v-if="scope.row.isCompany" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status" width="80px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="注册时间"
        prop="createTime"
        width="180px"
      />
      <el-table-column
        :show-overflow-tooltip="false"
        align="center"
        fixed="right"
        label="操作"
        width="150px"
      >
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)">
            编辑
          </el-button>
          <el-button link type="primary" @click="openDetail(scope.row.id)">
            详情
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['partner:user:delete']">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <UserForm ref="formRef" @success="getList" />
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as PartnerApi from '@/api/partner/partner'
import { DICT_TYPE } from '@/utils/dict'
import { useMessage } from '@/hooks/web/useMessage'
import { useI18n } from '@/hooks/web/useI18n'
import UserForm from './UserForm.vue'

const { t } = useI18n()
const message = useMessage()

defineOptions({ name: 'Partner' })

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  nickname: null,
  mobile: null,
  createTime: [],
  customer: null,
  supplier: null,
  company: null
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PartnerApi.getPartnerPage(queryParams)
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
  handleQuery()
}

/** 打开详情 */
const { push } = useRouter()
const openDetail = (id: number) => {
  push({ name: 'PartnerDetail', params: { id } })
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除操作 */
const handleDelete = async (id: number) => {
  await message.delConfirm()
  await PartnerApi.deletePartner(id)
  message.success(t('common.delSuccess'))
  getList()
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
