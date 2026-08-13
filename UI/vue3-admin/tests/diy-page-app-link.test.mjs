import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { createRequire } from 'node:module'

const adminRoot = path.resolve(import.meta.dirname, '..')
const repoRoot = path.resolve(adminRoot, '..', '..')
const require = createRequire(import.meta.url)
const ts = require(path.join(adminRoot, 'node_modules', 'typescript'))

function read(relativePath) {
  return fs.readFileSync(path.join(adminRoot, relativePath), 'utf8')
}

function loadDiyPageLinkHelpers() {
  const source = read('src/components/AppLinkInput/diyPageLinks.ts')
  const output = ts.transpileModule(source, {
    compilerOptions: {
      module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2022
    }
  }).outputText
  const module = { exports: {} }
  Function('exports', 'module', output)(module.exports, module)
  return module.exports
}

test('loads every standalone page with validated 200-row pagination', async () => {
  const { DIY_PAGE_BATCH_SIZE, loadAllStandaloneDiyPages } = loadDiyPageLinkHelpers()
  const calls = []
  const allPages = Array.from({ length: 401 }, (_, index) => ({
    id: index + 1,
    name: `页面${index + 1}`
  }))

  const result = await loadAllStandaloneDiyPages(async ({ pageNo, pageSize }) => {
    calls.push({ pageNo, pageSize })
    const start = (pageNo - 1) * pageSize
    return {
      list: allPages.slice(start, start + pageSize),
      total: allPages.length
    }
  })

  assert.equal(DIY_PAGE_BATCH_SIZE, 200)
  assert.equal(result.length, 401)
  assert.deepEqual(calls, [
    { pageNo: 1, pageSize: 200 },
    { pageNo: 2, pageSize: 200 },
    { pageNo: 3, pageSize: 200 }
  ])
})

test('builds exact compiled links and excludes invalid IDs', () => {
  const { buildStandaloneDiyPageLinks } = loadDiyPageLinkHelpers()
  assert.deepEqual(
    buildStandaloneDiyPageLinks([
      { id: 31, name: '红娘服务' },
      { id: 18, name: '测试1' },
      { id: 0, name: '零' },
      { id: -1, name: '负数' },
      { id: Number.NaN, name: '非法' }
    ]),
    [
      {
        name: '红娘服务',
        path: '/pages/index/page?id=31',
        exactQuery: true
      },
      {
        name: '测试1',
        path: '/pages/index/page?id=18',
        exactQuery: true
      }
    ]
  )
})

test('selector uses a dynamic custom-page group and preserves existing special selection', () => {
  const data = read('src/components/AppLinkInput/data.ts')
  const dialog = read('src/components/AppLinkInput/AppLinkSelectDialog.vue')
  const input = read('src/components/AppLinkInput/index.vue')
  const mallPage = fs.readFileSync(
    path.join(repoRoot, 'UI/uniapp-mall/pages/index/page.vue'),
    'utf8'
  )

  assert.doesNotMatch(data, /name:\s*'自定义页面'[\s\S]{0,100}path:\s*'\/pages\/index\/page'/)
  assert.match(dialog, /name:\s*DIY_PAGE_GROUP_NAME/)
  assert.match(dialog, /loadAllStandaloneDiyPages/)
  assert.match(dialog, /buildStandaloneDiyPageLinks/)
  assert.match(dialog, /diyPageLoadFailed/)
  assert.match(dialog, /retryLoadDiyPages/)
  assert.match(dialog, /:disabled="!activeAppLink\.path"/)
  assert.match(dialog, /isSameLink\(linkItem\.path,\s*link,\s*linkItem\.exactQuery\)/)
  assert.match(dialog, /APP_LINK_TYPE_ENUM\.MARRIAGE_MEMBER_DETAIL/)
  assert.match(dialog, /handleMarriageMemberSelected/)
  assert.match(input, /const appLink = computed/)
  assert.match(mallPage, /DiyApi\.getDiyPage\(id\)/)
})
