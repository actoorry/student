package vip.appap.suxin.module.marriage.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfilePageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfileRespVO;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMarriageProfileDO;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMarriageProfileMapper;
import vip.appap.suxin.module.marriage.dal.mysql.PartnerMomentMapper;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.service.PartnerCertificationRecordService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PartnerMarriageProfileServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PartnerMarriageProfileServiceImpl service;

    @Mock
    private PartnerMarriageProfileMapper partnerMarriageProfileMapper;
    @Mock
    private PartnerMomentMapper partnerMomentMapper;
    @Mock
    private PartnerMapper partnerMapper;
    @Mock
    private vip.appap.suxin.module.infra.service.file.FileService fileService;
    @Mock
    private PartnerCertificationRecordService partnerCertificationRecordService;

    @Test
    void getPartnerMarriageProfilePage_usesComponentRecommendationRules() {
        AppPartnerMarriageProfilePageReqVO reqVO = new AppPartnerMarriageProfilePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setRealVerifiedOnly(false);
        reqVO.setBackgroundImageRequired(false);
        reqVO.setOppositeSexOnly(false);

        PartnerMarriageProfileDO profile = new PartnerMarriageProfileDO();
        profile.setId(101L);
        PartnerDO partner = new PartnerDO();
        partner.setId(101L);
        partner.setAvatar("http://avatar.jpg");
        PageResult<PartnerMarriageProfileDO> pageResult = new PageResult<>(List.of(profile), 1L);
        when(partnerMarriageProfileMapper.selectRecommendPage(reqVO, 100L, 1)).thenReturn(pageResult);
        when(partnerMapper.selectByIds(List.of(101L))).thenReturn(List.of(partner));
        when(fileService.getFileListByBizTypeAndBizIds(any(), any())).thenReturn(Collections.emptyList());
        when(partnerMomentMapper.selectLatestByPartnerIds(any())).thenReturn(Collections.emptyMap());
        when(partnerCertificationRecordService.getMarriageVerifiedPartnerIds(any())).thenReturn(Collections.emptySet());

        PageResult<AppPartnerMarriageProfileRespVO> result = service.getPartnerMarriageProfilePage(reqVO, 100L, 1);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(0, result.getList().get(0).getMarriageVerified());
        verify(partnerMarriageProfileMapper).selectRecommendPage(reqVO, 100L, 1);
        verify(partnerCertificationRecordService).getMarriageVerifiedPartnerIds(List.of(101L));
    }

    @Test
    void getPartnerMarriageProfilePage_mixedMarriageVerified_projects0And1() {
        AppPartnerMarriageProfilePageReqVO reqVO = new AppPartnerMarriageProfilePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PartnerMarriageProfileDO profile1 = new PartnerMarriageProfileDO();
        profile1.setId(101L);
        PartnerMarriageProfileDO profile2 = new PartnerMarriageProfileDO();
        profile2.setId(102L);
        PartnerDO partner1 = new PartnerDO();
        partner1.setId(101L);
        PartnerDO partner2 = new PartnerDO();
        partner2.setId(102L);

        PageResult<PartnerMarriageProfileDO> pageResult = new PageResult<>(List.of(profile1, profile2), 2L);
        when(partnerMarriageProfileMapper.selectRecommendPage(reqVO, 100L, 1)).thenReturn(pageResult);
        when(partnerMapper.selectByIds(List.of(101L, 102L))).thenReturn(List.of(partner1, partner2));
        when(fileService.getFileListByBizTypeAndBizIds(any(), any())).thenReturn(Collections.emptyList());
        when(partnerMomentMapper.selectLatestByPartnerIds(any())).thenReturn(Collections.emptyMap());
        when(partnerCertificationRecordService.getMarriageVerifiedPartnerIds(List.of(101L, 102L)))
                .thenReturn(Set.of(102L));

        PageResult<AppPartnerMarriageProfileRespVO> result = service.getPartnerMarriageProfilePage(reqVO, 100L, 1);

        assertEquals(2, result.getList().size());
        AppPartnerMarriageProfileRespVO vo1 = result.getList().get(0);
        AppPartnerMarriageProfileRespVO vo2 = result.getList().get(1);
        assertEquals(101L, vo1.getId());
        assertEquals(0, vo1.getMarriageVerified());
        assertEquals(102L, vo2.getId());
        assertEquals(1, vo2.getMarriageVerified());
        verify(partnerCertificationRecordService, times(1)).getMarriageVerifiedPartnerIds(any());
    }

    @Test
    void getPartnerMarriageProfile_marriageVerified_returns1() {
        Long id = 201L;
        PartnerMarriageProfileDO profile = new PartnerMarriageProfileDO();
        profile.setId(id);
        PartnerDO partner = new PartnerDO();
        partner.setId(id);
        partner.setIdCard("440308199001011234");

        when(partnerMarriageProfileMapper.selectById(id)).thenReturn(profile);
        when(partnerMapper.selectById(id)).thenReturn(partner);
        when(partnerCertificationRecordService.getMarriageVerifiedPartnerIds(List.of(id))).thenReturn(Set.of(id));
        when(fileService.getFileListByBiz(any(), any())).thenReturn(Collections.emptyList());
        when(partnerMomentMapper.selectLatestByPartnerId(any())).thenReturn(null);

        AppPartnerMarriageProfileRespVO result = service.getPartnerMarriageProfile(id);

        assertNotNull(result);
        assertEquals(1, result.getMarriageVerified());
        assertEquals("4403**************", result.getMaskedIdCard());
        verify(partnerCertificationRecordService, times(1)).getMarriageVerifiedPartnerIds(List.of(id));
    }

    @Test
    void getPartnerMarriageProfile_singleNotVerified_returns0() {
        Long id = 202L;
        PartnerMarriageProfileDO profile = new PartnerMarriageProfileDO();
        profile.setId(id);
        PartnerDO partner = new PartnerDO();
        partner.setId(id);
        partner.setIdCard("440308199001011234");

        when(partnerMarriageProfileMapper.selectById(id)).thenReturn(profile);
        when(partnerMapper.selectById(id)).thenReturn(partner);
        when(partnerCertificationRecordService.getMarriageVerifiedPartnerIds(List.of(id))).thenReturn(Collections.emptySet());
        when(fileService.getFileListByBiz(any(), any())).thenReturn(Collections.emptyList());
        when(partnerMomentMapper.selectLatestByPartnerId(any())).thenReturn(null);

        AppPartnerMarriageProfileRespVO result = service.getPartnerMarriageProfile(id);

        assertNotNull(result);
        assertEquals(0, result.getMarriageVerified());
        verify(partnerCertificationRecordService, times(1)).getMarriageVerifiedPartnerIds(List.of(id));
    }

    @Test
    void getPartnerMarriageProfilePage_emptyPage_doesNotQueryMarriageVerified() {
        AppPartnerMarriageProfilePageReqVO reqVO = new AppPartnerMarriageProfilePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        when(partnerMarriageProfileMapper.selectRecommendPage(reqVO, 100L, 1))
                .thenReturn(PageResult.empty(0L));

        PageResult<AppPartnerMarriageProfileRespVO> result = service.getPartnerMarriageProfilePage(reqVO, 100L, 1);

        assertTrue(result.getList().isEmpty());
        verify(partnerCertificationRecordService, never()).getMarriageVerifiedPartnerIds(any());
    }

    @Test
    void getPartnerMarriageProfilePage_doesNotExposeFullIdCard() {
        AppPartnerMarriageProfilePageReqVO reqVO = new AppPartnerMarriageProfilePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PartnerMarriageProfileDO profile = new PartnerMarriageProfileDO();
        profile.setId(101L);
        PartnerDO partner = new PartnerDO();
        partner.setId(101L);
        partner.setIdCard("440308199001011234");

        PageResult<PartnerMarriageProfileDO> pageResult = new PageResult<>(List.of(profile), 1L);
        when(partnerMarriageProfileMapper.selectRecommendPage(reqVO, 100L, 1)).thenReturn(pageResult);
        when(partnerMapper.selectByIds(List.of(101L))).thenReturn(List.of(partner));
        when(fileService.getFileListByBizTypeAndBizIds(any(), any())).thenReturn(Collections.emptyList());
        when(partnerMomentMapper.selectLatestByPartnerIds(any())).thenReturn(Collections.emptyMap());
        when(partnerCertificationRecordService.getMarriageVerifiedPartnerIds(any())).thenReturn(Collections.emptySet());

        PageResult<AppPartnerMarriageProfileRespVO> result = service.getPartnerMarriageProfilePage(reqVO, 100L, 1);

        AppPartnerMarriageProfileRespVO vo = result.getList().get(0);
        assertEquals("4403**************", vo.getMaskedIdCard());
        assertFalse(vo.getMaskedIdCard().contains("199001011234"));
        assertNull(vo.getLatestMoment());
    }

}
