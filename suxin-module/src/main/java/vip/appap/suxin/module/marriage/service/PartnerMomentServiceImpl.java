package vip.appap.suxin.module.marriage.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.tenant.core.context.TenantContextHolder;
import vip.appap.suxin.module.infra.dal.dataobject.file.FileDO;
import vip.appap.suxin.module.infra.enums.FileAuditStatusEnum;
import vip.appap.suxin.module.infra.service.file.FileService;
import vip.appap.suxin.module.system.api.dto.SocialUserRespDTO;
import vip.appap.suxin.module.system.enums.SocialTypeEnum;
import vip.appap.suxin.module.system.service.SocialUserService;
import vip.appap.suxin.module.system.service.WxContentCheckService;
import vip.appap.suxin.module.marriage.controller.app.vo.*;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMarriageProfileDO;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMomentCommentDO;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMomentDO;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMomentLikeDO;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMarriageProfileMapper;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMomentCommentMapper;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMomentLikeMapper;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMomentMapper;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerRelPartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.dal.mysql.PartnerRelPartnerMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.marriage.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class PartnerMomentServiceImpl implements PartnerMomentService {

    private static final String MOMENT_IMAGE_BIZ_TYPE = "partner_dynamic_image";
    private static final String RELATION_TYPE_ATTENTION = "attention";

    @Resource
    private PartnerMomentMapper partnerMomentMapper;
    @Resource
    private PartnerMarriageProfileMapper partnerMarriageProfileMapper;
    @Resource
    private PartnerMomentLikeMapper partnerMomentLikeMapper;
    @Resource
    private PartnerMomentCommentMapper partnerMomentCommentMapper;
    @Resource
    private PartnerMapper partnerMapper;
    @Resource
    private PartnerRelPartnerMapper partnerRelPartnerMapper;
    @Resource
    private FileService fileService;
    @Resource
    private WxContentCheckService wxContentCheckService;
    @Resource
    private SocialUserService socialUserService;

    @Override
    public PageResult<AppPartnerMomentRespVO> getPartnerMomentPage(AppPartnerMomentPageReqVO pageReqVO,
                                                                   Long loginPartnerId) {
        // 如果指定了 partnerId，只查看该用户的动态
        if (pageReqVO.getPartnerId() != null && pageReqVO.getPartnerId() > 0) {
            PageResult<PartnerMomentDO> pageResult = partnerMomentMapper.selectPublicPage(pageReqVO, List.of(pageReqVO.getPartnerId()));
            return new PageResult<>(buildMomentRespList(pageResult.getList(), loginPartnerId), pageResult.getTotal());
        }
        Collection<Long> followedPartnerIds = null;
        if (Boolean.TRUE.equals(pageReqVO.getFollowing())) {
            followedPartnerIds = partnerRelPartnerMapper
                    .selectListByPartnerIdAndType(loginPartnerId, RELATION_TYPE_ATTENTION)
                    .stream().map(PartnerRelPartnerDO::getRelPartnerId).collect(Collectors.toSet());
        }
        PageResult<PartnerMomentDO> pageResult = partnerMomentMapper.selectPublicPage(pageReqVO, followedPartnerIds);
        return new PageResult<>(buildMomentRespList(pageResult.getList(), loginPartnerId), pageResult.getTotal());
    }

    @Override
    public PageResult<AppPartnerMomentRespVO> getMyPartnerMomentPage(AppPartnerMomentPageReqVO pageReqVO,
                                                                    Long loginPartnerId) {
        PartnerDO partner = partnerMapper.selectById(loginPartnerId);
        if (partner == null) {
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }
        PageResult<PartnerMomentDO> pageResult = partnerMomentMapper.selectOwnerPage(pageReqVO, loginPartnerId);
        return new PageResult<>(buildMomentRespList(pageResult.getList(), loginPartnerId), pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPartnerMoment(AppPartnerMomentCreateReqVO createReqVO, Long loginPartnerId) {
        PartnerDO partner = partnerMapper.selectById(loginPartnerId);
        if (partner == null) {
            throw exception(MARRIAGE_AUTH_MEMBER_NOT_EXISTS);
        }
        List<String> imageUrls = normalizeImageUrls(createReqVO.getImageUrls());
        String content = StrUtil.trimToEmpty(createReqVO.getContent());
        if (StrUtil.isBlank(content) && imageUrls.isEmpty()) {
            throw exception(MARRIAGE_MOMENT_CONTENT_EMPTY);
        }
        if (imageUrls.size() > 9) {
            throw exception(MARRIAGE_MOMENT_IMAGE_TOO_MANY);
        }

        // 提交所有图片审核（社交日志场景）
        submitImagesAudit(loginPartnerId, imageUrls, vip.appap.suxin.module.system.enums.WxMediaCheckSceneEnum.SOCIAL_LOG.getScene());

        PartnerMomentDO moment = PartnerMomentDO.builder()
                .partnerId(loginPartnerId)
                .content(content)
                .mediaType(imageUrls.isEmpty() ? 0 : 1)
                .mediaCount(imageUrls.size())
                .visibility(1)
                .status(1)
                .auditReason("")
                .pvCount(0)
                .likeCount(0)
                .commentCount(0)
                .shareCount(0)
                .recommendFlag(false)
                .recommendSort(0)
                .publishTime(LocalDateTime.now())
                .build();
        partnerMomentMapper.insert(moment);
        if (!imageUrls.isEmpty()) {
            fileService.bindFileBizByUrls(MOMENT_IMAGE_BIZ_TYPE, moment.getId(), imageUrls);
        }
        return moment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePartnerMoment(Long id, Long loginPartnerId) {
        PartnerMomentDO moment = validatePublishedMoment(id);
        if (!Objects.equals(moment.getPartnerId(), loginPartnerId)) {
            throw exception(MARRIAGE_MOMENT_DELETE_FORBIDDEN);
        }
        partnerMomentMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPartnerMomentLikeRespVO togglePartnerMomentLike(AppPartnerMomentLikeReqVO reqVO, Long loginPartnerId) {
        PartnerMomentDO moment = validatePublishedMoment(reqVO.getMomentId());
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        PartnerMomentLikeDO existed = partnerMomentLikeMapper.selectByMomentIdAndPartnerId(moment.getId(), loginPartnerId);
        boolean liked;
        if (existed != null) {
            int deleteCount = partnerMomentLikeMapper.deleteByIdPhysically(existed.getId(), tenantId);
            if (deleteCount > 0) {
                partnerMomentMapper.updateLikeCount(moment.getId(), -1);
            }
            liked = false;
        } else {
            partnerMomentLikeMapper.deleteByMomentIdAndPartnerIdPhysically(moment.getId(), loginPartnerId, tenantId);
            try {
                partnerMomentLikeMapper.insert(PartnerMomentLikeDO.builder()
                        .momentId(moment.getId())
                        .partnerId(loginPartnerId)
                        .build());
                partnerMomentMapper.updateLikeCount(moment.getId(), 1);
            } catch (DuplicateKeyException ignored) {
                // 并发重复点赞时，唯一索引已保证最终是已点赞状态。
            }
            liked = true;
        }
        PartnerMomentDO updated = partnerMomentMapper.selectById(moment.getId());
        return new AppPartnerMomentLikeRespVO(liked, updated.getLikeCount());
    }

    @Override
    public PageResult<AppPartnerMomentCommentRespVO> getPartnerMomentCommentPage(
            AppPartnerMomentCommentPageReqVO pageReqVO) {
        validatePublishedMoment(pageReqVO.getMomentId());
        PageResult<PartnerMomentCommentDO> rootPage = partnerMomentCommentMapper
                .selectRootPage(pageReqVO, pageReqVO.getMomentId());
        List<Long> rootIds = rootPage.getList().stream().map(PartnerMomentCommentDO::getId).toList();
        List<PartnerMomentCommentDO> replies = partnerMomentCommentMapper
                .selectListByParentIds(pageReqVO.getMomentId(), rootIds);
        List<PartnerMomentCommentDO> allComments = new ArrayList<>();
        allComments.addAll(rootPage.getList());
        allComments.addAll(replies);
        Map<Long, PartnerDO> partnerMap = getPartnerMap(allComments.stream()
                .flatMap(comment -> Arrays.stream(new Long[]{comment.getPartnerId(), comment.getReplyToPartnerId()}))
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .collect(Collectors.toSet()));
        Map<Long, List<AppPartnerMomentCommentRespVO>> replyMap = replies.stream()
                .map(comment -> buildCommentResp(comment, partnerMap))
                .collect(Collectors.groupingBy(AppPartnerMomentCommentRespVO::getParentId));
        List<AppPartnerMomentCommentRespVO> list = rootPage.getList().stream()
                .map(comment -> buildCommentResp(comment, partnerMap)
                        .setReplies(replyMap.getOrDefault(comment.getId(), List.of())))
                .toList();
        return new PageResult<>(list, rootPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPartnerMomentCommentRespVO createPartnerMomentComment(
            AppPartnerMomentCommentCreateReqVO createReqVO, Long loginPartnerId) {
        validatePublishedMoment(createReqVO.getMomentId());
        Long parentId = createReqVO.getParentId() == null ? 0L : createReqVO.getParentId();
        if (parentId > 0) {
            PartnerMomentCommentDO parent = partnerMomentCommentMapper.selectById(parentId);
            if (parent == null || !Objects.equals(parent.getMomentId(), createReqVO.getMomentId())
                    || !Objects.equals(parent.getStatus(), 1)) {
                throw exception(MARRIAGE_MOMENT_COMMENT_NOT_EXISTS);
            }
        }
        Long replyToPartnerId = createReqVO.getReplyToPartnerId() == null ? 0L : createReqVO.getReplyToPartnerId();
        PartnerMomentCommentDO comment = PartnerMomentCommentDO.builder()
                .momentId(createReqVO.getMomentId())
                .partnerId(loginPartnerId)
                .parentId(parentId)
                .replyToPartnerId(replyToPartnerId)
                .content(StrUtil.trim(createReqVO.getContent()))
                .status(1)
                .likeCount(0)
                .build();
        partnerMomentCommentMapper.insert(comment);
        partnerMomentMapper.updateCommentCount(createReqVO.getMomentId(), 1);
        Set<Long> partnerIds = new HashSet<>();
        partnerIds.add(loginPartnerId);
        if (replyToPartnerId > 0) {
            partnerIds.add(replyToPartnerId);
        }
        return buildCommentResp(comment, getPartnerMap(partnerIds));
    }

    private PartnerMomentDO validatePublishedMoment(Long momentId) {
        PartnerMomentDO moment = partnerMomentMapper.selectById(momentId);
        if (moment == null || !Objects.equals(moment.getStatus(), 1)) {
            throw exception(MARRIAGE_MOMENT_NOT_EXISTS);
        }
        return moment;
    }

    private List<AppPartnerMomentRespVO> buildMomentRespList(List<PartnerMomentDO> moments, Long loginPartnerId) {
        if (CollUtil.isEmpty(moments)) {
            return List.of();
        }
        Set<Long> momentIds = moments.stream().map(PartnerMomentDO::getId).collect(Collectors.toSet());
        Set<Long> partnerIds = moments.stream().map(PartnerMomentDO::getPartnerId).collect(Collectors.toSet());
        Map<Long, PartnerDO> partnerMap = getPartnerMap(partnerIds);
        Map<Long, PartnerMarriageProfileDO> profileMap = partnerMarriageProfileMapper.selectByIds(partnerIds).stream()
                .collect(Collectors.toMap(PartnerMarriageProfileDO::getId, Function.identity(), (a, b) -> a));

        Map<Long, List<String>> imageMap = fileService.getFileListByBizTypeAndBizIds(MOMENT_IMAGE_BIZ_TYPE, momentIds)
                .stream().collect(Collectors.groupingBy(FileDO::getBizId,
                        Collectors.mapping(FileDO::getUrl, Collectors.toList())));
        Set<Long> likedMomentIds = partnerMomentLikeMapper.selectListByMomentIdsAndPartnerId(momentIds, loginPartnerId)
                .stream().map(PartnerMomentLikeDO::getMomentId).collect(Collectors.toSet());
        Set<Long> followedPartnerIds = partnerRelPartnerMapper
                .selectListByPartnerIdAndRelPartnerIdsAndType(loginPartnerId, partnerIds, RELATION_TYPE_ATTENTION)
                .stream().map(PartnerRelPartnerDO::getRelPartnerId).collect(Collectors.toSet());

        return moments.stream().map(moment -> {
            PartnerDO partner = partnerMap.get(moment.getPartnerId());
            PartnerMarriageProfileDO profile = profileMap.get(moment.getPartnerId());
            Integer realVerified = profile != null && profile.getRealVerified() != null ? profile.getRealVerified() : 0;
            return new AppPartnerMomentRespVO()
                    .setId(moment.getId())
                    .setPartnerId(moment.getPartnerId())
                    .setAvatar(partner != null ? partner.getAvatar() : "")
                    .setNickname(resolveNickname(partner))
                    .setRealVerified(realVerified)
                    .setVerifiedLabel(Integer.valueOf(1).equals(realVerified) ? "已实名" : "未实名")
                    .setVerified(Integer.valueOf(1).equals(realVerified))
                    .setPublishTime(moment.getPublishTime())
                    .setFollowed(followedPartnerIds.contains(moment.getPartnerId()))
                    .setMine(Objects.equals(moment.getPartnerId(), loginPartnerId))
                    .setContent(moment.getContent())
                    .setImageUrls(imageMap.getOrDefault(moment.getId(), List.of()))
                    .setLikeCount(moment.getLikeCount())
                    .setCommentCount(moment.getCommentCount())
                    .setLiked(likedMomentIds.contains(moment.getId()));
        }).toList();
    }

    private AppPartnerMomentCommentRespVO buildCommentResp(PartnerMomentCommentDO comment, Map<Long, PartnerDO> partnerMap) {
        PartnerDO partner = partnerMap.get(comment.getPartnerId());
        PartnerDO replyToPartner = partnerMap.get(comment.getReplyToPartnerId());
        return new AppPartnerMomentCommentRespVO()
                .setId(comment.getId())
                .setMomentId(comment.getMomentId())
                .setPartnerId(comment.getPartnerId())
                .setNickname(resolveNickname(partner))
                .setAvatar(partner != null ? partner.getAvatar() : "")
                .setVerified(true)
                .setParentId(comment.getParentId())
                .setReplyToPartnerId(comment.getReplyToPartnerId())
                .setReplyToNickname(replyToPartner != null ? resolveNickname(replyToPartner) : "")
                .setContent(comment.getContent())
                .setLikeCount(comment.getLikeCount())
                .setCreateTime(comment.getCreateTime())
                .setReplies(List.of());
    }

    private Map<Long, PartnerDO> getPartnerMap(Collection<Long> partnerIds) {
        if (partnerIds == null || partnerIds.isEmpty()) {
            return Map.of();
        }
        return partnerMapper.selectByIds(partnerIds).stream()
                .collect(Collectors.toMap(PartnerDO::getId, Function.identity(), (a, b) -> a));
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }
        return imageUrls.stream().filter(StrUtil::isNotBlank).map(StrUtil::trim).distinct().toList();
    }

    private String resolveNickname(PartnerDO partner) {
        if (partner == null) {
            return "用户";
        }
        return StrUtil.blankToDefault(partner.getNickname(), StrUtil.blankToDefault(partner.getName(), "用户"));
    }

    /**
     * 提交所有图片审核
     *
     * @param userId    用户编号
     * @param imageUrls 图片URL列表
     * @param scene     场景值
     */
    private void submitImagesAudit(Long userId, List<String> imageUrls, Integer scene) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        // 获取用户的微信 openid
        String openid = getWxMaOpenid(userId);
        if (openid == null) {
            log.warn("[submitImagesAudit][无法获取用户openid，跳过审核：userId({})]", userId);
            return;
        }

        // 逐个提交审核
        for (String imageUrl : imageUrls) {
            try {
                if (!wxContentCheckService.isValidAuditImageUrl(imageUrl)) {
                    log.warn("[submitImagesAudit][不是有效的服务器图片地址，跳过审核：userId({}) imageUrl({})]", userId, imageUrl);
                    continue;
                }

                String traceId = wxContentCheckService.mediaCheckAsync(openid, imageUrl, 2, scene);
                log.info("[submitImagesAudit][审核已提交：userId({}) imageUrl({}) traceId({})]", userId, imageUrl, traceId);

                // 更新文件审核状态
                FileDO file = fileService.getFileByUrl(imageUrl);
                if (file != null) {
                    FileDO updateObj = new FileDO();
                    updateObj.setId(file.getId());
                    updateObj.setAuditStatus(FileAuditStatusEnum.AUDITING.getStatus());
                    updateObj.setAuditTraceId(traceId);
                    // TODO: 需要添加更新文件的方法
                }
            } catch (Exception e) {
                log.error("[submitImagesAudit][调用审核接口失败：userId({}) imageUrl({})]", userId, imageUrl, e);
                // 审核接口调用失败，降级处理，不影响业务流程
            }
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
            SocialUserRespDTO socialUser = socialUserService.getSocialUserByUserId(
                    vip.appap.suxin.framework.common.enums.UserTypeEnum.MEMBER.getValue(),
                    userId, SocialTypeEnum.WECHAT_MINI_PROGRAM.getType());
            if (socialUser != null && cn.hutool.core.util.StrUtil.isNotEmpty(socialUser.getOpenid())) {
                return socialUser.getOpenid();
            }
        } catch (Exception e) {
            log.warn("[getWxMaOpenid][获取用户openid异常：userId({})]", userId, e);
        }
        return null;
    }

}
