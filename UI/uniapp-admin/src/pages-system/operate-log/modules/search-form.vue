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
          操作人
        </view>
        <UserPicker
          ref="userPickerRef"
          v-model="formData.userId"
          type="radio"
          placeholder="请选择操作人员"
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          操作模块
        </view>
        <wd-input
          v-model="formData.type"
          placeholder="请输入操作模块"
          clearable
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          操作名
        </view>
        <wd-input
          v-model="formData.subType"
          placeholder="请输入操作名"
          clearable
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          操作内容
        </view>
        <wd-input
          v-model="formData.action"
          placeholder="请输入操作内容"
          clearable
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          操作时间
        </view>
        <view class="yd-search-form-date-range-container">
          <view class="flex-1" @click="visibleCreateTime[0] = true">
            <view class="yd-search-form-date-range-picker">
              {{ formatDate(formData.createTime?.[0]) || '开始日期' }}
            </view>
          </view>
          -
          <view class="flex-1" @click="visibleCreateTime[1] = true">
            <view class="yd-search-form-date-range-picker">
              {{ formatDate(formData.createTime?.[1]) || '结束日期' }}
            </view>
          </view>
        </view>
        <wd-datetime-picker-view v-if="visibleCreateTime[0]" v-model="tempCreateTime[0]" type="date" />
        <view v-if="visibleCreateTime[0]" class="yd-search-form-date-range-actions">
          <wd-button size="small" variant="plain" @click="visibleCreateTime[0] = false">
            取消
          </wd-button>
          <wd-button size="small" type="primary" @click="handleCreateTime0Confirm">
            确定
          </wd-button>
        </view>
        <wd-datetime-picker-view v-if="visibleCreateTime[1]" v-model="tempCreateTime[1]" type="date" />
        <view v-if="visibleCreateTime[1]" class="yd-search-form-date-range-actions">
          <wd-button size="small" variant="plain" @click="visibleCreateTime[1] = false">
            取消
          </wd-button>
          <wd-button size="small" type="primary" @click="handleCreateTime1Confirm">
            确定
          </wd-button>
        </view>
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          业务编号
        </view>
        <wd-input
          v-model="formData.bizId"
          placeholder="请输入业务编号"
          clearable
        />
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
import { computed, reactive, ref } from 'vue'
import UserPicker from '@/components/system-select/user-picker.vue'
import { getTopPopupModalStyle, getTopPopupStyle } from '@/utils'
import { formatDate, formatDateRange } from '@/utils/date'

const emit = defineEmits<{
  search: [data: Record<string, any>]
  reset: []
}>()

const visible = ref(false) // 搜索弹窗显示状态
const userPickerRef = ref<InstanceType<typeof UserPicker>>()
const formData = reactive({
  userId: undefined as number | undefined,
  type: undefined as string | undefined,
  subType: undefined as string | undefined,
  action: undefined as string | undefined,
  createTime: [undefined, undefined] as [number | undefined, number | undefined],
  bizId: undefined as number | undefined,
}) // 搜索表单数据

/** 搜索条件 placeholder 拼接 */
const placeholder = computed(() => {
  const conditions: string[] = []
  if (formData.userId !== undefined) {
    const nickname = userPickerRef.value?.getUserNickname(formData.userId)
    conditions.push(`操作人:${nickname || formData.userId}`)
  }
  if (formData.type) {
    conditions.push(`操作模块:${formData.type}`)
  }
  if (formData.subType) {
    conditions.push(`操作名:${formData.subType}`)
  }
  if (formData.action) {
    conditions.push(`操作内容:${formData.action}`)
  }
  if (formData.createTime?.[0] && formData.createTime?.[1]) {
    conditions.push(`操作时间:${formatDate(formData.createTime[0])}~${formatDate(formData.createTime[1])}`)
  }
  if (formData.bizId !== undefined) {
    conditions.push(`业务编号:${formData.bizId}`)
  }
  return conditions.length > 0 ? conditions.join(' | ') : '搜索操作日志'
})

const visibleCreateTime = ref<[boolean, boolean]>([false, false]) // 操作时间选择器状态
const tempCreateTime = ref<[number, number]>([Date.now(), Date.now()]) // 操作时间临时值

/** 确认操作时间开始日期 */
function handleCreateTime0Confirm() {
  formData.createTime = [tempCreateTime.value[0], formData.createTime?.[1]]
  visibleCreateTime.value[0] = false
}

/** 确认操作时间结束日期 */
function handleCreateTime1Confirm() {
  formData.createTime = [formData.createTime?.[0], tempCreateTime.value[1]]
  visibleCreateTime.value[1] = false
}

/** 搜索按钮操作 */
function handleSearch() {
  visible.value = false
  emit('search', {
    ...formData,
    createTime: formatDateRange(formData.createTime),
  })
}

/** 重置按钮操作 */
function handleReset() {
  formData.userId = undefined
  formData.type = undefined
  formData.subType = undefined
  formData.action = undefined
  formData.createTime = [undefined, undefined]
  formData.bizId = undefined
  visible.value = false
  emit('reset')
}
</script>
