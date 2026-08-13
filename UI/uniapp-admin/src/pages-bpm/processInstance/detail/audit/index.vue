<template>
  <view class="yd-page-container">
    <!-- 顶部导航栏 -->
    <wd-navbar
      :title="isApprove ? '审批同意' : '审批拒绝'"
      left-arrow placeholder safe-area-inset-top fixed
      @click-left="handleBack"
    />

    <!-- 审批表单 -->
    <view class="p-24rpx">
      <!-- 节点表单 -->
      <view v-if="isApprove && approveForm.rule.length > 0" class="mb-24rpx overflow-hidden rounded-16rpx bg-white">
        <FormCreate
          v-model="approveForm.value"
          v-model:api="approveFormApi"
          :option="approveForm.option"
          :rule="approveForm.rule"
          @change="handleApproveFormChange"
        />
      </view>

      <!-- 审批操作表单 -->
      <wd-form ref="formRef" :model="formData" :schema="formSchema">
        <wd-cell-group border>
          <!-- 下一个节点的审批人 -->
          <view v-if="isApprove && nextAssigneesActivityNode.length > 0" class="p-24rpx">
            <view class="mb-16rpx flex items-center">
              <text class="mr-8rpx text-[#f56c6c]">*</text>
              <text class="text-28rpx text-[#333]">下一个节点的审批人</text>
            </view>
            <ProcessInstanceTimeline
              :activity-nodes="nextAssigneesActivityNode"
              :show-status-icon="false"
              :enable-approve-user-select="true"
              @select-user-confirm="selectNextAssigneesConfirm"
            />
          </view>
          <!-- 签名 -->
          <view v-if="isApprove && taskInfo?.signEnable" class="border-b border-[#eee] p-24rpx">
            <view class="mb-16rpx flex items-center">
              <text class="mr-8rpx text-[#f56c6c]">*</text>
              <text class="text-28rpx text-[#333]">签名</text>
            </view>
            <view class="flex items-center gap-16rpx">
              <wd-button type="primary" size="small" @click="openSignatureModal">
                {{ formData.signPicUrl ? '重新签名' : '点击签名' }}
              </wd-button>
              <image
                v-if="formData.signPicUrl"
                :src="formData.signPicUrl"
                class="h-80rpx w-192rpx"
                mode="aspectFit"
                @click="previewSignature"
              />
            </view>
          </view>
          <!-- 审批意见 -->
          <wd-form-item prop="reason" title="审批意见：" title-width="180rpx">
            <wd-textarea
              v-model="formData.reason"
              placeholder="请输入审批意见"
              :maxlength="500"
              show-word-limit
              clearable
            />
          </wd-form-item>
        </wd-cell-group>
      </wd-form>

      <!-- 提交按钮 -->
      <view class="mt-48rpx">
        <wd-button
          :type="isApprove ? 'primary' : 'danger'"
          block
          :loading="formLoading"
          :disabled="formLoading"
          @click="handleSubmit"
        >
          {{ isApprove ? '同意' : '拒绝' }}
        </wd-button>
      </view>
    </view>

    <!-- 签名弹窗 -->
    <wd-popup v-model="showSignatureModal" position="bottom" custom-style="height: 60vh;">
      <view class="h-full flex flex-col">
        <view class="flex items-center justify-between border-b border-[#eee] p-24rpx">
          <text class="text-32rpx text-[#333] font-bold">手写签名</text>
          <wd-icon name="close" size="40rpx" @click="showSignatureModal = false" />
        </view>
        <view class="flex-1 p-24rpx">
          <wd-signature
            :height="300"
            :export-scale="2"
            background-color="#ffffff"
            @confirm="handleSignatureConfirm"
            @clear="handleSignatureClear"
          />
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<script lang="ts" setup>
import type { FormInstance } from '@wot-ui/ui/components/wd-form/types'
import type { ApprovalNodeInfo } from '@/api/bpm/processInstance'
import type { Task } from '@/api/bpm/task'
import type { FormCreateApi } from '@/pages-bpm/components/form-create/packages/wot-ui/types'
import type { FormCreatePreview } from '@/pages-bpm/utils'
import { useToast } from '@wot-ui/ui/components/wd-toast'
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { getApprovalDetail, getNextApproveNodes } from '@/api/bpm/processInstance'
import { approveTask, rejectTask } from '@/api/bpm/task'
import FormCreate from '@/pages-bpm/components/form-create/packages/wot-ui/src/index.vue'
import ProcessInstanceTimeline from '@/pages-bpm/processInstance/detail/components/time-line.vue'
import { setConfAndFields2 } from '@/pages-bpm/utils'
import { getEnvBaseUrl, navigateBackPlus } from '@/utils'
import { BpmCandidateStrategyEnum } from '@/utils/constants'
import { createFormSchema } from '@/utils/wot'

const props = defineProps<{
  processInstanceId?: string
  taskId?: string
  pass?: string
  variablesCacheKey?: string
}>()

definePage({
  style: {
    navigationBarTitleText: '',
    navigationStyle: 'custom',
  },
})
const taskId = computed(() => props.taskId || '')
const processInstanceId = computed(() => props.processInstanceId)
const isApprove = computed(() => props.pass !== 'false') // true: 同意, false: 拒绝
const toast = useToast()
const formLoading = ref(false) // 审批提交状态
const taskInfo = ref<Task | null>(null) // 任务信息

const nextAssigneesActivityNode = ref<ApprovalNodeInfo[]>([]) // 下一个节点审批人列表
const approveUserSelectTasks = ref<ApprovalNodeInfo[]>([]) // 需要选择审批人的节点列表
const approveUserSelectAssignees = ref<Record<string, number[]>>({}) // 审批人选择的审批人数据
const normalFormVariables = ref<Record<string, any>>({}) // 详情页流程表单暂存的可写变量
const approveFormApi = ref<FormCreateApi>() // 节点表单 API
const approveForm = ref<FormCreatePreview>({
  option: {},
  rule: [],
  value: {},
}) // 节点表单配置和数据
let nextAssigneeTimer: ReturnType<typeof setTimeout> | undefined // 下一节点审批人刷新防抖定时器

const showSignatureModal = ref(false) // 签名相关

const formData = reactive({
  reason: '',
  signPicUrl: '', // 签名图片 URL
}) // 表单数据
const formSchema = createFormSchema({
  reason: [{ required: () => !!taskInfo.value?.reasonRequire, message: '审批意见不能为空' }],
})
const formRef = ref<FormInstance>() // 表单组件引用

/** 返回上一页 */
function handleBack() {
  navigateBackPlus(`/pages-bpm/processInstance/detail/index?id=${processInstanceId.value}&taskId=${taskId.value}`)
}

/** 加载任务信息 */
async function loadTaskInfo() {
  const data = await getApprovalDetail({
    processInstanceId: processInstanceId.value,
    taskId: taskId.value,
  })
  taskInfo.value = data?.todoTask || null
  normalFormVariables.value = getCachedNormalFormVariables()

  if (isApprove.value && taskInfo.value?.formConf && taskInfo.value?.formFields?.length) {
    setConfAndFields2(
      approveForm,
      taskInfo.value.formConf,
      taskInfo.value.formFields,
      taskInfo.value.formVariables || {},
    )
    approveForm.value.option = {
      ...approveForm.value.option,
      // 审批页使用页面底部按钮提交，隐藏 form-create 内置按钮
      submitBtn: false,
      resetBtn: false,
    }
  } else {
    approveForm.value = { option: {}, rule: [], value: {} }
  }
}

/** 加载下一个节点审批人 */
async function loadNextApproveNodes() {
  if (!isApprove.value) {
    return
  }
  const params = {
    processInstanceId: processInstanceId.value,
    taskId: taskId.value,
    processVariablesStr: JSON.stringify(getApprovalVariables()),
  }
  const data = await getNextApproveNodes(params)
  nextAssigneesActivityNode.value = []
  approveUserSelectTasks.value = []
  approveUserSelectAssignees.value = {}
  if (data && data.length > 0) {
    nextAssigneesActivityNode.value = data
    // 获取审批人自选的任务
    approveUserSelectTasks.value = data.filter(
      (node: ApprovalNodeInfo) =>
        BpmCandidateStrategyEnum.APPROVE_USER_SELECT === node.candidateStrategy,
    ) || []
  }
}

/** 选择下一个节点审批人确认 */
function selectNextAssigneesConfirm(activityId: string, userList: any[]) {
  approveUserSelectAssignees.value[activityId] = userList.map(user => user.id)
}

/** 同步节点表单数据 */
function handleApproveFormChange(data: Record<string, any>) {
  approveForm.value.value = data
}

/** 获取详情页暂存的流程表单变量 */
function getCachedNormalFormVariables() {
  if (!props.variablesCacheKey) {
    return {}
  }
  return uni.getStorageSync(props.variablesCacheKey) || {}
}

/** 清理详情页暂存的流程表单变量 */
function clearCachedNormalFormVariables() {
  if (props.variablesCacheKey) {
    uni.removeStorageSync(props.variablesCacheKey)
  }
}

/** 合并流程表单变量和节点表单变量 */
function getApprovalVariables() {
  return {
    ...normalFormVariables.value,
    ...(approveFormApi.value?.formData() || approveForm.value.value || {}),
  }
}

/** 清理下一节点审批人刷新定时器 */
function clearNextAssigneeTimer() {
  if (nextAssigneeTimer) {
    clearTimeout(nextAssigneeTimer)
    nextAssigneeTimer = undefined
  }
}

/** 打开签名弹窗 */
function openSignatureModal() {
  showSignatureModal.value = true
}

/** 签名确认 */
async function handleSignatureConfirm(result: { tempFilePath: string, base64: string }) {
  toast.loading('上传中...')
  try {
    // 上传签名图片
    formData.signPicUrl = await uploadSignatureFile(result.tempFilePath)
    showSignatureModal.value = false
    toast.success('签名成功')
  } catch (err) {
    console.error('上传失败：', err)
    toast.show('上传失败')
  }
}

/** 上传签名文件 */
function uploadSignatureFile(tempFilePath: string): Promise<string> {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${getEnvBaseUrl()}/infra/file/upload`,
      filePath: tempFilePath,
      name: 'file',
      success: (uploadFileRes) => {
        try {
          const data = JSON.parse(uploadFileRes.data)
          if (data.code === 0 && data.data) {
            resolve(data.data)
          } else {
            reject(new Error(data.msg || '上传失败'))
          }
        } catch (err) {
          reject(err)
        }
      },
      fail: (err) => {
        console.error('上传失败:', err)
        reject(err)
      },
    })
  })
}

/** 签名清除 */
function handleSignatureClear() {
  formData.signPicUrl = ''
}

/** 预览签名 */
function previewSignature() {
  if (formData.signPicUrl) {
    uni.previewImage({
      urls: [formData.signPicUrl],
      current: formData.signPicUrl,
    })
  }
}

/** 提交表单 */
async function handleSubmit() {
  if (formLoading.value) {
    return
  }

  const { valid } = await formRef.value!.validate()
  if (!valid) {
    return
  }
  // 验证签名
  if (isApprove.value && taskInfo.value?.signEnable && !formData.signPicUrl) {
    toast.show('请先进行签名')
    return
  }
  if (isApprove.value && approveForm.value.rule.length > 0) {
    const { valid } = await (approveFormApi.value?.validate() || Promise.resolve({ valid: true, errors: [] }))
    if (!valid) {
      return
    }
  }

  // 验证审批人选择
  if (isApprove.value && approveUserSelectTasks.value.length > 0) {
    for (const task of approveUserSelectTasks.value) {
      if (!approveUserSelectAssignees.value[task.id] || approveUserSelectAssignees.value[task.id].length === 0) {
        toast.show(`请选择「${task.name}」的审批人`)
        return
      }
    }
  }

  formLoading.value = true
  try {
    if (isApprove.value) {
      const variables = getApprovalVariables()
      // 审批通过
      await approveTask({
        id: taskId.value as string,
        reason: formData.reason,
        signPicUrl: formData.signPicUrl || undefined,
        variables: Object.keys(variables).length > 0 ? variables : undefined,
        nextAssignees: Object.keys(approveUserSelectAssignees.value).length > 0
          ? approveUserSelectAssignees.value
          : undefined,
      })
    } else {
      // 审批拒绝
      await rejectTask({
        id: taskId.value as string,
        reason: formData.reason,
      })
    }
    clearCachedNormalFormVariables()
    toast.success('审批成功')
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages-bpm/processInstance/detail/index?id=${processInstanceId.value}&taskId=${taskId.value}`,
      })
    }, 1000)
  } finally {
    formLoading.value = false
  }
}

/** 审批表单变化后防抖刷新下一节点审批人 */
watch(
  () => approveForm.value.value,
  () => {
    if (!isApprove.value || approveForm.value.rule.length === 0) {
      return
    }
    clearNextAssigneeTimer()
    nextAssigneeTimer = setTimeout(() => {
      nextAssigneeTimer = undefined
      loadNextApproveNodes()
    }, 300)
  },
  { deep: true },
)

/** 页面卸载时清理定时器 */
onBeforeUnmount(() => {
  clearNextAssigneeTimer()
})

/** 初始化 */
onMounted(async () => {
  // 参数校验
  if (!props.taskId || !props.processInstanceId) {
    toast.show('参数错误')
    return
  }
  try {
    toast.loading('加载中...')
    // 加载任务信息和下一个节点审批人
    await loadTaskInfo()
    await loadNextApproveNodes()
  } finally {
    toast.close()
  }
})
</script>
