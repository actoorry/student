import { describe, it } from 'node:test';
import assert from 'node:assert';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  DEFAULT_CONTENT_BACKGROUND_COLOR,
  DEFAULT_CONTENT_COLOR,
  DEFAULT_CONTENT_RADIUS,
  DEFAULT_TITLE,
  DEFAULT_TITLE_COLOR,
  EMPTY_DECLARATION_TEXT,
  formatDeclarationBio,
  getDeclarationBio,
  hasDeclarationBio,
  normalizeColor,
  normalizeContentBackgroundColor,
  normalizeContentColor,
  normalizeContentRadius,
  normalizeTitle,
  normalizeTitleColor,
  TITLE_MAX_LENGTH,
} from '../sheep/components/s-partner-profile-declaration/partnerProfileDeclaration.js';
import {
  isKnownDiyComponent,
  isRetiredDiyComponent,
} from '../sheep/components/s-block-item/shared-diy-renderers.js';

const root = dirname(fileURLToPath(import.meta.url));
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8');

describe('PartnerProfileDeclaration mall contract', () => {
  it('normalizes title using the same bounded Unicode contract as admin', () => {
    assert.strictEqual(normalizeTitle('  真诚交友  '), '真诚交友');
    assert.strictEqual(normalizeTitle(''), DEFAULT_TITLE);
    assert.strictEqual(normalizeTitle(null), DEFAULT_TITLE);
    assert.strictEqual(
      Array.from(normalizeTitle('一二三四五六七八九十十一十二十三')).length,
      TITLE_MAX_LENGTH,
    );
  });

  it('normalizes all visual colors with safe defaults', () => {
    assert.strictEqual(normalizeColor('#abcdef', DEFAULT_TITLE_COLOR), '#ABCDEF');
    assert.strictEqual(normalizeTitleColor('red'), DEFAULT_TITLE_COLOR);
    assert.strictEqual(normalizeContentColor('#123456'), '#123456');
    assert.strictEqual(normalizeContentColor(null), DEFAULT_CONTENT_COLOR);
    assert.strictEqual(
      normalizeContentBackgroundColor('#fff9f6'),
      DEFAULT_CONTENT_BACKGROUND_COLOR,
    );
    assert.strictEqual(
      normalizeContentBackgroundColor('transparent'),
      DEFAULT_CONTENT_BACKGROUND_COLOR,
    );
  });

  it('normalizes content radius without coercing empty or boolean values', () => {
    assert.strictEqual(normalizeContentRadius(0), 0);
    assert.strictEqual(normalizeContentRadius(80), 80);
    assert.strictEqual(normalizeContentRadius(39.6), 40);
    assert.strictEqual(normalizeContentRadius(-1), DEFAULT_CONTENT_RADIUS);
    assert.strictEqual(normalizeContentRadius(81), DEFAULT_CONTENT_RADIUS);
    assert.strictEqual(normalizeContentRadius(null), DEFAULT_CONTENT_RADIUS);
    assert.strictEqual(normalizeContentRadius(''), DEFAULT_CONTENT_RADIUS);
    assert.strictEqual(normalizeContentRadius(false), DEFAULT_CONTENT_RADIUS);
  });

  it('shows the trimmed current profile bio without rewriting its content', () => {
    const profile = { id: 101, bio: '  期待一起认真生活。\n也尊重彼此的空间。  ' };
    assert.strictEqual(getDeclarationBio(profile), '期待一起认真生活。\n也尊重彼此的空间。');
    assert.strictEqual(formatDeclarationBio(profile), '期待一起认真生活。\n也尊重彼此的空间。');
    assert.strictEqual(hasDeclarationBio(profile), true);
  });

  it('uses an explicit empty state for missing, blank, or non-string bio', () => {
    for (const profile of [null, {}, { bio: '' }, { bio: '   ' }, { bio: 123 }]) {
      assert.strictEqual(getDeclarationBio(profile), '');
      assert.strictEqual(formatDeclarationBio(profile), EMPTY_DECLARATION_TEXT);
      assert.strictEqual(hasDeclarationBio(profile), false);
    }
  });

  it('consumes only the existing read-only profile context and applies no common style', () => {
    const component = read(
      '../sheep/components/s-partner-profile-declaration/s-partner-profile-declaration.vue',
    );
    const helper = read(
      '../sheep/components/s-partner-profile-declaration/partnerProfileDeclaration.js',
    );

    assert.match(component, /inject\('partnerRecommendationContext',\s*null\)/);
    assert.match(component, /v-if="hasContext"/);
    assert.match(component, /context\.value\?\.profile/);
    assert.match(helper, /profile\.bio\.trim\(\)/);
    assert.doesNotMatch(component, /usePartnerRecommendation|useUserStore|useSocialStore/);
    assert.doesNotMatch(component, /fetch\(|request\(|\.get\(|\.post\(|sheep\/api/);
    assert.doesNotMatch(component, /props\.data\?\.style|props\.styles|marginTop|paddingTop/);
    assert.doesNotMatch(component, /window\.|document\.|<svg|ElementPlus/);
    assert.doesNotMatch(helper, /SAMPLE_DECLARATION|期待一起|认真生活/);
  });

  it('uses the same explicit component ID for dispatch and known-ID recognition', () => {
    const blockItem = read('../sheep/components/s-block-item/s-block-item.vue');
    const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js');

    assert.match(
      blockItem,
      /<s-partner-profile-declaration v-if="type === 'PartnerProfileDeclaration'" :data="data"/,
    );
    assert.match(registry, /'PartnerProfileDeclaration'/);
    assert.strictEqual(isKnownDiyComponent('PartnerProfileDeclaration'), true);
    assert.strictEqual(isRetiredDiyComponent('PartnerProfileDeclaration'), false);
    assert.strictEqual(isKnownDiyComponent('FutureDeclarationComponent'), false);
  });
});
