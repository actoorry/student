import { describe, it } from 'node:test'
import assert from 'node:assert'
import { readFileSync } from 'node:fs'
import vm from 'node:vm'
import ts from 'typescript'

const expressIndex = readFileSync(
  new URL('../src/views/sales/delivery/express/index.vue', import.meta.url),
  'utf-8'
)
const expressForm = readFileSync(
  new URL('../src/views/sales/delivery/express/ExpressForm.vue', import.meta.url),
  'utf-8'
)
const templateIndex = readFileSync(
  new URL('../src/views/sales/delivery/expressTemplate/index.vue', import.meta.url),
  'utf-8'
)
const templateApi = readFileSync(
  new URL('../src/api/sales/delivery/expressTemplate/index.ts', import.meta.url),
  'utf-8'
)
const templateForm = readFileSync(
  new URL('../src/views/sales/delivery/expressTemplate/ExpressTemplateForm.vue', import.meta.url),
  'utf-8'
)
const productDeliveryForm = readFileSync(
  new URL('../src/views/product/spu/form/DeliveryForm.vue', import.meta.url),
  'utf-8'
)
const orderDeliveryForm = readFileSync(
  new URL('../src/views/sales/order/form/OrderDeliveryForm.vue', import.meta.url),
  'utf-8'
)

describe('sales delivery admin contract', () => {
  it('express 页面使用 sales:* 权限，且无旧 trade:delivery:express*', () => {
    const perms = [...expressIndex.matchAll(/v-hasPermi="\['([^']+)'\]"/g)].map((m) => m[1])
    assert.ok(perms.every((p) => p.startsWith('sales:sales_delivery_express:')), `旧权限残留: ${perms}`)
    assert.ok(!expressIndex.includes('trade:delivery:express'))
  })

  it('expressTemplate 页面使用 sales:* 权限，且无旧 trade:delivery:express-template*', () => {
    const perms = [...templateIndex.matchAll(/v-hasPermi="\['([^']+)'\]"/g)].map((m) => m[1])
    assert.ok(perms.every((p) => p.startsWith('sales:sales_delivery_express_template:')), `旧权限残留: ${perms}`)
    assert.ok(!templateIndex.includes('trade:delivery:express-template'))
  })

  it('express 页面有分页控件与翻页事件', () => {
    assert.match(expressIndex, /<Pagination/)
    assert.match(expressIndex, /@pagination="getList"/)
    assert.match(expressIndex, /v-model:total="total"/)
  })

  it('ExpressForm 重置恢复完整默认对象，无 picUrl 残留', () => {
    const resetBlock = expressForm.match(/const resetForm = \(\) => \{([\s\S]*?)\n  \}/)?.[1] ?? ''
    assert.match(resetBlock, /code:\s*''/)
    assert.match(resetBlock, /name:\s*''/)
    assert.match(resetBlock, /logo:\s*''/)
    assert.match(resetBlock, /sort:\s*0/)
    assert.match(resetBlock, /status:\s*CommonStatusEnum\.ENABLE/)
    assert.ok(!resetBlock.includes('picUrl'))
  })

  it('模板 API 类型使用 charges/frees，不再声明 templateCharge/templateFree', () => {
    assert.match(templateApi, /charges:\s*ExpressTemplateChargeVO\[\]/)
    assert.match(templateApi, /frees:\s*ExpressTemplateFreeVO\[\]/)
    assert.ok(!templateApi.includes('templateCharge'))
    assert.ok(!templateApi.includes('templateFree'))
  })

  it('模板表单含 charges/frees 嵌套校验与重复区域提示，元/分转换只操作提交副本', () => {
    assert.match(templateForm, /charges:\s*\[\{ validator: validateCharges/)
    assert.match(templateForm, /frees:\s*\[\{ validator: validateFrees/)
    assert.match(templateForm, /计费规则区域不能重复/)
    assert.match(templateForm, /包邮规则区域不能重复/)
    assert.match(templateForm, /cloneDeep\(formData\.value\)/)
  })

  it('模板表单 setup 初始化时可安全创建校验规则', () => {
    const script = templateForm.match(/<script lang="ts" setup>([\s\S]*?)<\/script>/)?.[1] ?? ''
    const setupCode = ts
      .transpileModule(script.replace(/^import .*$/gm, ''), {
        compilerOptions: { module: ts.ModuleKind.None, target: ts.ScriptTarget.ES2022 }
      })
      .outputText.replace(/export \{\};?/, '')

    const ref = (value: unknown) => ({ value })
    assert.doesNotThrow(() =>
      vm.runInNewContext(setupCode, {
        useI18n: () => ({ t: (key: string) => key }),
        useMessage: () => ({}),
        ref,
        reactive: (value: unknown) => value,
        defineExpose: () => undefined,
        defineEmits: () => () => undefined,
        onMounted: () => undefined,
        defaultProps: {}
      })
    )
  })

  it('商品物流表单只在快递方式要求模板，取消快递清空选择', () => {
    assert.match(productDeliveryForm, /v-if="formData\.deliveryTypes\?\.includes\(DeliveryTypeEnum\.EXPRESS\.type\)"/)
    assert.match(productDeliveryForm, /快递发货必须选择运费模板/)
    assert.match(productDeliveryForm, /applyDeliveryDefaults/)
    assert.match(productDeliveryForm, /deliveryTemplateId = undefined/)
  })

  it('订单发货表单分快递/无需发货分支，无需发货提交 0 与空串', () => {
    assert.match(orderDeliveryForm, /value="express">快递物流/)
    assert.match(orderDeliveryForm, /value="none">无需发货/)
    assert.match(orderDeliveryForm, /logisticsId = 0/)
    assert.match(orderDeliveryForm, /logisticsNo = ''/)
    assert.match(orderDeliveryForm, /请填写物流单号/)
  })
})
