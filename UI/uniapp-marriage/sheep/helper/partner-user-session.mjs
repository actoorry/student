export function shouldRefreshPartnerUserProfile(loggedIn, previousLoggedIn) {
  return loggedIn !== previousLoggedIn;
}

export function beginPartnerUserProfileRefresh(state, authenticated) {
  const generation = ++state.generation;
  const isAuthenticated = authenticated === true;
  state.authenticated = isAuthenticated;
  state.error = '';
  if (!isAuthenticated) {
    state.loading = false;
    state.profile = null;
    return { generation, shouldLoad: false };
  }
  state.loading = true;
  return { generation, shouldLoad: true };
}

