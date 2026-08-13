<template>
  <s-layout
    title="个人主页"
    navbar="normal"
    showLeftButton
    :bgStyle="{ backgroundColor: 'var(--marriage-soft)' }"
    :onShareAppMessage="shareInfo"
  >
    <scroll-view class="profile-scroll" scroll-y>
      <view v-if="loading" class="state-card">资料加载中...</view>
      <view v-else-if="unavailable" class="state-card">
        <text class="state-title">{{ errorText || '该会员资料暂不可用' }}</text>
        <view class="state-actions">
          <view v-if="memberId" class="secondary-button" @tap="loadMember">重试</view>
          <view class="primary-button" @tap="safeBack">返回</view>
        </view>
      </view>
      <view v-else class="profile-content">
        <view class="hero-card" @tap="previewHero">
          <image v-if="heroImage" class="hero-image" :src="heroImage" mode="aspectFill" />
          <view v-else class="hero-placeholder">
            <image v-if="member.avatarImage" class="fallback-avatar" :src="member.avatarImage" mode="aspectFill" />
            <text v-else>{{ avatarText }}</text>
          </view>
          <view class="hero-shade" />
          <view class="hero-copy">
            <view class="name-row">
              <text class="profile-name">{{ member.name || '处佳缘用户' }}</text>
              <text v-if="member.age" class="profile-age">{{ member.age }}岁</text>
              <text v-if="member.verifiedLabel" class="verified-tag">{{ member.verifiedLabel }}</text>
            </view>
            <text class="hero-summary">{{ summary || '资料待完善' }}</text>
            <view v-if="visibleTags.length" class="tag-row">
              <text v-for="tag in visibleTags" :key="tag" class="hero-tag">{{ tag }}</text>
            </view>
          </view>
        </view>

        <view v-if="certifications.length" class="certification-row">
          <view v-for="item in certifications" :key="item.key" class="certification-card">
            <text class="certification-icon">✓</text>
            <view>
              <text class="certification-title">{{ item.title }}</text>
              <text class="certification-desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <text class="section-title">关于我</text>
          <text class="bio">{{ member.bio || '这个人还没有填写个人介绍' }}</text>
          <view v-if="facts.length" class="fact-grid">
            <view v-for="fact in facts" :key="fact.label" class="fact-item">
              <text class="fact-label">{{ fact.label }}</text>
              <text class="fact-value">{{ fact.value }}</text>
            </view>
          </view>
          <view v-else class="section-empty">
            <text>该用户暂未填写资料</text>
          </view>
        </view>

        <view class="section-card">
          <text class="section-title">择偶条件</text>
          <view v-if="member.interestTags.length" class="interest-row">
            <text v-for="tag in member.interestTags" :key="tag" class="interest-tag">{{ tag }}</text>
          </view>
          <view v-else class="section-empty">
            <text>该用户暂未填写择偶条件</text>
          </view>
        </view>

        <view class="section-card">
          <text class="section-title">真实相册</text>
          <view v-if="albumImages.length" class="album-grid">
            <image
              v-for="(image, index) in albumImages"
              :key="image"
              class="album-image"
              :src="image"
              mode="aspectFill"
              @tap="previewAlbum(index)"
            />
          </view>
          <view v-else class="section-empty section-empty--album">
            <text class="section-empty-icon">＋</text>
            <text>该用户暂未上传相册</text>
          </view>
        </view>
        <view class="action-spacer" />
      </view>
    </scroll-view>

    <view v-if="showInteractionActions" class="profile-actions">
      <view class="secondary-button" @tap="shareProfile">分享</view>
      <view class="secondary-button" :class="{ selected: followed }" @tap="followMember">
        {{ followed ? '已关注' : '关注' }}
      </view>
      <view class="primary-button" @tap="openChat">打招呼</view>
    </view>
  </s-layout>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import { onLoad, onUnload } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { showAuthModal, showShareModal } from '@/sheep/hooks/useModal';
  import { INTERACTION_GUARD_SCENES, requireRealNameInteraction } from '@/sheep/hooks/useRealNameInteractionGuard';
  import MarriageProfileApi from '@/sheep/api/marriage/profile';
  import InteractionApi from '@/sheep/api/marriage/interaction';
  import { getResponseData, parsePositiveId } from '@/sheep/helper/marriage';
  import {
    buildMemberShareInfo,
    capturePendingBindUserId,
  } from '@/sheep/helper/member-profile-share';

  const emptyMember = () => ({
    id: 0,
    sex: 0,
    name: '',
    age: 0,
    city: '',
    education: '',
    job: '',
    height: 0,
    weight: 0,
    income: '',
    maritalStatus: '',
    houseStatus: '',
    carStatus: '',
    realVerified: 0,
    marriageVerified: 0,
    verifiedLabel: '',
    avatarImage: '',
    mainImage: '',
    onlineLabel: '',
    viewerLabel: '',
    bio: '',
    tags: [],
    interestTags: [],
    albumImages: [],
  });

  const memberId = ref(0);
  const currentPartnerId = ref(0);
  const currentPartnerResolved = ref(false);
  const recordedMemberId = ref(0);
  const loading = ref(false);
  const unavailable = ref(true);
  const errorText = ref('');
  const followed = ref(false);
  const member = ref(emptyMember());
  let loadSequence = 0;

  const heroImage = computed(
    () => member.value.mainImage || member.value.albumImages[0] || member.value.avatarImage || '',
  );
  const albumImages = computed(() => member.value.albumImages.filter(Boolean));
  const avatarText = computed(() => String(member.value.name || '缘').slice(0, 1));
  const summary = computed(() =>
    [
      Number(member.value.age) > 0 ? `${member.value.age}岁` : '',
      member.value.city,
      Number(member.value.height) > 0 ? `${member.value.height}cm` : '',
      member.value.education,
    ]
      .filter(Boolean)
      .join(' · '),
  );
  const visibleTags = computed(() =>
    [member.value.onlineLabel, member.value.viewerLabel, ...member.value.tags].filter(Boolean).slice(0, 5),
  );
  const facts = computed(() =>
    [
      ['现居地区', member.value.city],
      ['职业', member.value.job],
      ['身高', Number(member.value.height) > 0 ? `${member.value.height}cm` : ''],
      ['体重', Number(member.value.weight) > 0 ? `${member.value.weight}kg` : ''],
      ['收入', member.value.income],
      ['学历', member.value.education],
      ['婚况', member.value.maritalStatus],
      ['住房', member.value.houseStatus],
      ['车辆', member.value.carStatus],
    ]
      .filter(([, value]) => value)
      .map(([label, value]) => ({ label, value })),
  );
  const certifications = computed(() => {
    const items = [];
    if (Number(member.value.realVerified) === 1) {
      items.push({ key: 'real', title: '实名认证', desc: '身份已核验' });
    }
    if (Number(member.value.marriageVerified) === 1) {
      items.push({ key: 'marriage', title: '婚恋认证', desc: '婚恋状态已核验' });
    }
    return items;
  });
  const isSelf = computed(
    () => currentPartnerResolved.value && currentPartnerId.value > 0 && currentPartnerId.value === memberId.value,
  );
  const showInteractionActions = computed(() => !loading.value && !unavailable.value && !isSelf.value);
  const shareInfo = computed(() =>
    buildMemberShareInfo(member.value, currentPartnerId.value || sheep.$store('user').userInfo?.id) || {},
  );
  const toast = (title) => uni.showToast({ title, icon: 'none' });

  async function resolveCurrentPartner() {
    currentPartnerResolved.value = false;
    currentPartnerId.value = 0;
    if (!sheep.$store('user').isLogin) return;
    try {
      const current = getResponseData(await MarriageProfileApi.getLoginUser(), '当前会员资料加载失败');
      currentPartnerId.value = parsePositiveId(current?.userId);
      currentPartnerResolved.value = currentPartnerId.value > 0;
    } catch (_) {
      // 保持公开资料可用；身份未确认时不记录浏览，避免把本人浏览误写成关系数据。
    }
  }

  async function loadMember() {
    if (!memberId.value) {
      unavailable.value = true;
      errorText.value = '缺少有效的会员编号';
      return;
    }
    const requestId = ++loadSequence;
    loading.value = true;
    unavailable.value = false;
    errorText.value = '';
    member.value = emptyMember();
    followed.value = false;
    try {
      await resolveCurrentPartner();
      const profile = getResponseData(
        await MarriageProfileApi.get(memberId.value),
        '会员资料加载失败',
      );
      if (requestId !== loadSequence) return;
      member.value = { ...emptyMember(), ...(profile || {}) };
      unavailable.value = !parsePositiveId(member.value.id);
      if (unavailable.value) {
        errorText.value = '该会员资料暂不可用';
        return;
      }
      recordViewIfEligible();
    } catch (error) {
      if (requestId !== loadSequence) return;
      unavailable.value = true;
      errorText.value = error.message || '会员资料加载失败';
    } finally {
      if (requestId === loadSequence) loading.value = false;
    }
  }

  function recordViewIfEligible() {
    if (
      !sheep.$store('user').isLogin ||
      !currentPartnerResolved.value ||
      isSelf.value ||
      recordedMemberId.value === memberId.value
    ) {
      return;
    }
    recordedMemberId.value = memberId.value;
    InteractionApi.recordView(memberId.value)
      .then((response) => getResponseData(response, '记录浏览失败'))
      .catch(() => {});
  }

  function requireLogin() {
    if (sheep.$store('user').isLogin) return true;
    showAuthModal();
    return false;
  }


  async function followMember() {
    if (followed.value || isSelf.value || !(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.FOLLOW))) return;
    try {
      getResponseData(await InteractionApi.follow(memberId.value), '关注失败');
      followed.value = true;
      toast('已关注');
    } catch (error) {
      toast(error.message || '关注失败');
    }
  }

  async function openChat() {
    if (isSelf.value || !(await requireRealNameInteraction(INTERACTION_GUARD_SCENES.MESSAGE))) return;
    sheep.$router.go('/pages/messages/chat/index', {
      peerId: memberId.value,
      nickname: encodeURIComponent(member.value.name || ''),
      avatar: encodeURIComponent(member.value.avatarImage || ''),
    });
  }

  function previewHero() {
    if (heroImage.value) uni.previewImage({ urls: [heroImage.value], current: heroImage.value });
  }

  function previewAlbum(index) {
    uni.previewImage({ urls: albumImages.value, current: albumImages.value[index] });
  }

  function shareProfile() {
    showShareModal();
  }

  function safeBack() {
    if (sheep.$router.hasHistory()) sheep.$router.back();
    else sheep.$router.go('/pages/index/index');
  }

  onLoad((options) => {
    capturePendingBindUserId(options, sheep.$store('user').isLogin);
    memberId.value = parsePositiveId(options?.id);
    loadMember();
  });
  onUnload(() => {
    loadSequence += 1;
  });
</script>

<style scoped>
  .profile-scroll { height: calc(100vh - 90rpx); background: var(--marriage-soft); }
  .profile-content { padding: 20rpx 24rpx; }
  .state-card { margin: 30rpx 24rpx; padding: 90rpx 36rpx; color: var(--marriage-muted); text-align: center; background: var(--marriage-surface); border-radius: 24rpx; box-shadow: var(--marriage-shadow); }
  .state-title { display: block; font-size: 28rpx; }
  .state-actions { display: flex; justify-content: center; gap: 20rpx; margin-top: 30rpx; }
  .hero-card { position: relative; height: 820rpx; overflow: hidden; background: #ff8e8e; border-radius: 24rpx; }
  .hero-image { width: 100%; height: 100%; }
  .hero-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #fff; font-size: 120rpx; }
  .fallback-avatar { width: 180rpx; height: 180rpx; border: 6rpx solid rgba(255,255,255,.65); border-radius: 50%; }
  .hero-shade { position: absolute; inset: 0; background: linear-gradient(180deg,transparent 42%,rgba(42,24,29,.78)); }
  .hero-copy { position: absolute; left: 32rpx; right: 32rpx; bottom: 38rpx; color: #fff; }
  .name-row { display: flex; align-items: center; flex-wrap: wrap; gap: 14rpx; }
  .profile-name { font-size: 48rpx; font-weight: 800; }
  .profile-age { font-size: 27rpx; }
  .verified-tag { padding: 7rpx 13rpx; color: #fff; font-size: 21rpx; background: rgba(255,107,107,.92); border-radius: 12rpx; }
  .hero-summary { display: block; margin-top: 12rpx; font-size: 27rpx; }
  .tag-row, .interest-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 18rpx; }
  .hero-tag { padding: 8rpx 14rpx; font-size: 21rpx; background: rgba(255,255,255,.2); border: 1rpx solid rgba(255,255,255,.35); border-radius: 999rpx; }
  .certification-row { margin-top: 20rpx; }
  .certification-card { display: flex; gap: 16rpx; align-items: center; margin-top: 12rpx; padding: 22rpx; background: var(--marriage-mint); border-radius: 24rpx; }
  .certification-icon { display: flex; align-items: center; justify-content: center; width: 50rpx; height: 50rpx; color: #fff; background: var(--marriage-primary); border-radius: 50%; }
  .certification-title, .certification-desc { display: block; }
  .certification-title { color: var(--marriage-text); font-size: 27rpx; font-weight: 700; }
  .certification-desc { margin-top: 5rpx; color: var(--marriage-muted); font-size: 21rpx; }
  .section-card { margin-top: 20rpx; padding: 30rpx; background: var(--marriage-surface); border-radius: 24rpx; box-shadow: var(--marriage-shadow); }
  .section-title { color: var(--marriage-text); font-size: 31rpx; font-weight: 750; }
  .bio { display: block; margin-top: 18rpx; color: var(--marriage-muted); font-size: 27rpx; line-height: 1.75; }
  .fact-grid { display: flex; flex-wrap: wrap; margin-top: 20rpx; }
  .fact-item { width: 50%; padding: 16rpx 0; box-sizing: border-box; }
  .fact-label { display: block; color: var(--marriage-muted); font-size: 22rpx; }
  .fact-value { display: block; margin-top: 7rpx; color: var(--marriage-text); font-size: 27rpx; }
  .interest-tag { padding: 12rpx 19rpx; color: #61568b; font-size: 24rpx; background: var(--marriage-lavender); border-radius: 999rpx; }
  .section-empty { display: flex; align-items: center; justify-content: center; min-height: 120rpx; margin-top: 20rpx; padding: 24rpx; box-sizing: border-box; color: var(--marriage-muted); font-size: 24rpx; background: var(--marriage-soft); border-radius: 16rpx; }
  .section-empty--album { flex-direction: column; min-height: 220rpx; }
  .section-empty-icon { display: block; margin-bottom: 8rpx; color: var(--marriage-primary); font-size: 50rpx; font-weight: 300; line-height: 50rpx; }
  .album-grid { display: flex; flex-wrap: wrap; gap: 10rpx; margin-top: 20rpx; }
  .album-image { width: calc((100% - 20rpx) / 3); height: 190rpx; border-radius: 16rpx; }
  .action-spacer { height: 150rpx; }
  .profile-actions { position: fixed; z-index: 20; left: 0; right: 0; bottom: 0; display: flex; gap: 16rpx; padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom)); background: rgba(255,255,255,.98); box-shadow: 0 -6rpx 16rpx rgba(26,28,30,.06); }
  .primary-button, .secondary-button { display: flex; align-items: center; justify-content: center; min-width: 120rpx; height: 78rpx; padding: 0 28rpx; box-sizing: border-box; font-size: 25rpx; border-radius: 999rpx; }
  .primary-button { flex: 1; color: #fff; background: var(--marriage-primary); }
  .secondary-button { color: var(--marriage-primary); background: #fff; border: 1rpx solid #ffcaca; }
  .secondary-button.selected { color: var(--marriage-muted); background: var(--marriage-soft); border-color: var(--marriage-line); }
</style>
