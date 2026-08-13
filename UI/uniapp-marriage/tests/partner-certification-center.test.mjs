import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const mallRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const repoRoot = path.resolve(mallRoot, '..', '..');
const readMall = (relative) => fs.readFileSync(path.join(mallRoot, relative), 'utf8');
const readRepo = (relative) => fs.readFileSync(path.join(repoRoot, relative), 'utf8');
const profileHelperSource = readMall('pages/mine-profile/partner-certification.js');
const centerHelperSource = readMall('pages/mine-certifications/partner-certification.js');
const profileHelper = await import(`data:text/javascript;base64,${Buffer.from(profileHelperSource).toString('base64')}`);
const centerHelper = await import(`data:text/javascript;base64,${Buffer.from(centerHelperSource).toString('base64')}`);
const helper = { ...profileHelper, ...centerHelper };

test('certification routes are compiled, while only the center is a management link', () => {
  const pages = JSON.parse(readMall('pages.json'));
  const paths = new Set([
    ...(pages.pages || []).map((item) => `/${item.path}`),
    ...(pages.subPackages || []).flatMap((pkg) => pkg.pages.map((item) => `/${pkg.root}/${item.path}`)),
  ]);
  [
    '/pages/mine-certifications/index',
    '/pages/mine-profile/nameCheck/index',
    '/pages/mine-profile/marriageCheck/index',
  ].forEach((route) => assert.ok(paths.has(route), `${route} must be compiled`));
  const centerPackage = pages.subPackages.find((item) => item.root === 'pages/mine-certifications');
  assert.equal(centerPackage.pages[0].meta.auth, false);
  const profilePackage = pages.subPackages.find((item) => item.root === 'pages/mine-profile');
  assert.equal(profilePackage.pages.find((item) => item.path === 'nameCheck/index').meta.auth, true);
  assert.equal(profilePackage.pages.find((item) => item.path === 'marriageCheck/index').meta.auth, true);
  const data = readRepo('UI/vue3-admin/src/components/AppLinkInput/data.ts');
  assert.match(data, /我的认证', path: '\/pages\/mine-certifications\/index'/);
  assert.doesNotMatch(data, /path: '\/pages\/mine-profile\/(?:nameCheck|marriageCheck)\/index'/);
});

test('state mapping, input boundaries and sensitive-form cleanup are deterministic', () => {
  assert.deepEqual(helper.certificationState({ realVerified: 0, singleVerified: 0 }), {
    realVerified: false, marriageVerified: false, realAction: '去认证', marriageAction: '需先实名',
  });
  assert.equal(helper.certificationState({ realVerified: 1, singleVerified: 1 }).marriageAction, '查看结果');
  assert.equal(helper.validateRealName('张三', '11010119900101001x').idCard.endsWith('X'), true);
  assert.equal(helper.validateRealName('John', '110101199001010011').valid, false);
  assert.equal(helper.validateRealName('张'.repeat(21), '110101199001010011').valid, false);
  const form = { name: '张三', idCard: '110101199001010011' };
  helper.clearSensitiveForm(form);
  assert.deepEqual(form, { name: '', idCard: '' });
});

test('certification pages guard session changes and keep PII out of routes and storage', () => {
  const source = [
    readMall('pages/mine-certifications/index.vue'),
    readMall('pages/mine-profile/nameCheck/index.vue'),
    readMall('pages/mine-profile/marriageCheck/index.vue'),
  ].join('\n');
  assert.match(source, /from '\.\.\/partner-certification'/);
  assert.match(readMall('pages/mine-certifications/index.vue'), /from '\.\/partner-certification'/);
  assert.match(source, /requestId !== state\.requestId/);
  assert.match(source, /onHide\(\(\) => clearSensitiveForm\(form\)\)/);
  assert.match(source, /sheep\.\$store\('user'\)\.isLogin/);
  assert.doesNotMatch(source, /setStorageSync|localStorage|idCard=.*\?/);
  assert.match(readMall('sheep/api/partner/certification.js'), /custom: quiet/);
  assert.match(readMall('sheep/api/marriage/profile.js'), /get-real-name-info/);
});
