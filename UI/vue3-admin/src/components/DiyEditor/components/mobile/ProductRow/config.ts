import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 商品双列 - 单行展示数量 */
export const PRODUCT_ROW_DISPLAY_COUNT = 2

/** 商品字段 */
export interface ProductRowFieldProperty {
  show: boolean
  color: string
}

/** 商品双列属性 */
export interface ProductRowProperty {
  // 主标题
  title: string
  titleColor: string
  titleSize: number
  // 副标题
  description: string
  descriptionColor: string
  descriptionSize: number
  // 展开更多
  more: {
    show: boolean
    type: 'text' | 'icon' | 'all'
    text: string
    url: string
  }
  // 数据来源：手工选品 | 规则获取
  dataSource: 'manual' | 'rule'
  // 手工选品
  spuIds: number[]
  // 规则选品
  rule: {
    categorySales?: number
    sortField?: '' | 'price' | 'salesCount' | 'createTime'
    sortAsc?: boolean
    keyword?: string
  }
  // 商品字段
  fields: {
    name: ProductRowFieldProperty
    price: ProductRowFieldProperty
  }
  // 角标
  badge: {
    show: boolean
    imgUrl: string
  }
  borderRadiusTop: number
  borderRadiusBottom: number
  space: number
  style: ComponentStyle
}

export const component = {
  id: 'ProductRow',
  name: '商品双列',
  icon: 'fluent:text-column-two-24-filled',
  property: {
    title: '精选推荐',
    titleColor: '#323233',
    titleSize: 16,
    description: '',
    descriptionColor: '#969799',
    descriptionSize: 12,
    more: {
      show: true,
      type: 'all',
      text: '展开更多',
      url: ''
    },
    dataSource: 'rule',
    spuIds: [],
    rule: {
      categorySales: undefined,
      sortField: '',
      sortAsc: false,
      keyword: ''
    },
    fields: {
      name: { show: true, color: '#000' },
      price: { show: true, color: '#ff3000' }
    },
    badge: { show: false, imgUrl: '' },
    borderRadiusTop: 8,
    borderRadiusBottom: 8,
    space: 8,
    style: {
      bgType: 'color',
      bgColor: '',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8
    } as ComponentStyle
  }
} as DiyComponent<ProductRowProperty>
