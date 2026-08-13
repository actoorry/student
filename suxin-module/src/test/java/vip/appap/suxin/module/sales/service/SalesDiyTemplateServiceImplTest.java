package vip.appap.suxin.module.sales.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyTemplateCreateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyTemplateDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDiyPageMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDiyTemplateMapper;
import vip.appap.suxin.module.sales.service.SalesDiyPageService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesDiyTemplateServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesDiyTemplateServiceImpl service;
    @Mock
    private SalesDiyTemplateMapper diyTemplateMapper;
    @Mock
    private SalesDiyPageMapper diyPageMapper;
    @Mock
    private SalesDiyPageService diyPageService;
    @Test
    void createDiyTemplateWithDefaultPages_createsTheStandardMallPages() {
        SalesDiyTemplateCreateReqVO reqVO = new SalesDiyTemplateCreateReqVO();
        reqVO.setName("商城默认模板");
        when(diyTemplateMapper.selectByName("商城默认模板")).thenReturn(null);

        service.createDiyTemplateWithDefaultPages(reqVO, List.of("首页", "我的"));

        verify(diyTemplateMapper).insert(any(SalesDiyTemplateDO.class));
        verify(diyPageMapper, org.mockito.Mockito.times(2)).insert(any(SalesDiyPageDO.class));
        verify(diyPageService, never()).createDiyPage(any());
    }

}
