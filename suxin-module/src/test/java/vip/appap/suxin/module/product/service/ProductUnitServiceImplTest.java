package vip.appap.suxin.module.product.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitCreateReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitUpdateReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.dal.mysql.ProductUnitMapper;
import vip.appap.suxin.module.product.enums.DictTypeConstants;
import vip.appap.suxin.module.system.api.DictDataApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductUnitServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ProductUnitServiceImpl service;

    @Mock
    private ProductUnitMapper unitMapper;
    @Mock
    private DictDataApi dictDataApi;

    @Test
    void createUnit_withValidFactor_savesWithoutRelativeUnit() {
        ProductUnitCreateReqVO reqVO = createReqVO("kg", 2, "1000");

        assertDoesNotThrow(() -> service.createUnit(reqVO));

        ArgumentCaptor<ProductUnitDO> captor = ArgumentCaptor.forClass(ProductUnitDO.class);
        verify(unitMapper).insert(captor.capture());
        assertEquals("1000", captor.getValue().getRelativeFactor());
        assertNull(captor.getValue().getRelativeUnitId());
        verify(dictDataApi).validateDictDataList(eq(DictTypeConstants.PRODUCT_UNIT_TYPE), any(Collection.class));
    }

    @Test
    void createUnit_withTypeZero_rejects() {
        ProductUnitCreateReqVO reqVO = createReqVO("base", 0, "1");

        assertThrows(RuntimeException.class, () -> service.createUnit(reqVO));
    }

    @Test
    void createUnit_withFactorOne_savesAsManagementUnit() {
        ProductUnitCreateReqVO reqVO = createReqVO("g", 2, "1.00");

        assertDoesNotThrow(() -> service.createUnit(reqVO));

        ArgumentCaptor<ProductUnitDO> captor = ArgumentCaptor.forClass(ProductUnitDO.class);
        verify(unitMapper).insert(captor.capture());
        assertEquals("1.00", captor.getValue().getRelativeFactor());
    }

    @Test
    void createUnit_withInvalidFactor_rejects() {
        ProductUnitCreateReqVO reqVO = createReqVO("kg", 2, "0");

        assertThrows(RuntimeException.class, () -> service.createUnit(reqVO));
    }

    @Test
    void updateUnit_withFactorOne_succeeds() {
        when(unitMapper.selectById(1L)).thenReturn(unit(1L, "g", 2, "1"));
        ProductUnitUpdateReqVO reqVO = updateReqVO(1L, "gram", 2, "1");

        assertDoesNotThrow(() -> service.updateUnit(reqVO));
        verify(unitMapper).updateById(any(ProductUnitDO.class));
    }

    @Test
    void deleteUnit_withFactorOne_succeeds() {
        when(unitMapper.selectById(1L)).thenReturn(unit(1L, "g", 2, "1"));

        assertDoesNotThrow(() -> service.deleteUnit(1L));
        verify(unitMapper).deleteById(1L);
    }

    @Test
    void deleteUnit_forTypeZeroBaseUnit_rejects() {
        when(unitMapper.selectById(1L)).thenReturn(unit(1L, "unit", 0, null));

        assertThrows(RuntimeException.class, () -> service.deleteUnit(1L));
    }

    @Test
    void deleteUnit_forBlankFactorBaseUnit_rejects() {
        when(unitMapper.selectById(1L)).thenReturn(unit(1L, "g", 2, null));

        assertThrows(RuntimeException.class, () -> service.deleteUnit(1L));
    }

    @Test
    void validateUnitNameUnique_existingName_rejects() {
        when(unitMapper.selectByName("kg")).thenReturn(unit(1L, "kg", 2, "1000"));

        assertThrows(RuntimeException.class, () -> service.validateUnitNameUnique(null, "kg"));
    }

    private ProductUnitCreateReqVO createReqVO(String name, Integer type, String relativeFactor) {
        ProductUnitCreateReqVO reqVO = new ProductUnitCreateReqVO();
        reqVO.setName(name);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setType(type);
        reqVO.setRelativeFactor(relativeFactor);
        return reqVO;
    }

    private ProductUnitUpdateReqVO updateReqVO(Long id, String name, Integer type, String relativeFactor) {
        ProductUnitUpdateReqVO reqVO = new ProductUnitUpdateReqVO();
        reqVO.setId(id);
        reqVO.setName(name);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setType(type);
        reqVO.setRelativeFactor(relativeFactor);
        return reqVO;
    }

    private ProductUnitDO unit(Long id, String name, Integer type, String relativeFactor) {
        return ProductUnitDO.builder()
                .id(id)
                .name(name)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .type(type)
                .relativeFactor(relativeFactor)
                .build();
    }

}
