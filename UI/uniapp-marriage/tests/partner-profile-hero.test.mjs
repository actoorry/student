import { describe, it } from 'node:test';
import assert from 'node:assert';
import {
  buildAvatarCandidates,
  buildMainCandidates,
  DEFAULT_AGE_COLOR,
  DEFAULT_AVATAR_SIZE,
  DEFAULT_AVATAR_URL,
  DEFAULT_HEIGHT,
  DEFAULT_NAME_COLOR,
  EMPTY_NAME_PLACEHOLDER,
  formatDisplayAge,
  formatDisplayName,
  normalizeAgeColor,
  normalizeAvatarSize,
  normalizeHeight,
  normalizeNameColor,
  resolveAvatarUrl,
  resolveMainUrl,
} from '../sheep/components/s-partner-profile-hero/partnerProfileHero.js';

const readFileSync = (await import('node:fs')).readFileSync;
const { join, dirname } = await import('node:path');
const { fileURLToPath } = await import('node:url');

const __filename = fileURLToPath(import.meta.url);
const root = dirname(__filename);
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8');

describe('PartnerProfileHero mall helpers', () => {
  it('normalizes height within valid range', () => {
    assert.strictEqual(normalizeHeight(640), 640);
    assert.strictEqual(normalizeHeight(1400), 1400);
    assert.strictEqual(normalizeHeight(1040), 1040);
    assert.strictEqual(normalizeHeight(500), DEFAULT_HEIGHT);
    assert.strictEqual(normalizeHeight(1500), DEFAULT_HEIGHT);
  });

  it('falls back height default for invalid values', () => {
    assert.strictEqual(normalizeHeight(null), DEFAULT_HEIGHT);
    assert.strictEqual(normalizeHeight(undefined), DEFAULT_HEIGHT);
    assert.strictEqual(normalizeHeight('abc'), DEFAULT_HEIGHT);
    assert.strictEqual(normalizeHeight(NaN), DEFAULT_HEIGHT);
  });

  it('normalizes avatar size within valid range', () => {
    assert.strictEqual(normalizeAvatarSize(64), 64);
    assert.strictEqual(normalizeAvatarSize(160), 160);
    assert.strictEqual(normalizeAvatarSize(92), 92);
    assert.strictEqual(normalizeAvatarSize(50), DEFAULT_AVATAR_SIZE);
    assert.strictEqual(normalizeAvatarSize(200), DEFAULT_AVATAR_SIZE);
  });

  it('falls back avatar size default for invalid values', () => {
    assert.strictEqual(normalizeAvatarSize(null), DEFAULT_AVATAR_SIZE);
    assert.strictEqual(normalizeAvatarSize(''), DEFAULT_AVATAR_SIZE);
  });

  it('normalizes #RRGGBB colors only', () => {
    assert.strictEqual(normalizeNameColor('#201917'), '#201917');
    assert.strictEqual(normalizeAgeColor('#6F5A55'), '#6F5A55');
    assert.strictEqual(normalizeNameColor('#abcdef'), '#abcdef');
  });

  it('falls back color defaults for invalid colors', () => {
    assert.strictEqual(normalizeNameColor('#12345'), DEFAULT_NAME_COLOR);
    assert.strictEqual(normalizeNameColor('red'), DEFAULT_NAME_COLOR);
    assert.strictEqual(normalizeNameColor('rgb(0,0,0)'), DEFAULT_NAME_COLOR);
    assert.strictEqual(normalizeNameColor(null), DEFAULT_NAME_COLOR);
    assert.strictEqual(normalizeNameColor(123), DEFAULT_NAME_COLOR);
  });

  it('formats display name with fallback placeholder', () => {
    assert.strictEqual(formatDisplayName({ name: '林小鹿' }), '林小鹿');
    assert.strictEqual(formatDisplayName({ name: '  ' }), EMPTY_NAME_PLACEHOLDER);
    assert.strictEqual(formatDisplayName({}), EMPTY_NAME_PLACEHOLDER);
    assert.strictEqual(formatDisplayName(null), EMPTY_NAME_PLACEHOLDER);
    assert.strictEqual(formatDisplayName({ name: 123 }), EMPTY_NAME_PLACEHOLDER);
  });

  it('formats display age only for values convertible to valid 1..150 integers', () => {
    assert.strictEqual(formatDisplayAge({ age: 26 }), '26');
    assert.strictEqual(formatDisplayAge({ age: 1 }), '1');
    assert.strictEqual(formatDisplayAge({ age: 150 }), '150');
    assert.strictEqual(formatDisplayAge({ age: '26' }), '26');
    assert.strictEqual(formatDisplayAge({ age: 0 }), '');
    assert.strictEqual(formatDisplayAge({ age: 151 }), '');
    assert.strictEqual(formatDisplayAge({ age: 'abc' }), '');
    assert.strictEqual(formatDisplayAge({ age: null }), '');
    assert.strictEqual(formatDisplayAge({}), '');
  });

  it('builds main candidates in order and deduplicates', () => {
    const profile = {
      mainImage: 'main.jpg',
      albumImages: ['album1.jpg', 'album2.jpg', 'main.jpg', '  '],
      avatarImage: 'avatar.jpg',
    };
    const candidates = buildMainCandidates(profile);
    assert.deepStrictEqual(candidates, ['main.jpg', 'album1.jpg', 'album2.jpg', 'avatar.jpg']);
  });

  it('falls back main candidates to album, avatar and empty', () => {
    assert.deepStrictEqual(buildMainCandidates({ mainImage: '', albumImages: ['a.jpg'], avatarImage: '' }), ['a.jpg']);
    assert.deepStrictEqual(buildMainCandidates({ mainImage: '', albumImages: [], avatarImage: 'av.jpg' }), ['av.jpg']);
    assert.deepStrictEqual(buildMainCandidates({}), []);
  });

  it('resolves main url by candidate index', () => {
    const profile = { albumImages: ['a.jpg', 'b.jpg'] };
    assert.strictEqual(resolveMainUrl(profile, 0), 'a.jpg');
    assert.strictEqual(resolveMainUrl(profile, 1), 'b.jpg');
    assert.strictEqual(resolveMainUrl(profile, 99), 'b.jpg');
    assert.strictEqual(resolveMainUrl({}, 0), '');
  });

  it('builds avatar candidates from profile only', () => {
    assert.deepStrictEqual(buildAvatarCandidates({ avatarImage: 'av.jpg' }), ['av.jpg']);
    assert.deepStrictEqual(buildAvatarCandidates({ avatarImage: '  ' }), []);
    assert.deepStrictEqual(buildAvatarCandidates({}), []);
  });

  it('resolves avatar url to default when no avatar or failed', () => {
    assert.strictEqual(resolveAvatarUrl({ avatarImage: 'av.jpg' }, false), 'av.jpg');
    assert.strictEqual(resolveAvatarUrl({}, false), DEFAULT_AVATAR_URL);
    assert.strictEqual(resolveAvatarUrl({ avatarImage: 'av.jpg' }, true), DEFAULT_AVATAR_URL);
  });
});

describe('PartnerProfileHero mall component contract', () => {
  const component = read('../sheep/components/s-partner-profile-hero/s-partner-profile-hero.vue');
  const helpers = read('../sheep/components/s-partner-profile-hero/partnerProfileHero.js');
  const blockItem = read('../sheep/components/s-block-item/s-block-item.vue');
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js');

  it('is dispatched explicitly from s-block-item', () => {
    assert.match(blockItem, /<s-partner-profile-hero\s+v-if="type === 'PartnerProfileHero'"/);
  });

  it('is registered as a known standard DIY component', () => {
    assert.match(registry, /'PartnerProfileHero'/);
  });

  it('consumes the partner recommendation context via inject', () => {
    assert.match(component, /inject\('partnerRecommendationContext',\s*null\)/);
  });

  it('does not request recommendation API or read current user as candidate', () => {
    assert.doesNotMatch(component, /\/marriage\/recommend-member\/page/);
    assert.doesNotMatch(component, /\$store\('user'\)\.userInfo/);
    assert.doesNotMatch(component, /isLogin/);
  });

  it('does not write context to property, store, share or logs', () => {
    assert.doesNotMatch(component, /property\s*=|JSON\.stringify|persist|share|console\.log/);
  });

  it('renders only when a valid partner context exists', () => {
    assert.match(component, /v-if="hasContext"/);
    assert.match(component, /typeof\s+ctx\.partnerId\s+===\s+'number'/);
  });

  it('maps mainImage, albumImages and avatarImage fallback', () => {
    assert.match(component, /buildMainCandidates\(profile\.value\)/);
    assert.match(component, /buildAvatarCandidates\(profile\.value\)/);
  });

  it('uses default avatar fallback without looping', () => {
    assert.match(helpers, new RegExp(DEFAULT_AVATAR_URL));
    assert.match(component, /DEFAULT_AVATAR_URL/);
    assert.match(component, /avatarUrl\.value === DEFAULT_AVATAR_URL/);
  });

  it('handles image load errors with bounds', () => {
    assert.match(component, /@error="handleMainImageError"/);
    assert.match(component, /@error="handleAvatarError"/);
  });

  it('resets image state when partner context changes', () => {
    assert.match(component, /watch\(/);
    assert.match(component, /resetMainImageState\(\)/);
    assert.match(component, /resetAvatarState\(\)/);
    assert.match(component, /partnerId\.value/);
  });

  it('displays fallback name and bounded age', () => {
    assert.match(helpers, new RegExp(EMPTY_NAME_PLACEHOLDER));
    assert.match(component, /formatDisplayName\(profile\.value\)/);
    assert.match(component, /formatDisplayAge\(profile\.value\)/);
  });

  it('does not perform navigation, preview or interaction', () => {
    assert.doesNotMatch(component, /@tap|@click|uni\.previewImage|uni\.navigateTo|sheep\.\$router\.go/);
  });
});
