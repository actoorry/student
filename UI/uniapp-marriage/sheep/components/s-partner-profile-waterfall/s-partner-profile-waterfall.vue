<template>
  <view class="profile-waterfall">
    <view v-for="profile in state.items" :key="profile.id" class="profile-card" :style="cardStyle">
      <image
        v-if="currentImage(profile)"
        class="profile-card__image"
        :style="{ width: `${settings.imageWidthPercent}%` }"
        :src="currentImage(profile)"
        mode="aspectFill"
        @error="nextImage(profile)"
      />
      <view
        v-else
        class="profile-card__image profile-card__image--empty"
        :style="{ width: `${settings.imageWidthPercent}%` }"
      >
        <text>暂无照片</text>
      </view>

      <view class="profile-card__content">
        <text
          v-if="settings.fields.name.show"
          class="name"
          :style="{ color: settings.fields.name.color }"
        >
          {{ displayName(profile) }}
        </text>
        <view v-if="visibleBadges(profile, settings).length" class="badges">
          <text
            v-for="badge in visibleBadges(profile, settings)"
            :key="badge"
            class="badge"
            :style="badgeStyle(badge)"
          >
            ✓ {{ badge }}
          </text>
        </view>
        <view v-if="shownAge(profile) || shownSex(profile)" class="age-row">
          <text v-if="shownAge(profile)" class="age" :style="{ color: settings.fields.age.color }">
            {{ shownAge(profile) }}岁
          </text>
          <text v-if="shownSex(profile)" class="sex" :style="{ color: settings.fields.sex.color }">
            {{ shownSex(profile) }}
          </text>
        </view>
        <view v-if="shownText(profile, 'job')" class="detail-row">
          <view class="detail-icon detail-icon--job"></view>
          <text class="detail-label">职业</text>
          <text class="detail-value" :style="{ color: settings.fields.job.color }">
            {{ shownText(profile, 'job') }}
          </text>
        </view>
        <view v-if="shownText(profile, 'city')" class="detail-row">
          <view class="detail-icon detail-icon--location"></view>
          <text class="detail-label">居住地</text>
          <text class="detail-value" :style="{ color: settings.fields.city.color }">
            {{ shownText(profile, 'city') }}
          </text>
        </view>
        <view v-if="hasMissingProfileDetails(profile, settings)" class="missing-profile-tip">
          <text>该用户还没有填写相关资料</text>
        </view>
        <view class="actions">
          <button
            v-if="settings.contactButton.show"
            class="action"
            :style="contactButtonStyle"
            :disabled="state.contactingId === profile.id"
            @click="contact(profile)"
          >
            联系对方
          </button>
          <button
            v-if="settings.profileButton.show"
            class="action"
            :style="profileButtonStyle"
            @click="viewProfile(profile.id)"
          >
            查看资料
          </button>
        </view>
      </view>
    </view>

    <view class="load-more-sentinel" />
    <uni-load-more
      v-if="state.items.length || state.status === 'loading'"
      :status="state.status"
      :content-text="{ contentdown: '上拉加载更多' }"
    />
    <s-empty
      v-if="!state.items.length && state.status !== 'loading'"
      icon="/static/data-empty.png"
      text="暂无推荐"
    />
  </view>
</template>

<script setup>
  import { computed, getCurrentInstance, nextTick, onMounted, onUnmounted, reactive } from 'vue';
  import sheep from '@/sheep';
  import RecommendApi from '@/sheep/api/marriage/recommend';
  import { INTERACTION_GUARD_SCENES, requireRealNameInteraction } from '@/sheep/hooks/useRealNameInteractionGuard';
  import { getResponseData } from '@/sheep/helper/marriage';
  import {
    displayAge,
    displayName,
    displaySex,
    displayText,
    imageCandidates,
    hasMissingProfileDetails,
    visibleBadges,
  } from './partnerProfileWaterfall';

  const props = defineProps({
    data: { type: Object, default: () => ({}) },
    styles: { type: Object, default: () => ({}) },
  });

  const defaultField = () => ({ show: true, color: '#6F5A55' });
  const settings = computed(() => ({
    rule: {
      realVerifiedOnly: props.data.rule?.realVerifiedOnly === true,
      backgroundImageRequired: props.data.rule?.backgroundImageRequired === true,
      oppositeSexOnly: props.data.rule?.oppositeSexOnly === true,
    },
    fields: {
      name: { show: props.data.fields?.name?.show !== false, color: props.data.fields?.name?.color || '#241A18' },
      age: { ...defaultField(), ...props.data.fields?.age },
      sex: { ...defaultField(), ...props.data.fields?.sex },
      job: { ...defaultField(), ...props.data.fields?.job },
      city: { ...defaultField(), ...props.data.fields?.city },
    },
    badges: props.data.badges || {},
    imageWidthPercent: clamp(props.data.imageWidthPercent, 35, 55, 45),
    cardHeight: clamp(props.data.cardHeight, 260, 440, 320),
    cardGap: clamp(props.data.cardGap, 0, 64, 24),
    cardBackgroundColor: props.data.cardBackgroundColor || '#FFFFFF',
    cardRadius: clamp(props.data.cardRadius, 0, 64, 32),
    shadowColor: props.data.shadowColor || '#000000',
    shadowOpacity: clamp(props.data.shadowOpacity, 0, 0.3, 0.08),
    shadowBlur: clamp(props.data.shadowBlur, 0, 48, 24),
    contactButton: { show: true, textColor: '#FFFFFF', backgroundColor: '#D92D45', radius: 28, ...props.data.contactButton },
    profileButton: { show: true, textColor: '#D92D45', backgroundColor: '#FFFFFF', borderColor: '#D92D45', radius: 28, ...props.data.profileButton },
  }));

  const state = reactive({
    items: [],
    total: 0,
    pageNo: 1,
    status: 'more',
    imageIndexes: {},
    contactingId: 0,
  });
  let observer = null;

  const cardStyle = computed(() => ({
    height: `${settings.value.cardHeight}rpx`,
    marginBottom: `${settings.value.cardGap}rpx`,
    backgroundColor: settings.value.cardBackgroundColor,
    borderRadius: `${settings.value.cardRadius}rpx`,
    boxShadow: `0 10rpx ${settings.value.shadowBlur}rpx ${hexToRgba(settings.value.shadowColor, settings.value.shadowOpacity)}`,
  }));
  const contactButtonStyle = computed(() => ({
    color: settings.value.contactButton.textColor,
    backgroundColor: settings.value.contactButton.backgroundColor,
    borderColor: settings.value.contactButton.backgroundColor,
    borderRadius: `${settings.value.contactButton.radius}rpx`,
  }));
  const profileButtonStyle = computed(() => ({
    color: settings.value.profileButton.textColor,
    backgroundColor: settings.value.profileButton.backgroundColor,
    borderColor: settings.value.profileButton.borderColor,
    borderRadius: `${settings.value.profileButton.radius}rpx`,
  }));

  function clamp(value, min, max, fallback) {
    const number = Number(value);
    return Number.isFinite(number) ? Math.min(max, Math.max(min, number)) : fallback;
  }
  function hexToRgba(hex, opacity) {
    const value = /^#[0-9a-f]{6}$/i.test(hex) ? hex.slice(1) : '000000';
    const number = Number.parseInt(value, 16);
    return `rgba(${(number >> 16) & 255}, ${(number >> 8) & 255}, ${number & 255}, ${opacity})`;
  }
  function shownAge(profile) { return settings.value.fields.age.show ? displayAge(profile) : ''; }
  function shownSex(profile) { return settings.value.fields.sex.show ? displaySex(profile) : ''; }
  function shownText(profile, key) { return settings.value.fields[key].show ? displayText(profile, key) : ''; }
  function currentImage(profile) { return imageCandidates(profile)[state.imageIndexes[profile.id] || 0] || ''; }
  function nextImage(profile) { state.imageIndexes[profile.id] = (state.imageIndexes[profile.id] || 0) + 1; }
  function badgeStyle(label) {
    const key = label === '会员' ? 'member' : label === '实名认证' ? 'realName' : 'marriage';
    const config = settings.value.badges[key] || {};
    return { color: config.textColor || '#D92D45', backgroundColor: config.backgroundColor || '#FFF0F2' };
  }

  async function load(reset = false) {
    if (state.status === 'loading' || (!reset && state.status === 'noMore')) return;
    if (reset) {
      state.items = [];
      state.total = 0;
      state.pageNo = 1;
      state.imageIndexes = {};
    }
    state.status = 'loading';
    try {
      const page = getResponseData(
        await RecommendApi.getRecommendPage({
          pageNo: state.pageNo,
          pageSize: 10,
          ...settings.value.rule,
        }),
        '推荐加载失败',
      ) || {};
      const list = Array.isArray(page.list) ? page.list : [];
      const existingIds = new Set(state.items.map((item) => item.id));
      state.items = [...state.items, ...list.filter((item) => item?.id && !existingIds.has(item.id))];
      state.total = Number(page.total) || 0;
      state.status = state.items.length < state.total ? 'more' : 'noMore';
    } catch (error) {
      state.status = 'more';
      uni.showToast({ title: error?.message || '推荐加载失败', icon: 'none' });
    }
  }

  function loadMore() {
    if (state.status !== 'more') return;
    state.pageNo += 1;
    load();
  }
  function viewProfile(id) {
    if (id) sheep.$router.go(`/pages/member-detail/index?id=${id}`);
  }
  async function contact(profile) {
    if (!profile?.id || state.contactingId) return;
    state.contactingId = profile.id;
    try {
      if (!(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.MESSAGE))) return;
      sheep.$router.go(`/pages/messages/chat/index?peerId=${profile.id}`);
    } catch (error) {
      uni.showToast({ title: error?.message || '联系失败', icon: 'none' });
    } finally {
      state.contactingId = 0;
    }
  }

  function initObserver() {
    observer = uni.createIntersectionObserver(getCurrentInstance()?.proxy, { observeAll: false });
    observer.relativeToViewport({ bottom: 120 }).observe('.load-more-sentinel', (result) => {
      if (result.intersectionRatio > 0) loadMore();
    });
  }

  onMounted(async () => {
    await load(true);
    await nextTick();
    initObserver();
  });
  onUnmounted(() => observer?.disconnect());
</script>

<style scoped lang="scss">
  .profile-card { display: flex; overflow: hidden; box-sizing: border-box; }
  .profile-card__image { flex-shrink: 0; height: 100%; background: #f1ecec; }
  .profile-card__image--empty { display: flex; align-items: center; justify-content: center; color: #9b8d8b; font-size: 24rpx; }
  .profile-card__content { display: flex; flex: 1; flex-direction: column; min-width: 0; padding: 20rpx 22rpx; box-sizing: border-box; }
  .age-row, .detail-row, .actions, .badges { display: flex; align-items: center; }
  .name { overflow: hidden; font-size: 36rpx; font-weight: 700; line-height: 44rpx; text-overflow: ellipsis; white-space: nowrap; }
  .badges { flex-wrap: wrap; gap: 6rpx; margin-top: 8rpx; }
  .badge { padding: 4rpx 10rpx; font-size: 18rpx; font-weight: 700; border-radius: 18rpx; }
  .age-row { gap: 28rpx; margin: 10rpx 0; }
  .age { font-size: 30rpx; font-weight: 700; }
  .sex { font-size: 26rpx; }
  .detail-row { min-height: 44rpx; border-top: 1rpx dashed #e4dcdd; }
  .detail-icon { position: relative; width: 25rpx; height: 25rpx; margin: 0 7rpx 0 2rpx; flex-shrink: 0; box-sizing: border-box; color: #ee6d73; }
  .detail-icon--job { height: 19rpx; border: 3rpx solid currentColor; border-radius: 2rpx; }
  .detail-icon--job::before { position: absolute; left: 6rpx; top: -9rpx; width: 8rpx; height: 6rpx; border: 3rpx solid currentColor; border-bottom: 0; border-radius: 2rpx 2rpx 0 0; content: ''; }
  .detail-icon--location { width: 21rpx; height: 21rpx; margin-right: 11rpx; border: 3rpx solid currentColor; border-radius: 50% 50% 50% 0; transform: rotate(-45deg); }
  .detail-icon--location::before { position: absolute; left: 5rpx; top: 5rpx; width: 5rpx; height: 5rpx; border-radius: 50%; background: currentColor; content: ''; }
  .detail-label { width: 72rpx; color: #8a7d7a; font-size: 21rpx; }
  .detail-value { flex: 1; overflow: hidden; font-size: 24rpx; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
  .missing-profile-tip { display: flex; flex: 1; align-items: center; color: #a89c9a; font-size: 22rpx; line-height: 32rpx; }
  .actions { gap: 12rpx; margin-top: auto; }
  .action { flex: 1; height: 48rpx; padding: 0; margin: 0; font-size: 22rpx; font-weight: 700; line-height: 46rpx; border: 2rpx solid; }
  .action::after { border: none; }
  .load-more-sentinel { height: 1px; }
</style>
