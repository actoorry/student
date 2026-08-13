/** 戎集汇子包路由（统一前缀 pages/rongjh） */
export const RONGJH_ROUTES = {
  warriorIndex: '/pages/rongjh/warrior/index',
  warriorApply: '/pages/rongjh/warrior/apply',
  warriorDetail: (id, videos) => {
    let url = `/pages/rongjh/warrior/detail?id=${id}`;
    if (videos) {
      url += `&videos=${encodeURIComponent(JSON.stringify(videos))}`;
    }
    return url;
  },
  foundationIndex: '/pages/rongjh/foundation/index',
  foundationApply: '/pages/rongjh/foundation/apply',
  foundationMyApplications: '/pages/rongjh/foundation/my-applications',
  foundationDetail: (id) => `/pages/rongjh/foundation/detail?id=${id}`,
  identityApply: '/pages/rongjh/identity/apply',
  zoneIndex: '/pages/rongjh/zone/index',
  zoneNamed: (name) => `/pages/rongjh/zone/index?name=${encodeURIComponent(name || '')}`,
  zoneSpecialty: '/pages/rongjh/zone/index?type=specialty&name=全国特产',
  // 军创区：直接进入商品二级列表页展示军创区商品（不再进入分类导航页 / zone 占位页）
  zoneMilitary: () => '/pages/goods/list?isMilitary=true',
  noticeIndex: '/pages/rongjh/notice/index',
  noticeDetail: (id, title) =>
    `/pages/rongjh/notice/detail?id=${id}&title=${encodeURIComponent(title || '')}`,
  aboutIndex: '/pages/rongjh/about/index',
  protocolIndex: (type) => `/pages/rongjh/protocol/index?type=${type || 'user'}`,
  debugIndex: '/pages/rongjh/debug/index',
  serviceIndex: '/pages/rongjh/service/index',
  changePasswordIndex: '/pages/rongjh/change-password/index',
  localServiceIndex: '/pages/rongjh/local-service/index',
  localServiceNamed: (name) =>
    `/pages/rongjh/local-service/index?categoryName=${encodeURIComponent(name || '同城服务')}&showLocation=true`,
};

export default RONGJH_ROUTES;
