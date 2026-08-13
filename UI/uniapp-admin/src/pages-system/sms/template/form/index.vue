<template>
  <view class="yd-page-container">
    <!-- 顶部导航栏 -->
    <wd-navbar
      :title="getTitle"
      left-arrow placeholder safe-area-inset-top fixed
      @click-left="handleBack"
    />

    <!-- 表单区域 -->
    <view>
      <wd-form ref="formRef" :model="formData" :schema="formSchema">
        <wd-cell-group border>
          <wd-form-item
            title="短信类型"
            title-width="200rpx"
            prop="type"
            is-link
            :value="getWotPickerFormValue(templateTypeOptions, formData.type)"
            placeholder="请选择短信类型"
            @click="pickerVisible.type = true"
          />
          <wd-picker
            v-model:visible="pickerVisible.type"
            :model-value="formData.type"
            :columns="templateTypeOptions"
            @confirm="({ value }) => formData.type = value[0]"
          />
          <wd-form-item title="模板名称" title-width="200rpx" prop="name">
            <wd-input
              v-model="formData.name"
              clearable
              placeholder="请输入模板名称"
            />
          </wd-form-item>
          <wd-form-item title="模板编码" title-width="200rpx" prop="code">
            <wd-input
              v-model="formData.code"
              clearable
              placeholder="请输入模板编码"
            />
          </wd-form-item>
          <wd-form-item
            title="短信渠道"
            title-width="200rpx"
            prop="channelId"
            is-link
            :value="getWotPickerFormValue(channelOptions, formData.channelId)"
            placeholder="请选择短信渠道"
            @click="pickerVisible.channelId = true"
          />
          <wd-picker
            v-model:visible="pickerVisible.channelId"
            :model-value="formData.channelId"
            :columns="channelOptions"
            @confirm="({ value }) => formData.channelId = value[0]"
          />
          <wd-form-item title="开启状态" title-width="200rpx" prop="status" center>
            <wd-radio-group v-model="formData.status" type="button">
              <wd-radio
                v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                :key="dict.value"
                :value="dict.value"
              >
                {{ dict.label }}
              </wd-radio>
            </wd-radio-group>
          </wd-form-item>
          <wd-form-item title="模板内容" title-width="200rpx" prop="content">
            <wd-textarea
              v-model="formData.content"
              clearable
              placeholder="请输入模板内容"
              :rows="4"
            />
          </wd-form-item>
          <wd-form-item title="API 模板编号" title-width="200rpx" prop="apiTemplateId">
            <wd-input
              v-model="formData.apiTemplateId"
              clearable
              placeholder="请输入短信 API 的模板编号"
            />
          </wd-form-item>
          <wd-form-item title="备注" title-width="200rpx" prop="remark">
            <wd-textarea
              v-model="formData.remark"
              clearable
              placeholder="请输入备注"
            />
          </wd-form-item>
        </wd-cell-group>
      </wd-form>
    </view>

    <!-- 底部保存按钮 -->
    <view class="yd-detail-footer">
      <wd-button
        type="primary"
        block
        :loading="formLoading"
        @click="handleSubmit"
      >
        保存
      </wd-button>
    </view>
  </view>
</template>

<script lang="ts" setup>
import type { FormInstance } from '@wot-ui/ui/components/wd-form/types'
import type { SmsChannel } from '@/api/system/sms/channel'
import type { SmsTemplate } from '@/api/system/sms/template'
import { useToast } from '@wot-ui/ui/components/wd-toast'
import { computed, onMounted, ref } from 'vue'
import { getSimpleSmsChannelList } from '@/api/system/sms/channel'
import { createSmsTemplate, getSmsTemplate, updateSmsTemplate } from '@/api/system/sms/template'
import { getIntDictOptions } from '@/hooks/useDict'
import { navigateBackPlus } from '@/utils'
import { CommonStatusEnum, DICT_TYPE } from '@/utils/constants'
import { createFormSchema, getWotPickerFormValue } from '@/utils/wot'

const props = defineProps<{
  id?: number | any
}>()

definePage({
  style: {
    navigationBarTitleText: '',
    navigationStyle: 'custom',
  },
})

const toast = useToast()
const getTitle = computed(() => props.id ? '编辑短信模板' : '新增短信模板')
const formLoading = ref(false) // 表单提交状态
const formData = ref<SmsTemplate>({
  id: undefined,
  type: undefined,
  name: '',
  code: '',
  channelId: undefined,
  status: CommonStatusEnum.ENABLE,
  content: '',
  apiTemplateId: '',
  remark: '',
}) // 表单数据
const formSchema = createFormSchema({
  type: [{ required: true, message: '短信类型不能为空' }],
  name: [{ required: true, message: '模板名称不能为空' }],
  code: [{ required: true, message: '模板编码不能为空' }],
  channelId: [{ required: true, message: '短信渠道不能为空' }],
  status: [{ required: true, message: '开启状态不能为空' }],
  content: [{ required: true, message: '模板内容不能为空' }],
  apiTemplateId: [{ required: true, message: 'API 模板编号不能为空' }],
})
const formRef = ref<FormInstance>() // 表单组件引用
const pickerVisible = ref<Record<string, boolean>>({})

/** 短信渠道列表 */
const channelList = ref<SmsChannel[]>([])

/** 短信类型选项 */
const templateTypeOptions = computed(() => {
  return getIntDictOptions(DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE).map(item => ({
    value: item.value,
    label: item.label,
  }))
})

/** 短信渠道选项 */
const channelOptions = computed(() => {
  return channelList.value.map(item => ({
    value: item.id,
    label: item.signature,
  }))
})

/** 返回上一页 */
function handleBack() {
  navigateBackPlus('/pages-system/sms/index')
}

/** 加载短信渠道列表 */
async function loadChannelList() {
  channelList.value = await getSimpleSmsChannelList()
}

/** 加载详情 */
async function getDetail() {
  if (!props.id) {
    return
  }
  formData.value = await getSmsTemplate(props.id)
}

/** 提交表单 */
async function handleSubmit() {
  const { valid } = await formRef.value.validate()
  if (!valid) {
    return
  }

  formLoading.value = true
  try {
    if (props.id) {
      await updateSmsTemplate(formData.value)
      toast.success('修改成功')
    } else {
      await createSmsTemplate(formData.value)
      toast.success('新增成功')
    }
    setTimeout(() => {
      handleBack()
    }, 500)
  } finally {
    formLoading.value = false
  }
}

/** 初始化 */
onMounted(async () => {
  await loadChannelList()
  await getDetail()
})
</script>

<style lang="scss" scoped>
</style>
