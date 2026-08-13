import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const adminRoot = resolve(import.meta.dirname, '..')
const repoRoot = resolve(adminRoot, '..', '..')
const readAdmin = (file) => readFileSync(resolve(adminRoot, file), 'utf8')
const readRepo = (file) => readFileSync(resolve(repoRoot, file), 'utf8')

const componentLibraries = readAdmin('src/components/DiyEditor/util.ts')
const decorate = readAdmin('src/views/sales/promotion/diy/template/decorate.vue')
const editor = readAdmin('src/components/DiyEditor/index.vue')
const library = readAdmin('src/components/DiyEditor/components/ComponentLibrary.vue')
const appStore = readRepo('UI/uniapp-mall/sheep/store/app.js')
const rendererRegistry = readRepo('UI/uniapp-mall/sheep/components/s-block-item/shared-diy-renderers.js')

const retiredComponentIds = [
  'MarriageLoginPrompt',
  'MarriageRecommendProfileDeck',
  'MarriageIdentityTrustBanner'
]

assert.doesNotMatch(componentLibraries, /特色组件/)
for (const componentId of retiredComponentIds) {
  assert.doesNotMatch(componentLibraries, new RegExp(componentId))
  assert.doesNotMatch(rendererRegistry, new RegExp(`import\\s+${componentId}\\s+from`))
  assert.equal(
    existsSync(resolve(adminRoot, `src/components/DiyEditor/components/mobile/${componentId}`)),
    false
  )
  assert.equal(
    existsSync(resolve(adminRoot, `../uniapp-mall/sheep/components/${componentId
      .replace(/([a-z])([A-Z])/g, '$1-$2')
      .toLowerCase()}`)),
    false
  )
}

assert.doesNotMatch(decorate, /TenantApi|productCode|productPolicy|getSocialComponentLibraries|marriageDiyContract/)
assert.doesNotMatch(editor, /allowedComponentIds/)
assert.doesNotMatch(library, /allowedComponentIds/)
assert.match(rendererRegistry, /getSharedDiyRenderer/)
assert.match(rendererRegistry, /isKnownDiyComponent/)
assert.doesNotMatch(appStore, /resolveProductStartup|validateActiveTemplate/)
assert.equal(existsSync(resolve(adminRoot, '../uniapp-mall/product-foundation')), false)
assert.equal(existsSync(resolve(adminRoot, '../uniapp-mall/product-scopes')), false)

// Retired persisted blocks must remain in the editor model. Otherwise the deep
// model watcher drops them while the page loads and immediately rewrites the
// page config, which makes historical pages impossible to edit safely.
assert.equal(
  existsSync(resolve(adminRoot, 'src/components/DiyEditor/components/RetiredDiyComponent.vue')),
  true
)
assert.match(editor, /RETIRED_EDITOR_COMPONENT_IDS/)
assert.match(editor, /createRetiredDiyComponent/)
for (const componentId of retiredComponentIds) {
  assert.match(editor, new RegExp(componentId))
  assert.match(rendererRegistry, new RegExp(componentId))
}
assert.match(
  editor,
  /props\.componentDefinitions\[item\.id\]\s*\|\|\s*componentConfigs\[item\.id\]\s*\|\|\s*createRetiredDiyComponent\(item\)/
)
assert.match(editor, /preview:\s*markRaw\(RetiredDiyComponent\)/)
assert.match(editor, /propertyPanel:\s*markRaw\(RetiredDiyComponent\)/)
