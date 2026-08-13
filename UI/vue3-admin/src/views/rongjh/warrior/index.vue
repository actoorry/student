<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="80px">
      <el-form-item v-if="showTypeFilter" label="人员类别" prop="type">
        <el-select v-model="queryParams.type" class="!w-180px" clearable placeholder="全部">
          <el-option v-for="item in typeFilterOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" class="!w-180px" clearable placeholder="真实姓名" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" class="!w-180px" clearable placeholder="会员手机号" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="省份" prop="stateId">
        <el-input v-model="queryParams.stateId" class="!w-180px" clearable placeholder="区域编号" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-150px" clearable placeholder="全部">
          <el-option v-for="item in WARRIOR_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-tabs v-if="!isMemberPage" v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待审核" name="0" />
      <el-tab-pane label="已通过" name="1" />
      <el-tab-pane label="已驳回" name="2" />
    </el-tabs>

    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="会员编号" prop="id" width="90" />
      <el-table-column v-if="showTypeColumn" align="center" label="人员类别" prop="typeName" min-width="120" />
      <el-table-column align="center" label="昵称" prop="nickname" min-width="100" />
      <el-table-column align="center" label="手机号" prop="mobile" width="120" />
      <el-table-column align="center" label="真实姓名" prop="name" min-width="100" />
      <el-table-column align="center" label="身份证号" prop="idCard" min-width="160" />
      <el-table-column align="center" label="省份" prop="province" min-width="100" />
      <el-table-column align="center" label="城市" prop="city" min-width="100" />
      <el-table-column v-if="showServiceUnitColumn" align="center" label="服役单位" prop="serviceUnit" min-width="120" show-overflow-tooltip />
      <el-table-column align="center" label="状态" prop="status" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="申请时间" prop="createTime" width="170" />
      <el-table-column align="center" label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="['rongjh:warrior:query']" link type="primary" @click="openDetail(row.id)">详情</el-button>
          <el-button
            v-if="row.status === 0"
            v-hasPermi="['rongjh:warrior:audit']"
            link
            type="success"
            @click="openAudit('approve', row.id)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.status === 0"
            v-hasPermi="['rongjh:warrior:audit']"
            link
            type="warning"
            @click="openAudit('reject', row.id)"
          >
            驳回
          </el-button>
          <el-button
            v-if="row.status === 1"
            v-hasPermi="['rongjh:warrior:audit']"
            link
            type="danger"
            @click="openAudit('blacklist', row.id)"
          >
            拉黑
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>

  <WarriorDetail ref="detailRef" />
  <WarriorAuditForm ref="auditRef" @success="getList" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as WarriorApi from '@/api/rongjh/warrior'
import { SANSHU_TYPE_OPTIONS, WARRIOR_STATUS_OPTIONS, WARRIOR_TYPE_OPTIONS } from '@/api/rongjh/warrior'
import WarriorDetail from './WarriorDetail.vue'
import WarriorAuditForm from './WarriorAuditForm.vue'
import { useRoute } from 'vue-router'

defineOptions({ name: 'RongjhWarrior' })

const route = useRoute()
const isAuditSanshuMode = computed(
  () =>
    route.name === 'RongjhIdentityAudit' ||
    (!isMemberPage.value && route.meta?.query?.typeGroup === 'SANSHU')
)
const isMemberPage = computed(
  () => route.name === 'RongjhWarriorMember' || route.name === 'RongjhIdentityMember'
)
/** 成员管理：共用一张表，默认可查看全部身份类型 */
const showTypeColumn = computed(() => isMemberPage.value || isAuditSanshuMode.value)
const showServiceUnitColumn = computed(() => isMemberPage.value || !isAuditSanshuMode.value)
const showTypeFilter = computed(() => isMemberPage.value || isAuditSanshuMode.value)
const typeFilterOptions = computed(() => (isMemberPage.value ? WARRIOR_TYPE_OPTIONS : SANSHU_TYPE_OPTIONS))
const defaultTypeGroup = computed(() => (isAuditSanshuMode.value ? 'SANSHU' : 'SELF'))
const loading = ref(true)
const total = ref(0)
const list = ref<WarriorApi.WarriorVO[]>([])
const activeTab = ref('0')
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  mobile: undefined as string | undefined,
  type: undefined as string | undefined,
  typeGroup: 'SELF' as string | undefined,
  status: 0 as number | undefined,
  stateId: undefined as number | undefined
})
const queryFormRef = ref()
const detailRef = ref()
const auditRef = ref()

const statusTagType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await WarriorApi.getWarriorPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  applyRouteDefaults()
  if (isMemberPage.value) {
    queryParams.status = 1
  } else {
    queryParams.status = Number(activeTab.value)
  }
  handleQuery()
}

const handleTabChange = (name: string | number) => {
  queryParams.status = Number(name)
  queryParams.pageNo = 1
  getList()
}

const openDetail = (id: number) => {
  detailRef.value.open(id)
}

const openAudit = (type: 'approve' | 'reject' | 'blacklist', id: number) => {
  auditRef.value.open(type, id)
}

const applyRouteDefaults = () => {
  queryParams.type = undefined
  queryParams.typeGroup = undefined

  // 成员管理：不按类型分组，默认展示全部已通过成员
  if (isMemberPage.value) {
    queryParams.status = 1
    if (route.meta.query?.status !== undefined) {
      queryParams.status = Number(route.meta.query.status)
    }
    return
  }

  queryParams.typeGroup = defaultTypeGroup.value
  if (route.meta.query) {
    if (route.meta.query.typeGroup) {
      queryParams.typeGroup = String(route.meta.query.typeGroup)
    }
    if (route.meta.query.type) {
      queryParams.type = String(route.meta.query.type)
      queryParams.typeGroup = undefined
    }
    if (route.meta.query.status !== undefined) {
      activeTab.value = String(route.meta.query.status)
      queryParams.status = Number(route.meta.query.status)
    }
  } else if (!isAuditSanshuMode.value) {
    queryParams.type = 'SELF'
    queryParams.typeGroup = undefined
  }
}

onMounted(() => {
  applyRouteDefaults()
  getList()
})
</script>
