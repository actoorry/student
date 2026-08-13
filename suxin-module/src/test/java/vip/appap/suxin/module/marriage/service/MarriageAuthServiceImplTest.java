package vip.appap.suxin.module.marriage.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMarriageProfileDO;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMarriageProfileMapper;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.service.PartnerCertificationRecordService;
import vip.appap.suxin.module.partner.service.PartnerService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class MarriageAuthServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MarriageAuthServiceImpl service;

    @Mock
    private PartnerService partnerService;

    @Mock
    private PartnerMarriageProfileMapper partnerMarriageProfileMapper;

    @Mock
    private PartnerCertificationRecordService partnerCertificationRecordService;

    @Test
    void getRealNameInfo_doesNotTreatHistoricalIdentityFieldsAsVerification() {
        PartnerDO partner = new PartnerDO();
        partner.setId(1L);
        partner.setName("张三");
        partner.setIdCard("110101199001010011");
        PartnerMarriageProfileDO profile = new PartnerMarriageProfileDO();
        profile.setId(1L);
        profile.setRealVerified(0);
        when(partnerService.getPartner(1L)).thenReturn(partner);
        when(partnerMarriageProfileMapper.selectById(1L)).thenReturn(profile);

        var result = service.getRealNameInfo(1L);

        assertEquals(0, result.getRealVerified());
        assertNull(result.getName());
        assertNull(result.getMaskedIdCard());
    }

    @Test
    void getRealNameInfo_usesPassedCertificationRecordWhenProfileProjectionIsStale() {
        PartnerDO partner = new PartnerDO();
        partner.setId(1L);
        partner.setName("张三");
        partner.setIdCard("110101199001010011");
        PartnerMarriageProfileDO profile = new PartnerMarriageProfileDO();
        profile.setId(1L);
        profile.setRealVerified(0);
        when(partnerService.getPartner(1L)).thenReturn(partner);
        when(partnerMarriageProfileMapper.selectById(1L)).thenReturn(profile);
        when(partnerCertificationRecordService.getRealNameVerifiedStatus(1L)).thenReturn(1);

        var result = service.getRealNameInfo(1L);

        assertEquals(1, result.getRealVerified());
        assertEquals("张三", result.getName());
        assertEquals("110101********0011", result.getMaskedIdCard());
    }
}
