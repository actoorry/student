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
          <wd-form-item title="配置名" title-width="200rpx" prop="name">
            <wd-input
              v-model="formData.name"
              clearable
              placeholder="请输入配置名"
            />
          </wd-form-item>
          <wd-form-item
            title="存储器"
            title-width="200rpx"
            prop="storage"
            :is-link="!formData.id"
            :value="getWotPickerFormValue(getIntDictOptions(DICT_TYPE.INFRA_FILE_STORAGE), formData.storage)"
            placeholder="请选择存储器"
            @click="!formData.id && (pickerVisible.storage = true)"
          />
          <wd-picker
            v-model:visible="pickerVisible.storage"
            :model-value="formData.storage"
            :columns="getIntDictOptions(DICT_TYPE.INFRA_FILE_STORAGE)"
            @confirm="({ value }) => formData.storage = value[0]"
          />
          <wd-form-item title="备注" title-width="200rpx" prop="remark">
            <wd-textarea
              v-model="formData.remark"
              clearable
              placeholder="请输入备注"
            />
          </wd-form-item>
        </wd-cell-group>

        <!-- DB / Local / FTP / SFTP 配置 -->
        <wd-cell-group v-if="formData.storage && formData.storage >= 10 && formData.storage <= 12" border title="存储配置">
          <wd-form-item title="基础路径" title-width="200rpx" prop="config.basePath">
            <wd-input
              v-model="formData.config!.basePath"
              clearable
              placeholder="请输入基础路径"
            />
          </wd-form-item>
          <!-- FTP / SFTP 配置 -->
          <template v-if="formData.storage >= 11 && formData.storage <= 12">
            <wd-form-item title="主机地址" title-width="200rpx" prop="config.host">
              <wd-input
                v-model="formData.config!.host"
                clearable
                placeholder="请输入主机地址"
              />
            </wd-form-item>
            <wd-form-item title="主机端口" title-width="200rpx" prop="config.port">
              <wd-input
                v-model.number="formData.config!.port"
                type="number"
                clearable
                placeholder="请输入主机端口"
              />
            </wd-form-item>
            <wd-form-item title="用户名" title-width="200rpx" prop="config.username">
              <wd-input
                v-model="formData.config!.username"
                clearable
                placeholder="请输入用户名"
              />
            </wd-form-item>
            <wd-form-item title="密码" title-width="200rpx" prop="config.password">
              <wd-input
                v-model="formData.config!.password"
                clearable
                placeholder="请输入密码"
              />
            </wd-form-item>
          </template>
          <!-- FTP 连接模式 -->
          <wd-form-item v-if="formData.storage === 11" title="连接模式" title-width="200rpx" prop="config.mode" center>
            <wd-radio-group v-model="formData.config!.mode" type="button">
              <wd-radio value="Active">
                主动模式
              </wd-radio>
              <wd-radio value="Passive">
                被动模式
              </wd-radio>
            </wd-radio-group>
          </wd-form-item>
        </wd-cell-group>

        <!-- S3 配置 -->
        <wd-cell-group v-if="formData.storage === 20" border title="S3 配置">
          <wd-form-item title="节点地址" title-width="200rpx" prop="config.endpoint">
            <wd-input
              v-model="formData.config!.endpoint"
              clearable
              placeholder="请输入节点地址"
            />
          </wd-form-item>
          <wd-form-item title="存储 bucket" title-width="200rpx" prop="config.bucket">
            <wd-input
              v-model="formData.config!.bucket"
              clearable
              placeholder="请输入 bucket"
            />
          </wd-form-item>
          <wd-form-item title="accessKey" title-width="200rpx" prop="config.accessKey">
            <wd-input
              v-model="formData.config!.accessKey"
              clearable
              placeholder="请输入 accessKey"
            />
          </wd-form-item>
          <wd-form-item title="accessSecret" title-width="200rpx" prop="config.accessSecret">
            <wd-input
              v-model="formData.config!.accessSecret"
              clearable
              placeholder="请输入 accessSecret"
            />
          </wd-form-item>
          <wd-form-item title="Path Style" title-width="200rpx" prop="config.enablePathStyleAccess" center>
            <wd-radio-group v-model="formData.config!.enablePathStyleAccess" type="button">
              <wd-radio :value="true">
                启用
              </wd-radio>
              <wd-radio :value="false">
                禁用
              </wd-radio>
            </wd-radio-group>
          </wd-form-item>
          <wd-form-item title="公开访问" title-width="200rpx" prop="config.enablePublicAccess" center>
            <wd-radio-group v-model="formData.config!.enablePublicAccess" type="button">
              <wd-radio :value="true">
                公开
              </wd-radio>
              <wd-radio :value="false">
                私有
              </wd-radio>
            </wd-radio-group>
          </wd-form-item>
          <wd-form-item title="区域" title-width="200rpx" prop="config.region">
            <wd-input
              v-model="formData.config!.region"
              clearable
              placeholder="请填写区域，一般仅 AWS 需要填写"
            />
          </wd-form-item>
        </wd-cell-group>

        <!-- 通用配置 -->
        <wd-cell-group v-if="formData.storage" border title="通用配置">
          <wd-form-item title="自定义域名" title-width="200rpx" prop="config.domain">
            <wd-input
              v-model="formData.config!.domain"
              clearable
              placeholder="请输入自定义域名"
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
import type { FileConfig } from '@/api/infra/file/config'
import { useToast } from '@wot-ui/ui/components/wd-toast'
import { computed, onMounted, ref } from 'vue'
import { createFileConfig, getFileConfig, updateFileConfig } from '@/api/infra/file/config'
import { getIntDictOptions } from '@/hooks/useDict'
import { navigateBackPlus } from '@/utils'
import { DICT_TYPE } from '@/utils/constants'
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
const getTitle = computed(() => props.id ? '编辑文件配置' : '新增文件配置')
const formLoading = ref(false) // 表单提交状态
const formData = ref<FileConfig>({
  id: undefined,
  name: '',
  storage: undefined,
  remark: '',
  config: {
    basePath: '',
    host: '',
    port: undefined,
    username: '',
    password: '',
    mode: 'Passive',
    endpoint: '',
    bucket: '',
    accessKey: '',
    accessSecret: '',
    enablePathStyleAccess: false,
    enablePublicAccess: false,
    region: '',
    domain: '',
  },
}) // 表单数据
const formSchema = createFormSchema({
  name: [{ required: true, message: '配置名不能为空' }],
  storage: [{ required: true, message: '存储器不能为空' }],
})
const formRef = ref<FormInstance>() // 表单组件引用
const pickerVisible = ref<Record<string, boolean>>({})

/** 返回上一页 */
function handleBack() {
  navigateBackPlus('/pages-infra/file/index')
}

/** 加载详情 */
async function getDetail() {
  if (!props.id) {
    return
  }
  const data = await getFileConfig(props.id)
  formData.value = {
    ...data,
    config: data.config || {
      basePath: '',
      host: '',
      port: undefined,
      username: '',
      password: '',
      mode: 'Passive',
      endpoint: '',
      bucket: '',
      accessKey: '',
      accessSecret: '',
      enablePathStyleAccess: false,
      enablePublicAccess: false,
      region: '',
      domain: '',
    },
  }
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
      await updateFileConfig(formData.value)
      toast.success('修改成功')
    } else {
      await createFileConfig(formData.value)
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
onMounted(() => {
  getDetail()
})
</script>

<style lang="scss" scoped>
</style>
