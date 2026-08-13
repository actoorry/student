/**
 * Code-owned extension registry for persisted sales-DIY component IDs.
 * The ordinary renderer keeps its established core mappings below; every
 * additional stable ID is registered here with its compiled uni-app renderer.
 */
export const SHARED_DIY_RENDERERS = Object.freeze({})

/**
 * Persisted blocks in retained templates that are intentionally no longer
 * rendered. They differ from unknown IDs, which remain diagnosable fallbacks.
 */
export const RETIRED_DIY_COMPONENT_IDS = Object.freeze([
  'MarriageLoginPrompt',
  'MarriageRecommendProfileDeck',
  'MarriageIdentityTrustBanner',
  'PartnerFloatingInteractionButtons'
])

export const STANDARD_DIY_COMPONENT_IDS = Object.freeze([
  'SearchBar', 'NoticeBar', 'MenuSwiper', 'MenuTextGrid', 'MenuList', 'MenuGrid', 'Popover',
  'FloatingActionButton', 'ImageBar', 'Carousel', 'HomeBackgroundSwipe', 'TitleBar', 'MagicCube', 'VideoPlayer',
  'Divider', 'HotZone', 'ProductCard', 'ProductList', 'ProductRow', 'ProductWaterfall',
  'PromotionCombination', 'PromotionSeckill', 'PromotionPoint', 'MpLive', 'CouponCard',
  'PromotionArticle', 'UserCard', 'UserOrder', 'UserWallet',
  'UserCoupon', 'UserAuthButton', 'PartnerProfileHero', 'PartnerProfileDeclaration',
  'PartnerProfileBasicInfo', 'PartnerRealNameVerificationBadge', 'PartnerMarriageVerificationBadge',
  'PartnerProfilePreference', 'PartnerProfileAlbum', 'PartnerProfileMoment', 'PartnerUserHeader',
  'PartnerPrivacyAgreement', 'PartnerProfileWaterfall'
])

export function getSharedDiyRenderer(componentId) {
  const renderer = SHARED_DIY_RENDERERS[componentId]
  if (!renderer) return null
  return renderer
}

export function isKnownDiyComponent(componentId) {
  return STANDARD_DIY_COMPONENT_IDS.includes(componentId) || !!getSharedDiyRenderer(componentId)
}

export function isRetiredDiyComponent(componentId) {
  return RETIRED_DIY_COMPONENT_IDS.includes(componentId)
}
