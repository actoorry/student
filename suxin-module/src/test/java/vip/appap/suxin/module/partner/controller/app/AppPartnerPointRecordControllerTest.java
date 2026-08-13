package vip.appap.suxin.module.partner.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPointRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerPointPageRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerPointRecordDO;
import vip.appap.suxin.module.partner.service.PartnerPointRecordService;
import vip.appap.suxin.module.partner.service.PartnerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppPartnerPointRecordControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppPartnerPointRecordController controller;

    @Mock
    private PartnerPointRecordService pointRecordService;
    @Mock
    private PartnerService partnerService;

    @Test
    void getPointRecordPage_overridesRequestUserIdAndReturnsBalance() {
        when(partnerService.getPartner(7L)).thenReturn(PartnerDO.builder().id(7L).point(88).build());
        when(pointRecordService.getPointRecordPage(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageResult<>(List.of(PartnerPointRecordDO.builder().id(1L).point(10).totalPoint(88).build()), 1L));
        PartnerPointRecordPageReqVO reqVO = new PartnerPointRecordPageReqVO();
        reqVO.setUserId(99L);

        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(7L);

            CommonResult<AppPartnerPointPageRespVO> result = controller.getPointRecordPage(reqVO);

            ArgumentCaptor<PartnerPointRecordPageReqVO> captor = ArgumentCaptor.forClass(PartnerPointRecordPageReqVO.class);
            verify(pointRecordService).getPointRecordPage(captor.capture());
            assertEquals(7L, captor.getValue().getUserId());
            assertEquals(88, result.getData().getTotalPoint());
            assertEquals(1L, result.getData().getRecords().getTotal());
        }
    }

}
