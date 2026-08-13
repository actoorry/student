<template>
  <ContentWrap>
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-lg font-bold">会员详情</span>
          <el-button @click="back"><Icon class="mr-5px" icon="ep:arrow-left" />返回</el-button>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="会员编号">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.mobile }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.nickname }}</el-descriptions-item>
        <el-descriptions-item label="头像">
          <img v-if="detail.avatar" :src="detail.avatar" style="width: 60px" />
        </el-descriptions-item>
        <el-descriptions-item label="真实名字">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          <dict-tag :type="DICT_TYPE.SYSTEM_USER_SEX" :value="detail.sex" />
        </el-descriptions-item>
        <el-descriptions-item label="等级">{{ detail.levelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分组">{{ detail.groupName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ detail.point }}</el-descriptions-item>
        <el-descriptions-item label="经验">{{ detail.experience }}</el-descriptions-item>
        <el-descriptions-item label="标签" :span="2">
          <el-tag v-for="item in detail.tagNames" :key="item" class="mr-5px" size="small">{{ item }}</el-tag>
          <span v-if="!detail.tagNames?.length">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="注册IP">{{ detail.registerIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册终端">{{ detail.registerTerminal }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="最后登录IP">{{ detail.loginIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ detail.loginDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </ContentWrap>
</template>
<script lang="ts" setup>
import * as PartnerMemberApi from '@/api/partner/member'
import { DICT_TYPE } from '@/utils/dict'

defineOptions({ name: 'MemberDetail' })

const route = useRoute()
const { back } = useRouter()
const detail = ref({})

onMounted(async () => {
  const id = route.params.id as unknown as number
  if (id) {
    detail.value = await PartnerMemberApi.getMember(id)
  }
})
</script>
