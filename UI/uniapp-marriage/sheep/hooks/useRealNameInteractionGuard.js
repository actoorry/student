import sheep from '@/sheep';
import MarriageProfileApi from '@/sheep/api/marriage/profile';
import { showAuthModal, showRealNameVerificationModal } from '@/sheep/hooks/useModal';
import { getResponseData } from '@/sheep/helper/marriage';
import { INTERACTION_GUARD_SCENES, realNameInteractionGuard } from '@/sheep/helper/real-name-interaction-guard';

export { INTERACTION_GUARD_SCENES };

export async function requireRealNameInteraction(scene) {
  const user = sheep.$store('user');
  const result = await realNameInteractionGuard.check({
    identity: user.isLogin ? user.userInfo?.id : null,
    fetchStatus: async () => getResponseData(await MarriageProfileApi.getRealVerifiedStatus(), '实名状态获取失败'),
  });
  if (result.code === 'login-required') showAuthModal();
  else if (result.code === 'verification-required') showRealNameVerificationModal(scene);
  else if (result.code === 'check-failed') uni.showToast({ title: '实名状态获取失败，请重试', icon: 'none' });
  return result.code === 'allowed';
}
