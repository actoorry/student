<template>
  <div>
    <Dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="渠道费率" label-width="180px" prop="feeRate">
          <el-input v-model="formData.feeRate" clearable placeholder="请输入渠道费率">
            <template #append>%</template>
          </el-input>
        </el-form-item>
        <el-form-item label="小程序 APPID" label-width="180px" prop="config.appid">
          <el-input v-model="formData.config.appid" clearable placeholder="请输入小程序 APPID" />
        </el-form-item>
        <el-form-item label="Offer ID" label-width="180px" prop="config.offerId">
          <el-input v-model="formData.config.offerId" clearable placeholder="请输入 Offer ID" />
        </el-form-item>
        <el-form-item label="环境" label-width="180px" prop="config.env">
          <el-radio-group v-model="formData.config.env">
            <el-radio :value="0">现网</el-radio>
            <el-radio :value="1">沙箱</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="AppKey" label-width="180px" prop="config.appKey">
          <el-input
            v-model="formData.config.appKey"
            clearable
            placeholder="请输入与环境匹配的 AppKey"
            show-password
          />
        </el-form-item>
        <el-form-item label="渠道状态" label-width="180px" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio
              v-for="dict in getDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="parseInt(dict.value)"
              :value="parseInt(dict.value)"
            >
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" label-width="180px" prop="remark">
          <el-input v-model="formData.remark" :style="{ width: '100%' }" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </Dialog>
  </div>
</template>
<script lang="ts" setup>
import { CommonStatusEnum } from '@/utils/constants'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as ChannelApi from '@/api/accountant/channel'

defineOptions({ name: 'WechatVirtualChannelForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formData = ref<any>({
  appId: '',
  code: '',
  status: undefined,
  feeRate: undefined,
  remark: '',
  config: {
    appid: '',
    offerId: '',
    env: 0,
    appKey: ''
  }
})
const formRules = {
  feeRate: [{ required: true, message: '请输入渠道费率', trigger: 'blur' }],
  status: [{ required: true, message: '渠道状态不能为空', trigger: 'blur' }],
  'config.appid': [{ required: true, message: '请输入小程序 APPID', trigger: 'blur' }],
  'config.offerId': [{ required: true, message: '请输入 Offer ID', trigger: 'blur' }],
  'config.env': [{ required: true, message: '请选择环境', trigger: 'change' }],
  'config.appKey': [{ required: true, message: '请输入 AppKey', trigger: 'blur' }]
}
const formRef = ref()

/** 打开弹窗 */
const open = async (appId, code) => {
  dialogVisible.value = true
  formLoading.value = true
  resetForm(appId, code)
  try {
    const data = await ChannelApi.getChannel(appId, code)
    if (data && data.id) {
      formData.value = data
      formData.value.config = JSON.parse(data.config)
    }
    dialogTitle.value = !formData.value.id ? '创建支付渠道' : '编辑支付渠道'
  } finally {
    formLoading.value = false
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = { ...formData.value } as unknown as ChannelApi.ChannelVO
    data.config = JSON.stringify(formData.value.config)
    if (!data.id) {
      await ChannelApi.createChannel(data)
      message.success(t('common.createSuccess'))
    } else {
      await ChannelApi.updateChannel(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = (appId, code) => {
  formData.value = {
    appId: appId,
    code: code,
    status: CommonStatusEnum.ENABLE,
    feeRate: 0,
    remark: '',
    config: {
      appid: '',
      offerId: '',
      env: 0,
      appKey: ''
    }
  }
  formRef.value?.resetFields()
}
</script>
