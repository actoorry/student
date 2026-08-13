import { describe, it } from 'node:test'
import assert from 'node:assert'
import { readFileSync } from 'node:fs'

const configPage = readFileSync(
  new URL('../src/views/sales/config/index.vue', import.meta.url),
  'utf-8'
)

describe('sales config tab save contract', () => {
  it('only validates fields belonging to the active tab before saving', () => {
    assert.match(configPage, /<el-tabs v-model="activeTab">/)
    assert.match(configPage, /const tabFormFields = \{[\s\S]*?delivery: \[[\s\S]*?deliveryExpressFreePrice[\s\S]*?\]/)
    assert.match(configPage, /const currentTabFormFields = getCurrentTabFormFields\(\)/)
    assert.match(configPage, /await formRef\.value\.validateField\(currentTabFormFields\)/)
    assert.match(configPage, /activeTab\.value === 'brokerage' && !formData\.value\.brokerageEnabled/)
    assert.doesNotMatch(configPage, /const valid = await formRef\.value\.validate\(\)/)
  })
})
