import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { describe, it } from 'node:test'
import { fileURLToPath } from 'node:url'

const root = dirname(fileURLToPath(import.meta.url))
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf8')

describe('StickySearchMenuHeader admin contract', () => {
  const navigationConfig = read(
    '../src/components/DiyEditor/components/mobile/NavigationBar/config.ts'
  )
  const navigationPreview = read(
    '../src/components/DiyEditor/components/mobile/NavigationBar/index.vue'
  )
  const navigationProperty = read(
    '../src/components/DiyEditor/components/mobile/NavigationBar/property.vue'
  )
  const config = read(
    '../src/components/DiyEditor/components/mobile/StickySearchMenuHeader/config.ts'
  )
  const preview = read(
    '../src/components/DiyEditor/components/mobile/StickySearchMenuHeader/index.vue'
  )
  const menuPreview = read('../src/components/DiyEditor/components/mobile/MenuTextGrid/index.vue')
  const propertyPanel = read(
    '../src/components/DiyEditor/components/mobile/StickySearchMenuHeader/property.vue'
  )
  const editor = read('../src/components/DiyEditor/index.vue')
  const util = read('../src/components/DiyEditor/util.ts')

  it('keeps navigation visible by default and treats only false as disabled', () => {
    assert.match(navigationConfig, /enabled\?:\s*boolean/)
    assert.match(navigationConfig, /enabled:\s*true/)
    assert.match(navigationPreview, /property\.enabled\s*===\s*false/)
    assert.match(navigationPreview, /顶部导航栏已关闭/)
    assert.match(navigationProperty, /formData\.enabled\s*!==\s*false/)
    assert.match(navigationProperty, /formData\.enabled\s*=\s*Boolean\(\$event\)/)
  })

  it('registers one code-owned sticky component with typed nested properties', () => {
    assert.match(util, /'StickySearchMenuHeader'/)
    assert.match(config, /id:\s*'StickySearchMenuHeader'/)
    assert.match(config, /name:\s*'搜索导航头部'/)
    assert.match(config, /layout:\s*'sticky'/)
    assert.match(config, /sticky\?:\s*boolean/)
    assert.match(config, /sticky:\s*true/)
    assert.match(config, /spacing:\s*StickySearchMenuHeaderSpacingProperty/)
    assert.match(config, /search:\s*SearchProperty/)
    assert.match(config, /menu:\s*MenuTextGridProperty/)
    assert.match(config, /menu\.scrollable\s*=\s*true/)
    assert.match(config, /menu\.row\s*=\s*1/)
  })

  it('reuses existing SearchBar and MenuTextGrid previews', () => {
    assert.match(preview, /SearchBar\s+from[^\n]*SearchBar\/index\.vue/)
    assert.match(preview, /MenuTextGrid\s+from[^\n]*MenuTextGrid\/index\.vue/)
    assert.match(preview, /<SearchBar\s+:property="searchProperty"/)
    assert.match(preview, /<MenuTextGrid\s+:property="menuProperty"/)
    assert.match(preview, /vertical-align="bottom"/)
    assert.match(preview, /vertical-align="bottom"\s+compact/)
    assert.match(preview, /scrollable:\s*true/)
    assert.match(preview, /row:\s*1/)
  })

  it('exposes fixed horizontal menu settings without arbitrary child selection', () => {
    assert.match(propertyPanel, /label="每屏显示"/)
    assert.match(propertyPanel, /formData\.value\.menu\.scrollable\s*=\s*true/)
    assert.match(propertyPanel, /formData\.value\.menu\.row\s*=\s*1/)
    assert.doesNotMatch(propertyPanel, /label="横向滑动"/)
    assert.doesNotMatch(propertyPanel, /label="行数"/)
    assert.doesNotMatch(propertyPanel, /componentId|componentPath|importPath/)
  })

  it('uses visible rpx sliders for fixed height and both bottom-content gaps', () => {
    assert.match(config, /searchMenuGap:\s*number/)
    assert.match(config, /menuBottomGap:\s*number/)
    assert.match(config, /searchMenuGap:\s*0/)
    assert.match(config, /menuBottomGap:\s*0/)
    assert.match(propertyPanel, /label="展示高度"[\s\S]*formData\.background\.height/)
    assert.match(propertyPanel, /\{\{\s*formData\.background\.height\s*\}\}\s*rpx/)
    assert.match(propertyPanel, /label="搜索导航间距"[\s\S]*formData\.spacing\.searchMenuGap/)
    assert.match(propertyPanel, /label="导航底部间距"[\s\S]*formData\.spacing\.menuBottomGap/)
    assert.doesNotMatch(propertyPanel, /label="垂直位置"|search\.offsetTop/)
    assert.match(preview, /height:\s*`\$\{Math\.max\(background\.value\.height,\s*320\)/)
    assert.doesNotMatch(preview, /minHeight|searchOffsetTop/)
    assert.match(preview, /class="bottom-content"/)
    assert.match(preview, /\.brand-spacer\s*\{[^}]*flex:\s*1\s+1\s+auto/s)
    assert.match(preview, /paddingBottom:\s*'0px'/)
    assert.match(preview, /paddingTop:\s*'0px'/)
    assert.match(menuPreview, /verticalAlign\?:\s*'center'\s*\|\s*'bottom'/)
    assert.match(menuPreview, /verticalAlign:\s*'center'/)
    assert.match(menuPreview, /compact\?:\s*boolean/)
    assert.match(menuPreview, /compact:\s*false/)
    assert.match(menuPreview, /props\.compact[\s\S]*COMPACT_ROW_PADDING/)
    assert.match(menuPreview, /menu-text-grid-bottom-align/)
    assert.match(menuPreview, /align-items:\s*flex-end\s*!important/)
    assert.match(propertyPanel, /label="是否吸顶"/)
    assert.match(propertyPanel, /formData\.sticky\s*!==\s*false/)
    assert.match(propertyPanel, /formData\.sticky\s*=\s*Boolean\(\$event\)/)
  })

  it('applies sticky layout outside the preview and persists only id/property', () => {
    assert.match(util, /layout\?:\s*'sticky'/)
    assert.match(
      editor,
      /element\.layout\s*===\s*'sticky'\s*&&\s*element\.property\?\.sticky\s*!==\s*false/
    )
    assert.match(editor, /\.sticky-component-container\s*\{[^}]*position:\s*sticky/s)
    assert.match(
      editor,
      /return\s*\{\s*id:\s*component\.id,\s*property:\s*component\.property\s*\}/
    )
    assert.doesNotMatch(editor, /return\s*\{[^}]*layout:\s*component\.layout/s)
  })
})
