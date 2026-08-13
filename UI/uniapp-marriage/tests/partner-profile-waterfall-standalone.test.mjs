import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

import {
  displayAge,
  displayName,
  displaySex,
  hasMissingProfileDetails,
  imageCandidates,
  visibleBadges,
} from '../sheep/components/s-partner-profile-waterfall/partnerProfileWaterfall.js';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (relativePath) => readFile(path.join(root, relativePath), 'utf8');

test('人物瀑布流已注册为婚恋端标准装修组件', async () => {
  const [blockItem, registry] = await Promise.all([
    read('sheep/components/s-block-item/s-block-item.vue'),
    read('sheep/components/s-block-item/shared-diy-renderers.js'),
  ]);
  assert.match(blockItem, /<s-partner-profile-waterfall/);
  assert.match(blockItem, /type === 'PartnerProfileWaterfall'/);
  assert.match(registry, /'PartnerProfileWaterfall'/);
});

test('人物瀑布流自己分页请求并携带组件规则，不依赖首页人物作用域或模板编号', async () => {
  const [component, api, home] = await Promise.all([
    read('sheep/components/s-partner-profile-waterfall/s-partner-profile-waterfall.vue'),
    read('sheep/api/marriage/recommend.js'),
    read('pages/index/index.vue'),
  ]);
  assert.match(component, /RecommendApi\.getRecommendPage/);
  assert.match(component, /\.\.\.settings\.value\.rule/);
  assert.match(component, /props\.data\.rule\?\.realVerifiedOnly === true/);
  assert.doesNotMatch(component, /partnerRecommendationContext|inject\(/);
  assert.doesNotMatch(api, /templateId/);
  assert.match(api, /realVerifiedOnly/);
  assert.doesNotMatch(home, /<swiper|usePartnerRecommendation|partnerRecommendation/);
  assert.match(home, /v-for="\(item, index\) in template\.components"/);
});

test('人物字段和图片降级保持安全默认值', () => {
  assert.equal(displayName({ name: '  林薇  ' }), '林薇');
  assert.equal(displayName({}), '未命名用户');
  assert.equal(displayAge({ age: 26 }), '26');
  assert.equal(displayAge({ age: 0 }), '');
  assert.equal(displaySex({ sex: 2 }), '女');
  assert.deepEqual(
    imageCandidates({ mainImage: ' a ', albumImages: ['a', 'b'], avatarImage: 'c' }),
    ['a', 'b', 'c'],
  );
  assert.deepEqual(
    visibleBadges(
      { memberActive: 1, realVerified: 1, marriageVerified: 0 },
      { badges: {} },
    ),
    ['会员', '实名认证'],
  );
  const fields = {
    age: { show: true },
    sex: { show: true },
    job: { show: true },
    city: { show: true },
  };
  assert.equal(hasMissingProfileDetails({ age: 26, sex: 2, job: '', city: '' }, { fields }), true);
  assert.equal(
    hasMissingProfileDetails({ age: 26, sex: 2, job: '设计师', city: '成都' }, { fields }),
    false,
  );
});

test('资料缺失时人物卡展示统一占位提示', async () => {
  const component = await read(
    'sheep/components/s-partner-profile-waterfall/s-partner-profile-waterfall.vue',
  );
  assert.match(component, /hasMissingProfileDetails\(profile, settings\)/);
  assert.match(component, /该用户还没有填写相关资料/);
});

test('职业和居住地使用不影响字段内容的跨端 CSS 图标', async () => {
  const component = await read(
    'sheep/components/s-partner-profile-waterfall/s-partner-profile-waterfall.vue',
  );
  assert.match(component, /class="detail-icon detail-icon--job"/);
  assert.match(component, /class="detail-icon detail-icon--location"/);
  assert.doesNotMatch(component, /<text class="detail-icon">[▣⌖]<\/text>/);
  assert.doesNotMatch(component, /<image class="detail-icon"/);
});
