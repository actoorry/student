<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
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
      <el-form-item label="证书类型" prop="certificateType">
        <el-select
          v-model="queryParams.certificateType"
          placeholder="请选择证书类型"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        >
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_CERTIFICATE_TYPE)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-200px">
          <el-option label="有效" value="valid" />
          <el-option label="30天内到期" value="expiring" />
          <el-option label="60天内到期" value="expiring_60" />
          <el-option label="90天内到期" value="expiring_90" />
          <el-option label="已过期" value="expired" />
          <el-option label="考核逾期" value="assessment_overdue" />
          <el-option label="考核将到期" value="assessment_due_60" />
        </el-select>
      </el-form-item>
      <el-form-item label="到期日期" prop="expireDate">
        <el-date-picker
          v-model="queryParams.expireDate"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item label="缺证书文件" prop="missingAttachment">
        <el-switch v-model="queryParams.missingAttachment" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['hr:certificate:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['hr:certificate:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['hr:certificate:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
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
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="姓名" align="center" prop="name" min-width="90px" />
      <el-table-column label="工号" align="center" prop="employeeNo" min-width="90px" />
      <el-table-column label="科室" align="center" prop="deptName" min-width="100px" />
      <el-table-column label="证书类型" align="center" prop="certificateType" min-width="120px">
        <template #default="scope">
          <DictTag :type="DICT_TYPE.HR_CERTIFICATE_TYPE" :value="scope.row.certificateType" />
        </template>
      </el-table-column>
      <el-table-column label="证书名称" align="center" prop="certificateName" min-width="120px" />
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
          <span v-else class="text-gray-400">未上传</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="120px" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['hr:certificate:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['hr:certificate:delete']">删除</el-button>
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

  <!-- 表单弹窗 -->
  <CertificateForm ref="formRef" @success="getList" />

  <!-- 附件预览 -->
  <el-image-viewer v-if="previewVisible" :url-list="[previewUrl]" @close="previewVisible = false" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { isEmpty } from '@/utils/is'
import { dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
import { CertificateApi, Certificate } from '@/api/hr/certificate'
import CertificateForm from './CertificateForm.vue'

/** 人员证书 列表 */
defineOptions({ name: 'HrCertificate' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<Certificate[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  certificateType: undefined,
  status: undefined,
  expireDate: [],
  missingAttachment: false,
  createTime: []
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
    const data = await CertificateApi.getCertificatePage(queryParams)
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
  queryFormRef.value?.resetFields()
  queryParams.missingAttachment = false
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await CertificateApi.deleteCertificate(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    await CertificateApi.deleteCertificateList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Certificate[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await CertificateApi.exportCertificate(queryParams)
    download.excel(data, '人员证书.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

// 附件预览：图片用 el-image-viewer，PDF 新窗口打开
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

onMounted(() => {
  getList()
})
</script>
