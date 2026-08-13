<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="分组名称" prop="name">
        <el-input v-model="queryParams.name" class="!w-240px" clearable placeholder="请输入分组名称" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['partner:group:create']">
          <Icon class="mr-5px" icon="ep:plus" />新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true">
      <el-table-column align="center" label="分组编号" prop="id" width="100px" />
      <el-table-column align="center" label="分组名称" prop="name" />
      <el-table-column align="center" label="备注" prop="remark" />
      <el-table-column align="center" label="状态" prop="status" width="80px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180px" />
      <el-table-column align="center" fixed="right" label="操作" width="150px">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['partner:group:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['partner:group:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>
  <GroupForm ref="formRef" @success="getList" />
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as PartnerGroupApi from '@/api/partner/group'
import { DICT_TYPE } from '@/utils/dict'
import { useMessage } from '@/hooks/web/useMessage'
import { useI18n } from '@/hooks/web/useI18n'
import GroupForm from './GroupForm.vue'

const { t } = useI18n()
const message = useMessage()
defineOptions({ name: 'MemberGroup' })

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({ pageNo: 1, pageSize: 10, name: null })
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await PartnerGroupApi.getMemberGroupPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value.resetFields(); handleQuery() }
const formRef = ref()
const openForm = (type: string, id?: number) => { formRef.value.open(type, id) }
const handleDelete = async (id: number) => {
  await message.delConfirm()
  await PartnerGroupApi.deleteMemberGroup(id)
  message.success(t('common.delSuccess'))
  getList()
}
onMounted(() => { getList() })
</script>
