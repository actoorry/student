<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="98px"
    >
      <el-form-item label="合作伙伴名称" prop="partnerName">
        <el-input
          v-model="queryParams.partnerName"
          class="!w-240px"
          clearable
          placeholder="请输入名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="partnerMobile">
        <el-input
          v-model="queryParams.partnerMobile"
          class="!w-240px"
          clearable
          placeholder="请输入手机号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="认证类型" prop="certType">
        <el-select v-model="queryParams.certType" class="!w-240px" clearable placeholder="请选择">
          <el-option label="实名认证" value="REAL_NAME" />
          <el-option label="婚姻认证" value="REAL_MARRIAGE" />
        </el-select>
      </el-form-item>
      <el-form-item label="认证状态" prop="state">
        <el-select v-model="queryParams.state" class="!w-240px" clearable placeholder="请选择">
          <el-option label="通过/已婚" value="1" />
          <el-option label="不通过/未婚" value="2" />
          <el-option label="异常/离婚" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
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
    <el-table
      v-loading="loading"
      :data="list"
      :show-overflow-tooltip="true"
      :stripe="true"
    >
      <el-table-column align="center" label="ID" prop="id" width="80px" />
      <el-table-column align="center" label="合作伙伴名称" prop="partnerName" width="140px" />
      <el-table-column align="center" label="手机号" prop="partnerMobile" width="120px" />
      <el-table-column align="center" label="认证类型" prop="certType" width="100px">
        <template #default="scope">
          <dict-tag :type="'partner_cert_type'" :value="scope.row.certType" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="认证状态" prop="stateLabel" width="100px">
        <template #default="scope">
          <el-tag v-if="scope.row.state === '1'" type="success">{{ scope.row.stateLabel }}</el-tag>
          <el-tag v-else-if="scope.row.state === '2'" type="danger">{{ scope.row.stateLabel }}</el-tag>
          <el-tag v-else type="warning">{{ scope.row.stateLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="供应商编码" prop="providerCode" width="120px" />
      <el-table-column align="center" label="供应商流水号" prop="providerSeqNo" min-width="180px" />
      <el-table-column align="center" label="返回码" prop="code" width="100px" />
      <el-table-column align="center" label="返回消息" prop="message" width="120px" />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="创建时间"
        prop="createTime"
        width="180px"
      />
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as CertificationApi from '@/api/partner/certification'

defineOptions({ name: 'CertificationRealname' })

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  partnerName: null,
  partnerMobile: null,
  certType: null,
  state: null,
  createTime: []
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CertificationApi.getCertificationRecordPage(queryParams)
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

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
