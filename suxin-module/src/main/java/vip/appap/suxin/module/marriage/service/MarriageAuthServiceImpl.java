package vip.appap.suxin.module.marriage.service;

import cn.hutool.core.util.StrUtil;

import vip.appap.suxin.module.infra.dal.dataobject.file.FileDO;
import vip.appap.suxin.module.infra.enums.FileAuditStatusEnum;
import vip.appap.suxin.module.infra.service.file.FileService;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriagePreferenceUpdateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageProfileRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageUserInfoRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageProfileUpdateReqVO;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerImageDO;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMarriageProfileDO;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerImageMapper;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMarriageProfileMapper;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.service.PartnerCertificationRecordService;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.service.SocialUserService;
import vip.appap.suxin.module.system.service.WxContentCheckService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.marriage.enums.ErrorCodeConstants.MARRIAGE_AUTH_MEMBER_NOT_EXISTS;
import static vip.appap.suxin.module.marriage.enums.ErrorCodeConstants.MARRIAGE_PROFILE_REAL_NAME_LOCKED;

/**
 * 婚恋用户认证 Service 实现类
 */
@Service
@Validated
@Slf4j
public class MarriageAuthServiceImpl implements MarriageAuthService {

    @Resource
    private PartnerService partnerService;
    @Resource
    private PartnerMapper partnerMapper;
    @Resource
    private PartnerMarriageProfileMapper partnerMarriageProfileMapper;
    @Resource
    private PartnerImageMapper partnerImageMapper;
    @Resource
    private PartnerCertificationRecordService partnerCertificationRecordService;
    @Resource
    private FileService fileService;
    @Resource
    private WxContentCheckService wxContentCheckService;
    @Resource
    private SocialUserService socialUserService;

    @Override
    public MarriageUserInfoRespVO getLoginUserInfo(Long userId) {
        log.info("[getLoginUserInfo][获取登录用户信息，userId={}]", userId);
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            log.warn("[getLoginUserInfo][用户不存在，userId={}]", userId);
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }

        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(userId);

        MarriageUserInfoRespVO respVO = new MarriageUserInfoRespVO();
        respVO.setUserId(partner.getId());
        respVO.setNickname(partner.getNickname());
        respVO.setAvatar(partner.getAvatar());
        respVO.setBackgroundImage(profile != null ? profile.getBackgroundImage() : null);
        respVO.setMobile(partner.getMobile());
        respVO.setMember(partner.getIsMember());
        boolean realVerified = isRealVerified(userId, profile);
        respVO.setRealVerified(realVerified ? 1 : 0);
        respVO.setVerifiedLabel(realVerified ? "已实名" : "");
        int[] completionResult = calculateProfileCompletion(partner, profile);
        respVO.setProfileCompletion(completionResult[0]);
        respVO.setIncompleteFields(getIncompleteFields(partner, profile));
        return respVO;
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        log.info("[updateAvatar][更新用户头像，userId={}]", userId);
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            log.warn("[updateAvatar][用户不存在，userId={}]", userId);
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }

        // 提交图片审核（资料场景）
        submitImageAudit(userId, avatarUrl, vip.appap.suxin.module.system.enums.WxMediaCheckSceneEnum.PROFILE.getScene());

        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(userId);
        updateObj.setAvatar(avatarUrl);
        partnerMapper.updateById(updateObj);
    }

    @Override
    public MarriageProfileRespVO getMyProfile(Long userId) {
        log.info("[getMyProfile][获取我的婚恋资料，userId={}]", userId);
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            log.warn("[getMyProfile][用户不存在，userId={}]", userId);
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }

        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(userId);
        MarriageProfileRespVO respVO = new MarriageProfileRespVO();
        respVO.setUserId(partner.getId());
        respVO.setNickname(partner.getNickname());
        respVO.setAvatar(partner.getAvatar());
        respVO.setMobile(partner.getMobile());
        respVO.setName(partner.getName());
        respVO.setMaskedIdCard(maskIdCard(partner.getIdCard()));
        respVO.setSex(partner.getSex());
        respVO.setBirthday(partner.getBirthday() != null ? partner.getBirthday().toString().substring(0, 10) : null);
        respVO.setAreaId(partner.getAreaId());

        if (profile != null) {
            respVO.setMaritalStatus(profile.getMaritalStatus());
            respVO.setHeightCm(profile.getHeightCm());
            respVO.setWeightKg(profile.getWeightKg());
            respVO.setEducation(profile.getEducation());
            respVO.setIncomeLevel(profile.getIncomeLevel());
            respVO.setLiveAreaId(profile.getLiveAreaId());
            respVO.setHouseStatus(profile.getHouseStatus());
            respVO.setCarStatus(profile.getCarStatus());
            respVO.setJobTitle(profile.getJobTitle());
            respVO.setBio(profile.getBio());
            respVO.setBackgroundImage(profile.getBackgroundImage());
            respVO.setMateMaritalStatus(profile.getMateMaritalStatus());
            respVO.setMateMinHeightCm(profile.getMateMinHeightCm());
            respVO.setMateMaxHeightCm(profile.getMateMaxHeightCm());
            respVO.setMateMinWeightKg(profile.getMateMinWeightKg());
            respVO.setMateMaxWeightKg(profile.getMateMaxWeightKg());
            respVO.setMateMinEducation(profile.getMateMinEducation());
            respVO.setMateLiveAreaId(profile.getMateLiveAreaId());
            respVO.setMateMinIncomeLevel(profile.getMateMinIncomeLevel());
            respVO.setMateHouseStatus(profile.getMateHouseStatus());
            respVO.setMateCarStatus(profile.getMateCarStatus());
            respVO.setMateJobTitle(profile.getMateJobTitle());
            respVO.setMateRemark(profile.getMateRemark());
        }
        boolean realVerified = isRealVerified(userId, profile);
        respVO.setRealVerified(realVerified ? 1 : 0);
        respVO.setVerifiedLabel(realVerified ? "已实名" : "");
        respVO.setSingleVerified(partnerCertificationRecordService.getMarriageVerifiedStatus(userId));

        int[] completionResult = calculateProfileCompletion(partner, profile);
        respVO.setProfileCompletion(completionResult[0]);
        respVO.setIncompleteFields(getIncompleteFields(partner, profile));
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyProfile(Long userId, MarriageProfileUpdateReqVO updateVO) {
        log.info("[updateMyProfile][更新我的资料，userId={}]", userId);
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            log.warn("[updateMyProfile][用户不存在，userId={}]", userId);
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }
        boolean realVerified = isRealVerified(userId);
        validateRealNameLockedFields(partner, updateVO, realVerified);

        PartnerDO partnerUpdate = new PartnerDO();
        partnerUpdate.setId(userId);
        boolean hasPartnerUpdate = false;
        if (updateVO.getNickname() != null) {
            partnerUpdate.setNickname(updateVO.getNickname());
            hasPartnerUpdate = true;
        }
        if (!realVerified && updateVO.getSex() != null) {
            partnerUpdate.setSex(updateVO.getSex());
            hasPartnerUpdate = true;
        }
        if (!realVerified && updateVO.getBirthday() != null) {
            partnerUpdate.setBirthday(parseBirthday(updateVO.getBirthday()));
            hasPartnerUpdate = true;
        }
        if (updateVO.getAreaId() != null) {
            partnerUpdate.setAreaId(updateVO.getAreaId().intValue());
            hasPartnerUpdate = true;
        }
        if (hasPartnerUpdate) {
            partnerMapper.updateById(partnerUpdate);
        }

        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(userId);
        boolean isNew = profile == null;
        if (isNew) {
            profile = PartnerMarriageProfileDO.builder()
                    .id(userId)
                    .profileStatus(0)
                    .recommendFlag(true)
                    .realVerified(0)
                    .build();
        }

        profile.setHeightCm(updateVO.getHeightCm());
        profile.setWeightKg(updateVO.getWeightKg());
        profile.setEducation(updateVO.getEducation());
        profile.setIncomeLevel(updateVO.getIncomeLevel());
        profile.setLiveAreaId(updateVO.getLiveAreaId());
        profile.setMaritalStatus(updateVO.getMaritalStatus());
        profile.setHouseStatus(updateVO.getHouseStatus());
        profile.setCarStatus(updateVO.getCarStatus());
        profile.setJobTitle(updateVO.getJobTitle());
        profile.setBio(updateVO.getBio());

        if (isNew) {
            partnerMarriageProfileMapper.insert(profile);
        } else {
            partnerMarriageProfileMapper.updateById(profile);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyPreference(Long userId, MarriagePreferenceUpdateReqVO updateVO) {
        log.info("[updateMyPreference][更新择偶条件，userId={}]", userId);
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            log.warn("[updateMyPreference][用户不存在，userId={}]", userId);
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }

        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(userId);
        boolean isNew = profile == null;
        if (isNew) {
            profile = PartnerMarriageProfileDO.builder()
                    .id(userId)
                    .profileStatus(0)
                    .recommendFlag(true)
                    .realVerified(0)
                    .build();
        }

        profile.setMateMaritalStatus(updateVO.getMateMaritalStatus());
        profile.setMateMinHeightCm(updateVO.getMateMinHeightCm());
        profile.setMateMaxHeightCm(updateVO.getMateMaxHeightCm());
        profile.setMateMinWeightKg(updateVO.getMateMinWeightKg());
        profile.setMateMaxWeightKg(updateVO.getMateMaxWeightKg());
        profile.setMateMinEducation(updateVO.getMateMinEducation());
        profile.setMateLiveAreaId(updateVO.getMateLiveAreaId());
        profile.setMateMinIncomeLevel(updateVO.getMateMinIncomeLevel());
        profile.setMateHouseStatus(updateVO.getMateHouseStatus());
        profile.setMateCarStatus(updateVO.getMateCarStatus());
        profile.setMateJobTitle(updateVO.getMateJobTitle());
        profile.setMateRemark(updateVO.getMateRemark());

        if (isNew) {
            partnerMarriageProfileMapper.insert(profile);
        } else {
            partnerMarriageProfileMapper.updateById(profile);
        }
    }

    @Override
    public void updateBackgroundImage(Long userId, String backgroundImage) {
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }

        // 提交图片审核（资料场景）
        submitImageAudit(userId, backgroundImage, vip.appap.suxin.module.system.enums.WxMediaCheckSceneEnum.PROFILE.getScene());

        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(userId);
        if (profile == null) {
            profile = PartnerMarriageProfileDO.builder()
                    .id(userId)
                    .backgroundImage(backgroundImage)
                    .profileStatus(0)
                    .recommendFlag(true)
                    .realVerified(0)
                    .build();
            partnerMarriageProfileMapper.insert(profile);
        } else {
            profile.setBackgroundImage(backgroundImage);
            partnerMarriageProfileMapper.updateById(profile);
        }
    }

    @Override
    public void clearBackgroundImage(Long userId) {
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }
        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(userId);
        if (profile != null) {
            profile.setBackgroundImage(null);
            partnerMarriageProfileMapper.updateById(profile);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAlbumImage(Long userId, String imageUrl, Integer type) {
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }

        // 提交图片审核（资料场景）
        submitImageAudit(userId, imageUrl, vip.appap.suxin.module.system.enums.WxMediaCheckSceneEnum.PROFILE.getScene());

        List<PartnerImageDO> existing = partnerImageMapper.selectListByProfileIdsAndType(List.of(userId), type);
        int maxSortNo = existing.stream().mapToInt(PartnerImageDO::getSortNo).max().orElse(0);
        PartnerImageDO image = PartnerImageDO.builder()
                .profileId(userId)
                .type(type)
                .imageUrl(imageUrl)
                .sortNo(maxSortNo + 1)
                .build();
        partnerImageMapper.insert(image);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAlbumImage(Long userId, String imageUrl, Integer type) {
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }
        List<PartnerImageDO> existing = partnerImageMapper.selectListByProfileIdsAndType(List.of(userId), type);
        for (PartnerImageDO image : existing) {
            if (imageUrl.equals(image.getImageUrl())) {
                partnerImageMapper.deleteById(image.getId());
                break;
            }
        }
    }

    @Override
    public List<String> getAlbumImages(Long userId, Integer type) {
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }
        List<PartnerImageDO> images = partnerImageMapper.selectListByProfileIdsAndType(List.of(userId), type);
        return images.stream().map(PartnerImageDO::getImageUrl).filter(StrUtil::isNotBlank).distinct().toList();
    }

    @Override
    public Integer getRealVerifiedStatus(Long userId) {
        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(userId);
        return isRealVerified(userId, profile) ? 1 : 0;
    }

    @Override
    public MarriageUserInfoRespVO getRealNameInfo(Long userId) {
        log.info("[getRealNameInfo][获取实名认证信息，userId={}]", userId);
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            log.warn("[getRealNameInfo][用户不存在，userId={}]", userId);
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }

        boolean realVerified = isRealVerified(userId);
        MarriageUserInfoRespVO respVO = new MarriageUserInfoRespVO();
        respVO.setRealVerified(realVerified ? 1 : 0);
        // Historical identity fields alone are not proof of verification.
        respVO.setName(realVerified ? partner.getName() : null);
        respVO.setMaskedIdCard(realVerified ? maskIdCard(partner.getIdCard()) : null);
        return respVO;
    }

    private LocalDateTime parseBirthday(String birthday) {
        if (StrUtil.isBlank(birthday)) {
            return null;
        }
        try {
            return LocalDate.parse(birthday).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isRealVerified(Long userId) {
        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(userId);
        return isRealVerified(userId, profile);
    }

    private boolean isRealVerified(Long userId, PartnerMarriageProfileDO profile) {
        if (profile != null && Integer.valueOf(1).equals(profile.getRealVerified())) {
            return true;
        }
        return Integer.valueOf(1).equals(partnerCertificationRecordService.getRealNameVerifiedStatus(userId));
    }

    private void validateRealNameLockedFields(PartnerDO partner, MarriageProfileUpdateReqVO updateVO,
                                              boolean realVerified) {
        if (!realVerified) {
            return;
        }
        if (updateVO.getSex() != null && !Objects.equals(updateVO.getSex(), partner.getSex())) {
            throw exception(MARRIAGE_PROFILE_REAL_NAME_LOCKED);
        }
        if (updateVO.getBirthday() != null && !isSameBirthday(updateVO.getBirthday(), partner.getBirthday())) {
            throw exception(MARRIAGE_PROFILE_REAL_NAME_LOCKED);
        }
    }

    private boolean isSameBirthday(String birthday, LocalDateTime currentBirthday) {
        LocalDateTime nextBirthday = parseBirthday(birthday);
        if (nextBirthday == null || currentBirthday == null) {
            return nextBirthday == null && currentBirthday == null;
        }
        return Objects.equals(nextBirthday.toLocalDate(), currentBirthday.toLocalDate());
    }

    private int[] calculateProfileCompletion(PartnerDO partner, PartnerMarriageProfileDO profile) {
        int total = 9;
        int filled = 0;

        if (StrUtil.isNotBlank(partner.getNickname())) filled++;
        if (partner.getAvatar() != null) filled++;
        if (profile != null && profile.getHeightCm() != null && profile.getHeightCm() > 0) filled++;
        if (profile != null && profile.getEducation() != null && profile.getEducation() > 0) filled++;
        if (profile != null && profile.getIncomeLevel() != null && profile.getIncomeLevel() > 0) filled++;
        if (profile != null && StrUtil.isNotBlank(profile.getJobTitle())) filled++;
        if (profile != null && StrUtil.isNotBlank(profile.getBio())) filled++;
        if (profile != null && profile.getLiveAreaId() != null && profile.getLiveAreaId() > 0) filled++;
        if (profile != null && profile.getMaritalStatus() != null && profile.getMaritalStatus() > 0) filled++;

        int percent = (int) (filled * 100.0 / total);
        return new int[]{percent, filled};
    }

    private List<String> getIncompleteFields(PartnerDO partner, PartnerMarriageProfileDO profile) {
        List<String> incomplete = new ArrayList<>();

        if (partner.getAvatar() == null) incomplete.add("上传头像");
        if (profile == null || profile.getHeightCm() == null || profile.getHeightCm() <= 0) incomplete.add("填写身高");
        if (profile == null || profile.getEducation() == null || profile.getEducation() <= 0) incomplete.add("选择学历");
        if (profile == null || profile.getIncomeLevel() == null || profile.getIncomeLevel() <= 0) incomplete.add("填写月收入");
        if (profile == null || StrUtil.isBlank(profile.getJobTitle())) incomplete.add("填写职业");
        if (profile == null || StrUtil.isBlank(profile.getBio())) incomplete.add("填写内心独白");
        if (profile == null || profile.getLiveAreaId() == null || profile.getLiveAreaId() <= 0) incomplete.add("选择所在城市");
        if (profile == null || profile.getMaritalStatus() == null || profile.getMaritalStatus() <= 0) incomplete.add("选择婚姻状态");

        return incomplete;
    }

    private String maskIdCard(String idCard) {
        if (StrUtil.isBlank(idCard) || idCard.length() < 8) {
            return "";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 检查文件审核状态
     *
     * @param imageUrl 图片URL
     */
    private void checkFileAuditStatus(String imageUrl) {
        FileDO file = fileService.getFileByUrl(imageUrl);
        if (file == null) {
            log.warn("[checkFileAuditStatus][文件不存在：{}]", imageUrl);
            return;
        }

        Integer auditStatus = file.getAuditStatus();
        if (auditStatus == null) {
            return;
        }

        // 审核中
        if (FileAuditStatusEnum.AUDITING.getStatus().equals(auditStatus)) {
            throw new vip.appap.suxin.framework.common.exception.ServiceException(
                    vip.appap.suxin.module.infra.enums.ErrorCodeConstants.FILE_AUDIT_PENDING);
        }
        // 审核不通过
        if (FileAuditStatusEnum.AUDIT_REJECT.getStatus().equals(auditStatus)) {
            throw new vip.appap.suxin.framework.common.exception.ServiceException(
                    vip.appap.suxin.module.infra.enums.ErrorCodeConstants.FILE_AUDIT_REJECTED);
        }
    }

    /**
     * 提交图片审核
     *
     * @param userId    用户编号
     * @param imageUrl  图片URL
     * @param scene     场景值
     */
    private void submitImageAudit(Long userId, String imageUrl, Integer scene) {
        try {
            if (!wxContentCheckService.isValidAuditImageUrl(imageUrl)) {
                log.warn("[submitImageAudit][不是有效的服务器图片地址，跳过审核：userId({}) imageUrl({})]", userId, imageUrl);
                return;
            }

            // 获取用户的微信 openid
            String openid = getWxMaOpenid(userId);
            if (openid == null) {
                log.warn("[submitImageAudit][无法获取用户openid，跳过审核：userId({})]", userId);
                return;
            }

            // 调用微信审核接口
            String traceId = wxContentCheckService.mediaCheckAsync(openid, imageUrl, 2, scene);
            log.info("[submitImageAudit][审核已提交：userId({}) imageUrl({}) traceId({})]", userId, imageUrl, traceId);

            // 更新文件审核状态
            FileDO file = fileService.getFileByUrl(imageUrl);
            if (file != null) {
                FileDO updateObj = new FileDO();
                updateObj.setId(file.getId());
                updateObj.setAuditStatus(FileAuditStatusEnum.AUDITING.getStatus());
                updateObj.setAuditTraceId(traceId);
                // TODO: 需要添加更新文件审核状态的方法
            }
        } catch (Exception e) {
            log.error("[submitImageAudit][调用审核接口失败：userId({}) imageUrl({})]", userId, imageUrl, e);
            // 审核接口调用失败，降级处理，不影响业务流程
        }
    }

    /**
     * 获取用户的微信小程序 openid
     *
     * @param userId 用户编号
     * @return openid
     */
    private String getWxMaOpenid(Long userId) {
        try {
            vip.appap.suxin.module.system.api.dto.SocialUserRespDTO socialUser =
                    socialUserService.getSocialUserByUserId(
                            vip.appap.suxin.framework.common.enums.UserTypeEnum.MEMBER.getValue(),
                            userId, vip.appap.suxin.module.system.enums.SocialTypeEnum.WECHAT_MINI_PROGRAM.getType());
            if (socialUser != null && cn.hutool.core.util.StrUtil.isNotEmpty(socialUser.getOpenid())) {
                return socialUser.getOpenid();
            }
        } catch (Exception e) {
            log.warn("[getWxMaOpenid][获取用户openid异常：userId({})]", userId, e);
        }
        return null;
    }

}
