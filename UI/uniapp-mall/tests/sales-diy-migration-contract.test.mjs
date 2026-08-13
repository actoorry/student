import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath } from 'node:url'

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..')

const readSource = (relativePath) => readFile(path.join(repoRoot, relativePath), 'utf8')

test('mall DIY API calls use the canonical sales routes', async () => {
  const source = await readSource('UI/uniapp-mall/sheep/api/promotion/diy.js')

  for (const route of [
    '/sales/promotion/diy-template/used',
    '/sales/promotion/diy-template/get',
    '/sales/promotion/diy-page/get'
  ]) {
    assert.match(source, new RegExp(`url: '${route.replaceAll('/', '\\/')}'`))
  }
  assert.doesNotMatch(source, /url: '\/promotion\/diy-(template|page)\//)
})

test('sales DIY management guards match the canonical backend permissions', async () => {
  const [templateView, pageView] = await Promise.all([
    readSource('UI/vue3-admin/src/views/sales/promotion/diy/template/index.vue'),
    readSource('UI/vue3-admin/src/views/sales/promotion/diy/page/index.vue')
  ])

  for (const permission of ['create', 'update', 'delete', 'use']) {
    assert.match(
      templateView,
      new RegExp(`sales:sales_promotion_diy_template:${permission}`)
    )
  }
  for (const permission of ['create', 'update', 'delete']) {
    assert.match(pageView, new RegExp(`sales:sales_promotion_diy_page:${permission}`))
  }
  assert.doesNotMatch(`${templateView}\n${pageView}`, /promotion:diy-(template|page):/)
})

test('backend controllers remain the canonical sales-DIY endpoints', async () => {
  const [templateController, pageController, appTemplateController, appPageController] =
    await Promise.all([
      readSource(
        'suxin-module/src/main/java/vip/appap/suxin/module/sales/controller/admin/SalesPromotionDiyTemplateController.java'
      ),
      readSource(
        'suxin-module/src/main/java/vip/appap/suxin/module/sales/controller/admin/SalesPromotionDiyPageController.java'
      ),
      readSource(
        'suxin-module/src/main/java/vip/appap/suxin/module/sales/controller/app/AppSalesPromotionDiyTemplateController.java'
      ),
      readSource(
        'suxin-module/src/main/java/vip/appap/suxin/module/sales/controller/app/AppSalesPromotionDiyPageController.java'
      )
    ])

  for (const source of [templateController, appTemplateController]) {
    assert.match(source, /@RequestMapping\("\/sales\/promotion\/diy-template"\)/)
  }
  for (const source of [pageController, appPageController]) {
    assert.match(source, /@RequestMapping\("\/sales\/promotion\/diy-page"\)/)
  }
  assert.match(
    templateController,
    /sales:sales_promotion_diy_template:update/
  )
  assert.match(pageController, /sales:sales_promotion_diy_page:update/)
})

test('original template and independent-page semantics stay intact', async () => {
  const [templateService, editor, standalonePage, standalonePageForm] = await Promise.all([
    readSource(
      'suxin-module/src/main/java/vip/appap/suxin/module/sales/service/SalesPromotionDiyTemplateServiceImpl.java'
    ),
    readSource('UI/vue3-admin/src/views/sales/promotion/diy/template/decorate.vue'),
    readSource('UI/uniapp-mall/pages/index/page.vue'),
    readSource('UI/vue3-admin/src/views/sales/promotion/diy/page/DiyPageForm.vue')
  ])

  assert.match(templateService, /convertCreateVo\(diyTemplate\.getId\(\), "首页", remark\)/)
  assert.match(templateService, /convertCreateVo\(diyTemplate\.getId\(\), "我的", remark\)/)
  assert.match(editor, /\{ name: '基础设置', icon: 'ep:iphone' \}/)
  assert.match(editor, /\{ name: '首页', icon: 'ep:home-filled' \}/)
  assert.match(editor, /\{ name: '我的', icon: 'ep:user-filled' \}/)
  assert.doesNotMatch(editor, /\bdebugger\b/)
  assert.match(standalonePage, /let id = options\.id/)
  assert.match(standalonePage, /DiyApi\.getDiyPage\(id\)/)
  assert.doesNotMatch(standalonePageForm, /templateId/)
})

test('manual SQL migration is focused, transactional, and reversible', async () => {
  const source = await readSource(
    'openspec/changes/fix-sales-diy-migration-consistency/sql/01_sales_diy_menu_permission_migration.sql'
  )

  assert.match(source, /START TRANSACTION;/)
  assert.match(source, /-- ROLLBACK;/)
  assert.match(source, /SELECT DATABASE\(\) AS target_database/)
  assert.match(source, /system_role_menu/)
  assert.match(source, /id IN \(2437, 2438, 2439, 2440, 2441, 2443, 2444, 2445, 2446\)/)
  for (const permission of [
    'sales:sales_promotion_diy_template:query',
    'sales:sales_promotion_diy_template:create',
    'sales:sales_promotion_diy_template:update',
    'sales:sales_promotion_diy_template:delete',
    'sales:sales_promotion_diy_template:use',
    'sales:sales_promotion_diy_page:query',
    'sales:sales_promotion_diy_page:create',
    'sales:sales_promotion_diy_page:update',
    'sales:sales_promotion_diy_page:delete'
  ]) {
    assert.match(source, new RegExp(permission))
  }
  assert.doesNotMatch(source, /\b(INSERT|DELETE FROM|ALTER|DROP|TRUNCATE)\b/)
})

test('manual route and tenant access repair is exact, idempotent, and reversible', async () => {
  const source = await readSource(
    'openspec/changes/fix-sales-diy-migration-consistency/sql/02_sales_diy_route_tenant_access_repair.sql'
  )

  assert.match(source, /SET @target_database =/)
  assert.match(source, /SET @target_tenant_id = 303/)
  assert.match(source, /SET @expected_package_id = 113/)
  assert.match(source, /SET @target_parent_component_name =/)
  assert.match(source, /SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci/)
  assert.match(source, /_utf8mb4'suxin1' COLLATE utf8mb4_unicode_ci/)
  assert.match(source, /_utf8mb4'SalesDiyTemplateDirectory' COLLATE utf8mb4_unicode_ci/)
  assert.match(source, /BINARY DATABASE\(\) = BINARY @target_database/)
  assert.match(source, /UPDATE system_menu[\s\S]*id = 2435/)
  assert.match(source, /UPDATE system_tenant_package/)
  assert.match(source, /JSON_ARRAY_APPEND\(menu_ids, '\$', 2362\)/)
  assert.match(source, /JSON_ARRAY_APPEND\(menu_ids, '\$', 2446\)/)
  assert.match(source, /JSON_CONTAINS\(menu_ids, CAST\(2362 AS JSON\), '\$'\) = 0/)
  assert.match(source, /INSERT INTO system_role_menu/)
  assert.match(source, /existing\.role_id = @target_role_id/)
  assert.match(source, /existing\.menu_id = required\.menu_id/)
  assert.match(source, /SET @preflight_ok =/)
  assert.match(source, /START TRANSACTION;/)
  assert.match(source, /-- COMMIT;/)
  assert.match(source, /-- ROLLBACK;/)
  assert.match(source, /SET menu_ids = @before_package_menu_ids/)
  assert.match(source, /creator = @migration_marker/)

  for (const menuId of [
    2362, 2435, 2436, 2437, 2438, 2439, 2440, 2441, 2442, 2443, 2444, 2445, 2446
  ]) {
    assert.match(source, new RegExp(`\\b${menuId}\\b`))
  }

  assert.doesNotMatch(source, /\b(ALTER|DROP|TRUNCATE)\b/)
  assert.doesNotMatch(source, /sales_promotion_diy_(template|page)\s+(SET|VALUES)/)
})
