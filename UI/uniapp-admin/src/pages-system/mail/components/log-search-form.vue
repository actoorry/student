<template>
  <!-- 搜索框入口 -->
  <view @click="visible = true">
    <wd-search :placeholder="placeholder" hide-cancel disabled />
  </view>

  <!-- 搜索弹窗 -->
  <wd-popup
    v-model="visible"
    position="top"
    :custom-style="getTopPopupStyle()"
    :modal-style="getTopPopupModalStyle()"
    @close="visible = false"
  >
    <view class="yd-search-form-container">
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          用户编号
        </view>
        <wd-input
          v-model="formData.userId"
          placeholder="请输入用户编号"
          clearable
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          用户类型
        </view>
        <wd-radio-group v-model="formData.userType" type="button">
          <wd-radio :value="-1">
            全部
          </wd-radio>
          <wd-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.USER_TYPE)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </wd-radio>
        </wd-radio-group>
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          发送状态
        </view>
        <wd-radio-group v-model="formData.sendStatus" type="button">
          <wd-radio :value="-1">
            全部
          </wd-radio>
          <wd-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_MAIL_SEND_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </wd-radio>
        </wd-radio-group>
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          邮箱账号
        </view>
        <view
          class="flex items-center justify-between rounded-12rpx bg-[#f7f8fa] p-24rpx"
          @click="pickerVisible.accountId = true"
        >
          <text class="text-28rpx text-[#333]">
            {{ getWotPickerDisplay(accountOptions, formData.accountId, { placeholder: '请选择邮箱账号' }) }}
          </text>
          <wd-icon name="arrow-down" size="32rpx" color="#666" />
        </view>
        <wd-picker
          v-model:visible="pickerVisible.accountId"
          :model-value="formData.accountId"
          :columns="accountOptions"
          @confirm="({ value }) => formData.accountId = value[0]"
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          模板编号
        </view>
        <wd-input
          v-model="formData.templateId"
          placeholder="请输入模板编号"
          clearable
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          发送时间
        </view>
        <view class="yd-search-form-date-range-container">
          <view class="flex-1" @click="visibleSendTime[0] = true">
            <view class="yd-search-form-date-range-picker">
              {{ formatDate(formData.sendTime?.[0]) || '开始日期' }}
            </view>
          </view>
          -
          <view class="flex-1" @click="visibleSendTime[1] = true">
            <view class="yd-search-form-date-range-picker">
              {{ formatDate(formData.sendTime?.[1]) || '结束日期' }}
            </view>
          </view>
        </view>
        <wd-datetime-picker-view v-if="visibleSendTime[0]" v-model="tempSendTime[0]" type="date" />
        <view v-if="visibleSendTime[0]" class="yd-search-form-date-range-actions">
          <wd-button size="small" variant="plain" @click="visibleSendTime[0] = false">
            取消
          </wd-button>
          <wd-button size="small" type="primary" @click="handleSendTime0Confirm">
            确定
          </wd-button>
        </view>
        <wd-datetime-picker-view v-if="visibleSendTime[1]" v-model="tempSendTime[1]" type="date" />
        <view v-if="visibleSendTime[1]" class="yd-search-form-date-range-actions">
          <wd-button size="small" variant="plain" @click="visibleSendTime[1] = false">
            取消
          </wd-button>
          <wd-button size="small" type="primary" @click="handleSendTime1Confirm">
            确定
          </wd-button>
        </view>
      </view>
      <view class="yd-search-form-actions">
        <wd-button class="flex-1" variant="plain" @click="handleReset">
          重置
        </wd-button>
        <wd-button class="flex-1" type="primary" @click="handleSearch">
          搜索
        </wd-button>
      </view>
    </view>
  </wd-popup>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getSimpleMailAccountList } from '@/api/system/mail/account'
import { getDictLabel, getIntDictOptions } from '@/hooks/useDict'
import { getTopPopupModalStyle, getTopPopupStyle } from '@/utils'
import { DICT_TYPE } from '@/utils/constants'
import { formatDate, formatDateRange } from '@/utils/date'
import { getWotPickerDisplay } from '@/utils/wot'

const emit = defineEmits<{
  search: [data: Record<string, any>]
  reset: []
}>()

const formData = reactive({
  sendTime: [undefined, undefined] as [number | undefined, number | undefined],
  userId: undefined as number | undefined,
  userType: -1,
  sendStatus: -1,
  accountId: undefined as number | undefined,
  templateId: undefined as number | undefined,
}) // 搜索表单数据
const visible = ref(false) // 搜索弹窗显示状态
const pickerVisible = ref<Record<string, boolean>>({})

/** 邮箱账号列表 */
const accountList = ref<{ id?: number, mail: string }[]>([])

/** 邮箱账号选项 */
const accountOptions = computed(() => {
  return accountList.value.map(item => ({
    value: item.id,
    label: item.mail,
  }))
})

/** 获取邮箱账号名称 */
function getAccountMail(accountId?: number) {
  return accountList.value.find(item => item.id === accountId)?.mail
}

/** 搜索条件 placeholder 拼接 */
const placeholder = computed(() => {
  const conditions: string[] = []
  if (formData.sendTime?.[0] && formData.sendTime?.[1]) {
    conditions.push(`时间:${formatDate(formData.sendTime[0])}~${formatDate(formData.sendTime[1])}`)
  }
  if (formData.userId) {
    conditions.push(`用户:${formData.userId}`)
  }
  if (formData.userType !== -1) {
    conditions.push(`类型:${getDictLabel(DICT_TYPE.USER_TYPE, formData.userType)}`)
  }
  if (formData.sendStatus !== -1) {
    conditions.push(`发送:${getDictLabel(DICT_TYPE.SYSTEM_MAIL_SEND_STATUS, formData.sendStatus)}`)
  }
  if (formData.accountId) {
    conditions.push(`账号:${getAccountMail(formData.accountId) || formData.accountId}`)
  }
  if (formData.templateId) {
    conditions.push(`模板:${formData.templateId}`)
  }
  return conditions.length > 0 ? conditions.join(' | ') : '搜索邮件日志'
})

const visibleSendTime = ref<[boolean, boolean]>([false, false]) // 发送时间选择器状态
const tempSendTime = ref<[number, number]>([Date.now(), Date.now()]) // 发送时间临时值

/** 确认发送时间开始日期 */
function handleSendTime0Confirm() {
  formData.sendTime = [tempSendTime.value[0], formData.sendTime?.[1]]
  visibleSendTime.value[0] = false
}

/** 确认发送时间结束日期 */
function handleSendTime1Confirm() {
  formData.sendTime = [formData.sendTime?.[0], tempSendTime.value[1]]
  visibleSendTime.value[1] = false
}

/** 搜索按钮操作 */
function handleSearch() {
  visible.value = false
  const dateRange = formatDateRange(formData.sendTime)
  emit('search', {
    beginTime: dateRange?.[0],
    endTime: dateRange?.[1],
    userId: formData.userId,
    userType: formData.userType === -1 ? undefined : formData.userType,
    sendStatus: formData.sendStatus === -1 ? undefined : formData.sendStatus,
    accountId: formData.accountId || undefined,
    templateId: formData.templateId || undefined,
  })
}

/** 重置按钮操作 */
function handleReset() {
  formData.sendTime = [undefined, undefined]
  formData.userId = undefined
  formData.userType = -1
  formData.sendStatus = -1
  formData.accountId = undefined
  formData.templateId = undefined
  visible.value = false
  emit('reset')
}

/** 初始化 */
onMounted(async () => {
  try {
    accountList.value = await getSimpleMailAccountList()
  } catch {
    accountList.value = []
  }
})
</script>
