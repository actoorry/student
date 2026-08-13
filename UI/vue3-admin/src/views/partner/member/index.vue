<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" class="!w-240px" clearable placeholder="请输入手机号" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" class="!w-240px" clearable placeholder="请输入昵称" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="等级" prop="levelId">
        <el-select v-model="queryParams.levelId" class="!w-240px" clearable placeholder="请选择等级">
          <el-option v-for="item in levelList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="分组" prop="groupId">
        <el-select v-model="queryParams.groupId" class="!w-240px" clearable placeholder="请选择分组">
          <el-option v-for="item in groupList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="注册时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]" class="!w-240px" end-placeholder="结束日期" start-placeholder="开始日期" type="daterange" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="会员编号" prop="id" width="100px" />
      <el-table-column align="center" label="头像" prop="avatar" width="80px">
        <template #default="scope"><img v-if="scope.row.avatar" :src="scope.row.avatar" style="width: 40px" /></template>
      </el-table-column>
      <el-table-column align="center" label="手机号" prop="mobile" width="120px" />
      <el-table-column align="center" label="昵称" prop="nickname" width="100px" />
      <el-table-column align="center" label="等级" prop="levelName" width="100px" />
      <el-table-column align="center" label="分组" prop="groupName" width="100px" />
      <el-table-column align="center" label="积分" prop="point" width="80px" />
      <el-table-column align="center" label="经验" prop="experience" width="80px" />
      <el-table-column align="center" label="标签" prop="tagNames" width="150px">
        <template #default="scope">
          <el-tag v-for="item in scope.row.tagNames" :key="item" class="mr-5px" size="small">{{ item }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="注册时间" prop="createTime" width="180px" />
      <el-table-column :show-overflow-tooltip="false" align="center" fixed="right" label="操作" width="150px">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['partner:member:update']">编辑</el-button>
          <el-button link type="primary" @click="openDetail(scope.row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>
  <MemberForm ref="formRef" @success="getList" />
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as PartnerMemberApi from '@/api/partner/member'
import * as PartnerLevelApi from '@/api/partner/level'
import * as PartnerGroupApi from '@/api/partner/group'
import MemberForm from './MemberForm.vue'

defineOptions({ name: 'Member' })

const loading = ref(true)
const total = ref(0)
const list = ref([])
const levelList = ref([])
const groupList = ref([])
const queryParams = reactive({ pageNo: 1, pageSize: 10, mobile: null, nickname: null, levelId: null, groupId: null, createTime: [] })
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await PartnerMemberApi.getMemberPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value.resetFields(); handleQuery() }
const { push } = useRouter()
const openDetail = (id: number) => { push({ name: 'PartnerMemberDetail', params: { id } }) }
const formRef = ref()
const openForm = (type: string, id?: number) => { formRef.value.open(type, id) }

onMounted(async () => {
  getList()
  levelList.value = await PartnerLevelApi.getMemberLevelSimpleList()
  groupList.value = await PartnerGroupApi.getMemberGroupSimpleList()
})
</script>
