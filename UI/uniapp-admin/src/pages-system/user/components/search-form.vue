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
          用户名称
        </view>
        <wd-input
          v-model="formData.username"
          placeholder="请输入用户名称"
          clearable
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          用户昵称
        </view>
        <wd-input
          v-model="formData.nickname"
          placeholder="请输入用户昵称"
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
import { getTopPopupModalStyle, getTopPopupStyle } from '@/utils'

const emit = defineEmits<{
  search: [data: Record<string, any>]
  reset: []
}>()

const visible = ref(false) // 搜索弹窗显示状态
const formData = reactive({
  username: undefined as string | undefined,
  nickname: undefined as string | undefined,
}) // 搜索表单数据

const placeholder = computed(() => {
  const conditions: string[] = []
  if (formData.username) {
    conditions.push(`用户名:${formData.username}`)
  }
  if (formData.nickname) {
    conditions.push(`昵称:${formData.nickname}`)
  }
  return conditions.length > 0 ? conditions.join(' | ') : '搜索用户'
})

/** 搜索按钮操作 */
function handleSearch() {
  visible.value = false
  emit('search', { ...formData })
}

/** 重置按钮操作 */
function handleReset() {
  formData.username = undefined
  formData.nickname = undefined
  visible.value = false
  emit('reset')
}
</script>
