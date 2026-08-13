package vip.appap.suxin.module.partner.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInConfigSaveReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerSignInConfigRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerSignInRecordRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerSignInSummaryRespVO;
import vip.appap.suxin.module.partner.convert.PartnerSignInConfigConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInConfigDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInRecordDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerSignInConfigMapper;
import vip.appap.suxin.module.partner.dal.mysql.PartnerSignInRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_NOT_EXISTS;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_SIGN_IN_ALREADY;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_SIGN_IN_CONFIG_NOT_EXISTS;

/**
 * 会员签到 Service 实现类
 */
@Service
@Validated
public class PartnerSignInServiceImpl implements PartnerSignInService {

    @Resource
    private PartnerSignInConfigMapper partnerSignInConfigMapper;

    @Resource
    private PartnerSignInRecordMapper partnerSignInRecordMapper;

    @Resource
    private PartnerService partnerService;

    @Resource
    private PartnerPointRecordService pointRecordService;

    @Resource
    private PartnerExperienceRecordService experienceRecordService;

    @Override
    public void saveSignInConfig(PartnerSignInConfigSaveReqVO saveReqVO) {
        PartnerSignInConfigDO dbConfig = saveReqVO.getId() != null
                ? partnerSignInConfigMapper.selectById(saveReqVO.getId()) : null;
        if (dbConfig != null) {
            partnerSignInConfigMapper.updateById(PartnerSignInConfigConvert.INSTANCE.convert(saveReqVO));
            return;
        }
        partnerSignInConfigMapper.insert(PartnerSignInConfigConvert.INSTANCE.convert(saveReqVO));
    }

    @Override
    public void saveSignInConfigList(List<PartnerSignInConfigSaveReqVO> saveReqVOList) {
        if (CollUtil.isEmpty(saveReqVOList)) {
            return;
        }
        saveReqVOList.forEach(this::saveSignInConfig);
    }

    @Override
    public List<PartnerSignInConfigDO> getSignInConfigList() {
        List<PartnerSignInConfigDO> list = partnerSignInConfigMapper.selectList();
        if (CollUtil.isEmpty(list)) {
            return list;
        }
        return list.stream()
                .sorted(Comparator.comparing(PartnerSignInConfigDO::getDay)
                        .thenComparing(PartnerSignInConfigDO::getId))
                .toList();
    }

    @Override
    public List<PartnerSignInConfigDO> getEnabledSignInConfigList() {
        return partnerSignInConfigMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public PageResult<PartnerSignInRecordDO> getSignInRecordPage(PartnerSignInRecordPageReqVO pageReqVO) {
        return partnerSignInRecordMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerSignInRecordDO signIn(Long userId) {
        PartnerDO partner = partnerService.getPartner(userId);
        if (partner == null) {
            throw exception(PARTNER_NOT_EXISTS);
        }

        PartnerSignInRecordDO todayRecord = getTodaySignInRecord(userId);
        if (todayRecord != null) {
            throw exception(PARTNER_SIGN_IN_ALREADY);
        }

        List<PartnerSignInConfigDO> configs = getEnabledSignInConfigList();
        if (CollUtil.isEmpty(configs)) {
            throw exception(PARTNER_SIGN_IN_CONFIG_NOT_EXISTS);
        }

        Integer consecutiveDay = calculateConsecutiveDay(userId, LocalDate.now().minusDays(1)) + 1;
        PartnerSignInConfigDO config = selectRewardConfig(configs, consecutiveDay);
        Integer point = config.getPoint();
        Integer experience = config.getExperience();

        PartnerSignInRecordDO record = PartnerSignInRecordDO.builder()
                .userId(userId)
                .day(consecutiveDay)
                .point(point)
                .experience(experience)
                .build();
        partnerSignInRecordMapper.insert(record);

        if (point != null && point > 0) {
            pointRecordService.createPointRecord(userId, point, 1, String.valueOf(record.getId()));
        }
        if (experience != null && experience > 0) {
            experienceRecordService.createExperienceRecord(userId, experience,
                    defaultZero(partner.getExperience()) + experience, 1, String.valueOf(record.getId()));
        }
        return record;
    }

    @Override
    public AppPartnerSignInSummaryRespVO getSignInSummary(Long userId) {
        PartnerSignInRecordDO todayRecord = getTodaySignInRecord(userId);
        boolean signedToday = todayRecord != null;
        Integer currentDay = signedToday ? todayRecord.getDay()
                : calculateConsecutiveDay(userId, LocalDate.now().minusDays(1));

        List<PartnerSignInConfigDO> configs = getEnabledSignInConfigList();
        PartnerSignInConfigDO nextConfig = CollUtil.isEmpty(configs) ? null
                : selectRewardConfig(configs, currentDay + 1);

        AppPartnerSignInSummaryRespVO respVO = new AppPartnerSignInSummaryRespVO();
        respVO.setSignedToday(signedToday);
        respVO.setCurrentDay(currentDay);
        respVO.setTodayRecord(BeanUtils.toBean(todayRecord, AppPartnerSignInRecordRespVO.class));
        respVO.setConfigs(BeanUtils.toBean(configs, AppPartnerSignInConfigRespVO.class));
        if (nextConfig != null) {
            respVO.setNextRewardDay(currentDay + 1);
            respVO.setNextRewardPoint(nextConfig.getPoint());
            respVO.setNextRewardExperience(nextConfig.getExperience());
        }
        return respVO;
    }

    @Override
    public PartnerSignInRecordDO getTodaySignInRecord(Long userId) {
        return partnerSignInRecordMapper.selectByUserIdAndDate(userId, LocalDateTime.now());
    }

    private Integer calculateConsecutiveDay(Long userId, LocalDate startDate) {
        List<PartnerSignInRecordDO> records = partnerSignInRecordMapper.selectRecentListByUserId(
                userId, startDate.plusDays(1).atStartOfDay(), 366);
        if (CollUtil.isEmpty(records)) {
            return 0;
        }
        Set<LocalDate> signedDates = new HashSet<>();
        records.stream()
                .filter(record -> record.getCreateTime() != null)
                .map(record -> record.getCreateTime().toLocalDate())
                .forEach(signedDates::add);

        int consecutiveDay = 0;
        LocalDate cursor = startDate;
        while (signedDates.contains(cursor)) {
            consecutiveDay++;
            cursor = cursor.minusDays(1);
        }
        return consecutiveDay;
    }

    private PartnerSignInConfigDO selectRewardConfig(List<PartnerSignInConfigDO> configs, Integer consecutiveDay) {
        return configs.stream()
                .filter(config -> config.getDay().equals(consecutiveDay))
                .findFirst()
                .orElse(configs.get(0));
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

}
