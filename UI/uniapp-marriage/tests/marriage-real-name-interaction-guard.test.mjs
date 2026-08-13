import assert from 'node:assert/strict';
import test from 'node:test';

import {
  INTERACTION_GUARD_SCENES,
  createRealNameInteractionGuard,
  getVerificationPrompt,
} from '../sheep/helper/real-name-interaction-guard.js';

test('only the five code-owned interaction scenes expose fixed prompt metadata', () => {
  assert.deepEqual(Object.keys(INTERACTION_GUARD_SCENES).sort(), ['COMMENT', 'FOLLOW', 'LIKE', 'MESSAGE', 'PUBLISH']);
  assert.equal(getVerificationPrompt(INTERACTION_GUARD_SCENES.MESSAGE).kind, 'person');
  assert.equal(getVerificationPrompt(INTERACTION_GUARD_SCENES.LIKE).kind, 'dynamic');
  assert.equal(getVerificationPrompt('arbitrary-route').route, '/pages/mine-certifications/index');
  assert.equal(getVerificationPrompt('arbitrary-route').scene, INTERACTION_GUARD_SCENES.MESSAGE);
});

test('requires login, strictly accepts numeric one, and keeps failed checks distinct', async () => {
  const guard = createRealNameInteractionGuard();
  assert.equal((await guard.check({ identity: null, fetchStatus: async () => 1 })).code, 'login-required');
  assert.equal((await guard.check({ identity: 10, fetchStatus: async () => 1 })).code, 'allowed');
  guard.invalidate();
  assert.equal((await guard.check({ identity: 10, fetchStatus: async () => '1' })).code, 'verification-required');
  guard.invalidate();
  assert.equal((await guard.check({ identity: 10, fetchStatus: async () => { throw new Error('offline'); } })).code, 'check-failed');
});

test('caches only positive verification, single-flights per identity, expires, and isolates late identities', async () => {
  let now = 1000;
  const guard = createRealNameInteractionGuard({ now: () => now, ttl: 300000 });
  let calls = 0;
  let release;
  const pending = new Promise((resolve) => { release = resolve; });
  const fetchStatus = async () => { calls += 1; await pending; return 1; };
  const first = guard.check({ identity: 10, fetchStatus });
  const second = guard.check({ identity: 10, fetchStatus });
  await Promise.resolve();
  assert.equal(calls, 1);
  release();
  assert.equal((await first).code, 'allowed');
  assert.equal((await second).code, 'allowed');
  assert.equal((await guard.check({ identity: 10, fetchStatus })).code, 'allowed');
  assert.equal(calls, 1);
  now += 300001;
  await guard.check({ identity: 10, fetchStatus: async () => { calls += 1; return 0; } });
  assert.equal(calls, 2);

  let resolveOld;
  const old = guard.check({ identity: 11, fetchStatus: () => new Promise((resolve) => { resolveOld = resolve; }) });
  const current = await guard.check({ identity: 12, fetchStatus: async () => 0 });
  resolveOld(1);
  assert.equal((await old).code, 'allowed');
  assert.equal(current.code, 'verification-required');
  let newCalls = 0;
  await guard.check({ identity: 12, fetchStatus: async () => { newCalls += 1; return 0; } });
  assert.equal(newCalls, 1);
});
