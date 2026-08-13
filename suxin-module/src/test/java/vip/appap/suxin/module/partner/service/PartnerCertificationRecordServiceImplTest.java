package vip.appap.suxin.module.partner.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.marriage.service.PartnerCertificationRecordServiceImpl;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerNameCheckReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerCertificationRecordDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerCertificationRecordMapper;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PartnerCertificationRecordServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PartnerCertificationRecordServiceImpl service;

    @Mock
    private PartnerCertificationRecordMapper partnerCertificationRecordMapper;

    @Mock
    private PartnerMapper partnerMapper;

    @Mock
    private ChinaDataPayCertificationClient chinaDataPayCertificationClient;

    @Test
    void getRealNameVerifiedStatus_passedRecord_returnsVerified() {
        when(partnerCertificationRecordMapper.selectLatestByPartnerIdAndCertTypeAndStates(
                eq(1L), eq("REAL_NAME"), eq(List.of("1"))))
                .thenReturn(PartnerCertificationRecordDO.builder().id(10L).state("1").build());

        assertEquals(1, service.getRealNameVerifiedStatus(1L));
    }

    @Test
    void getRealNameVerifiedStatus_noPassedRecord_returnsUnverified() {
        assertEquals(0, service.getRealNameVerifiedStatus(1L));
    }

    @Test
    void getMarriageVerifiedPartnerIds_emptyCollection_returnsEmptyWithoutQuery() {
        Set<Long> result = service.getMarriageVerifiedPartnerIds(Collections.emptyList());
        assertTrue(result.isEmpty());
        verify(partnerCertificationRecordMapper, never()).selectMarriageVerifiedPartnerIds(any(), any(), any());
    }

    @Test
    void getMarriageVerifiedPartnerIds_nullCollection_returnsEmptyWithoutQuery() {
        Set<Long> result = service.getMarriageVerifiedPartnerIds(null);
        assertTrue(result.isEmpty());
        verify(partnerCertificationRecordMapper, never()).selectMarriageVerifiedPartnerIds(any(), any(), any());
    }

    @Test
    void getMarriageVerifiedPartnerIds_mixedProfiles_returnsOnlyVerifiedIds() {
        when(partnerCertificationRecordMapper.selectMarriageVerifiedPartnerIds(any(), any(), any()))
                .thenReturn(Set.of(101L, 103L));

        Set<Long> result = service.getMarriageVerifiedPartnerIds(List.of(101L, 102L, 103L));

        assertEquals(2, result.size());
        assertTrue(result.contains(101L));
        assertTrue(result.contains(103L));
        assertFalse(result.contains(102L));
        verify(partnerCertificationRecordMapper).selectMarriageVerifiedPartnerIds(
                eq(List.of(101L, 102L, 103L)),
                eq("REAL_MARRIAGE"),
                argThat(states -> states.containsAll(List.of("1", "2", "3")) && states.size() == 3)
        );
    }

    @Test
    void getMarriageVerifiedPartnerIds_multipleValidStates_passedAllStates() {
        when(partnerCertificationRecordMapper.selectMarriageVerifiedPartnerIds(any(), any(), any()))
                .thenReturn(Set.of(100L));

        service.getMarriageVerifiedPartnerIds(List.of(100L));

        verify(partnerCertificationRecordMapper).selectMarriageVerifiedPartnerIds(
                eq(List.of(100L)),
                eq("REAL_MARRIAGE"),
                argThat(states -> states.containsAll(List.of("1", "2", "3")) && states.size() == 3)
        );
    }

    @Test
    void getMarriageVerifiedPartnerIds_deduplicatesReturnedIds() {
        when(partnerCertificationRecordMapper.selectMarriageVerifiedPartnerIds(any(), any(), any()))
                .thenReturn(Set.of(200L, 201L));

        Set<Long> result = service.getMarriageVerifiedPartnerIds(List.of(200L, 201L, 202L));

        assertEquals(2, result.size());
    }

    @Test
    void nameCheck_rejectsNameLongerThanTwentyChineseCharactersBeforeCallingProvider() {
        PartnerDO partner = new PartnerDO();
        partner.setId(1L);
        partner.setMobile("13800138000");
        when(partnerMapper.selectById(1L)).thenReturn(partner);
        PartnerNameCheckReqVO reqVO = new PartnerNameCheckReqVO();
        reqVO.setName("张".repeat(21));
        reqVO.setIdCard("110101199001010011");

        assertFalse(service.nameCheck(1L, reqVO).getPassed());
        verifyNoInteractions(chinaDataPayCertificationClient);
    }
}
