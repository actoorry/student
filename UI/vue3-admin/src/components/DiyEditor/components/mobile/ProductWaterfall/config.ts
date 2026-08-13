import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'

/** 商品字段 */
export interface ProductWaterfallFieldProperty {
  show: boolean
  color: string
}

/** 商品瀑布流属性 */
export interface ProductWaterfallProperty {
  fields: {
    name: ProductWaterfallFieldProperty
    introduction: ProductWaterfallFieldProperty
    price: ProductWaterfallFieldProperty
    marketPrice: ProductWaterfallFieldProperty
    salesCount: ProductWaterfallFieldProperty
    stock: ProductWaterfallFieldProperty
  }
  badge: {
    show: boolean
    imgUrl: string
  }
  btnBuy: {
    type: 'text' | 'img'
    text: string
    bgBeginColor: string
    bgEndColor: string
    imgUrl: string
  }
  borderRadiusTop: number
  borderRadiusBottom: number
  space: number
  /** 每次加载商品数量 */
  pageSize: number
  /** 自动获取规则 */
  rule: {
    categorySales?: number
    sortField?: '' | 'price' | 'salesCount' | 'createTime'
    sortAsc?: boolean
    keyword?: string
  }
  style: ComponentStyle
}

export const component = {
  id: 'ProductWaterfall',
  name: '商品瀑布流',
  icon: 'fluent:text-column-two-24-filled',
  property: {
    fields: {
      name: { show: true, color: '#000' },
      introduction: { show: true, color: '#999' },
      price: { show: true, color: '#ff3000' },
      marketPrice: { show: true, color: '#c4c4c4' },
      salesCount: { show: true, color: '#c4c4c4' },
      stock: { show: false, color: '#c4c4c4' }
    },
    badge: { show: false, imgUrl: '' },
    btnBuy: {
      type: 'text',
      text: '立即购买',
      bgBeginColor: '#FF6000',
      bgEndColor: '#FE832A',
      imgUrl: ''
    },
    borderRadiusTop: 6,
    borderRadiusBottom: 6,
    space: 8,
    pageSize: 10,
    rule: {
      categorySales: undefined,
      sortField: '',
      sortAsc: false,
      keyword: ''
    },
    style: {
      bgType: 'color',
      bgColor: '',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8
    } as ComponentStyle
  }
} as DiyComponent<ProductWaterfallProperty>
