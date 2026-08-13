<template>
  <Dialog v-model="dialogVisible" title="薪资导入" width="480">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :auto-upload="false"
      :disabled="formLoading"
      :limit="1"
      :on-exceed="handleExceed"
      accept=".xlsx, .xls"
      drag
    >
      <Icon icon="ep:upload" />
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <template #tip>
        <div class="el-upload__tip text-center">
          <div class="el-upload__tip">
            文件名需含「YYYY年MM月」，如「2025年12月份工资数据.xls」；表头须为 39 列客户工资表格式。
          </div>
          <el-link
            :underline="false"
            style="font-size: 12px; vertical-align: baseline"
            type="primary"
            @click="importTemplate"
          >
            下载模板
          </el-link>
        </div>
      </template>
    </el-upload>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import { SalaryApi } from '@/api/hr/salary'
import download from '@/utils/download'

defineOptions({ name: 'HrSalaryImportForm' })

const message = useMessage()
const dialogVisible = ref(false)
const formLoading = ref(false)
const uploadRef = ref()
const fileList = ref([])

const emits = defineEmits(['success'])

/** 打开弹窗 */
const open = () => {
  dialogVisible.value = true
  fileList.value = []
  resetForm()
}
defineExpose({ open })

/** 文件数超出提示 */
const handleExceed = () => {
  message.error('最多只能上传一个文件！')
}

/** 下载模板 */
const importTemplate = async () => {
  const res = await SalaryApi.importSalaryTemplate()
  download.excel(res, '薪资导入模板.xls')
}

/** 提交导入 */
const submitForm = async () => {
  if (fileList.value.length === 0) {
    message.error('请上传文件')
    return
  }
  const file = fileList.value[0].raw as File
  formLoading.value = true
  try {
    // 1. 预检：解析年月、校验表头、查重复
    const check = await SalaryApi.checkSalaryImport(file)
    if (check.headerValid !== true) {
      const missing = (check.missingHeaders || []).join('、')
      message.error(
        missing ? `表头校验失败，缺失列：${missing}` : '表头校验失败，请使用 39 列客户工资表格式'
      )
      return
    }
    if (!check.totalRows || check.totalRows <= 0) {
      message.error('导入文件无数据行，请检查 Excel 内容')
      return
    }
    // 2. 重复确认
    let confirmOverwrite = false
    if (check.duplicateCount > 0) {
      try {
        const sample = (check.duplicateEmployeeNos || []).slice(0, 3).join('、')
        await ElMessageBox.confirm(
          `检测到 ${check.year}年${check.month}月 已存在 ${check.duplicateCount} 条薪资记录${
            sample ? `（如：${sample}）` : ''
          }。继续导入将覆盖这些记录，是否继续？`,
          '存在相同数据',
          {
            confirmButtonText: '继续导入',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        confirmOverwrite = true
      } catch {
        // 用户点取消
        return
      }
    }
    // 3. 正式导入
    const res = await SalaryApi.importSalary(file, confirmOverwrite)
    // 4. 结果展示
    showResult(res)
    dialogVisible.value = false
    emits('success')
  } catch {
    // 业务错误（文件名无法识别、表头缺失、需确认未确认等）由 axios 拦截器已提示
  } finally {
    formLoading.value = false
  }
}

/** 展示导入结果 */
const showResult = (res: any) => {
  const period = `${res.year}年${res.month}月`
  if (res.failureCount === 0) {
    message.success(`导入成功（${period}）：新增 ${res.insertCount} 条，覆盖 ${res.updateCount} 条`)
    return
  }
  const failureItems = Object.entries(res.failureRows || {})
    .map(([k, v]) => `<li>${k}：${v}</li>`)
    .join('')
  ElMessageBox.alert(
    `<p>期间：${period}</p>
     <p>新增成功：<b>${res.insertCount}</b> 条；覆盖更新：<b>${res.updateCount}</b> 条；
        失败：<b style="color:red">${res.failureCount}</b> 条</p>
     <hr/>
     <p>失败明细：</p>
     <ul style="max-height:240px;overflow:auto">${failureItems}</ul>`,
    '导入结果',
    { dangerouslyUseHTMLString: true }
  )
}

/** 重置表单 */
const resetForm = async () => {
  formLoading.value = false
  await nextTick()
  uploadRef.value?.clearFiles()
}
</script>
