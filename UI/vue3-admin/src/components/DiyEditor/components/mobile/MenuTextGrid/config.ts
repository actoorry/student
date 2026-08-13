import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'
import { cloneDeep } from 'lodash-es'

/** 文字分类导航区属性 */
export interface MenuTextGridProperty {
  // 是否单行横向滑动
  scrollable?: boolean
  // 行数
  row: number
  // 列数（横向滑动时表示每屏显示数量）
  column: number
  // 文字颜色（全局默认）
  textColor: string
  // 文字大小（px）
  fontSize: number
  // 导航菜单列表
  list: MenuTextGridItemProperty[]
  // 组件样式
  style: ComponentStyle
}

/** 文字分类导航区项目属性 */
export interface MenuTextGridItemProperty {
  // 标题
  title: string
  // 标题颜色（为空时使用全局 textColor）
  titleColor: string
  // 链接
  url: string
}

export const EMPTY_MENU_TEXT_GRID_ITEM_PROPERTY = {
  title: '导航文字',
  titleColor: '',
  url: ''
} as MenuTextGridItemProperty

const DEFAULT_MENU_ITEMS: MenuTextGridItemProperty[] = [
  { title: '其他更多', titleColor: '', url: '' },
  { title: '食品保健', titleColor: '', url: '' },
  { title: '酒汇天下', titleColor: '', url: '' },
  { title: '鞋服箱包', titleColor: '', url: '' },
  { title: '个人洗护', titleColor: '', url: '' },
  { title: '家居日用品', titleColor: '', url: '' },
  { title: '办公文具', titleColor: '', url: '' },
  { title: '生鲜水果', titleColor: '', url: '' },
  { title: '家用电器', titleColor: '', url: '' },
  { title: '米面粮油', titleColor: '', url: '' }
]

// 定义组件
export const component = {
  id: 'MenuTextGrid',
  name: '文字分类导航区',
  icon: 'ep:grid',
  property: {
    scrollable: false,
    row: 2,
    column: 5,
    textColor: '#ffffff',
    fontSize: 13,
    list: DEFAULT_MENU_ITEMS.map((item) => cloneDeep(item)),
    style: {
      bgType: 'color',
      bgColor: '#e60012',
      marginBottom: 0,
      paddingTop: 12,
      paddingBottom: 12,
      paddingLeft: 8,
      paddingRight: 8
    } as ComponentStyle
  }
} as DiyComponent<MenuTextGridProperty>
