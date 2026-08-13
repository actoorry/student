import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import {
  buildAlbumCandidates,
  computeIdentityKey,
  createAlbumMediaState,
  getSuccessfulAlbumCandidates,
  getVisibleAlbumItems,
  normalizeImageHeight,
  normalizeImageRadius,
  normalizeSubtitle,
  normalizeTitle,
  transitionAlbumMediaState,
} from '../sheep/components/s-partner-profile-album/partnerProfileAlbum.js'

const root = dirname(fileURLToPath(import.meta.url))
const read = (path) => readFileSync(join(root, path), 'utf8')

describe('PartnerProfileAlbum media contract', () => {
  it('uses only albumImages, preserving the first nine valid unique values', () => {
    const profile = {
      mainImage: 'main.jpg',
      albumImages: [' a.jpg ', '', 1, 'b.jpg', 'a.jpg', ...Array.from({ length: 10 }, (_, i) => `p${i}.jpg`)],
    }
    const snapshot = JSON.stringify(profile)
    assert.deepEqual(buildAlbumCandidates(profile), ['a.jpg', 'b.jpg', 'p0.jpg', 'p1.jpg', 'p2.jpg', 'p3.jpg', 'p4.jpg', 'p5.jpg', 'p6.jpg'])
    assert.equal(JSON.stringify(profile), snapshot)
  })

  it('keeps Unicode text bounds and visual number bounds deterministic', () => {
    assert.equal(normalizeTitle('😀'.repeat(12)), '😀'.repeat(12))
    assert.equal(normalizeTitle('😀'.repeat(13)), '相册')
    assert.equal(normalizeSubtitle(''), '')
    assert.equal(normalizeImageHeight(120.6), 121)
    assert.equal(normalizeImageHeight(481), 240)
    assert.equal(normalizeImageRadius(-1), 28)
  })

  it('isolates old generations and makes image transitions one-way', () => {
    const first = createAlbumMediaState({ albumImages: ['a.jpg', 'b.jpg'] }, 3)
    const succeeded = transitionAlbumMediaState(first, 3, 'a.jpg', 'success')
    const ignoredOpposite = transitionAlbumMediaState(succeeded, 3, 'a.jpg', 'failed')
    const next = createAlbumMediaState({ albumImages: ['next.jpg'] }, 4)
    assert.strictEqual(ignoredOpposite, succeeded)
    assert.strictEqual(transitionAlbumMediaState(next, 3, 'a.jpg', 'success'), next)
    const failed = transitionAlbumMediaState(succeeded, 3, 'b.jpg', 'failed')
    assert.deepEqual(getVisibleAlbumItems(failed).map((item) => item.candidate), ['a.jpg'])
    assert.deepEqual(getSuccessfulAlbumCandidates(failed), ['a.jpg'])
  })

  it('uses a structured identity so separators in URLs cannot collide', () => {
    assert.notEqual(computeIdentityKey(1, ['a|b', 'c']), computeIdentityKey(1, ['a', 'b|c']))
  })
})

describe('PartnerProfileAlbum renderer contract', () => {
  const component = read('../sheep/components/s-partner-profile-album/s-partner-profile-album.vue')
  const blockItem = read('../sheep/components/s-block-item/s-block-item.vue')
  const registry = read('../sheep/components/s-block-item/shared-diy-renderers.js')

  it('is explicitly dispatched and known without changing retired IDs', () => {
    assert.match(blockItem, /<s-partner-profile-album v-if="type === 'PartnerProfileAlbum'"/)
    assert.match(registry, /'PartnerProfileAlbum'/)
    assert.match(registry, /'MarriageRecommendProfileDeck'/)
  })

  it('uses context, CDN resolution, generation-bound media events and click-only recording', () => {
    assert.match(component, /import sheep from '@\/sheep'/)
    assert.match(component, /inject\('partnerRecommendationContext',\s*null\)/)
    assert.match(component, /sheep\.\$url\.cdn\(url\)/)
    assert.match(component, /@load="handleImageLoad\(item\.candidate, item\.generation\)"/)
    assert.match(component, /uni\.previewImage/)
    assert.match(component, /InteractionApi\.recordView\(targetPartnerId\)/)
    assert.doesNotMatch(component, /recommend-member\/page|auth\/get-album-images|console\.log/)
  })

  it('keeps an explicit empty state when the album is empty or all images fail', () => {
    assert.match(component, /const shouldRender = computed\(\(\) => hasContext\.value\)/)
    assert.match(component, /const hasVisibleImages = computed/)
    assert.match(component, /v-if="hasVisibleImages"/)
    assert.match(component, /该用户暂未上传相册/)
  })
})
