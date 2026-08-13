<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-row>
        <el-col :span="12">
          <el-form-item v-if="formData.id === undefined" label="关联客商" prop="partnerId">
            <PartnerSelect v-model="formData.partnerId" />
          </el-form-item>
          <el-form-item v-else label="关联客商">
            <el-input :model-value="partnerDisplayName" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="归属部门" prop="deptId">
            <el-tree-select
              v-model="formData.deptId"
              :data="deptList"
              :props="defaultProps"
              check-strictly
              node-key="id"
              placeholder="请选择归属部门"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="formData.username"
              placeholder="请输入账号"
              :disabled="formData.id !== undefined"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item v-if="formData.id === undefined" label="账号密码" prop="password">
            <el-input
              v-model="formData.password"
              placeholder="请输入账号密码"
              show-password
              type="password"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="岗位">
            <el-select v-model="formData.postIds" multiple placeholder="请选择">
              <el-option
                v-for="item in postList"
                :key="item.id"
                :label="item.name"
                :value="item.id!"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备注">
            <el-input v-model="formData.remark" placeholder="请输入内容" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="业务角色">
            <el-checkbox v-model="formData.isSale" label="销售" />
            <el-checkbox v-model="formData.isPurchase" label="采购" />
            <el-checkbox v-model="formData.isMes" label="生产" />
            <el-checkbox v-model="formData.isStock" label="仓库" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { CommonStatusEnum } from '@/utils/constants'
import { defaultProps, handleTree } from '@/utils/tree'
import * as PostApi from '@/api/system/post'
import * as DeptApi from '@/api/system/dept'
import * as UserApi from '@/api/system/user'
import PartnerSelect from './components/PartnerSelect.vue'
import * as PartnerApi from '@/api/partner/partner'
import { FormRules } from 'element-plus'

defineOptions({ name: 'SystemUserForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  partnerId: undefined as number | undefined,
  deptId: '',
  id: undefined as number | undefined,
  username: '',
  password: '',
  postIds: [] as number[],
  remark: '',
  status: CommonStatusEnum.ENABLE,
  roleIds: [],
  isSale: false,
  isPurchase: false,
  isMes: false,
  isStock: false
})
const formRules = computed<FormRules>(() => ({
  partnerId: formData.value.id ? [] : [{ required: true, message: '关联客商不能为空', trigger: 'change' }],
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: formData.value.id ? [] : [{ required: true, message: '账号密码不能为空', trigger: 'blur' }]
}))
const formRef = ref() // 表单 Ref
const deptList = ref<Tree[]>([]) // 树形结构
const postList = ref([] as PostApi.PostVO[]) // 岗位列表
const partnerDisplayName = ref('') // 编辑时回显客商名称

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await UserApi.getPartner(id)
      formData.value = {
        ...formData.value,
        ...data,
        partnerId: data.id // 共享主键：user.id == partner.id
      }
      // 回显客商名称
      partnerDisplayName.value = data.nickname || ''
      if (!partnerDisplayName.value) {
        try {
          const partner = await PartnerApi.getPartner(data.id)
          partnerDisplayName.value = partner.nickname || partner.name || ''
        } catch {}
      }
    } finally {
      formLoading.value = false
    }
  }
  // 加载部门树
  deptList.value = handleTree(await DeptApi.getSimpleDeptList())
  // 加载岗位列表
  postList.value = await PostApi.getSimplePostList()
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
    const data = formData.value as unknown as UserApi.PartnerVO
    if (formType.value === 'create') {
      await UserApi.createPartner(data)
      message.success(t('common.createSuccess'))
    } else {
      await UserApi.updatePartner(data)
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
    partnerId: undefined,
    deptId: '',
    id: undefined,
    username: '',
    password: '',
    postIds: [],
    remark: '',
    status: CommonStatusEnum.ENABLE,
    roleIds: [],
    isSale: false,
    isPurchase: false,
    isMes: false,
    isStock: false
  }
  formRef.value?.resetFields()
  partnerDisplayName.value = ''
}
</script>
