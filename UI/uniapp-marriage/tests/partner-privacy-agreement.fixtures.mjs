export const PARTNER_PRIVACY_AGREEMENT_FIXTURES = Object.freeze({
  original: Object.freeze({
    entryTitle: '隐私协议', entryIcon: 'LOCK', detailTitle: '隐私协议',
    detailSubtitle: '我们会按照必要、正当、透明的原则保护你的个人信息。',
    sections: Object.freeze([
      Object.freeze({ title: '信息收集', content: '为提供账号登录、资料展示、匹配推荐、订单支付、活动报名和客户服务，我们会根据你使用的功能收集必要的信息。' }),
      Object.freeze({ title: '信息使用', content: '我们仅在实现产品功能、保障账号安全、优化服务体验、履行法律法规要求的范围内使用相关信息。' }),
      Object.freeze({ title: '信息共享', content: '未经你的授权，我们不会向无关第三方共享你的个人信息。涉及支付、消息通知等必要服务时，会按照最小必要原则提供所需信息。' }),
      Object.freeze({ title: '信息保护', content: '我们会采取访问控制、传输加密、权限隔离等措施保护你的信息安全，并持续改进安全管理能力。' }),
      Object.freeze({ title: '用户权利', content: '你可以在应用内查看和修改个人资料，也可以通过客服渠道申请查询、更正或删除相关个人信息。' }),
    ]),
  }),
  style: Object.freeze({ bgType: 'color', bgColor: '', bgImg: '', margin: 0, marginTop: 0, marginRight: 0, marginBottom: 0, marginLeft: 0, padding: 0, paddingTop: 0, paddingRight: 0, paddingBottom: 0, paddingLeft: 0, borderRadius: 0, borderTopLeftRadius: 0, borderTopRightRadius: 0, borderBottomRightRadius: 0, borderBottomLeftRadius: 0 }),
  routes: Object.freeze([
    Object.freeze({ query: { source: 'template', id: '12', slot: 'home', index: '0' }, url: '/pages/public/privacy-agreement?source=template&id=12&slot=home&index=0' }),
    Object.freeze({ query: { source: 'page', id: '9', index: '2' }, url: '/pages/public/privacy-agreement?source=page&id=9&index=2' }),
  ]),
})
