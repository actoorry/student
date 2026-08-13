package vip.appap.suxin.module.marriage.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.dict.core.DictFrameworkUtils;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;

import vip.appap.suxin.module.infra.dal.dataobject.file.FileDO;
import vip.appap.suxin.module.infra.service.file.FileService;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfilePageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfileRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentSimpleRespVO;
import vip.appap.suxin.module.marriage.convert.PartnerMarriageProfileConvert;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMarriageProfileDO;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMomentDO;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMarriageProfileMapper;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMomentMapper;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.enums.DictTypeConstants;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.partner.service.PartnerCertificationRecordService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMultiMap;

@Service
@Validated
@Slf4j
public class PartnerMarriageProfileServiceImpl implements PartnerMarriageProfileService {

    private static final String BIZ_TYPE_PARTNER_ALBUM = "partner_album";
    private static final String BIZ_TYPE_MOMENT_IMAGE = "partner_dynamic_image";

    @Resource
    private PartnerMarriageProfileMapper partnerMarriageProfileMapper;
    @Resource
    private PartnerMomentMapper partnerMomentMapper;
    @Resource
    private PartnerMapper partnerMapper;
    @Resource
    private FileService fileService;
    @Resource
    private PartnerCertificationRecordService partnerCertificationRecordService;

    @Override
    public PageResult<AppPartnerMarriageProfileRespVO> getPartnerMarriageProfilePage(@Valid AppPartnerMarriageProfilePageReqVO pageReqVO,
                                                                                     Long loginUserId,
                                                                                     Integer loginUserSex) {
        PageResult<PartnerMarriageProfileDO> pageResult = partnerMarriageProfileMapper.selectRecommendPage(
                pageReqVO, loginUserId, loginUserSex);
        if (CollUtil.isEmpty(pageResult.getList())) {
            log.info("[getPartnerMarriageProfilePage][查询结果为空，pageNo={}, pageSize={}]",
                    pageReqVO.getPageNo(), pageReqVO.getPageSize());
            return PageResult.empty(pageResult.getTotal());
        }
        log.info("[getPartnerMarriageProfilePage][查询成功，总数={}，当前页数量={}，仅实名={}，要求背景图={}，仅异性={}]",
                pageResult.getTotal(), pageResult.getList().size(), pageReqVO.getRealVerifiedOnly(),
                pageReqVO.getBackgroundImageRequired(), pageReqVO.getOppositeSexOnly());

        List<Long> profileIds = convertList(pageResult.getList(), PartnerMarriageProfileDO::getId);
        Set<Long> marriageVerifiedIds = partnerCertificationRecordService.getMarriageVerifiedPartnerIds(profileIds);
        Map<Long, List<FileDO>> profileAlbumMap = convertMultiMap(
                fileService.getFileListByBizTypeAndBizIds(BIZ_TYPE_PARTNER_ALBUM, profileIds), FileDO::getBizId);
        Map<Long, PartnerDO> partnerMap = partnerMapper.selectByIds(profileIds).stream()
                .collect(Collectors.toMap(PartnerDO::getId, p -> p));

        // 查询每个用户的最近一条动态
        Map<Long, PartnerMomentDO> latestMomentMap = partnerMomentMapper.selectLatestByPartnerIds(profileIds);
        Set<Long> latestMomentIds = latestMomentMap.values().stream()
                .map(PartnerMomentDO::getId).collect(Collectors.toSet());
        Map<Long, List<String>> momentImageMap = latestMomentIds.isEmpty()
                ? Map.of()
                : fileService.getFileListByBizTypeAndBizIds(BIZ_TYPE_MOMENT_IMAGE, latestMomentIds)
                    .stream().collect(Collectors.groupingBy(FileDO::getBizId,
                            Collectors.mapping(FileDO::getUrl, Collectors.toList())));

        List<AppPartnerMarriageProfileRespVO> list = pageResult.getList().stream()
                .filter(profile -> partnerMap.get(profile.getId()) != null)
                .map(profile -> {
                    PartnerMomentDO latestMoment = latestMomentMap.get(profile.getId());
                    List<String> momentImages = latestMoment != null
                            ? momentImageMap.getOrDefault(latestMoment.getId(), List.of()) : List.of();
                    return buildRespVO(profile, partnerMap.get(profile.getId()),
                            profileAlbumMap.get(profile.getId()), latestMoment, momentImages,
                            marriageVerifiedIds.contains(profile.getId()));
                })
                .toList();
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public void clearRecommendCache() {
        log.info("[clearRecommendCache][清除推荐会员缓存]");
    }

    @Override
    public AppPartnerMarriageProfileRespVO getPartnerMarriageProfile(Long id) {
        PartnerMarriageProfileDO profile = partnerMarriageProfileMapper.selectById(id);
        PartnerDO partner = partnerMapper.selectById(id);
        if (profile == null || partner == null) {
            return null;
        }
        Set<Long> marriageVerifiedIds = partnerCertificationRecordService.getMarriageVerifiedPartnerIds(List.of(id));
        List<FileDO> albumImages = fileService.getFileListByBiz(BIZ_TYPE_PARTNER_ALBUM, id);
        PartnerMomentDO latestMoment = partnerMomentMapper.selectLatestByPartnerId(id);
        List<String> momentImages = latestMoment != null
                ? fileService.getFileListByBiz(BIZ_TYPE_MOMENT_IMAGE, latestMoment.getId())
                    .stream().map(FileDO::getUrl).filter(StrUtil::isNotBlank).toList()
                : List.of();
        return buildRespVO(profile, partner, albumImages, latestMoment, momentImages,
                marriageVerifiedIds.contains(id));
    }

    private AppPartnerMarriageProfileRespVO buildRespVO(PartnerMarriageProfileDO profile,
                                                        PartnerDO partner,
                                                        List<FileDO> albumImages,
                                                        PartnerMomentDO latestMoment,
                                                        List<String> momentImages,
                                                        boolean marriageVerified) {
        AppPartnerMarriageProfileRespVO respVO = PartnerMarriageProfileConvert.INSTANCE.convert(profile, partner);
        String city = formatCity(profile.getLiveAreaId());
        String education = formatEducation(profile.getEducation());
        String income = formatIncome(profile.getIncomeLevel());
        String maritalStatus = formatMaritalStatus(profile.getMaritalStatus());
        String houseStatus = formatHouseStatus(profile.getHouseStatus());
        String carStatus = formatCarStatus(profile.getCarStatus());
        List<String> albumImageUrls = buildAlbumImages(albumImages);

        respVO.setCity(city)
                .setEducation(education)
                .setIncome(income)
                .setMaritalStatus(maritalStatus)
                .setHouseStatus(houseStatus)
                .setCarStatus(carStatus)
                .setSex(partner.getSex())
                .setRealVerified(profile.getRealVerified())
                .setMarriageVerified(marriageVerified ? 1 : 0)
                .setMemberActive(Boolean.TRUE.equals(partner.getIsMember())
                        && partner.getMemberExpireTime() != null
                        && partner.getMemberExpireTime().isAfter(LocalDateTime.now()) ? 1 : 0)
                .setMaskedIdCard(maskIdCard(partner.getIdCard()))
                .setVerifiedLabel(Integer.valueOf(1).equals(profile.getRealVerified()) ? "已实名" : "")
                .setOnlineLabel("")
                .setViewerLabel("")
                .setAvatarImage(StrUtil.blankToDefault(respVO.getAvatarImage(), ""))
                .setMainImage(StrUtil.blankToDefault(profile.getBackgroundImage(), ""))
                .setTags(Collections.emptyList())
                .setInterestTags(buildInterestTags(profile))
                .setAlbumImages(albumImageUrls)
                .setLatestMoment(buildLatestMoment(latestMoment, momentImages));
        return respVO;
    }

    private String maskIdCard(String idCard) {
        if (StrUtil.length(idCard) < 4) {
            return "";
        }
        return idCard.substring(0, 4) + "**************";
    }

    private AppPartnerMomentSimpleRespVO buildLatestMoment(PartnerMomentDO moment, List<String> imageUrls) {
        if (moment == null) {
            return null;
        }
        return new AppPartnerMomentSimpleRespVO()
                .setId(moment.getId())
                .setContent(moment.getContent())
                .setImageUrls(CollUtil.isNotEmpty(imageUrls) ? imageUrls : List.of())
                .setPublishTime(moment.getPublishTime())
                .setLikeCount(moment.getLikeCount())
                .setCommentCount(moment.getCommentCount());
    }

    private List<String> buildInterestTags(PartnerMarriageProfileDO profile) {
        List<String> result = new ArrayList<>();
        String mateMarital = formatMaritalStatus(profile.getMateMaritalStatus());
        if (StrUtil.isNotBlank(mateMarital)) {
            result.add(mateMarital);
        }
        if (profile.getMateMinHeightCm() != null && profile.getMateMinHeightCm() > 0
                && profile.getMateMaxHeightCm() != null && profile.getMateMaxHeightCm() > 0) {
            result.add(profile.getMateMinHeightCm() + "-" + profile.getMateMaxHeightCm() + "cm");
        } else if (profile.getMateMinHeightCm() != null && profile.getMateMinHeightCm() > 0) {
            result.add(profile.getMateMinHeightCm() + "cm以上");
        } else if (profile.getMateMaxHeightCm() != null && profile.getMateMaxHeightCm() > 0) {
            result.add(profile.getMateMaxHeightCm() + "cm以下");
        }
        if (profile.getMateMinWeightKg() != null && profile.getMateMinWeightKg() > 0
                && profile.getMateMaxWeightKg() != null && profile.getMateMaxWeightKg() > 0) {
            result.add(profile.getMateMinWeightKg() + "-" + profile.getMateMaxWeightKg() + "kg");
        }
        String mateEducation = formatEducation(profile.getMateMinEducation());
        if (StrUtil.isNotBlank(mateEducation)) {
            result.add(mateEducation);
        }
        String mateCity = formatCity(profile.getMateLiveAreaId());
        if (StrUtil.isNotBlank(mateCity)) {
            result.add(mateCity);
        }
        String mateIncome = formatIncome(profile.getMateMinIncomeLevel());
        if (StrUtil.isNotBlank(mateIncome)) {
            result.add(mateIncome);
        }
        String mateHouse = formatHouseStatus(profile.getMateHouseStatus());
        if (StrUtil.isNotBlank(mateHouse)) {
            result.add(mateHouse);
        }
        String mateCar = formatCarStatus(profile.getMateCarStatus());
        if (StrUtil.isNotBlank(mateCar)) {
            result.add(mateCar);
        }
        if (StrUtil.isNotBlank(profile.getMateJobTitle())) {
            result.add(profile.getMateJobTitle());
        }
        if (StrUtil.isNotBlank(profile.getMateRemark())) {
            result.add(profile.getMateRemark());
        }
        return result.stream().distinct().toList();
    }

    private List<String> buildAlbumImages(List<FileDO> albumImages) {
        List<String> result = new ArrayList<>();
        if (CollUtil.isNotEmpty(albumImages)) {
            albumImages.stream()
                    .map(FileDO::getUrl)
                    .filter(StrUtil::isNotBlank)
                    .forEach(result::add);
        }
        return result.stream().distinct().toList();
    }

    private String formatCity(Long liveAreaId) {
        if (liveAreaId == null || liveAreaId <= 0 || liveAreaId > Integer.MAX_VALUE) {
            return "";
        }
        String fullName = AreaUtils.format(liveAreaId.intValue(), "/");
        if (StrUtil.isBlank(fullName)) {
            return "";
        }
        String[] names = fullName.split("/");
        if (names.length >= 2) {
            return names[names.length - 2] + " " + names[names.length - 1];
        }
        return fullName;
    }

    private String formatEducation(Integer education) {
        return DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.PARTNER_EDUCATION, education);
    }

    private String formatIncome(Integer incomeLevel) {
        return DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.PARTNER_INCOME_LEVEL, incomeLevel);
    }

    private String formatMaritalStatus(Integer maritalStatus) {
        return DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.PARTNER_MARITAL_STATUS, maritalStatus);
    }

    private String formatHouseStatus(Integer houseStatus) {
        return DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.PARTNER_HOUSE_STATUS, houseStatus);
    }

    private String formatCarStatus(Integer carStatus) {
        return DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.PARTNER_CAR_STATUS, carStatus);
    }

}
