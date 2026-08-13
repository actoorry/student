<template>
  <div v-loading="loading">
    <el-row :gutter="10">
      <!-- 基本信息 -->
      <el-col :span="14" class="detail-info-item">
        <UserBasicInfo :user="user">
          <template #header>
            <div class="card-header">
              <CardTitle title="基本信息" />
              <el-button size="small" text type="primary" @click="openForm('update')">
                编辑
              </el-button>
            </div>
          </template>
        </UserBasicInfo>
      </el-col>
    </el-row>
    <!-- 表单弹窗 -->
    <UserForm ref="formRef" @success="fetchData" />
  </div>
</template>
<script setup lang="ts">
import * as PartnerApi from '@/api/partner/partner'
import UserBasicInfo from './UserBasicInfo.vue'
import UserForm from '../UserForm.vue'

defineOptions({ name: 'PartnerDetail' })

const route = useRoute()
const id = route.params.id as string

const loading = ref(false)
const user = ref<PartnerApi.PartnerVO>({} as PartnerApi.PartnerVO)

/** 获取详情数据 */
const fetchData = async () => {
  loading.value = true
  try {
    user.value = await PartnerApi.getPartner(parseInt(id))
  } finally {
    loading.value = false
  }
}

/** 打开编辑表单 */
const formRef = ref()
const openForm = (type: string) => {
  formRef.value.open(type, parseInt(id))
}

/** 初始化 */
onMounted(() => {
  fetchData()
})
</script>
