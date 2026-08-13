package vip.appap.suxin.module.product.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigListRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigUpdateReqVO;
import vip.appap.suxin.module.product.enums.ProductDisplaySceneEnum;
import vip.appap.suxin.module.product.service.ProductDisplayConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductDisplayConfigControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ProductDisplayConfigController controller;

    @Mock
    private ProductDisplayConfigService productDisplayConfigService;

    @Test
    void getDisplayConfigList_delegatesToService() {
        ProductDisplayConfigListRespVO vo = new ProductDisplayConfigListRespVO();
        vo.setSceneCode(ProductDisplaySceneEnum.MEMBER_PAGE.getCode());
        vo.setConfigState("ACTIVE");
        when(productDisplayConfigService.getDisplayConfigList()).thenReturn(List.of(vo));

        CommonResult<List<ProductDisplayConfigListRespVO>> result = controller.getDisplayConfigList();

        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), result.getData().get(0).getSceneCode());
        verify(productDisplayConfigService).getDisplayConfigList();
    }

    @Test
    void updateDisplayConfig_delegatesToService() {
        ProductDisplayConfigUpdateReqVO reqVO = new ProductDisplayConfigUpdateReqVO();
        reqVO.setSceneCode(ProductDisplaySceneEnum.MEMBER_PAGE.getCode());
        reqVO.setCategoryId(90L);

        ProductDisplayConfigListRespVO vo = new ProductDisplayConfigListRespVO();
        vo.setSceneCode(ProductDisplaySceneEnum.MEMBER_PAGE.getCode());
        vo.setConfigState("ACTIVE");
        when(productDisplayConfigService.updateDisplayConfig(any(ProductDisplayConfigUpdateReqVO.class))).thenReturn(vo);

        CommonResult<ProductDisplayConfigListRespVO> result = controller.updateDisplayConfig(reqVO);

        assertNotNull(result.getData());
        assertEquals("ACTIVE", result.getData().getConfigState());
        verify(productDisplayConfigService).updateDisplayConfig(reqVO);
    }

}
