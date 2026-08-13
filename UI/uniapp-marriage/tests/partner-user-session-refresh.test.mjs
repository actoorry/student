import assert from 'node:assert/strict';
import test from 'node:test';

const importSessionContract = async () => {
  try {
    return await import('../sheep/helper/partner-user-session.mjs');
  } catch (error) {
    assert.fail(`partner user session contract is unavailable: ${error.message}`);
  }
};

test('logging out refreshes the current-user profile runtime immediately', async () => {
  const { shouldRefreshPartnerUserProfile } = await importSessionContract();

  assert.equal(shouldRefreshPartnerUserProfile(false, true), true);
  assert.equal(shouldRefreshPartnerUserProfile(true, false), true);
  assert.equal(shouldRefreshPartnerUserProfile(false, false), false);
});

test('anonymous profile refresh clears stale identity and invalidates an in-flight request', async () => {
  const { beginPartnerUserProfileRefresh } = await importSessionContract();
  const state = {
    authenticated: true,
    loading: true,
    error: '旧错误',
    profile: { userId: 7, nickname: '哈哈' },
    generation: 3,
  };

  const pendingLogin = beginPartnerUserProfileRefresh(state, true);
  const logout = beginPartnerUserProfileRefresh(state, false);

  assert.deepEqual(logout, { generation: 5, shouldLoad: false });
  assert.deepEqual(state, {
    authenticated: false,
    loading: false,
    error: '',
    profile: null,
    generation: 5,
  });
  assert.notEqual(pendingLogin.generation, state.generation);
});
