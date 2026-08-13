package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPointRecordPageReqVO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerPointRecordMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;

import static org.mockito.Mockito.verify;

class PartnerPointRecordServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PartnerPointRecordServiceImpl service;

    @Mock
    private PartnerPointRecordMapper partnerPointRecordMapper;
    @Mock
    private PartnerService partnerService;

    @Test
    void getPointRecordPage_usesMapperRequestFilters() {
        PartnerPointRecordPageReqVO reqVO = new PartnerPointRecordPageReqVO();
        reqVO.setUserId(1L);
        reqVO.setBizType(1);

        service.getPointRecordPage(reqVO);

        verify(partnerPointRecordMapper).selectPage(reqVO);
    }

}
