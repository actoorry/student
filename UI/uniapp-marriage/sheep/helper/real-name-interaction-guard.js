export const INTERACTION_GUARD_SCENES = Object.freeze({
  MESSAGE: 'message',
  FOLLOW: 'follow',
  PUBLISH: 'publish',
  COMMENT: 'comment',
  LIKE: 'like',
});

const CERTIFICATION_ROUTE = '/pages/mine-certifications/index';
const PERSON_SCENES = new Set([INTERACTION_GUARD_SCENES.MESSAGE, INTERACTION_GUARD_SCENES.FOLLOW]);

export function getVerificationPrompt(scene) {
  const verifiedScene = Object.values(INTERACTION_GUARD_SCENES).includes(scene)
    ? scene
    : INTERACTION_GUARD_SCENES.MESSAGE;
  return Object.freeze({
    scene: verifiedScene,
    kind: PERSON_SCENES.has(verifiedScene) ? 'person' : 'dynamic',
    route: CERTIFICATION_ROUTE,
  });
}

function normalizeIdentity(identity) {
  const numericIdentity = Number(identity);
  return Number.isSafeInteger(numericIdentity) && numericIdentity > 0 ? String(numericIdentity) : '';
}

export function createRealNameInteractionGuard({ now = () => Date.now(), ttl = 5 * 60 * 1000 } = {}) {
  let activeIdentity = '';
  let generation = 0;
  const positiveCache = new Map();
  const inFlight = new Map();

  function changeIdentity(identity) {
    if (identity === activeIdentity) return;
    activeIdentity = identity;
    generation += 1;
    positiveCache.clear();
    inFlight.clear();
  }

  function invalidate() {
    generation += 1;
    positiveCache.clear();
    inFlight.clear();
  }

  async function check({ identity, fetchStatus }) {
    const identityKey = normalizeIdentity(identity);
    if (!identityKey) {
      changeIdentity('');
      return { code: 'login-required' };
    }
    changeIdentity(identityKey);
    const cachedUntil = positiveCache.get(identityKey);
    if (cachedUntil && cachedUntil > now()) return { code: 'allowed', cached: true };

    const currentGeneration = generation;
    let request = inFlight.get(identityKey);
    if (!request) {
      request = Promise.resolve()
        .then(fetchStatus)
        .then((status) => {
          if (status === 1) {
            if (generation === currentGeneration && activeIdentity === identityKey) {
              positiveCache.set(identityKey, now() + ttl);
            }
            return { code: 'allowed' };
          }
          return { code: 'verification-required' };
        })
        .catch(() => ({ code: 'check-failed' }))
        .finally(() => {
          if (inFlight.get(identityKey) === request) inFlight.delete(identityKey);
        });
      inFlight.set(identityKey, request);
    }
    return request;
  }

  return Object.freeze({ check, invalidate, get identity() { return activeIdentity; } });
}

export const realNameInteractionGuard = createRealNameInteractionGuard();

export function invalidateRealNameInteractionGuard() {
  realNameInteractionGuard.invalidate();
}
