import { describe, it } from 'node:test';
import assert from 'node:assert';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  BASIC_INFO_FIELD_DEFINITIONS,
  buildPartnerProfileBasicInfoItems,
  formatBasicInfoMeasurement,
  isBasicInfoFieldVisible,
  normalizeBasicInfoText,
} from '../sheep/components/s-partner-profile-basic-info/partnerProfileBasicInfo.js';
import {
  isKnownDiyComponent,
  isRetiredDiyComponent,
} from '../sheep/components/s-block-item/shared-diy-renderers.js';

const root = dirname(fileURLToPath(import.meta.url));
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8');

const fullProfile = Object.freeze({
  id: 101,
  city: ' 成都市 ',
  job: '产品设计师',
  height: 165,
  weight: 52,
  income: '15k-20k',
  education: '本科',
  maritalStatus: '未婚',
  houseStatus: '已购房',
  carStatus: '已购车',
});

describe('PartnerProfileBasicInfo mall helpers', () => {
  it('owns exactly the requested eight fields in fixed order', () => {
    assert.deepStrictEqual(
      BASIC_INFO_FIELD_DEFINITIONS.map((field) => field.label),
      ['现居地区', '工作', '身高', '体重', '月收入', '学历', '住房', '车辆'],
    );
    assert.strictEqual(
      BASIC_INFO_FIELD_DEFINITIONS.some((field) => field.profileKey === 'maritalStatus'),
      false,
    );
  });

  it('only strict false hides a field', () => {
    assert.strictEqual(isBasicInfoFieldVisible(false), false);
    for (const value of [true, undefined, null, 0, 1, 'false', {}]) {
      assert.strictEqual(isBasicInfoFieldVisible(value), true);
    }
  });

  it('trims non-empty strings and rejects non-string values', () => {
    assert.strictEqual(normalizeBasicInfoText('  成都  '), '成都');
    assert.strictEqual(normalizeBasicInfoText('   '), '');
    assert.strictEqual(normalizeBasicInfoText(null), '');
    assert.strictEqual(normalizeBasicInfoText(123), '');
  });

  it('formats positive finite measurements with fixed units', () => {
    assert.strictEqual(formatBasicInfoMeasurement(165, 'cm'), '165cm');
    assert.strictEqual(formatBasicInfoMeasurement('52', 'kg'), '52kg');
    assert.strictEqual(formatBasicInfoMeasurement(52.6, 'kg'), '53kg');
    for (const value of [0, -1, '', '  ', 'abc', NaN, Infinity, null, false, {}]) {
      assert.strictEqual(formatBasicInfoMeasurement(value, 'cm'), '');
    }
  });

  it('maps all valid profile fields in fixed order and excludes marital status', () => {
    assert.deepStrictEqual(buildPartnerProfileBasicInfoItems(fullProfile, {}), [
      { key: 'city', label: '现居地区', value: '成都市' },
      { key: 'job', label: '工作', value: '产品设计师' },
      { key: 'height', label: '身高', value: '165cm' },
      { key: 'weight', label: '体重', value: '52kg' },
      { key: 'income', label: '月收入', value: '15k-20k' },
      { key: 'education', label: '学历', value: '本科' },
      { key: 'houseStatus', label: '住房', value: '已购房' },
      { key: 'carStatus', label: '车辆', value: '已购车' },
    ]);
  });

  it('applies each visibility switch independently without reordering', () => {
    const items = buildPartnerProfileBasicInfoItems(fullProfile, {
      showCity: false,
      showWeight: false,
      showEducation: false,
      showCarStatus: false,
    });
    assert.deepStrictEqual(
      items.map((item) => item.label),
      ['工作', '身高', '月收入', '住房'],
    );
  });

  it('uses default-on semantics for missing or polluted switches', () => {
    const items = buildPartnerProfileBasicInfoItems(fullProfile, {
      showCity: undefined,
      showJob: null,
      showHeight: 'false',
      showWeight: false,
    });
    assert.deepStrictEqual(
      items.map((item) => item.label),
      ['现居地区', '工作', '身高', '月收入', '学历', '住房', '车辆'],
    );
  });

  it('omits invalid values without creating empty cards', () => {
    const items = buildPartnerProfileBasicInfoItems(
      {
        city: '  ',
        job: 123,
        height: 0,
        weight: Infinity,
        income: ' 稳定收入 ',
        education: null,
        houseStatus: '租房',
        carStatus: '',
      },
      {},
    );
    assert.deepStrictEqual(items, [
      { key: 'income', label: '月收入', value: '稳定收入' },
      { key: 'houseStatus', label: '住房', value: '租房' },
    ]);
  });

  it('returns no content for no profile or all switches disabled', () => {
    assert.deepStrictEqual(buildPartnerProfileBasicInfoItems(null, {}), []);
    const allDisabled = Object.fromEntries(
      BASIC_INFO_FIELD_DEFINITIONS.map((field) => [field.toggleKey, false]),
    );
    assert.deepStrictEqual(buildPartnerProfileBasicInfoItems(fullProfile, allDisabled), []);
  });

  it('keeps multiple profile mappings isolated', () => {
    const first = buildPartnerProfileBasicInfoItems(fullProfile, { showJob: false });
    const second = buildPartnerProfileBasicInfoItems(
      { ...fullProfile, city: '上海市', job: '工程师' },
      { showCity: false },
    );
    assert.strictEqual(first[0].value, '成都市');
    assert.strictEqual(first.some((item) => item.key === 'job'), false);
    assert.strictEqual(second[0].value, '工程师');
    assert.strictEqual(second.some((item) => item.key === 'city'), false);
  });
});

describe('PartnerProfileBasicInfo mall component contract', () => {
  const component = read(
    '../sheep/components/s-partner-profile-basic-info/s-partner-profile-basic-info.vue',
  );
  const helper = read(
    '../sheep/components/s-partner-profile-basic-info/partnerProfileBasicInfo.js',
  );
  const blockItem = read('../sheep/components/s-block-item/s-block-item.vue');
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js');

  it('consumes only the existing read-only profile context', () => {
    assert.match(component, /inject\('partnerRecommendationContext',\s*null\)/);
    assert.match(component, /context\.value\?\.profile/);
    assert.match(component, /v-if="shouldRender"/);
    assert.match(component, /const shouldRender = computed\(\(\) => hasContext\.value\)/);
    assert.match(component, /该用户暂未填写资料/);
    assert.doesNotMatch(component, /usePartnerRecommendation|useUserStore|useSocialStore/);
    assert.doesNotMatch(component, /fetch\(|request\(|\.get\(|\.post\(|sheep\/api/);
  });

  it('does not apply common style again or use web-only APIs', () => {
    assert.doesNotMatch(component, /props\.data\?\.style|props\.styles|marginTop|paddingTop/);
    assert.doesNotMatch(component, /window\.|document\.|<svg|ElementPlus/);
  });

  it('does not persist or act on runtime profile data', () => {
    assert.doesNotMatch(component, /JSON\.stringify|setStorage|console\.log|navigateTo|@tap|@click/);
    assert.doesNotMatch(helper, /partnerId|maritalStatus|api|route|action|script/);
  });

  it('uses the same explicit component ID for dispatch and known-ID recognition', () => {
    assert.match(
      blockItem,
      /<s-partner-profile-basic-info v-if="type === 'PartnerProfileBasicInfo'" :data="data"/,
    );
    assert.match(registry, /'PartnerProfileBasicInfo'/);
    assert.strictEqual(isKnownDiyComponent('PartnerProfileBasicInfo'), true);
    assert.strictEqual(isRetiredDiyComponent('PartnerProfileBasicInfo'), false);
    assert.strictEqual(isKnownDiyComponent('FutureBasicInfoComponent'), false);
  });

  it('preserves existing social and retired component recognition', () => {
    assert.strictEqual(isKnownDiyComponent('PartnerProfileHero'), true);
    assert.strictEqual(isKnownDiyComponent('PartnerProfileDeclaration'), true);
    assert.strictEqual(isRetiredDiyComponent('MarriageRecommendProfileDeck'), true);
  });
});
