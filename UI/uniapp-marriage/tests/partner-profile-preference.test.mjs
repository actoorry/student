import { describe, it } from 'node:test';
import assert from 'node:assert';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  DEFAULT_TITLE,
  DEFAULT_TITLE_COLOR,
  DEFAULT_ACCENT_COLOR,
  DEFAULT_TAG_TEXT_COLOR,
  DEFAULT_TAG_BACKGROUND_COLOR,
  DEFAULT_TAG_RADIUS,
  TAG_RADIUS_RANGE,
  TITLE_MAX_LENGTH,
  hasInterestTags,
  normalizeAccentColor,
  normalizeColor,
  normalizeInterestTags,
  normalizeTagBackgroundColor,
  normalizeTagRadius,
  normalizeTagTextColor,
  normalizeTitle,
  normalizeTitleColor,
} from '../sheep/components/s-partner-profile-preference/partnerProfilePreference.js';
import {
  isKnownDiyComponent,
  isRetiredDiyComponent,
} from '../sheep/components/s-block-item/shared-diy-renderers.js';

const root = dirname(fileURLToPath(import.meta.url));
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8');

// =============================================================================
// Mall pure-function helpers
// =============================================================================

describe('PartnerProfilePreference mall helpers', () => {
  // --- title ---
  it('normalizes title using the same bounded Unicode contract as admin', () => {
    assert.strictEqual(normalizeTitle('  我的择偶条件  '), '我的择偶条件');
    assert.strictEqual(normalizeTitle(''), DEFAULT_TITLE);
    assert.strictEqual(normalizeTitle(null), DEFAULT_TITLE);
    assert.strictEqual(normalizeTitle(123), DEFAULT_TITLE);
    assert.strictEqual(
      Array.from(normalizeTitle('一二三四五六七八九十十一十二十三')).length,
      TITLE_MAX_LENGTH,
    );
    // Emoji counts as one Unicode character
    assert.strictEqual(normalizeTitle('❤️择偶'), '❤️择偶');
  });

  // --- color ---
  it('normalizes all visual colors with safe defaults', () => {
    assert.strictEqual(normalizeColor('#abcdef', DEFAULT_TITLE_COLOR), '#ABCDEF');
    assert.strictEqual(normalizeColor('#2d2324', DEFAULT_TITLE_COLOR), '#2D2324');
    assert.strictEqual(normalizeColor('red', DEFAULT_TITLE_COLOR), DEFAULT_TITLE_COLOR);
    assert.strictEqual(normalizeColor(null, DEFAULT_TITLE_COLOR), DEFAULT_TITLE_COLOR);
    assert.strictEqual(normalizeColor('', DEFAULT_TITLE_COLOR), DEFAULT_TITLE_COLOR);
    assert.strictEqual(normalizeColor('#12345', DEFAULT_TITLE_COLOR), DEFAULT_TITLE_COLOR);
    assert.strictEqual(normalizeColor('#1234567', DEFAULT_TITLE_COLOR), DEFAULT_TITLE_COLOR);
  });

  it('provides convenience color normalizers with correct defaults', () => {
    assert.strictEqual(normalizeTitleColor('#c84449'), '#C84449');
    assert.strictEqual(normalizeTitleColor('bad'), DEFAULT_TITLE_COLOR);
    assert.strictEqual(normalizeAccentColor('#c84449'), '#C84449');
    assert.strictEqual(normalizeAccentColor('bad'), DEFAULT_ACCENT_COLOR);
    assert.strictEqual(normalizeTagTextColor('#8f675d'), '#8F675D');
    assert.strictEqual(normalizeTagTextColor('bad'), DEFAULT_TAG_TEXT_COLOR);
    assert.strictEqual(normalizeTagBackgroundColor('#fff2e7'), '#FFF2E7');
    assert.strictEqual(normalizeTagBackgroundColor('bad'), DEFAULT_TAG_BACKGROUND_COLOR);
  });

  // --- radius ---
  it('normalizes tag radius without coercing empty or boolean values', () => {
    assert.strictEqual(normalizeTagRadius(0), 0);
    assert.strictEqual(normalizeTagRadius(64), 64);
    assert.strictEqual(normalizeTagRadius(31.6), 32);
    assert.strictEqual(normalizeTagRadius(-1), DEFAULT_TAG_RADIUS);
    assert.strictEqual(normalizeTagRadius(65), DEFAULT_TAG_RADIUS);
    assert.strictEqual(normalizeTagRadius(null), DEFAULT_TAG_RADIUS);
    assert.strictEqual(normalizeTagRadius(''), DEFAULT_TAG_RADIUS);
    assert.strictEqual(normalizeTagRadius(false), DEFAULT_TAG_RADIUS);
    assert.strictEqual(normalizeTagRadius(NaN), DEFAULT_TAG_RADIUS);
    assert.strictEqual(normalizeTagRadius(Infinity), DEFAULT_TAG_RADIUS);
  });

  // --- tags ---
  it('returns empty array for non-array interestTags', () => {
    for (const value of [null, undefined, 'string', 123, {}, true]) {
      assert.deepStrictEqual(normalizeInterestTags(value), []);
    }
  });

  it('filters empty, whitespace-only, and non-string items', () => {
    assert.deepStrictEqual(
      normalizeInterestTags(['未婚', '', '  ', 123, null, {}, '本科']),
      ['未婚', '本科'],
    );
  });

  it('trims whitespace from tag strings', () => {
    assert.deepStrictEqual(
      normalizeInterestTags(['  未婚  ', '\t本科\n', ' 已购房 ']),
      ['未婚', '本科', '已购房'],
    );
  });

  it('preserves server order and deduplicates after trimming', () => {
    assert.deepStrictEqual(
      normalizeInterestTags(['本科', '未婚', '本科', ' 未婚 ', '已购房', '本科']),
      ['本科', '未婚', '已购房'],
    );
  });

  it('does not sort, translate, or mutate the original array', () => {
    const original = ['成都', '未婚', '160-175cm', '本科'];
    const copy = [...original];
    const result = normalizeInterestTags(original);
    assert.deepStrictEqual(result, ['成都', '未婚', '160-175cm', '本科']);
    assert.deepStrictEqual(original, copy);
  });

  it('handles long text naturally without truncation', () => {
    const longTag =
      '希望对方性格开朗阳光为人正直有上进心能一起规划未来生活接受异地';
    assert.deepStrictEqual(normalizeInterestTags([longTag]), [longTag]);
  });

  it('does not interpret HTML-like or script-like content', () => {
    const tags = [
      '<script>alert(1)</script>',
      '<b>bold</b>',
      'onclick="alert(1)"',
      '正常标签',
    ];
    const result = normalizeInterestTags(tags);
    assert.strictEqual(result.length, 4);
    // All tags are kept as-is; no interpretation
    assert.strictEqual(result[0], '<script>alert(1)</script>');
    assert.strictEqual(result[1], '<b>bold</b>');
    assert.strictEqual(result[2], 'onclick="alert(1)"');
    assert.strictEqual(result[3], '正常标签');
  });

  it('handles full complete interestTags without modification', () => {
    const tags = ['未婚', '160-175cm', '本科', '成都市', '15k-20k', '已购房', '已购车', '教师', '接受异地'];
    assert.deepStrictEqual(normalizeInterestTags(tags), tags);
  });

  it('returns empty for empty array', () => {
    assert.deepStrictEqual(normalizeInterestTags([]), []);
    assert.deepStrictEqual(normalizeInterestTags(['', '  ', '\t']), []);
  });

  it('hasInterestTags returns correct boolean', () => {
    assert.strictEqual(hasInterestTags({ interestTags: ['未婚', '本科'] }), true);
    assert.strictEqual(hasInterestTags({ interestTags: [] }), false);
    assert.strictEqual(hasInterestTags({ interestTags: ['', '  '] }), false);
    assert.strictEqual(hasInterestTags({}), false);
    assert.strictEqual(hasInterestTags(null), false);
    assert.strictEqual(hasInterestTags({ interestTags: 'not array' }), false);
  });

  it('keeps multiple calls isolated without cross-contamination', () => {
    const first = normalizeInterestTags(['未婚', '本科']);
    const second = normalizeInterestTags(['已购房', '已购车']);
    assert.deepStrictEqual(first, ['未婚', '本科']);
    assert.deepStrictEqual(second, ['已购房', '已购车']);
  });
});

// =============================================================================
// Mall component contract
// =============================================================================

describe('PartnerProfilePreference mall component contract', () => {
  const component = read(
    '../sheep/components/s-partner-profile-preference/s-partner-profile-preference.vue',
  );
  const helper = read(
    '../sheep/components/s-partner-profile-preference/partnerProfilePreference.js',
  );
  const blockItem = read('../sheep/components/s-block-item/s-block-item.vue');
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js');

  it('consumes only the existing read-only profile context', () => {
    assert.match(component, /inject\('partnerRecommendationContext',\s*null\)/);
    assert.match(component, /context\.value\?\.profile/);
    assert.match(component, /v-if="shouldRender"/);
    assert.match(component, /const shouldRender = computed\(\(\) => hasContext\.value\)/);
    assert.match(component, /该用户暂未填写择偶条件/);
    assert.doesNotMatch(component, /usePartnerRecommendation|useUserStore|useSocialStore/);
    assert.doesNotMatch(component, /fetch\(|request\(|\.get\(|\.post\(|sheep\/api/);
  });

  it('reads profile.interestTags without rebuilding from mate fields', () => {
    assert.match(component, /profile\.value\?\.interestTags/);
    assert.doesNotMatch(helper, /mateMarital|mateMinHeight|mateMaxHeight|mateMinWeight|mateMaxWeight/);
    assert.doesNotMatch(helper, /mateMinEducation|mateLiveArea|mateMinIncome|mateHouse|mateCar/);
    assert.doesNotMatch(helper, /mateJobTitle|mateRemark/);
  });

  it('renders tags as pure text via template interpolation', () => {
    assert.match(component, /{{ tag }}/);
    assert.doesNotMatch(component, /rich-text|v-html|innerHTML/);
    assert.doesNotMatch(component, /@tap|@click|navigateTo/);
  });

  it('does not apply common style again or use web-only APIs', () => {
    assert.doesNotMatch(component, /props\.data\?\.style|props\.styles|marginTop|paddingTop/);
    assert.doesNotMatch(component, /window\.|document\.|<svg|ElementPlus/);
  });

  it('does not persist or act on runtime profile data', () => {
    assert.doesNotMatch(component, /JSON\.stringify|setStorage|console\.log/);
    assert.doesNotMatch(component, /navigateTo|@tap|@click/);
    assert.doesNotMatch(helper, /partnerId|api|route|action/);
  });

  it('uses the same explicit component ID for dispatch and known-ID recognition', () => {
    assert.match(
      blockItem,
      /<s-partner-profile-preference v-if="type === 'PartnerProfilePreference'" :data="data"/,
    );
    assert.match(registry, /'PartnerProfilePreference'/);
    assert.strictEqual(isKnownDiyComponent('PartnerProfilePreference'), true);
    assert.strictEqual(isRetiredDiyComponent('PartnerProfilePreference'), false);
    assert.strictEqual(isKnownDiyComponent('FuturePreferenceComponent'), false);
  });

  it('preserves existing social and retired component recognition', () => {
    assert.strictEqual(isKnownDiyComponent('PartnerProfileHero'), true);
    assert.strictEqual(isKnownDiyComponent('PartnerProfileDeclaration'), true);
    assert.strictEqual(isKnownDiyComponent('PartnerProfileBasicInfo'), true);
    assert.strictEqual(isRetiredDiyComponent('MarriageRecommendProfileDeck'), true);
    assert.strictEqual(isRetiredDiyComponent('MarriageLoginPrompt'), true);
    assert.strictEqual(isRetiredDiyComponent('MarriageIdentityTrustBanner'), true);
  });

  it('keeps an explicit empty state without valid tags', () => {
    assert.match(component, /shouldRender/);
    assert.match(component, /v-if="validTags\.length"/);
    assert.match(component, /该用户暂未填写择偶条件/);
    assert.doesNotMatch(component, /SAMPLE_TAGS|sampleTags/);
    assert.doesNotMatch(helper, /SAMPLE_TAGS|sampleTags/);
  });
});

// =============================================================================
// Admin component source contract
// =============================================================================

describe('PartnerProfilePreference admin component contract', () => {
  const adminConfig = read(
    '../../vue3-admin/src/components/DiyEditor/components/mobile/PartnerProfilePreference/config.ts',
  );
  const adminIndex = read(
    '../../vue3-admin/src/components/DiyEditor/components/mobile/PartnerProfilePreference/index.vue',
  );
  const adminProperty = read(
    '../../vue3-admin/src/components/DiyEditor/components/mobile/PartnerProfilePreference/property.vue',
  );
  const pageLibs = read('../../vue3-admin/src/components/DiyEditor/util.ts');

  it('declares the correct component ID and display name', () => {
    assert.match(adminConfig, /id:\s*['"]PartnerProfilePreference['"]/);
    assert.match(adminConfig, /name:\s*['"]择偶条件['"]/);
    assert.match(adminConfig, /PartnerProfilePreferenceProperty/);
  });

  it('exports correct default values for all visual fields', () => {
    assert.match(adminConfig, /DEFAULT_TITLE\s*=\s*['"]择偶条件['"]/);
    assert.match(adminConfig, /DEFAULT_TITLE_COLOR\s*=\s*['"]#2D2324['"]/);
    assert.match(adminConfig, /DEFAULT_ACCENT_COLOR\s*=\s*['"]#C84449['"]/);
    assert.match(adminConfig, /DEFAULT_TAG_TEXT_COLOR\s*=\s*['"]#8F675D['"]/);
    assert.match(adminConfig, /DEFAULT_TAG_BACKGROUND_COLOR\s*=\s*['"]#FFF2E7['"]/);
    assert.match(adminConfig, /DEFAULT_TAG_RADIUS\s*=\s*32/);
    assert.match(adminConfig, /TAG_RADIUS_RANGE\s*=\s*\{ min: 0, max: 64 \}/);
    assert.match(adminConfig, /TITLE_MAX_LENGTH\s*=\s*12/);
  });

  it('provides anonymous sample tags not persisted to property', () => {
    assert.match(adminConfig, /SAMPLE_TAGS/);
    assert.match(adminConfig, /未婚/);
    assert.match(adminConfig, /160-175cm/);
    assert.match(adminConfig, /本科/);
    // SAMPLE_TAGS must not be inside createDefault function
    const defaultFn = adminConfig.match(
      /createDefaultPartnerProfilePreferenceProperty[\s\S]*?return\s*\{[\s\S]*?\n\s*\}/,
    )[0];
    assert.doesNotMatch(defaultFn, /SAMPLE_TAGS|sample|未婚/);
  });

  it('includes the component in the 社交组件 category', () => {
    const socialSection = pageLibs.match(/name:\s*['"]社交组件['"][\s\S]*?\]/);
    assert.ok(socialSection);
    assert.match(socialSection[0], /['"]PartnerProfilePreference['"]/);
  });

  it('admin preview uses anonymous sample tags and does not access network', () => {
    assert.match(adminIndex, /SAMPLE_TAGS|sampleTags/);
    assert.match(adminIndex, /v-for="\(tag,\s*idx\)\s+in\s+sampleTags"/);
    assert.doesNotMatch(adminIndex, /fetch\(|request\(|\.get\(|\.post\(|import\.meta|inject\(/);
  });

  it('admin property panel uses useVModel and ComponentContainerProperty', () => {
    assert.match(adminProperty, /useVModel/);
    assert.match(adminProperty, /ComponentContainerProperty/);
    assert.match(adminProperty, /el-form/);
    assert.match(adminProperty, /titleColor|accentColor|tagTextColor|tagBackgroundColor|tagRadius/);
  });

  it('admin property panel explains runtime dependency', () => {
    assert.match(adminProperty, /人物推荐/);
    assert.match(adminProperty, /择偶条件/);
  });

  it('normalizeColor and normalizeTitle exist in both admin and mall with matching contracts', () => {
    // Admin has them
    assert.match(adminConfig, /export function normalizeTitle/);
    assert.match(adminConfig, /export function normalizeColor/);
    assert.match(adminConfig, /export function normalizeTagRadius/);
    assert.match(adminConfig, /export function rpxToPreviewPx/);
    // Both use the same constants
    assert.match(adminConfig, /TITLE_MAX_LENGTH\s*=\s*12/);
    assert.match(adminConfig, /TAG_RADIUS_RANGE.*min:\s*0.*max:\s*64/);
  });
});
