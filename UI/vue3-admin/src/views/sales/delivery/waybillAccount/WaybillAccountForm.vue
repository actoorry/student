<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="620px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="140px"
      v-loading="formLoading"
    >
      <el-form-item label="账户名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入账户名称（租户内唯一）" maxlength="64" />
      </el-form-item>
      <el-form-item label="快递公司" prop="expressId">
        <el-select v-model="formData.expressId" placeholder="请选择快递公司" style="width: 100%">
          <el-option v-for="item in deliveryExpressList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>

      <el-divider content-position="left">快递100平台授权（签名与表单使用）</el-divider>
      <el-form-item label="授权 key" prop="key">
        <el-input
          v-model="formData.key"
          :placeholder="keyPlaceholder"
          :type="showCredential ? 'text' : 'password'"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>
      <el-form-item label="授权密钥" prop="secret">
        <el-input
          v-model="formData.secret"
          :placeholder="secretPlaceholder"
          :type="showCredential ? 'text' : 'password'"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>

      <el-divider content-position="left">电子面单账户（承运商）</el-divider>
      <el-form-item label="月结账号" prop="partnerId">
        <el-input
          v-model="formData.partnerId"
          :placeholder="partnerIdPlaceholder"
          autocomplete="new-password"
          maxlength="64"
        />
      </el-form-item>
      <el-form-item label="电子面单密码" prop="partnerKey">
        <el-input
          v-model="formData.partnerKey"
          :placeholder="partnerKeyPlaceholder"
          :type="showCredential ? 'text' : 'password'"
          show-password
          autocomplete="new-password"
          maxlength="128"
        />
      </el-form-item>
      <el-form-item label="电子面单密钥" prop="partnerSecret">
        <el-input
          v-model="formData.partnerSecret"
          :placeholder="partnerSecretPlaceholder"
          :type="showCredential ? 'text' : 'password'"
          show-password
          autocomplete="new-password"
          maxlength="128"
        />
      </el-form-item>
      <el-form-item label="网点" prop="net">
        <el-input v-model="formData.net" placeholder="例如 taobao/cainiao，或网点名称" maxlength="64" />
      </el-form-item>
      <el-form-item label="承载编号" prop="code">
        <el-input v-model="formData.code" placeholder="电子面单承载编号（可选）" maxlength="64" />
      </el-form-item>
      <el-form-item label="客户账户名称" prop="partnerName">
        <el-input v-model="formData.partnerName" placeholder="电子面单客户账户名称（可选）" maxlength="64" />
      </el-form-item>
      <el-form-item label="取消操作人" prop="checkMan">
        <el-input v-model="formData.checkMan" placeholder="取消电子面单时传给快递公司的操作人（可选）" maxlength="64" />
      </el-form-item>
      <el-form-item label="快递产品类型" prop="expType">
        <el-input v-model="formData.expType" placeholder="如标准快递；以承运商要求为准（可选）" maxlength="64" />
      </el-form-item>

      <el-divider content-position="left">模板与寄件地址</el-divider>
      <el-form-item label="模板 ID" prop="tempId">
        <el-input v-model="formData.tempId" placeholder="快递100模板 ID（必填）" maxlength="64" />
      </el-form-item>
      <el-form-item label="默认寄件地址" prop="defaultAddressId">
        <div class="w-100%">
          <el-select v-model="formData.defaultAddressId" placeholder="请选择商户寄件地址" style="width: 100%">
            <el-option
              v-for="item in senderAddressList"
              :key="item.id"
              :label="`${item.name} ${item.areaName || ''} ${item.detailAddress}`"
              :value="item.id"
            />
          </el-select>
          <div class="mt-5px">
            <el-button link type="primary" @click="senderCreateVisible = !senderCreateVisible">
              <Icon icon="ep:plus" class="mr-3px" />
              {{ senderCreateVisible ? '收起新增' : '新增寄件地址' }}
            </el-button>
          </div>
          <el-form
            v-if="senderCreateVisible"
            ref="senderFormRef"
            :model="senderForm"
            :rules="senderRules"
            label-width="70px"
            class="mt-5px rounded p-15px border border-solid border-[#e4e7ed]"
          >
            <el-form-item label="寄件人" prop="name">
              <el-input v-model="senderForm.name" placeholder="请输入寄件人名称" maxlength="10" />
            </el-form-item>
            <el-form-item label="手机号" prop="mobile">
              <el-input v-model="senderForm.mobile" placeholder="请输入手机号" maxlength="20" />
            </el-form-item>
            <el-form-item label="所在地" prop="areaId">
              <el-tree-select
                v-model="senderForm.areaId"
                :data="areaList"
                :props="defaultProps"
                :render-after-expand="true"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="详细地址" prop="detailAddress">
              <el-input v-model="senderForm.detailAddress" type="textarea" :rows="2" placeholder="请输入详细地址" maxlength="250" />
            </el-form-item>
            <el-form-item label="邮编" prop="postCode">
              <el-input v-model="senderForm.postCode" placeholder="请输入邮编（可选）" maxlength="20" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="senderCreating" @click="submitSenderAddress">保存寄件地址</el-button>
              <el-button @click="senderCreateVisible = false">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div v-if="senderAddressList.length === 0" style="font-size: 12px" class="w-100% color-red">
          暂无商户寄件地址，请先新增寄件地址或联系管理员创建
        </div>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :value="dict.value">
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formType === 'update'">
        <el-checkbox v-model="showCredential">显示凭据（掩码）</el-checkbox>
        <span style="font-size: 12px; color: #909399"> 留空的凭据字段表示保持不变</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import { getAreaTree } from '@/api/system/area'
import { defaultProps } from '@/utils/tree'
import * as WaybillAccountApi from '@/api/sales/delivery/waybillAccount'
import * as DeliveryExpressApi from '@/api/sales/delivery/express'
import * as PartnerAddressApi from '@/api/partner/address'

defineOptions({ name: 'SalesElectronicWaybillAccountForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const showCredential = ref(false) // 是否显示凭据
const deliveryExpressList = ref<DeliveryExpressApi.DeliveryExpressVO[]>([]) // 快递公司列表
const senderAddressList = ref<WaybillAccountApi.SenderAddressVO[]>([]) // 商户寄件地址列表
// 新增寄件地址
const senderCreateVisible = ref(false) // 是否展示新增寄件地址表单
const senderCreating = ref(false) // 新增寄件地址加载中
const areaList = ref([]) // 地区列表
const senderFormRef = ref() // 寄件地址表单 Ref
const senderForm = ref({
  name: '',
  mobile: '',
  areaId: undefined as number | undefined,
  detailAddress: '',
  postCode: ''
})
const senderRules = reactive({
  name: [{ required: true, message: '寄件人不能为空', trigger: 'blur' }],
  mobile: [{ required: true, message: '手机号不能为空', trigger: 'blur' }],
  areaId: [{ required: true, message: '请选择所在地', trigger: 'change' }],
  detailAddress: [{ required: true, message: '详细地址不能为空', trigger: 'blur' }]
})
// 凭据掩码占位：更新时展示，提交不携带（空值表示保留原值）
const keyPlaceholder = ref('')
const secretPlaceholder = ref('')
const partnerIdPlaceholder = ref('')
const partnerKeyPlaceholder = ref('')
const partnerSecretPlaceholder = ref('')

const formData = ref<WaybillAccountApi.WaybillAccountVO>({
  id: undefined,
  name: '',
  expressId: undefined,
  key: '',
  secret: '',
  partnerId: '',
  partnerKey: '',
  partnerSecret: '',
  net: '',
  code: '',
  partnerName: '',
  checkMan: '',
  expType: '',
  tempId: '',
  defaultAddressId: undefined,
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  name: [{ required: true, message: '账户名称不能为空', trigger: 'blur' }],
  expressId: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  key: [
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (formType.value === 'create' && (value == null || value.trim() === '')) {
          callback(new Error('创建时平台授权 key 不能为空'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  secret: [
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (formType.value === 'create' && (value == null || value.trim() === '')) {
          callback(new Error('创建时平台授权密钥不能为空'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  tempId: [{ required: true, message: '模板 ID 不能为空', trigger: 'blur' }],
  defaultAddressId: [{ required: true, message: '请选择默认寄件地址', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  showCredential.value = false
  resetForm()
  if (senderAddressList.value.length === 0) {
    senderAddressList.value = await WaybillAccountApi.getSenderAddressList()
  }
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await WaybillAccountApi.getWaybillAccount(id)
      // 凭据以掩码占位，不进入可提交字段（留空表示保留原值）
      keyPlaceholder.value = data.key || ''
      secretPlaceholder.value = data.secret || ''
      partnerIdPlaceholder.value = data.partnerId || ''
      partnerKeyPlaceholder.value = data.partnerKey || ''
      partnerSecretPlaceholder.value = data.partnerSecret || ''
      formData.value = {
        id: data.id,
        name: data.name,
        expressId: data.expressId,
        key: '',
        secret: '',
        partnerId: '',
        partnerKey: '',
        partnerSecret: '',
        net: data.net,
        code: data.code,
        partnerName: data.partnerName,
        checkMan: data.checkMan,
        expType: data.expType,
        tempId: data.tempId,
        defaultAddressId: data.defaultAddressId,
        status: data.status
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as WaybillAccountApi.WaybillAccountVO
    if (formType.value === 'create') {
      await WaybillAccountApi.createWaybillAccount(data)
      message.success(t('common.createSuccess'))
    } else {
      await WaybillAccountApi.updateWaybillAccount(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    expressId: undefined,
    key: '',
    secret: '',
    partnerId: '',
    partnerKey: '',
    partnerSecret: '',
    net: '',
    code: '',
    partnerName: '',
    checkMan: '',
    expType: '',
    tempId: '',
    defaultAddressId: undefined,
    status: CommonStatusEnum.ENABLE
  }
  keyPlaceholder.value = ''
  secretPlaceholder.value = ''
  partnerIdPlaceholder.value = ''
  partnerKeyPlaceholder.value = ''
  partnerSecretPlaceholder.value = ''
  formRef.value?.resetFields()
}

/** 新增寄件地址：复用 PartnerAddress 能力，user_id=0 表示租户级共享，type=1 表示商户寄件地址 */
const submitSenderAddress = async () => {
  const valid = await senderFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  senderCreating.value = true
  try {
    const id = await PartnerAddressApi.createAddress({
      userId: 0,
      defaulted: false,
      type: 1,
      name: senderForm.value.name,
      mobile: senderForm.value.mobile,
      areaId: senderForm.value.areaId,
      detailAddress: senderForm.value.detailAddress,
      postCode: senderForm.value.postCode
    })
    message.success('寄件地址创建成功')
    senderCreateVisible.value = false
    senderForm.value = { name: '', mobile: '', areaId: undefined, detailAddress: '', postCode: '' }
    senderFormRef.value?.resetFields()
    // 刷新寄件地址列表并选中新地址
    senderAddressList.value = await WaybillAccountApi.getSenderAddressList()
    formData.value.defaultAddressId = id
  } finally {
    senderCreating.value = false
  }
}

/** 初始化 **/
onMounted(async () => {
  deliveryExpressList.value = await DeliveryExpressApi.getSimpleDeliveryExpressList()
  senderAddressList.value = await WaybillAccountApi.getSenderAddressList()
  areaList.value = await getAreaTree()
})
</script>
