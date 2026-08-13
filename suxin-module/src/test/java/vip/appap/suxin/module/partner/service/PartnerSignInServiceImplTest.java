package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerSignInSummaryRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInConfigDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInRecordDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerSignInConfigMapper;
import vip.appap.suxin.module.partner.dal.mysql.PartnerSignInRecordMapper;
import vip.appap.suxin.module.partner.enums.ErrorCodeConstants;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PartnerSignInServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PartnerSignInServiceImpl service;

    @Mock
    private PartnerSignInConfigMapper partnerSignInConfigMapper;
    @Mock
    private PartnerSignInRecordMapper partnerSignInRecordMapper;
    @Mock
    private PartnerService partnerService;
    @Mock
    private PartnerPointRecordService pointRecordService;
    @Mock
    private PartnerExperienceRecordService experienceRecordService;

    @Test
    void signIn_whenAlreadySignedToday_throwsAndDoesNotGrantReward() {
        when(partnerService.getPartner(1L)).thenReturn(PartnerDO.builder().id(1L).build());
        when(partnerSignInRecordMapper.selectByUserIdAndDate(eq(1L), any(LocalDateTime.class)))
                .thenReturn(PartnerSignInRecordDO.builder().id(10L).build());

        ServiceException exception = assertThrows(ServiceException.class, () -> service.signIn(1L));

        assertEquals(ErrorCodeConstants.PARTNER_SIGN_IN_ALREADY.getCode(), exception.getCode());
        verify(partnerSignInRecordMapper, never()).insert(any(PartnerSignInRecordDO.class));
        verify(pointRecordService, never()).createPointRecord(any(), any(), any(), any());
    }

    @Test
    void signIn_whenConsecutiveThirdDay_usesThirdDayRewardConfig() {
        LocalDate today = LocalDate.now();
        when(partnerService.getPartner(1L)).thenReturn(PartnerDO.builder().id(1L).experience(20).build());
        when(partnerSignInRecordMapper.selectByUserIdAndDate(eq(1L), any(LocalDateTime.class))).thenReturn(null);
        when(partnerSignInConfigMapper.selectListByStatus(0)).thenReturn(List.of(
                config(1, 1, 10, 1),
                config(2, 2, 20, 2),
                config(3, 3, 30, 3)));
        when(partnerSignInRecordMapper.selectRecentListByUserId(eq(1L), any(LocalDateTime.class), eq(366)))
                .thenReturn(List.of(
                        record(11L, 2, today.minusDays(1).atTime(8, 0)),
                        record(12L, 1, today.minusDays(2).atTime(8, 0))));
        doAnswer(invocation -> {
            PartnerSignInRecordDO record = invocation.getArgument(0);
            record.setId(99L);
            return 1;
        }).when(partnerSignInRecordMapper).insert(any(PartnerSignInRecordDO.class));

        PartnerSignInRecordDO result = service.signIn(1L);

        assertEquals(3, result.getDay());
        assertEquals(30, result.getPoint());
        assertEquals(3, result.getExperience());
        verify(pointRecordService).createPointRecord(1L, 30, 1, "99");
        verify(experienceRecordService).createExperienceRecord(1L, 3, 23, 1, "99");
    }

    @Test
    void signIn_whenNoEnabledConfig_throwsBusinessError() {
        when(partnerService.getPartner(1L)).thenReturn(PartnerDO.builder().id(1L).build());
        when(partnerSignInRecordMapper.selectByUserIdAndDate(eq(1L), any(LocalDateTime.class))).thenReturn(null);
        when(partnerSignInConfigMapper.selectListByStatus(0)).thenReturn(List.of());

        ServiceException exception = assertThrows(ServiceException.class, () -> service.signIn(1L));

        assertEquals(ErrorCodeConstants.PARTNER_SIGN_IN_CONFIG_NOT_EXISTS.getCode(), exception.getCode());
    }

    @Test
    void getSignInSummary_whenNotSignedToday_returnsCurrentStreakAndNextReward() {
        LocalDate today = LocalDate.now();
        when(partnerSignInRecordMapper.selectByUserIdAndDate(eq(1L), any(LocalDateTime.class))).thenReturn(null);
        when(partnerSignInRecordMapper.selectRecentListByUserId(eq(1L), any(LocalDateTime.class), eq(366)))
                .thenReturn(List.of(record(11L, 1, today.minusDays(1).atTime(8, 0))));
        when(partnerSignInConfigMapper.selectListByStatus(0)).thenReturn(List.of(
                config(1, 1, 10, 0),
                config(2, 2, 20, 0)));

        AppPartnerSignInSummaryRespVO result = service.getSignInSummary(1L);

        assertEquals(false, result.getSignedToday());
        assertEquals(1, result.getCurrentDay());
        assertEquals(2, result.getNextRewardDay());
        assertEquals(20, result.getNextRewardPoint());
        assertEquals(0, result.getNextRewardExperience());
        assertNull(result.getTodayRecord());
        assertEquals(2, result.getConfigs().size());
    }

    private PartnerSignInConfigDO config(Integer id, Integer day, Integer point, Integer experience) {
        return PartnerSignInConfigDO.builder()
                .id(id)
                .day(day)
                .point(point)
                .experience(experience)
                .status(0)
                .build();
    }

    private PartnerSignInRecordDO record(Long id, Integer day, LocalDateTime createTime) {
        PartnerSignInRecordDO record = PartnerSignInRecordDO.builder()
                .id(id)
                .day(day)
                .point(10)
                .experience(0)
                .build();
        record.setCreateTime(createTime);
        return record;
    }

}
