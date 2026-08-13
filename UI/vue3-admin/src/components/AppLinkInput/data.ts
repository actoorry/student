// APP 链接分组
export interface AppLinkGroup {
  // 分组名称
  name: string
  // 链接列表
  links: AppLink[]
}

// APP 链接
export interface AppLink {
  // 链接名称
  name: string
  // 链接地址
  path: string
  // 链接的类型
  type?: APP_LINK_TYPE_ENUM
  // 固定查询参数也属于链接身份时，选择器必须按完整路径匹配
  exactQuery?: boolean
}

// APP 链接类型（需要特殊处理，例如商品详情）
export const enum APP_LINK_TYPE_ENUM {
  // 拼团活动
  ACTIVITY_COMBINATION,
  // 秒杀活动
  ACTIVITY_SECKILL,
  // 积分商城活动
  ACTIVITY_POINT,
  // 文章详情
  ARTICLE_DETAIL,
  // 优惠券详情
  COUPON_DETAIL,
  // 自定义页面详情
  DIY_PAGE_DETAIL,
  // 品类列表
  PRODUCT_CATEGORY_LIST,
  // 商品列表
  PRODUCT_LIST,
  // 商品详情
  PRODUCT_DETAIL_NORMAL,
  // 拼团商品详情
  PRODUCT_DETAIL_COMBINATION,
  // 秒杀商品详情
  PRODUCT_DETAIL_SECKILL,
  // 处佳缘会员资料详情
  MARRIAGE_MEMBER_DETAIL
}

// APP 链接列表（做一下持久化？）
export const APP_LINK_GROUP_LIST = [
  {
    name: '商城',
    links: [
      {
        name: '首页',
        path: '/pages/index/index'
      },
      // 暂时隐藏商品分类快捷入口，保留原代码与类型处理以便后续恢复。
      // {
      //   name: '商品分类',
      //   path: '/pages/index/category',
      //   type: APP_LINK_TYPE_ENUM.PRODUCT_CATEGORY_LIST
      // },
      {
        name: '购物车',
        path: '/pages/index/cart'
      },
      {
        name: '个人中心',
        path: '/pages/index/user'
      },
      {
        name: '商品搜索',
        path: '/pages/index/search'
      },
      {
        name: '客服',
        path: '/pages/chat/index'
      },
      {
        name: '系统设置',
        path: '/pages/public/setting'
      },
      {
        name: '常见问题',
        path: '/pages/public/faq'
      }
    ]
  },
  {
    name: '商城分销',
    links: [
      { name: '商城佣金首页', path: '/pages/commission/index' },
      { name: '商城佣金钱包', path: '/pages/commission/wallet' },
      { name: '商城佣金提现', path: '/pages/commission/withdraw' },
      { name: '商城推广团队', path: '/pages/commission/team' },
      { name: '商城推广订单', path: '/pages/commission/order' },
      { name: '商城分销商品', path: '/pages/commission/goods' },
      { name: '商城申请推广员', path: '/pages/commission/promoter' },
      { name: '商城佣金排行', path: '/pages/commission/commission-ranking' }
    ]
  },
  {
    name: '处佳缘',
    links: [
      { name: '动态', path: '/pages/dynamics/index' },
      { name: '我的动态', path: '/pages/mine-dynamics/index' },
      { name: '资料编辑页', path: '/pages/mine-profile/basic/index' },
      { name: '我的认证', path: '/pages/mine-certifications/index' },
      { name: '我的签到', path: '/pages/mine-checkin/index' },
      { name: '我的积分', path: '/pages/mine-points/index' },
      { name: '消息', path: '/pages/messages/index' },
      { name: '我看过的', path: '/pages/mine-view/index?mode=my-view', exactQuery: true },
      { name: '看过我的', path: '/pages/mine-view/index?mode=view-me', exactQuery: true },
      { name: '我关注的', path: '/pages/mine-follow/index?type=following', exactQuery: true },
      { name: '关注我的', path: '/pages/mine-follow/index?type=followers', exactQuery: true },
      {
        name: '资料详情页',
        path: '/pages/member-detail/index',
        type: APP_LINK_TYPE_ENUM.MARRIAGE_MEMBER_DETAIL
      }
    ]
  },
  {
    name: '商品',
    links: [
      {
        name: '商品列表',
        path: '/pages/goods/list',
        type: APP_LINK_TYPE_ENUM.PRODUCT_LIST
      },
      {
        name: '热卖榜单',
        path: '/pages/goods/sales-rank'
      },
      {
        name: '新品优先',
        path: '/pages/goods/new-rank'
      },
      {
        name: '商品详情',
        path: '/pages/goods/index',
        type: APP_LINK_TYPE_ENUM.PRODUCT_DETAIL_NORMAL
      },
      {
        name: '拼团商品详情',
        path: '/pages/goods/groupon',
        type: APP_LINK_TYPE_ENUM.PRODUCT_DETAIL_COMBINATION
      },
      {
        name: '秒杀商品详情',
        path: '/pages/goods/seckill',
        type: APP_LINK_TYPE_ENUM.PRODUCT_DETAIL_SECKILL
      }
    ]
  },
  {
    name: '营销活动',
    links: [
      {
        name: '拼团订单',
        path: '/pages/activity/groupon/order'
      },
      {
        name: '营销商品',
        path: '/pages/activity/index'
      },
      {
        name: '拼团活动',
        path: '/pages/activity/groupon/list',
        type: APP_LINK_TYPE_ENUM.ACTIVITY_COMBINATION
      },
      {
        name: '秒杀活动',
        path: '/pages/activity/seckill/list',
        type: APP_LINK_TYPE_ENUM.ACTIVITY_SECKILL
      },
      {
        name: '积分商城活动',
        path: '/pages/activity/point/list',
        type: APP_LINK_TYPE_ENUM.ACTIVITY_POINT
      },
      {
        name: '签到中心',
        path: '/pages/app/sign'
      },
      {
        name: '优惠券中心',
        path: '/pages/coupon/list'
      },
      {
        name: '优惠券详情',
        path: '/pages/coupon/detail',
        type: APP_LINK_TYPE_ENUM.COUPON_DETAIL
      },
      {
        name: '文章详情',
        path: '/pages/public/richtext',
        type: APP_LINK_TYPE_ENUM.ARTICLE_DETAIL
      }
    ]
  },
  {
    name: '戎集汇',
    links: [
      {
        name: '调试入口',
        path: '/pages/rongjh/debug/index'
      },
      {
        name: '战友会',
        path: '/pages/rongjh/warrior/index'
      },
      {
        name: '入会申请',
        path: '/pages/rongjh/warrior/apply'
      },
      {
        name: '身份认证申请',
        path: '/pages/rongjh/identity/apply'
      },
      {
        name: '爱心帮扶',
        path: '/pages/rongjh/foundation/index'
      },
      {
        name: '帮扶申请',
        path: '/pages/rongjh/foundation/apply'
      },
      {
        name: '专区',
        path: '/pages/rongjh/zone/index'
      },
      {
        name: '专区 · 礼品专区',
        path: '/pages/rongjh/zone/index?name=礼品专区'
      },
      {
        name: '全国特产',
        path: '/pages/rongjh/zone/index?type=specialty&name=全国特产'
      },
      {
        name: '军创区',
        path: '/pages/rongjh/zone/index?type=military&name=军创区'
      },
      {
        name: '戎公告',
        path: '/pages/rongjh/notice/index'
      },
      {
        name: '关于我们',
        path: '/pages/rongjh/about/index'
      },
      {
        name: '用户协议',
        path: '/pages/rongjh/protocol/index?type=user'
      },
      {
        name: '隐私政策',
        path: '/pages/rongjh/protocol/index?type=privacy'
      },
      {
        name: '联系我们',
        path: '/pages/rongjh/service/index'
      },
      {
        name: '修改密码',
        path: '/pages/rongjh/change-password/index'
      },
      {
        name: '同城特产',
        path: '/pages/rongjh/local-service/index'
      }
    ]
  },
  {
    name: '支付',
    links: [
      {
        name: '充值余额',
        path: '/pages/pay/recharge'
      },
      {
        name: '充值记录',
        path: '/pages/pay/recharge-log'
      }
    ]
  },
  {
    name: '用户中心',
    links: [
      {
        name: '用户信息',
        path: '/pages/user/info'
      },
      {
        name: '用户订单',
        path: '/pages/order/list'
      },
      {
        name: '售后订单',
        path: '/pages/order/aftersale/list'
      },
      {
        name: '商品收藏',
        path: '/pages/user/goods-collect'
      },
      {
        name: '浏览记录',
        path: '/pages/user/goods-log'
      },
      {
        name: '地址管理',
        path: '/pages/user/address/list'
      },
      {
        name: '用户佣金',
        path: '/pages/user/wallet/commission'
      },
      {
        name: '用户余额',
        path: '/pages/user/wallet/money'
      },
      {
        name: '用户积分',
        path: '/pages/user/wallet/score'
      }
    ]
  }
] as AppLinkGroup[]
