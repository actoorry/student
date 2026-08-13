package vip.appap.suxin.module.marriage.service;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfileRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageFollowRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageInteractionStatisticsRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageRelationMemberRespVO;
import vip.appap.suxin.module.marriage.enums.AppInteractionNotificationSceneEnum;
import vip.appap.suxin.module.marriage.service.bo.AppInteractionNotificationCreateBO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerRelPartnerDO;
import vip.appap.suxin.module.partner.service.PartnerRelPartnerService;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.dal.dataobject.NotifyTemplateDO;
import vip.appap.suxin.module.system.service.NotifyMessageService;
import vip.appap.suxin.module.system.service.NotifyTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.marriage.enums.ErrorCodeConstants.MARRIAGE_FOLLOW_PARTNER_NOT_EXISTS;
import static vip.appap.suxin.module.marriage.enums.ErrorCodeConstants.MARRIAGE_FOLLOW_SELF_NOT_ALLOWED;
import static vip.appap.suxin.module.marriage.enums.ErrorCodeConstants.MARRIAGE_FOLLOW_USER_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class MarriageInteractionServiceImpl implements MarriageInteractionService {

    private static final String RELATION_TYPE_ATTENTION = "attention";
    private static final String RELATION_TYPE_LOOK = "look";

    @Resource
    private PartnerService partnerService;
    @Resource
    private PartnerRelPartnerService partnerRelPartnerService;
    @Resource
    private PartnerMarriageProfileService partnerMarriageProfileService;
    @Resource
    private NotifyTemplateService notifyTemplateService;
    @Resource
    private NotifyMessageService notifyMessageService;
    @Resource
    private AppInteractionNotificationService appInteractionNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MarriageFollowRespVO follow(Long loginUserId, Long relPartnerId) {
        if (loginUserId.equals(relPartnerId)) {
            throw exception(MARRIAGE_FOLLOW_SELF_NOT_ALLOWED);
        }
        PartnerDO loginPartner = partnerService.getPartner(loginUserId);
        if (loginPartner == null) {
            throw exception(MARRIAGE_FOLLOW_USER_NOT_EXISTS);
        }
        PartnerDO relPartner = partnerService.getPartner(relPartnerId);
        if (relPartner == null) {
            throw exception(MARRIAGE_FOLLOW_PARTNER_NOT_EXISTS);
        }

        PartnerRelPartnerDO existed = partnerRelPartnerService.getPartnerRelPartner(
                loginUserId, relPartnerId, RELATION_TYPE_ATTENTION);
        if (existed != null) {
            boolean mutual = partnerRelPartnerService.getPartnerRelPartner(
                    relPartnerId, loginUserId, RELATION_TYPE_ATTENTION) != null;
            return new MarriageFollowRespVO(true, mutual);
        }

        partnerRelPartnerService.createPartnerRelPartner(loginUserId, relPartnerId, RELATION_TYPE_ATTENTION);
        boolean mutual = partnerRelPartnerService.getPartnerRelPartner(
                relPartnerId, loginUserId, RELATION_TYPE_ATTENTION) != null;
        if (mutual) {
            createMutualFollowNotification(relPartner, loginUserId);
            createMutualFollowNotification(loginPartner, relPartnerId);
        } else {
            createFollowNotification(loginPartner, relPartnerId);
        }
        return new MarriageFollowRespVO(true, mutual);
    }

    @Override
    public void unfollow(Long loginUserId, Long relPartnerId) {
        if (loginUserId.equals(relPartnerId)) {
            throw exception(MARRIAGE_FOLLOW_SELF_NOT_ALLOWED);
        }
        PartnerDO loginPartner = partnerService.getPartner(loginUserId);
        if (loginPartner == null) {
            throw exception(MARRIAGE_FOLLOW_USER_NOT_EXISTS);
        }
        PartnerDO relPartner = partnerService.getPartner(relPartnerId);
        if (relPartner == null) {
            throw exception(MARRIAGE_FOLLOW_PARTNER_NOT_EXISTS);
        }
        partnerRelPartnerService.deletePartnerRelPartner(loginUserId, relPartnerId, RELATION_TYPE_ATTENTION);
    }

    @Override
    public MarriageInteractionStatisticsRespVO getInteractionStatistics(Long loginUserId) {
        return new MarriageInteractionStatisticsRespVO()
                .setViewMeCount(partnerRelPartnerService.getPartnerRelPartnerCountByRelPartnerId(
                        loginUserId, RELATION_TYPE_LOOK))
                .setMyViewCount(partnerRelPartnerService.getPartnerRelPartnerCountByPartnerId(
                        loginUserId, RELATION_TYPE_LOOK))
                .setFollowMeCount(partnerRelPartnerService.getPartnerRelPartnerCountByRelPartnerId(
                        loginUserId, RELATION_TYPE_ATTENTION))
                .setMyFollowCount(partnerRelPartnerService.getPartnerRelPartnerCountByPartnerId(
                        loginUserId, RELATION_TYPE_ATTENTION));
    }

    @Override
    public PageResult<MarriageRelationMemberRespVO> getFollowMePage(Long loginUserId, PageParam pageParam) {
        return getRelationMemberPage(loginUserId, pageParam, true);
    }

    @Override
    public PageResult<MarriageRelationMemberRespVO> getMyFollowPage(Long loginUserId, PageParam pageParam) {
        return getRelationMemberPage(loginUserId, pageParam, false);
    }

    @Override
    public void recordView(Long loginUserId, Long targetPartnerId) {
        if (loginUserId.equals(targetPartnerId)) {
            return;
        }
        PartnerRelPartnerDO existed = partnerRelPartnerService.getPartnerRelPartner(
                loginUserId, targetPartnerId, RELATION_TYPE_LOOK);
        if (existed != null) {
            return;
        }
        partnerRelPartnerService.createPartnerRelPartner(loginUserId, targetPartnerId, RELATION_TYPE_LOOK);
    }

    @Override
    public PageResult<MarriageRelationMemberRespVO> getViewMePage(Long loginUserId, PageParam pageParam) {
        return getViewRelationMemberPage(loginUserId, pageParam, true);
    }

    @Override
    public PageResult<MarriageRelationMemberRespVO> getMyViewPage(Long loginUserId, PageParam pageParam) {
        return getViewRelationMemberPage(loginUserId, pageParam, false);
    }

    @Override
    public int generateViewMeSummaryNotifications(LocalDateTime startTime, LocalDateTime endTime) {
        List<PartnerRelPartnerDO> relations = partnerRelPartnerService
                .getPartnerRelPartnerListByTypeAndCreateTimeBetween(RELATION_TYPE_LOOK, startTime, endTime);
        if (relations.isEmpty()) {
            return 0;
        }
        Map<Long, List<PartnerRelPartnerDO>> relationMap = relations.stream()
                .collect(Collectors.groupingBy(PartnerRelPartnerDO::getRelPartnerId));
        int count = 0;
        for (Map.Entry<Long, List<PartnerRelPartnerDO>> entry : relationMap.entrySet()) {
            Long receiverId = entry.getKey();
            List<PartnerRelPartnerDO> viewerRelations = entry.getValue().stream()
                    .sorted(Comparator.comparing(PartnerRelPartnerDO::getCreateTime).reversed())
                    .toList();
            int viewerCount = viewerRelations.size();
            if (viewerCount <= 0) {
                continue;
            }
            List<Map<String, Object>> sampleViewers = viewerRelations.stream()
                    .limit(3)
                    .map(relation -> {
                        PartnerDO viewer = partnerService.getPartner(relation.getPartnerId());
                        Map<String, Object> item = new HashMap<>();
                        item.put("partnerId", relation.getPartnerId());
                        item.put("nickname", safeNickname(viewer));
                        item.put("avatar", viewer != null && viewer.getAvatar() != null ? viewer.getAvatar() : "");
                        return item;
                    })
                    .collect(Collectors.toList());
            Map<String, Object> payload = new HashMap<>();
            payload.put("startTime", startTime);
            payload.put("endTime", endTime);
            payload.put("viewerCount", viewerCount);
            payload.put("sampleViewers", sampleViewers);
            String content = viewerCount + " people viewed you in the latest summary window";
            appInteractionNotificationService.createNotification(new AppInteractionNotificationCreateBO()
                    .setBizType(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE)
                    .setScene(AppInteractionNotificationSceneEnum.VIEW_ME_SUMMARY.getScene())
                    .setUserId(receiverId)
                    .setTitle(AppInteractionNotificationSceneEnum.VIEW_ME_SUMMARY.getTitle())
                    .setContent(content)
                    .setPayload(payload)
                    .setBizKey("marriage:view-me:" + startTime + ":" + endTime)
                    .setEventTime(endTime));
            count++;
        }
        return count;
    }

    private PageResult<MarriageRelationMemberRespVO> getViewRelationMemberPage(Long loginUserId, PageParam pageParam,
                                                                               boolean reverse) {
        PageResult<PartnerRelPartnerDO> pageResult = partnerRelPartnerService.getPartnerRelPartnerPage(
                loginUserId, RELATION_TYPE_LOOK, pageParam, reverse);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }
        List<MarriageRelationMemberRespVO> list = pageResult.getList().stream()
                .map(relation -> buildRelationMember(loginUserId, relation,
                        reverse ? relation.getPartnerId() : relation.getRelPartnerId()))
                .filter(item -> item != null)
                .toList();
        return new PageResult<>(list, pageResult.getTotal());
    }

    private PageResult<MarriageRelationMemberRespVO> getRelationMemberPage(Long loginUserId, PageParam pageParam,
                                                                           boolean reverse) {
        PageResult<PartnerRelPartnerDO> pageResult = partnerRelPartnerService.getPartnerRelPartnerPage(
                loginUserId, RELATION_TYPE_ATTENTION, pageParam, reverse);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }
        List<MarriageRelationMemberRespVO> list = pageResult.getList().stream()
                .map(relation -> buildRelationMember(loginUserId, relation,
                        reverse ? relation.getPartnerId() : relation.getRelPartnerId()))
                .filter(item -> item != null)
                .toList();
        return new PageResult<>(list, pageResult.getTotal());
    }

    private MarriageRelationMemberRespVO buildRelationMember(Long loginUserId, PartnerRelPartnerDO relation,
                                                             Long memberId) {
        PartnerDO partner = partnerService.getPartner(memberId);
        if (partner == null) {
            return null;
        }
        AppPartnerMarriageProfileRespVO profile = partnerMarriageProfileService.getPartnerMarriageProfile(memberId);
        boolean followed = partnerRelPartnerService.getPartnerRelPartner(
                loginUserId, memberId, RELATION_TYPE_ATTENTION) != null;
        boolean mutual = followed && partnerRelPartnerService.getPartnerRelPartner(
                memberId, loginUserId, RELATION_TYPE_ATTENTION) != null;
        return new MarriageRelationMemberRespVO()
                .setPartnerId(memberId)
                .setNickname(resolveNickname(partner, profile))
                .setAvatar(resolveAvatar(partner, profile))
                .setAge(resolveAge(profile))
                .setCity(profile != null ? blankToEmpty(profile.getCity()) : "")
                .setBio(profile != null ? blankToEmpty(profile.getBio()) : "")
                .setFollowed(followed)
                .setMutual(mutual)
                .setCreateTime(relation.getCreateTime());
    }

    private void createFollowNotification(PartnerDO actorPartner, Long receiverId) {
        Map<String, Object> payload = buildActorPayload(actorPartner);
        appInteractionNotificationService.createNotification(new AppInteractionNotificationCreateBO()
                .setBizType(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE)
                .setScene(AppInteractionNotificationSceneEnum.FOLLOW.getScene())
                .setUserId(receiverId)
                .setActorId(actorPartner.getId())
                .setTitle(AppInteractionNotificationSceneEnum.FOLLOW.getTitle())
                .setContent(safeNickname(actorPartner) + " followed you")
                .setPayload(payload));
    }

    private void createMutualFollowNotification(PartnerDO actorPartner, Long receiverId) {
        Map<String, Object> payload = buildActorPayload(actorPartner);
        appInteractionNotificationService.createNotification(new AppInteractionNotificationCreateBO()
                .setBizType(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE)
                .setScene(AppInteractionNotificationSceneEnum.MUTUAL_FOLLOW.getScene())
                .setUserId(receiverId)
                .setActorId(actorPartner.getId())
                .setTitle(AppInteractionNotificationSceneEnum.MUTUAL_FOLLOW.getTitle())
                .setContent("You and " + safeNickname(actorPartner) + " followed each other")
                .setPayload(payload));
    }

    private Map<String, Object> buildActorPayload(PartnerDO actorPartner) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fromPartnerId", actorPartner.getId());
        payload.put("nickname", safeNickname(actorPartner));
        payload.put("avatar", actorPartner.getAvatar() != null ? actorPartner.getAvatar() : "");
        return payload;
    }

    private void sendNotifyMessage(PartnerDO fromPartner, Long toPartnerId, String templateCode, String fallbackContent) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("nickname", safeNickname(fromPartner));
        templateParams.put("fromPartnerId", fromPartner.getId());
        templateParams.put("avatar", fromPartner.getAvatar() != null ? fromPartner.getAvatar() : "");
        NotifyTemplateDO template = notifyTemplateService.getNotifyTemplateByCodeFromCache(templateCode);
        if (template != null) {
            String content = notifyTemplateService.formatNotifyTemplateContent(template.getContent(), templateParams);
            notifyMessageService.createNotifyMessage(
                    toPartnerId, UserTypeEnum.MEMBER.getValue(), template, content, templateParams);
            return;
        }
        log.warn("[sendNotifyMessage][templateCode({}) missing, fallback notify message is used for user({})]",
                templateCode, toPartnerId);
        NotifyTemplateDO fallbackTemplate = NotifyTemplateDO.builder()
                .id(0L)
                .code(templateCode)
                .type(1)
                .nickname("System")
                .build();
        notifyMessageService.createNotifyMessage(
                toPartnerId, UserTypeEnum.MEMBER.getValue(), fallbackTemplate, fallbackContent, templateParams);
    }

    private String resolveNickname(PartnerDO partner, AppPartnerMarriageProfileRespVO profile) {
        if (profile != null && isNotBlank(profile.getName())) {
            return profile.getName().trim();
        }
        return safeNickname(partner);
    }

    private String resolveAvatar(PartnerDO partner, AppPartnerMarriageProfileRespVO profile) {
        if (profile != null && isNotBlank(profile.getAvatarImage())) {
            return profile.getAvatarImage().trim();
        }
        return partner.getAvatar() != null ? partner.getAvatar() : "";
    }

    private Integer resolveAge(AppPartnerMarriageProfileRespVO profile) {
        if (profile == null || profile.getAge() == null) {
            return 0;
        }
        return profile.getAge();
    }

    private String safeNickname(PartnerDO partner) {
        if (partner == null || partner.getNickname() == null || partner.getNickname().trim().isEmpty()) {
            return "Someone";
        }
        return partner.getNickname().trim();
    }

    private String blankToEmpty(String value) {
        return isNotBlank(value) ? value.trim() : "";
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
