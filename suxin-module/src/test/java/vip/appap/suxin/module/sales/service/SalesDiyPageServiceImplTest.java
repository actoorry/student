package vip.appap.suxin.module.sales.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiyPageUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDiyPageMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.DIY_PAGE_NAME_USED;

class SalesDiyPageServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesDiyPageServiceImpl service;
    @Mock
    private SalesDiyPageMapper diyPageMapper;
    @Test
    void updateDiyPage_whenTemplatePageNameAlreadyExists_rejects() {
        SalesDiyPageDO targetPage = page(1L, "首页");
        SalesDiyPageDO duplicatePage = page(2L, "我的");
        when(diyPageMapper.selectById(1L)).thenReturn(targetPage);
        when(diyPageMapper.selectListByTemplateId(100L)).thenReturn(List.of(targetPage, duplicatePage));
        SalesDiyPageUpdateReqVO reqVO = new SalesDiyPageUpdateReqVO();
        reqVO.setId(1L);
        reqVO.setTemplateId(100L);
        reqVO.setName("我的");

        var exception = assertThrows(vip.appap.suxin.framework.common.exception.ServiceException.class,
                () -> service.updateDiyPage(reqVO));

        org.junit.jupiter.api.Assertions.assertEquals(DIY_PAGE_NAME_USED.getCode(), exception.getCode());
    }

    private SalesDiyPageDO page(Long id, String name) {
        SalesDiyPageDO page = new SalesDiyPageDO();
        page.setId(id);
        page.setTemplateId(100L);
        page.setName(name);
        return page;
    }

}
