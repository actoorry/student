<template>
  <!-- 汇总卡片 -->
  <ContentWrap>
    <div class="flex gap-4 mb-2">
      <el-card
        shadow="hover"
        class="flex-1 cursor-pointer"
        :class="{ '!border-red-500': activeStatus === 'expired' }"
        @click="toggleStatus('expired')"
      >
        <div class="text-center">
          <div class="text-2xl font-bold text-red-500">{{ summary.expiredCount || 0 }}</div>
          <div class="text-sm text-gray-600 mt-1">已过期</div>
        </div>
      </el-card>
      <el-card
        shadow="hover"
        class="flex-1 cursor-pointer"
        :class="{ '!border-orange-500': activeStatus === 'expiring_30' }"
        @click="toggleStatus('expiring_30')"
      >
        <div class="text-center">
          <div class="text-2xl font-bold text-orange-500">{{ summary.expiringCount || 0 }}</div>
          <div class="text-sm text-gray-600 mt-1">30天内到期</div>
        </div>
      </el-card>
      <el-card
        shadow="hover"
        class="flex-1 cursor-pointer"
        :class="{ '!border-amber-500': activeStatus === 'expiring_60' }"
        @click="toggleStatus('expiring_60')"
      >
        <div class="text-center">
          <div class="text-2xl font-bold text-amber-500">{{ summary.expiring60Count || 0 }}</div>
          <div class="text-sm text-gray-600 mt-1">60天内到期</div>
        </div>
      </el-card>
      <el-card
        shadow="hover"
        class="flex-1 cursor-pointer"
        :class="{ '!border-cyan-500': activeStatus === 'expiring_90' }"
        @click="toggleStatus('expiring_90')"
      >
        <div class="text-center">
          <div class="text-2xl font-bold text-cyan-500">{{ summary.expiring90Count || 0 }}</div>
          <div class="text-sm text-gray-600 mt-1">90天内到期</div>
        </div>
      </el-card>
      <el-card
        shadow="hover"
        class="flex-1 cursor-pointer"
        :class="{ '!border-yellow-500': activeStatus === 'assessment_overdue' }"
        @click="toggleStatus('assessment_overdue')"
      >
        <div class="text-center">
          <div class="text-2xl font-bold text-yellow-500">{{ summary.assessmentOverdueCount || 0 }}</div>
          <div class="text-sm text-gray-600 mt-1">考核逾期</div>
        </div>
      </el-card>
      <el-card
        shadow="hover"
        class="flex-1 cursor-pointer"
        :class="{ '!border-blue-500': activeStatus === 'assessment_due_60' }"
        @click="toggleStatus('assessment_due_60')"
      >
        <div class="text-center">
          <div class="text-2xl font-bold text-blue-500">{{ summary.assessmentDue60Count || 0 }}</div>
          <div class="text-sm text-gray-600 mt-1">考核前60天</div>
        </div>
      </el-card>
      <el-card
        shadow="hover"
        class="flex-1 cursor-pointer"
        :class="{ '!border-indigo-500': activeStatus === '' }"
        @click="toggleStatus('')"
      >
        <div class="text-center">
          <div class="text-2xl font-bold text-indigo-500">全部</div>
          <div class="text-sm text-gray-600 mt-1">点击查看全部</div>
        </div>
      </el-card>
    </div>
  </ContentWrap>

  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="78px"
    >
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入姓名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="科室" prop="dept">
        <el-tree-select
          v-model="queryParams.dept"
          :data="deptList"
          :props="defaultProps"
          check-strictly
          node-key="id"
          placeholder="请选择科室"
          clearable
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="证书类型" prop="certificateType">
        <el-select v-model="queryParams.certificateType" placeholder="请选择证书类型" clearable class="!w-200px">
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_CERTIFICATE_TYPE)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="缺证书文件" prop="missingAttachment">
        <el-switch v-model="queryParams.missingAttachment" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['hr:certificate:ledger']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出台账
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="姓名" align="center" prop="name" min-width="90px" />
      <el-table-column label="工号" align="center" prop="employeeNo" min-width="90px" />
      <el-table-column label="科室" align="center" prop="deptName" min-width="100px" />
      <el-table-column label="证书类型" align="center" prop="certificateType" min-width="120px">
        <template #default="scope">
          <DictTag :type="DICT_TYPE.HR_CERTIFICATE_TYPE" :value="scope.row.certificateType" />
        </template>
      </el-table-column>
      <el-table-column label="证书编号" align="center" prop="certificateNo" min-width="120px" />
      <el-table-column label="到期日期" align="center" prop="expireDate" min-width="110px" :formatter="dateFormatter2" />
      <el-table-column label="下次考核日" align="center" prop="nextAssessmentDate" min-width="110px" :formatter="dateFormatter2" />
      <el-table-column label="状态" align="center" prop="status" min-width="100px">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="证书文件" align="center" min-width="90px">
        <template #default="scope">
          <el-button v-if="scope.row.attachment" link type="primary" @click="handlePreview(scope.row.attachment)">查看</el-button>
          <span v-else class="text-red-400">缺失</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="90px" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['hr:certificate:update']">编辑</el-button>
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

  <!-- 表单弹窗（复用证书管理表单） -->
  <CertificateForm ref="formRef" @success="getList" />

  <!-- 附件预览 -->
  <el-image-viewer v-if="previewVisible" :url-list="[previewUrl]" @close="previewVisible = false" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import download from '@/utils/download'
import { dateFormatter2 } from '@/utils/formatTime'
import { CertificateApi, Certificate, CertificateLedgerSummary } from '@/api/hr/certificate'
import * as DeptApi from '@/api/system/dept'
import { defaultProps, handleTree } from '@/utils/tree'
import CertificateForm from './CertificateForm.vue'

/** 证书督查台账 */
defineOptions({ name: 'HrCertificateLedger' })

const message = useMessage()

const loading = ref(true)
const list = ref<Certificate[]>([])
const total = ref(0)
const summary = ref<CertificateLedgerSummary>({
  expiredCount: 0,
  expiringCount: 0,
  expiring60Count: 0,
  expiring90Count: 0,
  assessmentOverdueCount: 0,
  assessmentDue60Count: 0
})
const activeStatus = ref('') // '' 全部风险 / expired / expiring_30 / expiring_60 / expiring_90 / assessment_overdue / assessment_due_60
const deptList = ref<Tree[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  dept: undefined,
  certificateType: undefined,
  status: undefined,
  missingAttachment: false
})
const queryFormRef = ref()
const exportLoading = ref(false)

const statusTagType = (status?: string) => {
  switch (status) {
    case 'expired': return 'danger'
    case 'expiring': return 'warning'
    case 'expiring_60': return 'warning'
    case 'expiring_90': return 'info'
    case 'assessment_overdue': return 'warning'
    case 'assessment_due_60': return 'info'
    default: return 'success'
  }
}
const statusLabel = (status?: string) => {
  switch (status) {
    case 'expired': return '已过期'
    case 'expiring': return '30天内到期'
    case 'expiring_60': return '60天内到期'
    case 'expiring_90': return '90天内到期'
    case 'assessment_overdue': return '考核逾期'
    case 'assessment_due_60': return '考核将到期'
    default: return '有效'
  }
}

const getList = async () => {
  loading.value = true
  try {
    // 卡片联动：把 activeStatus 写入 queryParams.status
    queryParams.status = activeStatus.value || undefined
    const data = await CertificateApi.getCertificateLedgerPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getSummary = async () => {
  summary.value = await CertificateApi.getCertificateLedgerSummary()
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.missingAttachment = false
  activeStatus.value = ''
  handleQuery()
}

// 卡片点击：切换状态筛选，再次点击同卡片则恢复全部风险
const toggleStatus = (status: string) => {
  activeStatus.value = activeStatus.value === status ? '' : status
  queryParams.pageNo = 1
  getList()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    queryParams.status = activeStatus.value || undefined
    const data = await CertificateApi.exportCertificateLedger(queryParams)
    download.excel(data, '证书督查台账.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const previewVisible = ref(false)
const previewUrl = ref('')
const handlePreview = (url: string) => {
  if (!url) return
  const isImage = /\.(png|jpe?g|gif|webp|bmp)$/i.test(url)
  if (isImage) {
    previewUrl.value = url
    previewVisible.value = true
  } else {
    window.open(url, '_blank')
  }
}

onMounted(async () => {
  deptList.value = handleTree(await DeptApi.getSimpleDeptList())
  await getSummary()
  await getList()
})
</script>
