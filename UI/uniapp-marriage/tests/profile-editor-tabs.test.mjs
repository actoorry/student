import { describe, it } from 'node:test';
import assert from 'node:assert';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = dirname(fileURLToPath(import.meta.url));
const page = readFileSync(join(root, '../pages/mine-profile/basic/index.vue'), 'utf8');

describe('marriage profile editor tab contract', () => {
  it('uses native template pickers instead of runtime render-function form rows', () => {
    assert.match(page, /<picker[\s\S]*?>/);
    assert.doesNotMatch(page, /const FieldRow|const AreaRow|\bh\(['"]picker['"]/);
  });

  it('keeps all three tabs and the complete preference field order', () => {
    assert.match(page, /资料与相册[\s\S]*择偶条件[\s\S]*预览主页/);
    assert.match(
      page,
      /婚姻状态[\s\S]*身高范围[\s\S]*体重范围[\s\S]*学历[\s\S]*现居地[\s\S]*收入档位[\s\S]*房产情况[\s\S]*车辆情况[\s\S]*职业/,
    );
  });

  it('submits explicit profile and preference API payloads', () => {
    assert.match(page, /ProfileApi\.updateMyProfile\(profilePayload\(\)\)/);
    assert.match(page, /ProfileApi\.updateMyPreference\(preferencePayload\(\)\)/);
    for (const field of [
      'maritalStatus',
      'mateMinHeightCm',
      'mateMaxHeightCm',
      'mateMinWeightKg',
      'mateMaxWeightKg',
      'mateLiveAreaId',
      'mateRemark',
    ]) {
      assert.match(page, new RegExp(`${field}:`));
    }
  });

  it('renders profile details, preference tags, and album in preview', () => {
    assert.match(page, /profileTags/);
    assert.match(page, /preferenceTags/);
    assert.match(page, /class="preview-album"/);
  });

  it('keeps validation, retry, and duplicate-submit guards', () => {
    assert.match(page, /请输入昵称/);
    assert.match(page, /昵称不能超过 30 个字/);
    assert.match(page, /state\.loadError/);
    assert.match(page, /if\(state\.saving\)return/);
    assert.match(page, /loadGeneration/);
  });
});
