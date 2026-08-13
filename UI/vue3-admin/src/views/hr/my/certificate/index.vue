<template>
  <ContentWrap>
    <!-- 搜索工作栏：职工端无姓名搜索，保留状态/类型/到期范围 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="证书类型" prop="certificateType">
        <el-select v-model="queryParams.certificateType" placeholder="请选择证书类型" clearable class="!w-200px">
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_CERTIFICATE_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-180px">
          <el-option label="已过期" value="expired" />
          <el-option label="30天内到期" value="expiring" />
          <el-option label="60天内到期" value="expiring_60" />
          <el-option label="90天内到期" value="expiring_90" />
          <el-option label="考核逾期" value="assessment_overdue" />
          <el-option label="考核将到期" value="assessment_due_60" />
          <el-option label="有效" value="valid" />
        </el-select>
      </el-form-item>
      <el-form-item label="到期日期" prop="expireDate">
        <el-date-picker
          v-model="queryParams.expireDate"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD HH:mm:ss"
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
          v-hasPermi="['hr:my:certificate:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增证书
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="证书类型" align="center" prop="certificateType" min-width="120px">
        <template #default="scope">
          <DictTag :type="DICT_TYPE.HR_CERTIFICATE_TYPE" :value="scope.row.certificateType" />
        </template>
      </el-table-column>
      <el-table-column label="证书名称" align="center" prop="certificateName" min-width="120px" />
      <el-table-column label="证书编号" align="center" prop="certificateNo" min-width="120px" />
      <el-table-column label="发证日期" align="center" prop="issueDate" min-width="110px" :formatter="dateFormatter" />
      <el-table-column label="到期日期" align="center" prop="expireDate" min-width="110px" :formatter="dateFormatter" />
      <el-table-column label="下次考核日" align="center" prop="nextAssessmentDate" min-width="110px" :formatter="dateFormatter" />
      <el-table-column label="状态" align="center" prop="status" min-width="110px">
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
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['hr:my:certificate:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['hr:my:certificate:delete']">删除</el-button>
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
  <HrMyCertificateForm ref="formRef" @success="getList" />

  <!-- 附件预览 -->
  <el-image-viewer v-if="previewVisible" :url-list="[previewUrl]" @close="previewVisible = false" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { MyCertificateApi } from '@/api/hr/my/certificate'
import type { Certificate } from '@/api/hr/certificate'
import HrMyCertificateForm from './HrMyCertificateForm.vue'

/** 职工我的证书 列表 */
defineOptions({ name: 'HrMyCertificate' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<Certificate[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  certificateType: undefined,
  status: undefined,
  expireDate: []
})
const queryFormRef = ref()

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
    const data = await MyCertificateApi.getMyCertificatePage(queryParams)
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
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await MyCertificateApi.deleteMyCertificate(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
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

onMounted(getList)
</script>
