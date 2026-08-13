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
          监听器名字
        </view>
        <wd-input
          v-model="formData.name"
          placeholder="请输入监听器名字"
          clearable
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          监听器类型
        </view>
        <wd-radio-group v-model="formData.type" type="button">
          <wd-radio value="">
            全部
          </wd-radio>
          <wd-radio
            v-for="dict in getStrDictOptions(DICT_TYPE.BPM_PROCESS_LISTENER_TYPE)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </wd-radio>
        </wd-radio-group>
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
import { getDictLabel, getStrDictOptions } from '@/hooks/useDict'
import { getTopPopupModalStyle, getTopPopupStyle } from '@/utils'
import { DICT_TYPE } from '@/utils/constants'

const emit = defineEmits<{
  search: [data: Record<string, any>]
  reset: []
}>()

const visible = ref(false) // 搜索弹窗显示状态
const formData = reactive({
  name: undefined as string | undefined,
  type: '', // 空字符串表示全部
}) // 搜索表单数据

/** 搜索条件 placeholder 拼接 */
const placeholder = computed(() => {
  const conditions: string[] = []
  if (formData.name) {
    conditions.push(`名字:${formData.name}`)
  }
  if (formData.type) {
    conditions.push(`类型:${getDictLabel(DICT_TYPE.BPM_PROCESS_LISTENER_TYPE, formData.type)}`)
  }
  return conditions.length > 0 ? conditions.join(' | ') : '搜索流程监听器'
})

/** 搜索按钮操作 */
function handleSearch() {
  visible.value = false
  emit('search', {
    ...formData,
    type: formData.type || undefined,
  })
}

/** 重置按钮操作 */
function handleReset() {
  formData.name = undefined
  formData.type = ''
  visible.value = false
  emit('reset')
}
</script>
