<template>
  <Dialog v-model="dialogVisible" title="编辑会员" width="500px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="会员等级" prop="levelId">
        <el-select v-model="formData.levelId" class="!w-full" clearable placeholder="请选择等级">
          <el-option v-for="item in levelList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户分组" prop="groupId">
        <el-select v-model="formData.groupId" class="!w-full" clearable placeholder="请选择分组">
          <el-option v-for="item in groupList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="会员标签" prop="tagIds">
        <el-select v-model="formData.tagIds" class="!w-full" clearable multiple placeholder="请选择标签">
          <el-option v-for="item in tagList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as PartnerMemberApi from '@/api/partner/member'
import * as PartnerLevelApi from '@/api/partner/level'
import * as PartnerGroupApi from '@/api/partner/group'
import * as PartnerTagApi from '@/api/partner/tag'
import { useMessage } from '@/hooks/web/useMessage'

const message = useMessage()
const { t } = useI18n()
const dialogVisible = ref(false)
const formData = reactive({ id: undefined, levelId: undefined, groupId: undefined, tagIds: [] })
const formRules = reactive({})
const formRef = ref()
const emit = defineEmits(['success'])
const levelList = ref([])
const groupList = ref([])
const tagList = ref([])

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  resetForm()
  if (id) {
    const data = await PartnerMemberApi.getMember(id)
    Object.assign(formData, data)
    if (data.tagIds) {
      formData.tagIds = data.tagIds.split(',').map(Number)
    }
  }
}
const resetForm = () => {
  formData.id = undefined
  formData.levelId = undefined
  formData.groupId = undefined
  formData.tagIds = []
  formRef.value?.resetFields()
}
const submitForm = async () => {
  await formRef.value.validate()
  const submitData = { ...formData, tagIds: formData.tagIds?.join(',') }
  await PartnerMemberApi.updateMember(submitData)
  message.success(t('common.updateSuccess'))
  dialogVisible.value = false
  emit('success')
}

onMounted(async () => {
  levelList.value = await PartnerLevelApi.getMemberLevelSimpleList()
  groupList.value = await PartnerGroupApi.getMemberGroupSimpleList()
  tagList.value = await PartnerTagApi.getMemberTagSimpleList()
})
defineExpose({ open })
</script>
