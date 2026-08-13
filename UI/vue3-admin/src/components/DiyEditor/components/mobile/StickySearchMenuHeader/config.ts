import { cloneDeep } from 'lodash-es'
import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'
import {
  component as SEARCH_BAR_COMPONENT,
  SearchProperty
} from '@/components/DiyEditor/components/mobile/SearchBar/config'
import {
  component as MENU_TEXT_GRID_COMPONENT,
  MenuTextGridProperty
} from '@/components/DiyEditor/components/mobile/MenuTextGrid/config'

export interface StickySearchMenuHeaderBackgroundProperty {
  image: string
  color: string
  /** 背景展示高度，单位 rpx */
  height: number
}

export interface StickySearchMenuHeaderSpacingProperty {
  /** 搜索栏与文字导航之间的间距，单位 rpx */
  searchMenuGap: number
  /** 文字导航与头部底边之间的间距，单位 rpx */
  menuBottomGap: number
}

/** 吸顶搜索导航头部属性 */
export interface StickySearchMenuHeaderProperty {
  sticky?: boolean
  background: StickySearchMenuHeaderBackgroundProperty
  spacing: StickySearchMenuHeaderSpacingProperty
  search: SearchProperty
  menu: MenuTextGridProperty
  style: ComponentStyle
}

const search = cloneDeep(SEARCH_BAR_COMPONENT.property)
search.borderRadius = 16
search.placeholderPosition = 'center'
search.backgroundColor = '#ffffff'
search.style = {
  ...search.style,
  bgColor: 'transparent',
  paddingTop: 8,
  paddingRight: 12,
  paddingBottom: 0,
  paddingLeft: 12,
  marginBottom: 0
}

const menu = cloneDeep(MENU_TEXT_GRID_COMPONENT.property)
menu.scrollable = true
menu.row = 1
menu.style = {
  ...menu.style,
  bgColor: 'transparent',
  paddingTop: 0,
  paddingRight: 0,
  paddingBottom: 0,
  paddingLeft: 0,
  marginBottom: 0
}

export const component = {
  id: 'StickySearchMenuHeader',
  name: '搜索导航头部',
  icon: 'tabler:layout-navbar-expand',
  layout: 'sticky',
  property: {
    sticky: true,
    background: {
      image: '',
      color: '#e60012',
      height: 360
    },
    spacing: {
      searchMenuGap: 0,
      menuBottomGap: 0
    },
    search,
    menu,
    style: {
      bgType: 'color',
      bgColor: 'transparent',
      marginTop: 0,
      marginRight: 0,
      marginBottom: 0,
      marginLeft: 0,
      paddingTop: 0,
      paddingRight: 0,
      paddingBottom: 0,
      paddingLeft: 0,
      borderTopLeftRadius: 0,
      borderTopRightRadius: 0,
      borderBottomRightRadius: 0,
      borderBottomLeftRadius: 0
    } as ComponentStyle
  }
} as DiyComponent<StickySearchMenuHeaderProperty>
