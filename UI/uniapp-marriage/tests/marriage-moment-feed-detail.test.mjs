import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { describe, it, test } from 'node:test';

import {
  avatarText,
  formatMomentCount,
  mergeMomentChange,
  momentLayout,
  normalizeImageUrls,
  normalizeMomentItem,
  positiveId,
  validateMomentSnapshot,
} from '../sheep/helper/marriage-moment.js';

const root = dirname(fileURLToPath(import.meta.url));
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf8');

// ---------- 1.2 纯函数契约：规范化、ID、布局、回传字段 ----------

test('positiveId 仅接受可安全表达的正整数', () => {
  assert.equal(positiveId(10), 10);
  assert.equal(positiveId('10'), 10);
  assert.equal(positiveId(0), 0);
  assert.equal(positiveId(-1), 0);
  assert.equal(positiveId('abc'), 0);
  assert.equal(positiveId(NaN), 0);
  assert.equal(positiveId(1.5), 0);
  assert.equal(positiveId(undefined), 0);
  assert.equal(positiveId(null), 0);
});

test('normalizeMomentItem 规范化确定字段并保留未知字段', () => {
  const source = {
    id: '7',
    partnerId: 3,
    nickname: '  林薇  ',
    avatar: ' a ',
    verifiedLabel: '实名',
    content: '正文',
    imageUrls: [' x ', '', 'x', 'y', 1, 'z', 'z'],
    liked: 1,
    likeCount: '-2',
    commentCount: '5',
    followed: 0,
    mine: 1,
    publishTime: '2026-08-01T00:00:00Z',
    future: 'kept',
  };
  const item = normalizeMomentItem(source);
  assert.equal(item.id, 7);
  assert.equal(item.partnerId, 3);
  assert.equal(item.nickname, '林薇');
  assert.equal(item.avatar, 'a');
  assert.equal(item.verifiedLabel, '实名');
  assert.equal(item.content, '正文');
  assert.deepEqual(item.imageUrls, ['x', 'y', 'z']);
  assert.equal(item.liked, true);
  assert.equal(item.likeCount, 0);
  assert.equal(item.commentCount, 5);
  assert.equal(item.followed, false);
  assert.equal(item.mine, true);
  assert.equal(item.future, 'kept');

  const empty = normalizeMomentItem({});
  assert.equal(empty.id, 0);
  assert.equal(empty.nickname, '处佳缘用户');
  assert.deepEqual(empty.imageUrls, []);
  assert.equal(empty.liked, false);
  assert.equal(empty.likeCount, 0);
  assert.equal(empty.mine, false);
});

test('normalizeImageUrls 去空白、去重并截断到 9 张', () => {
  const urls = Array.from({ length: 12 }, (_, i) => `img${i}`);
  assert.equal(normalizeImageUrls(urls).length, 9);
  assert.deepEqual(normalizeImageUrls([' a ', 'a', '', 'b']), ['a', 'b']);
  assert.deepEqual(normalizeImageUrls('not-array'), []);
  assert.deepEqual(normalizeImageUrls([1, null, '']), []);
});

test('momentLayout 固定 1 / 2–4 / 5–9 图布局', () => {
  assert.equal(momentLayout(1).columns, 1);
  assert.equal(momentLayout(2).columns, 2);
  assert.equal(momentLayout(3).columns, 2);
  assert.equal(momentLayout(4).columns, 2);
  assert.equal(momentLayout(5).columns, 3);
  assert.equal(momentLayout(6).columns, 3);
  assert.equal(momentLayout(9).columns, 3);
  assert.equal(momentLayout(0).columns, 0);
  assert.equal(momentLayout(-1).columns, 0);
  assert.equal(momentLayout(99).columns, 3);
  assert.equal(momentLayout('4').columns, 2);
  assert.equal(momentLayout(1).className, 'moment-images--single');
  assert.equal(momentLayout(3).className, 'moment-images--pair');
  assert.equal(momentLayout(9).className, 'moment-images--grid');
});

test('mergeMomentChange 只按 ID 合并展示字段，removed 按 ID 移除', () => {
  const list = [
    { id: 1, nickname: '甲', content: 'A', liked: false, likeCount: 1, commentCount: 2 },
    { id: 2, nickname: '乙', content: 'B', liked: true, likeCount: 5, commentCount: 3 },
  ];
  const merged = mergeMomentChange(list, { id: 2, liked: false, likeCount: 6, commentCount: 4 });
  assert.deepEqual(merged, [
    list[0],
    { id: 2, nickname: '乙', content: 'B', liked: false, likeCount: 6, commentCount: 4 },
  ]);
  // 未变化项保持原引用
  assert.equal(merged[0], list[0]);

  // 作者/正文/媒体字段不被回传覆盖
  const partial = mergeMomentChange(list, { id: 1, liked: true, nickname: '篡改', content: 'X' });
  assert.equal(partial[0].nickname, '甲');
  assert.equal(partial[0].content, 'A');
  assert.equal(partial[0].liked, true);

  // 无匹配 / 无效 ID 时返回原数组引用
  assert.equal(mergeMomentChange(list, { id: 99, liked: true }), list);
  assert.equal(mergeMomentChange(list, { id: 0, liked: true }), list);
  assert.equal(mergeMomentChange(list, null), list);

  // removed 按 ID 移除，目标不存在时安全忽略
  assert.deepEqual(mergeMomentChange(list, { id: 1, removed: true }), [list[1]]);
  assert.equal(mergeMomentChange(list, { id: 99, removed: true }), list);
});

test('validateMomentSnapshot 校验正整数路由 ID 与快照一致性', () => {
  const moment = { id: 8, partnerId: 3, content: 'hi', imageUrls: ['x'] };
  assert.equal(validateMomentSnapshot(8, moment).id, 8);
  assert.equal(validateMomentSnapshot('8', moment).id, 8);
  assert.equal(validateMomentSnapshot(0, moment), null);
  assert.equal(validateMomentSnapshot(-1, moment), null);
  assert.equal(validateMomentSnapshot(9, moment), null);
  assert.equal(validateMomentSnapshot(8, null), null);
  assert.equal(validateMomentSnapshot(8, { id: 0, content: 'x' }), null);
  assert.equal(validateMomentSnapshot(8, undefined), null);
});

test('formatMomentCount 与 avatarText 提供稳定展示', () => {
  assert.equal(formatMomentCount(0), '0');
  assert.equal(formatMomentCount(999), '999');
  assert.equal(formatMomentCount(1500), '1.5k');
  assert.equal(formatMomentCount('800'), '800');
  assert.equal(formatMomentCount(-1), '0');
  assert.equal(avatarText('林薇'), '林');
  assert.equal(avatarText(''), '缘');
});

// ---------- 1.3 共享组件事件分流：内部操作不触发详情 ----------

describe('共享动态单元 s-moment-item', () => {
  const component = read('../sheep/components/s-moment-item/s-moment-item.vue');

  it('仅内容区触发详情，内部操作阻止冒泡并只发出语义事件', () => {
    assert.match(component, /@tap="openDetail"/);
    assert.match(component, /@tap\.stop="openMember"/);
    assert.match(component, /@tap\.stop="follow"/);
    assert.match(component, /@tap\.stop="openMore"/);
    assert.match(component, /@tap\.stop="like"/);
    assert.match(component, /@tap\.stop="comment"/);
    assert.match(component, /@tap\.stop="share"/);
    for (const event of ['open-detail', 'open-member', 'follow', 'like', 'comment', 'share', 'more']) {
      assert.match(component, new RegExp(`['"]${event}['"]`));
    }
  });

  it('组件只展示与发事件，不自行导航或预览', () => {
    assert.doesNotMatch(component, /uni\.navigateTo|sheep\.\$router\.go|uni\.previewImage/);
    assert.match(component, /momentLayout\(/);
    assert.match(component, /normalizeMomentItem\(props\.item\)/);
  });

  it('本人仅展示更多菜单，非本人关注入口只在 feed 模式且未关注时出现', () => {
    assert.match(component, /v-if="item\.mine" class="moment-more"/);
    assert.match(component, /v-else-if="mode === 'feed' && !item\.followed"/);
    assert.match(component, /mode: \{[\s\S]*default: 'feed'/);
  });

  it('分享、点赞和评论操作使用跨端图标并区分点赞状态', () => {
    assert.match(component, /<uni-icons[^>]+type="forward"/);
    assert.match(component, /:type="item\.liked \? 'hand-up-filled' : 'hand-up'"/);
    assert.match(component, /<uni-icons[^>]+type="chatbubble"/);
    assert.match(component, /:color="item\.liked \? '#df4f83' : '#867475'"/);
  });

  it('共享组件不使用 DOM / H5 专属能力或本地持久化', () => {
    assert.doesNotMatch(component, /\bwindow\b|\bdocument\b/);
    assert.doesNotMatch(component, /<svg|element-plus|ElementPlus/);
    assert.doesNotMatch(component, /setStorageSync|getStorageSync|localStorage/);
  });
});

// ---------- 2.2/2.3 三张列表接入共享展示并保留各自生命周期 ----------

describe('推荐/关注动态页接入共享展示', () => {
  const page = read('../pages/dynamics/index.vue');

  it('使用共享组件并声明全部语义事件', () => {
    assert.match(page, /<s-moment-item/);
    assert.match(page, /@open-detail="openMomentDetail"/);
    assert.match(page, /@open-member="openMember"/);
    assert.match(page, /@follow="followAuthor"/);
    assert.match(page, /@like="toggleLike"/);
    assert.match(page, /@comment="openMomentDetail"/);
    assert.match(page, /@share="shareMoment"/);
    assert.match(page, /@more="openMoreMenu"/);
  });

  it('保留分页、刷新、发布、登录/实名守卫与竞态隔离', () => {
    assert.match(page, /requireRealNameInteraction\(INTERACTION_GUARD_SCENES\.FOLLOW\)/);
    assert.match(page, /requireRealNameInteraction\(INTERACTION_GUARD_SCENES\.LIKE\)/);
    assert.match(page, /requireRealNameInteraction\(INTERACTION_GUARD_SCENES\.PUBLISH\)/);
    assert.match(page, /feedRequestId/);
    assert.match(page, /dedupeBy\(nextList, \(item\) => item\.id\)/);
    assert.match(page, /MomentApi\.getPage/);
    assert.match(page, /MomentApi\.delete/);
  });

  it('通过 EventChannel 导航详情并按 ID 回传合并', () => {
    assert.match(page, /\/pages\/moment-detail\/index\?id=/);
    assert.match(page, /'moment-detail-change'/);
    assert.match(page, /res\.eventChannel\.emit\('moment-snapshot'/);
    assert.match(page, /mergeMomentChange\(moments\.value, data\)/);
  });

  it('评论闭环迁移到详情页，列表不再内嵌评论弹层', () => {
    assert.doesNotMatch(page, /getCommentPage|createComment|comment-panel|commentsVisible/);
  });

  it('点赞/关注/删除按动态 ID 更新而非数组下标', () => {
    assert.match(page, /moment\.id === item\.id/);
    assert.match(page, /mergeMomentChange\(moments\.value, \{ id: item\.id, removed: true \}\)/);
  });
});

describe('人物动态页与我的动态页接入同一展示语义', () => {
  const partnerPage = read('../pages/partner-dynamics/index.vue');
  const minePage = read('../pages/mine-dynamics/index.vue');

  it('人物页按 partnerId 查询、记录查看，不显示关注入口，无删除', () => {
    assert.match(partnerPage, /mode="partner"/);
    assert.match(partnerPage, /MomentApi\.getPage\(\{/);
    assert.match(partnerPage, /partnerId: partnerId\.value/);
    assert.match(partnerPage, /InteractionApi\.recordView/);
    assert.match(partnerPage, /parsePositiveId\(query\?\.partnerId\)/);
    assert.doesNotMatch(partnerPage, /@follow=|mode="feed"/);
    assert.doesNotMatch(partnerPage, /MomentApi\.delete/);
    assert.match(partnerPage, /openMomentDetail/);
  });

  it('我的页使用本人分页、登录守卫与本人删除', () => {
    assert.match(minePage, /mode="mine"/);
    assert.match(minePage, /MomentApi\.getMyPage/);
    assert.match(minePage, /@more="openMoreMenu"/);
    assert.match(minePage, /MomentApi\.delete/);
    assert.match(minePage, /requireRealNameInteraction\(INTERACTION_GUARD_SCENES\.LIKE\)/);
    assert.doesNotMatch(minePage, /parsePositiveId\(query\?\.partnerId\)/);
  });
});

// ---------- 3 编译期动态详情页 ----------

describe('编译期动态详情页', () => {
  const page = read('../pages/moment-detail/index.vue');

  it('快照缺失/无效时展示稳定错误态并提供返回', () => {
    assert.match(page, /动态内容暂时无法打开，请返回列表重试/);
    assert.match(page, /function goBack\(\)/);
    assert.match(page, /uni\.navigateBack/);
    assert.match(page, /uni\.switchTab\(\{ url: '\/pages\/dynamics\/index' \}\)/);
    assert.doesNotMatch(page, /MomentApi\.getCommentPage[\s\S]*v-if="!snapshot/);
  });

  it('通过 EventChannel 接收快照、校验 ID，并按 ID 回传变化', () => {
    assert.match(page, /getOpenerEventChannel/);
    assert.match(page, /'moment-snapshot'/);
    assert.match(page, /validateMomentSnapshot\(routeId\.value, data\?\.moment\)/);
    assert.match(page, /eventChannel\?\.emit\('moment-detail-change', change\)/);
  });

  it('复用现有点赞、评论分页、回复/发布与实名守卫', () => {
    assert.match(page, /MomentApi\.getCommentPage/);
    assert.match(page, /MomentApi\.createComment/);
    assert.match(page, /MomentApi\.toggleLike/);
    assert.match(page, /MomentApi\.delete/);
    assert.match(page, /replyParentId|replyToPartnerId/);
    assert.match(page, /requireRealNameInteraction\(INTERACTION_GUARD_SCENES\.LIKE\)/);
    assert.match(page, /requireRealNameInteraction\(INTERACTION_GUARD_SCENES\.COMMENT\)/);
  });

  it('提供图片预览与加载/空/错/重试/到底/提交状态', () => {
    assert.match(page, /uni\.previewImage/);
    assert.match(page, /commentLoading/);
    assert.match(page, /commentError/);
    assert.match(page, /还没有评论/);
    assert.match(page, /上拉加载更多/);
    assert.match(page, /submitting/);
    assert.match(page, /momentLayout\(/);
  });

  it('请求去重与晚到响应隔离', () => {
    assert.match(page, /commentRequestId/);
    assert.match(page, /likeInFlight/);
    assert.match(page, /submitting/);
    assert.match(page, /commentRequestId \+= 1/);
  });

  it('不持久化快照、不读取 DIY/后端扩展', () => {
    assert.doesNotMatch(page, /setStorageSync|getStorageSync|localStorage|indexedDB/);
    assert.doesNotMatch(page, /template\.components|s-block/);
    assert.doesNotMatch(page, /\/marriage\/moment\/get\b|\/\?single=|moment\/detail\b/);
    assert.doesNotMatch(page, /\bwindow\b|\bdocument\b|<svg/);
  });
});

// ---------- 4 DIY 与跨端边界 ----------

test('pages.json 声明编译期详情页且不改变既有页面', () => {
  const pages = JSON.parse(read('../pages.json'));
  const detail = pages.subPackages.find((item) => item.root === 'pages/moment-detail');
  assert.ok(detail, 'pages.json 缺少 pages/moment-detail 子包');
  assert.equal(detail.pages[0].path, 'index');
  assert.ok(pages.pages.some((item) => item.path === 'pages/dynamics/index'));
  assert.deepEqual(pages.tabBar.list.map((item) => item.pagePath), [
    'pages/index/index',
    'pages/dynamics/index',
    'pages/messages/index',
    'pages/index/user',
  ]);
});

test('DIY 入口 PartnerProfileMoment 契约与固定入口保持不变', () => {
  const component = read('../sheep/components/s-partner-profile-moment/s-partner-profile-moment.vue');
  const block = read('../sheep/components/s-block-item/s-block-item.vue');
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js');
  assert.match(component, /inject\('partnerRecommendationContext', null\)/);
  assert.match(component, /\/pages\/partner-dynamics\/index/);
  assert.doesNotMatch(component, /MomentApi|InteractionApi|moment-detail/);
  assert.match(block, /type === 'PartnerProfileMoment'/);
  assert.match(registry, /'PartnerProfileMoment'/);

  const dynamics = read('../pages/dynamics/index.vue');
  const detail = read('../pages/moment-detail/index.vue');
  assert.doesNotMatch(dynamics, /template\.components|s-block/);
  assert.doesNotMatch(detail, /template\.components|s-block/);
});

test('共享代码保持 H5/App 跨端源码兼容', () => {
  const files = [
    '../sheep/components/s-moment-item/s-moment-item.vue',
    '../pages/moment-detail/index.vue',
    '../sheep/helper/marriage-moment.js',
  ];
  for (const file of files) {
    const source = read(file);
    assert.doesNotMatch(source, /\bwindow\b|\bdocument\b|\blocalStorage\b|\bindexedDB\b/);
    assert.doesNotMatch(source, /<svg|element-plus|ElementPlus/);
    assert.doesNotMatch(source, /uni\.setStorageSync|uni\.getStorageSync/);
  }
});
