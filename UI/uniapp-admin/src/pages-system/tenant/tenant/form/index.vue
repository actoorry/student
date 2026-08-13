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
          <wd-form-item title="租户名称" title-width="200rpx" prop="name">
            <wd-input
              v-model="formData.name"
              clearable
              placeholder="请输入租户名称"
            />
          </wd-form-item>
          <wd-form-item
            title="租户套餐"
            title-width="200rpx"
            prop="packageId"
            is-link
            :value="getWotPickerFormValue(packageOptions, formData.packageId, { valueKey: 'id', labelKey: 'name' })"
            placeholder="请选择租户套餐"
            @click="pickerVisible.packageId = true"
          />
          <wd-picker
            v-model:visible="pickerVisible.packageId"
            :model-value="formData.packageId"
            :columns="packageOptions"
            label-key="name"
            value-key="id"
            @confirm="({ value }) => formData.packageId = value[0]"
          />
          <wd-form-item title="联系人" title-width="200rpx" prop="contactName">
            <wd-input
              v-model="formData.contactName"
              clearable
              placeholder="请输入联系人"
            />
          </wd-form-item>
          <wd-form-item title="联系手机" title-width="200rpx" prop="contactMobile">
            <wd-input
              v-model="formData.contactMobile"
              clearable
              placeholder="请输入联系手机"
            />
          </wd-form-item>
          <!-- 新增时显示用户名和密码 -->
          <wd-form-item v-if="!props.id" title="用户名称" title-width="200rpx" prop="username">
            <wd-input
              v-model="formData.username"
              clearable
              placeholder="请输入用户名称"
            />
          </wd-form-item>
          <wd-form-item v-if="!props.id" title="用户密码" title-width="200rpx" prop="password">
            <wd-input
              v-model="formData.password"
              show-password
              clearable
              placeholder="请输入用户密码"
            />
          </wd-form-item>
          <wd-form-item title="账号额度" title-width="200rpx" prop="accountCount">
            <wd-input
              v-model="formData.accountCount"
              type="number"
              clearable
              placeholder="请输入账号额度"
            />
          </wd-form-item>
          <wd-form-item
            title="过期时间"
            title-width="200rpx"
            prop="expireTime"
            is-link
            :value="formatDate(formData.expireTime)"
            placeholder="请选择过期时间"
            @click="pickerVisible.expireTime = true"
          />
          <wd-datetime-picker
            v-model="formData.expireTime"
            v-model:visible="pickerVisible.expireTime"
            title="请选择过期时间"
            type="date"
          />
          <wd-form-item title="租户状态" title-width="200rpx" prop="status" center>
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
import type { Tenant } from '@/api/system/tenant'
import type { TenantPackage } from '@/api/system/tenant/package'
import { useToast } from '@wot-ui/ui/components/wd-toast'
import { computed, onMounted, ref } from 'vue'
import { createTenant, getTenant, updateTenant } from '@/api/system/tenant'
import { getTenantPackageList } from '@/api/system/tenant/package'
import { getIntDictOptions } from '@/hooks/useDict'
import { navigateBackPlus } from '@/utils'
import { CommonStatusEnum, DICT_TYPE } from '@/utils/constants'
import { formatDate } from '@/utils/date'
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
const getTitle = computed(() => props.id ? '编辑租户' : '新增租户')
const formLoading = ref(false) // 表单提交状态
const formData = ref<Tenant & { username?: string, password?: string }>({
  id: undefined,
  name: '',
  packageId: undefined,
  contactName: '',
  contactMobile: '',
  accountCount: undefined,
  expireTime: new Date(),
  websites: [],
  status: CommonStatusEnum.ENABLE,
  username: '',
  password: '',
}) // 表单数据
const formSchema = createFormSchema({
  name: [{ required: true, message: '租户名称不能为空' }],
  packageId: [{ required: true, message: '租户套餐不能为空' }],
  contactName: [{ required: true, message: '联系人不能为空' }],
  accountCount: [{ required: true, message: '账号额度不能为空' }],
  expireTime: [{ required: true, message: '过期时间不能为空' }],
  username: [{ required: () => !props.id, message: '用户名称不能为空' }],
  password: [{ required: () => !props.id, message: '用户密码不能为空' }],
})
const formRef = ref<FormInstance>() // 表单组件引用
const pickerVisible = ref<Record<string, boolean>>({})
const packageOptions = ref<TenantPackage[]>([])

/** 返回上一页 */
function handleBack() {
  navigateBackPlus('/pages-system/tenant/index')
}

/** 加载租户套餐列表 */
async function loadPackageList() {
  packageOptions.value = await getTenantPackageList()
}

/** 加载租户详情 */
async function getDetail() {
  if (!props.id) {
    return
  }
  formData.value = await getTenant(props.id)
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
      await updateTenant(formData.value)
      toast.success('修改成功')
    } else {
      await createTenant(formData.value)
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
  await loadPackageList()
  await getDetail()
})
</script>

<style lang="scss" scoped>
</style>
