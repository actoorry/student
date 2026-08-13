import { describe, it } from 'node:test';
import assert from 'node:assert';
import {
  buildRealNameDescription,
  DEFAULT_ACCENT_COLOR as REAL_NAME_ACCENT,
  DEFAULT_BADGE_BACKGROUND_COLOR as REAL_NAME_BG,
  DEFAULT_DESCRIPTION_COLOR as REAL_NAME_DESC,
  DEFAULT_TITLE_COLOR as REAL_NAME_TITLE,
  extractIdCardPrefix,
  isRealVerified,
  normalizeAccentColor as normalizeRealNameAccent,
  normalizeBadgeBackgroundColor as normalizeRealNameBg,
  normalizeColor,
  normalizeDescriptionColor as normalizeRealNameDesc,
  normalizeTitleColor as normalizeRealNameTitle,
} from '../sheep/components/s-partner-real-name-verification-badge/partnerRealNameVerificationBadge.js';
import {
  DEFAULT_ACCENT_COLOR as MARRIAGE_ACCENT,
  DEFAULT_BADGE_BACKGROUND_COLOR as MARRIAGE_BG,
  DEFAULT_DESCRIPTION_COLOR as MARRIAGE_DESC,
  DEFAULT_TITLE_COLOR as MARRIAGE_TITLE,
  isMarriageVerified,
  normalizeAccentColor as normalizeMarriageAccent,
  normalizeBadgeBackgroundColor as normalizeMarriageBg,
  normalizeDescriptionColor as normalizeMarriageDesc,
  normalizeTitleColor as normalizeMarriageTitle,
} from '../sheep/components/s-partner-marriage-verification-badge/partnerMarriageVerificationBadge.js';

const readFileSync = (await import('node:fs')).readFileSync;
const { join, dirname } = await import('node:path');
const { fileURLToPath } = await import('node:url');

const __filename = fileURLToPath(import.meta.url);
const root = dirname(__filename);
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8');

describe('Real name badge helpers', () => {
  it('isRealVerified only accepts strict numeric 1', () => {
    assert.strictEqual(isRealVerified({ realVerified: 1 }), true);
    assert.strictEqual(isRealVerified({ realVerified: 0 }), false);
    assert.strictEqual(isRealVerified({ realVerified: '1' }), false);
    assert.strictEqual(isRealVerified({ realVerified: true }), false);
    assert.strictEqual(isRealVerified({ realVerified: -1 }), false);
    assert.strictEqual(isRealVerified({}), false);
    assert.strictEqual(isRealVerified(null), false);
  });

  it('extracts exactly four leading digits from masked id card', () => {
    assert.strictEqual(extractIdCardPrefix('4403**************'), '4403');
    assert.strictEqual(extractIdCardPrefix('  4403**************'), '4403');
    assert.strictEqual(extractIdCardPrefix('440'), '');
    assert.strictEqual(extractIdCardPrefix('abcd**************'), '');
    assert.strictEqual(extractIdCardPrefix('44a3**************'), '');
    assert.strictEqual(extractIdCardPrefix(null), '');
    assert.strictEqual(extractIdCardPrefix(4403), '');
  });

  it('builds description with prefix or fallback', () => {
    assert.strictEqual(
      buildRealNameDescription({ realVerified: 1, maskedIdCard: '4403**************' }),
      '身份证前四位：4403',
    );
    assert.strictEqual(
      buildRealNameDescription({ realVerified: 1, maskedIdCard: 'abcd**************' }),
      '身份已核验',
    );
    assert.strictEqual(buildRealNameDescription({ realVerified: 1 }), '身份已核验');
    assert.strictEqual(buildRealNameDescription({ realVerified: 1, maskedIdCard: 4403 }), '身份已核验');
  });

  it('normalizes #RRGGBB colors only', () => {
    assert.strictEqual(normalizeColor('#C84449', REAL_NAME_ACCENT), '#C84449');
    assert.strictEqual(normalizeColor('#c84449', REAL_NAME_ACCENT), '#c84449');
  });

  it('falls back color defaults for invalid colors', () => {
    assert.strictEqual(normalizeColor('#12345', REAL_NAME_ACCENT), REAL_NAME_ACCENT);
    assert.strictEqual(normalizeColor('red', REAL_NAME_ACCENT), REAL_NAME_ACCENT);
    assert.strictEqual(normalizeColor(null, REAL_NAME_ACCENT), REAL_NAME_ACCENT);
    assert.strictEqual(normalizeColor(123, REAL_NAME_ACCENT), REAL_NAME_ACCENT);
  });

  it('uses real name default colors', () => {
    assert.strictEqual(normalizeRealNameBg(undefined), REAL_NAME_BG);
    assert.strictEqual(normalizeRealNameAccent(undefined), REAL_NAME_ACCENT);
    assert.strictEqual(normalizeRealNameTitle(undefined), REAL_NAME_TITLE);
    assert.strictEqual(normalizeRealNameDesc(undefined), REAL_NAME_DESC);
  });
});

describe('Marriage badge helpers', () => {
  it('isMarriageVerified only accepts strict numeric 1', () => {
    assert.strictEqual(isMarriageVerified({ marriageVerified: 1 }), true);
    assert.strictEqual(isMarriageVerified({ marriageVerified: 0 }), false);
    assert.strictEqual(isMarriageVerified({ marriageVerified: '1' }), false);
    assert.strictEqual(isMarriageVerified({ marriageVerified: true }), false);
    assert.strictEqual(isMarriageVerified({}), false);
    assert.strictEqual(isMarriageVerified(null), false);
  });

  it('uses marriage default colors', () => {
    assert.strictEqual(normalizeMarriageBg(undefined), MARRIAGE_BG);
    assert.strictEqual(normalizeMarriageAccent(undefined), MARRIAGE_ACCENT);
    assert.strictEqual(normalizeMarriageTitle(undefined), MARRIAGE_TITLE);
    assert.strictEqual(normalizeMarriageDesc(undefined), MARRIAGE_DESC);
  });
});

describe('Real name badge component contract', () => {
  const component = read('../sheep/components/s-partner-real-name-verification-badge/s-partner-real-name-verification-badge.vue');
  const helpers = read('../sheep/components/s-partner-real-name-verification-badge/partnerRealNameVerificationBadge.js');
  const blockItem = read('../sheep/components/s-block-item/s-block-item.vue');
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js');

  it('is dispatched explicitly from s-block-item', () => {
    assert.match(blockItem, /<s-partner-real-name-verification-badge\s+v-if="type === 'PartnerRealNameVerificationBadge'"/);
  });

  it('is registered as a known standard DIY component', () => {
    assert.match(registry, /'PartnerRealNameVerificationBadge'/);
  });

  it('consumes the partner recommendation context via inject', () => {
    assert.match(component, /inject\('partnerRecommendationContext',\s*null\)/);
  });

  it('renders only when a valid partner context and realVerified === 1', () => {
    assert.match(component, /v-if="hasContext && isVerified"/);
    assert.match(component, /isRealVerified\(profile\.value\)/);
  });

  it('does not request certification or recommendation API', () => {
    assert.doesNotMatch(component, /\/marriage\/recommend-member/);
    assert.doesNotMatch(component, /\/partner\/certification/);
    assert.doesNotMatch(component, /axios|fetch|XMLHttpRequest/);
  });

  it('does not read current user or maintain page index', () => {
    assert.doesNotMatch(component, /\$store\('user'\)\.userInfo/);
    assert.doesNotMatch(component, /isLogin/);
    assert.doesNotMatch(component, /currentIndex|active|index/);
  });

  it('does not write context to property, store, share or logs', () => {
    assert.doesNotMatch(component, /property\s*=|JSON\.stringify|persist|share|console\.log/);
  });

  it('only exposes safe id card prefix, never full masked value', () => {
    assert.match(helpers, /substring\(0, 4\)/);
    assert.doesNotMatch(component, /profile\.value\.maskedIdCard/);
    assert.doesNotMatch(component, /console\.log\(.*profile/);
  });

  it('does not use DOM, SVG, Element Plus or web-only APIs', () => {
    assert.doesNotMatch(component, /document\.|window\.|navigator\./);
    assert.doesNotMatch(component, /<svg|<component|ElementPlus/);
    assert.doesNotMatch(component, /uni\.previewImage|uni\.navigateTo|sheep\.\$router\.go/);
  });
});

describe('Marriage badge component contract', () => {
  const component = read('../sheep/components/s-partner-marriage-verification-badge/s-partner-marriage-verification-badge.vue');
  const blockItem = read('../sheep/components/s-block-item/s-block-item.vue');
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js');

  it('is dispatched explicitly from s-block-item', () => {
    assert.match(blockItem, /<s-partner-marriage-verification-badge\s+v-if="type === 'PartnerMarriageVerificationBadge'"/);
  });

  it('is registered as a known standard DIY component', () => {
    assert.match(registry, /'PartnerMarriageVerificationBadge'/);
  });

  it('consumes the partner recommendation context via inject', () => {
    assert.match(component, /inject\('partnerRecommendationContext',\s*null\)/);
  });

  it('renders only when a valid partner context and marriageVerified === 1', () => {
    assert.match(component, /v-if="hasContext && isVerified"/);
    assert.match(component, /isMarriageVerified\(profile\.value\)/);
  });

  it('does not request certification or recommendation API', () => {
    assert.doesNotMatch(component, /\/marriage\/recommend-member/);
    assert.doesNotMatch(component, /axios|fetch|XMLHttpRequest/);
  });

  it('does not show specific marital status', () => {
    assert.doesNotMatch(component, /已婚|未婚|离婚|MARRIED|UNMARRIED|DIVORCED/);
    assert.doesNotMatch(component, /profile\.value\.maritalStatus/);
  });

  it('does not write context to property, store, share or logs', () => {
    assert.doesNotMatch(component, /property\s*=|JSON\.stringify|persist|share|console\.log/);
  });

  it('does not use DOM, SVG, Element Plus or web-only APIs', () => {
    assert.doesNotMatch(component, /document\.|window\.|navigator\./);
    assert.doesNotMatch(component, /<svg|<component|ElementPlus/);
    assert.doesNotMatch(component, /uni\.previewImage|uni\.navigateTo|sheep\.\$router\.go/);
  });
});

describe('Verification badge distribution and privacy guard', () => {
  const blockItem = read('../sheep/components/s-block-item/s-block-item.vue');
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js');
  const realNameHelpers = read('../sheep/components/s-partner-real-name-verification-badge/partnerRealNameVerificationBadge.js');

  it('both IDs map consistently between dispatcher and registry', () => {
    assert.match(blockItem, /'PartnerRealNameVerificationBadge'/);
    assert.match(blockItem, /'PartnerMarriageVerificationBadge'/);
    assert.match(registry, /'PartnerRealNameVerificationBadge'/);
    assert.match(registry, /'PartnerMarriageVerificationBadge'/);
  });

  it('real name helper never returns more than four digits', () => {
    assert.strictEqual(extractIdCardPrefix('4403123456789').length, 4);
    assert.strictEqual(extractIdCardPrefix('4403**************'), '4403');
    assert.strictEqual(extractIdCardPrefix('440312'), '4403');
  });

  it('unknown component placeholder remains and retired IDs are still silent', () => {
    assert.match(blockItem, /unknown-diy-component/);
    assert.match(registry, /RETIRED_DIY_COMPONENT_IDS/);
    for (const id of ['MarriageLoginPrompt', 'MarriageRecommendProfileDeck', 'MarriageIdentityTrustBanner']) {
      assert.match(registry, new RegExp(id));
    }
  });

  it('real name helper rejects full masked value logging patterns', () => {
    assert.doesNotMatch(realNameHelpers, /console\.log/);
    assert.doesNotMatch(realNameHelpers, /maskedIdCard\s*\.\s*substring\(0,\s*[^4]/);
  });
});
