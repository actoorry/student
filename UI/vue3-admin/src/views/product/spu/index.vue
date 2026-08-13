<!-- 产品中心 - 产品列表  -->
<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="产品名称" prop="name">
        <el-input
          v-model="queryParams.name"
          class="!w-240px"
          clearable
          placeholder="请输入产品名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品分类" prop="categorySales">
        <el-cascader
          v-model="queryParams.categorySales"
          :options="categoryList"
          :props="defaultProps"
          class="w-1/1"
          clearable
          filterable
          placeholder="请选择产品分类"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
          end-placeholder="结束日期"
          start-placeholder="开始日期"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
        <el-button
          v-hasPermi="['product:spu:create']"
          plain
          type="primary"
          @click="openForm(undefined)"
        >
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
        <el-button
          v-hasPermi="['product:spu:export']"
          :loading="exportLoading"
          plain
          type="success"
          @click="handleExport"
        >
          <Icon class="mr-5px" icon="ep:download" />
          导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-tabs v-model="queryParams.tabType" @tab-click="handleTabClick">
      <el-tab-pane
        v-for="item in tabsData"
        :key="item.type"
        :label="item.name + '(' + item.count + ')'"
        :name="item.type"
      />
    </el-tabs>
    <el-table v-loading="loading" :data="list">
      <el-table-column type="expand">
        <template #default="{ row }">
          <el-form class="spu-table-expand" label-position="left">
            <el-row>
              <el-col :span="24">
                <el-row>
                  <el-col :span="8">
                    <el-form-item label="产品分类:">
                      <span>{{ formatCategoryName(row.categorySales) }}</span>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="市场价:">
                      <span>{{ fenToYuan(row.marketPrice) }}</span>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="成本价:">
                      <span>{{ fenToYuan(row.costPrice) }}</span>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <el-row>
                  <el-col :span="8">
                    <el-form-item label="浏览量:">
                      <span>{{ row.browseCount }}</span>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="虚拟销量:">
                      <span>{{ row.virtualSalesCount }}</span>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-col>
            </el-row>
          </el-form>
        </template>
      </el-table-column>
      <el-table-column label="产品编号" min-width="140" prop="id" />
      <el-table-column align="center" label="产品类型" min-width="100">
        <template #default="{ row }">
          <el-tag>{{ formatProductType(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="军创区" min-width="90">
        <template #default="{ row }">
          <el-tag :type="row.isMilitary ? 'danger' : 'info'">{{ row.isMilitary ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="配送方式" min-width="140">
        <template #default="{ row }">
          <el-tag v-for="type in row.deliveryTypes || []" :key="type" class="mr-4px">
            {{ formatDeliveryType(type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="产品信息" min-width="300">
        <template #default="{ row }">
          <div class="flex">
            <el-image
              fit="cover"
              :src="row.picUrl"
              class="flex-none w-50px h-50px"
              @click="imagePreview(row.picUrl)"
            />
            <div class="ml-4 overflow-hidden">
              <el-tooltip effect="dark" :content="row.name" placement="top">
                <div>
                  {{ row.name }}
                </div>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column align="center" label="价格" min-width="160" prop="price">
        <template #default="{ row }"> ¥ {{ fenToYuan(row.price) }}</template>
      </el-table-column>
      <el-table-column align="center" label="销量" min-width="90" prop="salesCount" />
      <el-table-column align="center" label="库存" min-width="90" prop="stock" />
      <el-table-column align="center" label="微信道具" min-width="170">
        <template #default="{ row }">
          <template v-if="isWechatVirtualGoodsProduct(row)">
            <el-tooltip
              v-if="getWechatVirtualReviewReason(row)"
              :content="'微信审核未通过：' + getWechatVirtualReviewReason(row)"
              placement="top"
            >
              <el-tag :type="getWechatVirtualStatusTag(row)">
                {{ getWechatVirtualStatusText(row) }}
              </el-tag>
            </el-tooltip>
            <el-tag v-else :type="getWechatVirtualStatusTag(row)">
              {{ getWechatVirtualStatusText(row) }}
            </el-tag>
            <div v-if="getWechatVirtualProductId(row)" class="goods-product-id">
              {{ getWechatVirtualProductId(row) }}
            </div>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="排序" min-width="70" prop="sort" />
      <el-table-column align="center" label="销售状态" min-width="80">
        <template #default="{ row }">
          <template v-if="row.status >= 0">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="上架"
              inactive-text="下架"
              inline-prompt
              @change="handleStatusChange(row)"
            />
          </template>
          <template v-else>
            <el-tag type="info">回收站</el-tag>
          </template>
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="创建时间"
        prop="createTime"
        width="180"
      />
      <el-table-column align="center" fixed="right" label="操作" min-width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)"> 详情 </el-button>
          <el-button
            v-hasPermi="['product:spu:update']"
            link
            type="primary"
            @click="openForm(row.id)"
          >
            修改
          </el-button>
          <el-button
            v-hasPermi="['product:spu:update']"
            link
            type="warning"
            @click="openSkuFromList(row.id)"
          >
            SKU 维护
          </el-button>
          <el-button
            v-if="isWechatVirtualGoodsProduct(row)"
            v-hasPermi="['product:wechat-virtual-goods:sync']"
            link
            type="primary"
            @click="handleWechatVirtualGoodsSync(row.id)"
          >
            同步道具
          </el-button>
          <el-button
            v-if="isWechatVirtualGoodsProduct(row)"
            v-hasPermi="['product:wechat-virtual-goods:publish']"
            link
            type="primary"
            @click="handleWechatVirtualGoodsPublish(row.id)"
          >
            发布道具
          </el-button>
          <el-button
            v-if="isWechatVirtualGoodsProduct(row)"
            v-hasPermi="['product:wechat-virtual-goods:query']"
            link
            type="primary"
            @click="handleWechatVirtualGoodsRefresh(row.id)"
          >
            刷新状态
          </el-button>
          <template v-if="queryParams.tabType === 4">
            <el-button
              v-hasPermi="['product:spu:delete']"
              link
              type="danger"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
            <el-button
              v-hasPermi="['product:spu:update']"
              link
              type="primary"
              @click="handleStatus02Change(row, ProductSpuStatusEnum.DISABLE.status)"
            >
              恢复
            </el-button>
          </template>
          <template v-else>
            <el-button
              v-hasPermi="['product:spu:update']"
              link
              type="danger"
              @click="handleStatus02Change(row, ProductSpuStatusEnum.RECYCLE.status)"
            >
              回收
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- SPU 产品详情/编辑弹窗 -->
  <SpuFormDialog
    v-model:visible="dialogVisible"
    :mode="dialogMode"
    :spu-id="currentSpuId"
    @success="onDialogSuccess"
  />

  <!-- SKU 维护弹窗（从列表页直接打开） -->
  <SkuFormDialog
    v-model:visible="skuDialogVisible"
    :is-detail="false"
    :prop-form-data="skuFormData"
    @confirm="onSkuSave"
  />
</template>
<script lang="ts" setup>
import { TabsPaneContext } from 'element-plus'
import { createImageViewer } from '@/components/ImageViewer'
import { dateFormatter } from '@/utils/formatTime'
import { defaultProps, handleTree, treeToString } from '@/utils/tree'
import {
  DeliveryTypeEnum,
  ProductSpuStatusEnum,
  ProductTypeEnum,
  ProductWechatVirtualGoodsStatusEnum
} from '@/utils/constants'
import { fenToYuan } from '@/utils'
import { convertToInteger, floatToFixed2, formatToFraction } from '@/utils'
import { cloneDeep } from 'lodash-es'
import download from '@/utils/download'
import * as ProductSpuApi from '@/api/product/spu'
import * as ProductCategoryApi from '@/api/product/category'
import {
  SALES_CATEGORY_ROOT_ID,
  WAREHOUSE_CATEGORY_ROOT_ID
} from '../category/categoryDimension'
import SpuFormDialog from './SpuFormDialog.vue'
import SkuFormDialog from './SkuFormDialog.vue'

defineOptions({ name: 'ProductSpu' })

const message = useMessage() // 消息弹窗
const route = useRoute() // 路由
const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中
const exportLoading = ref(false) // 导出的加载中
const total = ref(0) // 列表的总页数
const list = ref<ProductSpuApi.Spu[]>([]) // 列表的数据

// 弹窗相关状态
const dialogVisible = ref(false) // 弹窗可见性
const currentSpuId = ref<number | undefined>(undefined) // 当前操作的 SPU ID
const dialogMode = ref<'detail' | 'edit' | 'add'>('add') // 弹窗模式

// SKU 弹窗相关状态（从列表页直接打开）
const skuDialogVisible = ref(false) // SKU 弹窗可见性
const skuFormData = ref<ProductSpuApi.Spu>({}) // SKU 弹窗的 SPU 数据
const skuDialogLoading = ref(false) // SKU 数据加载中
// tabs 数据
const tabsData = ref([
  {
    name: '出售中',
    type: 0,
    count: 0
  },
  {
    name: '仓库中',
    type: 1,
    count: 0
  },
  {
    name: '已售罄',
    type: 2,
    count: 0
  },
  {
    name: '警戒库存',
    type: 3,
    count: 0
  },
  {
    name: '回收站',
    type: 4,
    count: 0
  }
])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  tabType: 0,
  name: '',
  categorySales: undefined,
  parentId: SALES_CATEGORY_ROOT_ID,
  createTime: undefined
}) // 查询参数
const queryFormRef = ref() // 搜索的表单Ref

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ProductSpuApi.getSpuPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 切换 Tab */
const handleTabClick = (tab: TabsPaneContext) => {
  queryParams.tabType = tab.paneName as number
  getList()
}

/** 获得每个 Tab 的数量 */
const getTabsCount = async () => {
  const res = await ProductSpuApi.getTabsCount()
  for (let objName in res) {
    tabsData.value[Number(objName)].count = res[objName]
  }
}

/** 添加到仓库 / 回收站的状态 */
const handleStatus02Change = async (row: any, newStatus: number) => {
  try {
    // 二次确认
    const text = newStatus === ProductSpuStatusEnum.RECYCLE.status ? '加入到回收站' : '恢复到仓库'
    await message.confirm(`确认要"${row.name}"${text}吗？`)
    // 发起修改
    await ProductSpuApi.updateStatus({ id: row.id, status: newStatus })
    message.success(text + '成功')
    // 刷新 tabs 数据
    await getTabsCount()
    // 刷新列表
    await getList()
  } catch {}
}

/** 更新上架/下架状态 */
const handleStatusChange = async (row: any) => {
  try {
    // 二次确认
    const text = row.status ? '上架' : '下架'
    await message.confirm(`确认要${text}"${row.name}"吗？`)
    // 发起修改
    await ProductSpuApi.updateStatus({ id: row.id, status: row.status })
    message.success(text + '成功')
    // 刷新 tabs 数据
    await getTabsCount()
    // 刷新列表
    await getList()
  } catch {
    // 异常时，需要重置回之前的值
    row.status =
      row.status === ProductSpuStatusEnum.DISABLE.status
        ? ProductSpuStatusEnum.ENABLE.status
        : ProductSpuStatusEnum.DISABLE.status
  }
}

const handleWechatVirtualGoodsSync = async (spuId: number) => {
  try {
    const results: ProductSpuApi.WechatVirtualGoods[] =
      await ProductSpuApi.syncWechatVirtualGoods(spuId)
    showWechatVirtualGoodsResult('同步道具', results)
    await getList()
  } catch {}
}

const handleWechatVirtualGoodsPublish = async (spuId: number) => {
  try {
    const results: ProductSpuApi.WechatVirtualGoods[] =
      await ProductSpuApi.publishWechatVirtualGoods(spuId)
    showWechatVirtualGoodsResult('发布道具', results)
    await getList()
  } catch {}
}

const handleWechatVirtualGoodsRefresh = async (spuId: number) => {
  try {
    const results: ProductSpuApi.WechatVirtualGoods[] =
      await ProductSpuApi.refreshWechatVirtualGoods(spuId)
    showWechatVirtualGoodsResult('刷新状态', results)
    await getList()
  } catch {}
}

const showWechatVirtualGoodsResult = (
  actionName: string,
  results: ProductSpuApi.WechatVirtualGoods[]
) => {
  const successCount = results.filter((r) => r.actionSuccess === true).length
  const failCount = results.filter((r) => r.actionSuccess === false).length
  const processingCount = results.length - successCount - failCount
  const failures = results
    .filter((r) => r.actionSuccess === false && r.actionMessage)
    .map((r) => `${r.skuName || r.skuId}：${r.actionMessage}`)
  const summary = `${actionName}完成：成功 ${successCount} 个，处理中 ${processingCount} 个，失败 ${failCount} 个`
  if (failures.length > 0) {
    message.warning(summary + '；失败详情：' + failures.join('；'))
  } else if (processingCount > 0) {
    message.success(summary)
  } else {
    message.success(summary)
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await ProductSpuApi.deleteSpu(id)
    message.success(t('common.delSuccess'))
    // 刷新tabs数据
    await getTabsCount()
    // 刷新列表
    await getList()
  } catch {}
}

/** 产品图预览 */
const imagePreview = (imgUrl: string) => {
  createImageViewer({
    urlList: [imgUrl]
  })
}

/** 搜索按钮操作 */
const handleQuery = () => {
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 新增或修改 */
const openForm = (id?: number) => {
  currentSpuId.value = id
  dialogMode.value = id ? 'edit' : 'add'
  dialogVisible.value = true
}

/** 查看产品详情 */
const openDetail = (id: number) => {
  currentSpuId.value = id
  dialogMode.value = 'detail'
  dialogVisible.value = true
}

/** 弹窗保存成功后刷新列表 */
const onDialogSuccess = () => {
  getTabsCount()
  getList()
}

/** 从列表页直接打开 SKU 维护弹窗 */
const openSkuFromList = async (spuId: number) => {
  skuDialogLoading.value = true
  try {
    const res = (await ProductSpuApi.getSpu(spuId)) as ProductSpuApi.Spu
    res.skus?.forEach((item) => {
      item.price = formatToFraction(item.price)
      item.marketPrice = formatToFraction(item.marketPrice)
      item.costPrice = formatToFraction(item.costPrice)
      item.firstBrokeragePrice = formatToFraction(item.firstBrokeragePrice)
      item.secondBrokeragePrice = formatToFraction(item.secondBrokeragePrice)
    })
    skuFormData.value = res
    skuDialogVisible.value = true
  } catch (e) {
    message.error('加载 SKU 数据失败')
    console.error(e)
  } finally {
    skuDialogLoading.value = false
  }
}

/** SKU 弹窗确认保存 */
const onSkuSave = async (data: Partial<ProductSpuApi.Spu>) => {
  try {
    const deepCopyFormData = cloneDeep(skuFormData.value) as ProductSpuApi.Spu
    deepCopyFormData.specType = data.specType
    deepCopyFormData.subCommissionType = data.subCommissionType
    deepCopyFormData.skus = data.skus
    // SKU 相关价格元转分
    deepCopyFormData.skus!.forEach((item) => {
      // product_sku 不持久化名称，提交时统一使用 SPU 名称满足保存接口契约
      item.name = deepCopyFormData.name
      item.price = convertToInteger(item.price)
      item.marketPrice = convertToInteger(item.marketPrice)
      item.costPrice = convertToInteger(item.costPrice)
      item.firstBrokeragePrice = convertToInteger(item.firstBrokeragePrice)
      item.secondBrokeragePrice = convertToInteger(item.secondBrokeragePrice)
    })
    await ProductSpuApi.updateSpu(deepCopyFormData)
    message.success('SKU 维护成功')
    // 刷新列表
    await getTabsCount()
    await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await ProductSpuApi.exportSpu(queryParams)
    download.excel(data, '产品列表.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 获取分类的节点的完整结构 */
const categoryList = ref() // 分类树
const formatCategoryName = (categorySales: number) => {
  if (categorySales === SALES_CATEGORY_ROOT_ID || categorySales === WAREHOUSE_CATEGORY_ROOT_ID) {
    return '待迁移：直接引用分类维度根'
  }
  return treeToString(categoryList.value, categorySales)
}
const formatProductType = (type?: number) => {
  return (
    Object.values(ProductTypeEnum).find((item) => item.type === type)?.name ||
    (type === undefined ? '-' : String(type))
  )
}
const isWechatVirtualGoodsProduct = (row: ProductSpuApi.Spu) =>
  row.isWechatMiniappVirtualGoods === true
const getWechatVirtualProductId = (row: ProductSpuApi.Spu) =>
  (row.skus || []).map((sku) => sku.wechatVirtualProductId).filter(Boolean).join(', ')
const getWechatVirtualStatus = (row: ProductSpuApi.Spu) => {
  const skus = row.skus || []
  if (!skus.length) return ProductWechatVirtualGoodsStatusEnum.NONE.status
  const statuses = skus.flatMap((sku) => [
    sku.wechatVirtualUploadStatus,
    sku.wechatVirtualPublishStatus,
    sku.wechatVirtualReviewStatus
  ])
  if (statuses.some((status) => status === ProductWechatVirtualGoodsStatusEnum.FAILED.status)) {
    return ProductWechatVirtualGoodsStatusEnum.FAILED.status
  }
  if (statuses.some((status) => status === ProductWechatVirtualGoodsStatusEnum.PROCESSING.status)) {
    return ProductWechatVirtualGoodsStatusEnum.PROCESSING.status
  }
  const allReady = skus.every(
    (sku) =>
      Boolean(sku.wechatVirtualProductId) &&
      sku.wechatVirtualUploadStatus === ProductWechatVirtualGoodsStatusEnum.SUCCESS.status &&
      sku.wechatVirtualPublishStatus === ProductWechatVirtualGoodsStatusEnum.SUCCESS.status &&
      sku.wechatVirtualReviewStatus === ProductWechatVirtualGoodsStatusEnum.SUCCESS.status
  )
  if (allReady) return ProductWechatVirtualGoodsStatusEnum.SUCCESS.status
  const anyProgress = statuses.some((status) => status != null)
  return anyProgress
    ? ProductWechatVirtualGoodsStatusEnum.INCOMPLETE.status
    : ProductWechatVirtualGoodsStatusEnum.NONE.status
}
const getWechatVirtualStatusConfig = (row: ProductSpuApi.Spu) =>
  Object.values(ProductWechatVirtualGoodsStatusEnum).find(
    (item) => item.status === getWechatVirtualStatus(row)
  ) || ProductWechatVirtualGoodsStatusEnum.NONE
const getWechatVirtualStatusText = (row: ProductSpuApi.Spu) => getWechatVirtualStatusConfig(row).name
const getWechatVirtualStatusTag = (row: ProductSpuApi.Spu) => getWechatVirtualStatusConfig(row).type
const isWechatVirtualSkuFailed = (sku: ProductSpuApi.Sku) =>
  [
    sku.wechatVirtualUploadStatus,
    sku.wechatVirtualPublishStatus,
    sku.wechatVirtualReviewStatus
  ].some((status) => status === ProductWechatVirtualGoodsStatusEnum.FAILED.status)
const getWechatVirtualReviewReason = (row: ProductSpuApi.Spu) =>
  (row.skus || [])
    .filter((sku) => isWechatVirtualSkuFailed(sku) && sku.wechatVirtualReviewFailReason)
    .map((sku) => `SKU ${sku.id}: ${sku.wechatVirtualReviewFailReason}`)
    .join('; ')
const formatDeliveryType = (type: number) => {
  return Object.values(DeliveryTypeEnum).find((item) => item.type === type)?.name || String(type)
}

/** 激活时 */
onActivated(() => {
  getList()
})

/** 初始化 **/
onMounted(async () => {

  if (route.meta.query) {
    Object.assign(queryParams, route.meta.query)
  }
  // 获得产品信息
  await getTabsCount()
  await getList()
  // 获得分类树
  const data = await ProductCategoryApi.getCategoryList({
    parentId: SALES_CATEGORY_ROOT_ID,
    status: 0
  })
  categoryList.value = handleTree(data, 'id', 'parentId')
})
</script>
<style lang="scss" scoped>
.spu-table-expand {
  padding-left: 42px;

  :deep(.el-form-item__label) {
    width: 82px;
    font-weight: bold;
    color: #99a9bf;
  }
}

.goods-product-id {
  margin-top: 4px;
  color: #606266;
  font-size: 12px;
  line-height: 16px;
}
</style>
