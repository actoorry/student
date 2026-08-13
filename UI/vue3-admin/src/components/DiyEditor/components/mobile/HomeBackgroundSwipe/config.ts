import { ComponentStyle, DiyComponent } from '@/components/DiyEditor/util'
import { cloneDeep } from 'lodash-es'

/** 首页滑动场景项 */
export interface HomeBackgroundSwipeItemProperty {
  // 背景颜色
  backgroundColor: string
  // 背景图片
  backgroundImage: string
  // 前景图片
  foregroundImage: string
  // 标题
  title: string
  // 副标题
  subtitle: string
  // 标签列表
  tags: string[]
}

/** 首页滑动场景属性 */
export interface HomeBackgroundSwipeProperty {
  // 组件高度（rpx）
  height: number
  // 左侧相邻场景露出宽度（rpx）
  previousMargin: number
  // 右侧相邻场景露出宽度（rpx）
  nextMargin: number
  // 场景列表
  slides: HomeBackgroundSwipeItemProperty[]
  // 初始索引
  initialIndex: number
  // 指示器样式：点 | 数字
  indicator: 'dot' | 'number'
  // 组件样式
  style: ComponentStyle
}

// 空场景项默认属性
export const EMPTY_HOME_BACKGROUND_SWIPE_ITEM_PROPERTY = {
  backgroundColor: '#F6D7DF',
  backgroundImage: '',
  foregroundImage: '',
  title: '遇见心动的人',
  subtitle: '认真交友，从一次了解开始',
  tags: ['实名认证', '同城']
} as HomeBackgroundSwipeItemProperty

// 定义组件
export const component = {
  id: 'HomeBackgroundSwipe',
  name: '静态场景轮播（历史兼容）',
  icon: 'fluent:phone-home-button-24-regular',
  property: {
    height: 960,
    previousMargin: 24,
    nextMargin: 24,
    slides: [
      cloneDeep(EMPTY_HOME_BACKGROUND_SWIPE_ITEM_PROPERTY),
      {
        ...cloneDeep(EMPTY_HOME_BACKGROUND_SWIPE_ITEM_PROPERTY),
        backgroundColor: '#D7E6F6',
        title: '发现同频的人',
        subtitle: '兴趣相投，话题不断'
      }
    ] as HomeBackgroundSwipeItemProperty[],
    initialIndex: 0,
    indicator: 'dot',
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 0
    } as ComponentStyle
  }
} as DiyComponent<HomeBackgroundSwipeProperty>
