import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import test from 'node:test'

const adminRoot = path.resolve(import.meta.dirname, '..')
const data = fs.readFileSync(
  path.join(adminRoot, 'src/components/AppLinkInput/data.ts'),
  'utf8'
)

const commissionRoutes = [
  '/pages/commission/index',
  '/pages/commission/wallet',
  '/pages/commission/withdraw',
  '/pages/commission/team',
  '/pages/commission/order',
  '/pages/commission/goods',
  '/pages/commission/promoter',
  '/pages/commission/commission-ranking'
]

test('adds a 商城分销 group with eight 商城-prefixed commission entries', () => {
  const start = data.indexOf("name: '商城分销'")
  assert.ok(start >= 0, '商城分销 group must exist')
  const end = data.indexOf("name: '处佳缘'", start)
  const group = data.slice(start, end === -1 ? data.length : end)
  for (const route of commissionRoutes) {
    assert.match(group, new RegExp(`path:\\s*['"]${route}['"]`))
  }
  // 每个条目显示名以“商城”开头，且路径与既有 commission 页族一致
  const entryNames = [...group.matchAll(/name:\s*'([^']+)',\s*path:/g)].map((m) => m[1])
  assert.ok(entryNames.length === 8, `expected 8 entries, got ${entryNames.length}`)
  for (const name of entryNames) {
    assert.match(name, /^商城/)
  }
  assert.doesNotMatch(data, /name:\s*['"]分销商城['"]/)
})

test('keeps every pre-existing category and entry unchanged', () => {
  for (const group of ['商城', '处佳缘', '商品', '营销活动', '戎集汇', '支付', '用户中心']) {
    assert.match(data, new RegExp(`name:\\s*['"]${group}['"]`))
  }
  // 处佳缘中的既有婚恋与分销条目保持原样
  for (const fragment of [
    "name: '动态', path: '/pages/dynamics/index'",
    "name: '消息', path: '/pages/messages/index'",
    "name: '我的认证', path: '/pages/mine-certifications/index'",
    "name: '分销中心', path: '/pages/commission/index'",
    "name: '推广排行', path: '/pages/commission/promoter'"
  ]) {
    assert.ok(data.includes(fragment), `missing preserved entry: ${fragment}`)
  }
})

test('allows the same commission path in both 处佳缘 and 商城分销', () => {
  // 同一 /pages/commission/* 路径可并列存在于两个分类，且互不覆盖
  const marriageStart = data.indexOf("name: '处佳缘'")
  const mallDistStart = data.indexOf("name: '商城分销'")
  assert.ok(marriageStart >= 0 && mallDistStart >= 0)
  for (const route of commissionRoutes) {
    const pathCount = (data.match(new RegExp(`path:\\s*['"]${route}['"]`, 'g')) || []).length
    assert.ok(pathCount >= 2, `${route} must be listed in both categories (found ${pathCount})`)
  }
})
