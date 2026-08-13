import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import test from 'node:test'

const adminRoot = path.resolve(import.meta.dirname, '..')
const data = fs.readFileSync(
  path.join(adminRoot, 'src/components/AppLinkInput/data.ts'),
  'utf8'
)

const routes = [
  '/pages/commission/index',
  '/pages/commission/wallet',
  '/pages/commission/withdraw',
  '/pages/commission/team',
  '/pages/commission/order',
  '/pages/commission/goods',
  '/pages/commission/promoter',
  '/pages/commission/commission-ranking'
]

test('registers the complete brokerage page family in the marriage AppLink group', () => {
  const start = data.indexOf("name: '处佳缘'")
  const end = data.indexOf('\n  },\n  {', start)
  const marriageGroup = data.slice(start, end === -1 ? data.length : end)

  for (const route of routes) {
    assert.match(marriageGroup, new RegExp(`path:\\s*['"]${route}['"]`))
  }
  assert.doesNotMatch(data, /name:\s*['"]分销商城['"]|分销商城/) 
  assert.doesNotMatch(marriageGroup, /action:showShareModal/)
})
