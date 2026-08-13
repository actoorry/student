<template>
  <PartnerDetailsHeader :partner="partner" :loading="loading">
    <el-button
      v-if="permissionListRef?.validateWrite"
      v-hasPermi="['partner:sales:update']"
      type="primary"
      @click="openForm"
    >
      编辑
    </el-button>
    <el-button v-if="permissionListRef?.validateOwnerUser" type="primary" @click="transfer">
      转移
    </el-button>
    <el-button v-if="permissionListRef?.validateWrite" @click="handleUpdateDealStatus">
      更改成交状态
    </el-button>
    <el-button
      v-if="partner.lockStatus && permissionListRef?.validateOwnerUser"
      @click="handleUnlock"
    >
      解锁
    </el-button>
    <el-button
      v-if="!partner.lockStatus && permissionListRef?.validateOwnerUser"
      @click="handleLock"
    >
      锁定
    </el-button>
    <el-button v-if="!partner.ownerUserId" type="primary" @click="handleReceive"> 领取</el-button>
    <el-button v-if="!partner.ownerUserId" type="primary" @click="handleDistributeForm">
      分配
    </el-button>
    <el-button
      v-if="partner.ownerUserId && permissionListRef?.validateOwnerUser"
      @click="handlePutPool"
    >
      放入公海
    </el-button>
  </PartnerDetailsHeader>
  <el-col>
    <el-tabs>
      <el-tab-pane label="跟进记录">
        <FollowUpList :biz-id="partnerId" :biz-type="BizTypeEnum.CRM_CUSTOMER" />
      </el-tab-pane>
      <el-tab-pane label="基本信息">
        <PartnerDetailsInfo :partner="partner" />
      </el-tab-pane>
      <el-tab-pane label="联系人" lazy>
        <ContactList
          :biz-id="partner.id!"
          :customer-id="partner.id!"
          :biz-type="BizTypeEnum.CRM_CUSTOMER"
        />
      </el-tab-pane>
      <el-tab-pane label="团队成员">
        <PermissionList
          ref="permissionListRef"
          :biz-id="partner.id!"
          :biz-type="BizTypeEnum.CRM_CUSTOMER"
          :show-action="!permissionListRef?.isPool || false"
          @quit-team="close"
        />
      </el-tab-pane>
      <el-tab-pane label="商机" lazy>
        <BusinessList
          :biz-id="partner.id!"
          :customer-id="partner.id!"
          :biz-type="BizTypeEnum.CRM_CUSTOMER"
        />
      </el-tab-pane>
      <el-tab-pane label="合同" lazy>
        <ContractList :biz-id="partner.id!" :biz-type="BizTypeEnum.CRM_CUSTOMER" />
      </el-tab-pane>
      <el-tab-pane label="回款" lazy>
        <ReceivablePlanList :customer-id="partner.id!" @create-receivable="createReceivable" />
        <ReceivableList ref="receivableListRef" :customer-id="partner.id!" />
      </el-tab-pane>
      <el-tab-pane label="操作日志">
        <OperateLogV2 :log-list="logList" />
      </el-tab-pane>
    </el-tabs>
  </el-col>

  <!-- 表单弹窗：添加/修改 -->
  <PartnerForm ref="formRef" @success="getPartner" />
  <PartnerDistributeForm ref="distributeForm" @success="getPartner" />
  <CrmTransferForm ref="transferFormRef" :biz-type="BizTypeEnum.CRM_CUSTOMER" @success="close" />
</template>
<script lang="ts" setup>
import { useTagsViewStore } from '@/store/modules/tagsView'
import * as PartnerApi from '@/api/partner/sales'
import PartnerForm from '@/views/partner/sales/PartnerForm.vue'
import PartnerDetailsInfo from './PartnerDetailsInfo.vue'
import PartnerDetailsHeader from './PartnerDetailsHeader.vue'
import ContactList from '@/views/crm/contact/components/ContactList.vue'
import ContractList from '@/views/crm/contract/components/ContractList.vue'
import BusinessList from '@/views/crm/business/components/BusinessList.vue'
import ReceivableList from '@/views/crm/receivable/components/ReceivableList.vue'
import ReceivablePlanList from '@/views/crm/receivable/plan/components/ReceivablePlanList.vue'
import PermissionList from '@/views/crm/permission/components/PermissionList.vue'
import CrmTransferForm from '@/views/crm/permission/components/TransferForm.vue'
import FollowUpList from '@/views/crm/followup/index.vue'
import { BizTypeEnum } from '@/api/crm/permission'
import type { OperateLogVO } from '@/api/system/operatelog'
import { getOperateLogPage } from '@/api/crm/operateLog'
import PartnerDistributeForm from '@/views/partner/sales/pool/PartnerDistributeForm.vue'

defineOptions({ name: 'PartnerSalesDetail' })

const partnerId = ref(0)
const loading = ref(true)
const message = useMessage()
const { delView } = useTagsViewStore()
const { push, currentRoute } = useRouter()

const permissionListRef = ref<InstanceType<typeof PermissionList>>()

const partner = ref<PartnerApi.PartnerVO>({} as PartnerApi.PartnerVO)
const getPartner = async () => {
  loading.value = true
  try {
    partner.value = await PartnerApi.getPartner(partnerId.value)
    await getOperateLog()
  } finally {
    loading.value = false
  }
}

const formRef = ref<InstanceType<typeof PartnerForm>>()
const openForm = () => {
  formRef.value?.open('update', partnerId.value)
}

const handleUpdateDealStatus = async () => {
  const dealStatus = !partner.value.dealStatus
  try {
    await message.confirm(`确定更新成交状态为【${dealStatus ? '已成交' : '未成交'}】吗？`)
    await PartnerApi.updatePartnerDealStatus(partnerId.value, dealStatus)
    message.success(`更新成交状态成功`)
    await getPartner()
  } catch {}
}

const transferFormRef = ref<InstanceType<typeof CrmTransferForm>>()
const transfer = () => {
  transferFormRef.value?.open(partnerId.value)
}

const handleLock = async () => {
  await message.confirm(`确定锁定客户【${partner.value.name}】 吗？`)
  await PartnerApi.lockPartner(unref(partnerId.value), true)
  message.success(`锁定客户【${partner.value.name}】成功`)
  await getPartner()
}

const handleUnlock = async () => {
  await message.confirm(`确定解锁客户【${partner.value.name}】 吗？`)
  await PartnerApi.lockPartner(unref(partnerId.value), false)
  message.success(`解锁客户【${partner.value.name}】成功`)
  await getPartner()
}

const handleReceive = async () => {
  await message.confirm(`确定领取客户【${partner.value.name}】 吗？`)
  await PartnerApi.receivePartner([unref(partnerId.value)])
  message.success(`领取客户【${partner.value.name}】成功`)
  await getPartner()
}

const distributeForm = ref<InstanceType<typeof PartnerDistributeForm>>()
const handleDistributeForm = async () => {
  distributeForm.value?.open(partnerId.value)
}

const handlePutPool = async () => {
  await message.confirm(`确定将客户【${partner.value.name}】放入公海吗？`)
  await PartnerApi.putPartnerPool(unref(partnerId.value))
  message.success(`客户【${partner.value.name}】放入公海成功`)
  close()
}

const logList = ref<OperateLogVO[]>([])
const getOperateLog = async () => {
  if (!partnerId.value) {
    return
  }
  const data = await getOperateLogPage({
    bizType: BizTypeEnum.CRM_CUSTOMER,
    bizId: partnerId.value
  })
  logList.value = data.list
}

const receivableListRef = ref<InstanceType<typeof ReceivableList>>()
const createReceivable = (planData: any) => {
  receivableListRef.value?.createReceivable(planData)
}

const close = () => {
  delView(unref(currentRoute))
  push({ name: 'PartnerSales' })
}

const { params } = useRoute()
onMounted(() => {
  if (!params.id) {
    message.warning('参数错误，客户不能为空！')
    close()
    return
  }
  partnerId.value = params.id as unknown as number
  getPartner()
})
</script>
