const EMPTY_VIEWPORT_STYLES = Object.freeze({
  main: Object.freeze({}),
  body: Object.freeze({}),
  scroll: Object.freeze({}),
});

const FITTED_VIEWPORT_STYLES = Object.freeze({
  main: Object.freeze({ height: '100%', minHeight: 0, overflow: 'hidden' }),
  body: Object.freeze({
    display: 'flex',
    flexDirection: 'column',
    minHeight: 0,
    overflow: 'hidden',
  }),
  scroll: Object.freeze({ flex: 1, minHeight: 0 }),
});

export function getViewportLayoutStyles(fitViewport) {
  return fitViewport ? FITTED_VIEWPORT_STYLES : EMPTY_VIEWPORT_STYLES;
}
