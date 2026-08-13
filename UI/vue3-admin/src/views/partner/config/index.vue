<template>
  <ContentWrap>
    <el-card shadow="never">
      <template #header>
        <span class="text-lg font-bold">会员配置</span>
      </template>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="180px" style="max-width: 600px">
        <el-form-item label="积分抵扣开关" prop="pointTradeDeductEnable">
          <el-switch v-model="formData.pointTradeDeductEnable" />
        </el-form-item>
        <el-form-item label="积分抵扣单价(分)" prop="pointTradeDeductUnitPrice">
          <el-input-number v-model="formData.pointTradeDeductUnitPrice" :min="0" placeholder="请输入积分抵扣单价" />
        </el-form-item>
        <el-form-item label="积分抵扣最大值(分)" prop="pointTradeDeductMaxPrice">
          <el-input-number v-model="formData.pointTradeDeductMaxPrice" :min="0" placeholder="请输入积分抵扣最大值" />
        </el-form-item>
        <el-form-item label="1元赠送积分" prop="pointTradeGivePoint">
          <el-input-number v-model="formData.pointTradeGivePoint" :min="0" placeholder="请输入赠送积分数" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </ContentWrap>
</template>
<script lang="ts" setup>
import * as PartnerConfigApi from '@/api/partner/config'
import { useMessage } from '@/hooks/web/useMessage'

const message = useMessage()
const formRef = ref()
const formData = reactive({
  pointTradeDeductEnable: false,
  pointTradeDeductUnitPrice: 0,
  pointTradeDeductMaxPrice: 0,
  pointTradeGivePoint: 0
})
const formRules = reactive({})

onMounted(async () => {
  const data = await PartnerConfigApi.getMemberConfig()
  if (data) {
    Object.assign(formData, data)
  }
})

const submitForm = async () => {
  await formRef.value.validate()
  await PartnerConfigApi.saveMemberConfig(formData)
  message.success('保存成功')
}
</script>
