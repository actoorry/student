package vip.appap.suxin.module.product.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigListRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductDisplayConfigUpdateReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductDisplayConfigDO;
import vip.appap.suxin.module.product.dal.mysql.ProductCategoryMapper;
import vip.appap.suxin.module.product.dal.mysql.ProductDisplayConfigMapper;
import vip.appap.suxin.module.product.enums.ProductDisplaySceneEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductDisplayConfigServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ProductDisplayConfigServiceImpl service;

    @Mock
    private ProductDisplayConfigMapper productDisplayConfigMapper;
    @Mock
    private ProductCategoryMapper productCategoryMapper;
    @Mock
    private ProductCategoryService productCategoryService;

    @Test
    void getEnabledCategoryIdsBySceneCode_enabledScene_returnsExistingEnabledCategoriesInConfiguredOrder() {
        when(productDisplayConfigMapper.selectEnabledBySceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE,
                CommonStatusEnum.ENABLE.getStatus())).thenReturn(ProductDisplayConfigDO.builder()
                .sceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE)
                .categoryIds("10,20,30")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build());
        when(productCategoryMapper.selectListByIdAndStatus(eq(List.of(10L, 20L, 30L)),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of(
                ProductCategoryDO.builder().id(30L).status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ProductCategoryDO.builder().id(10L).status(CommonStatusEnum.ENABLE.getStatus()).build()));

        List<Long> categoryIds = service.getEnabledCategoryIdsBySceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE);

        assertEquals(List.of(10L, 30L), categoryIds);
    }

    @Test
    void getEnabledCategoryIdsBySceneCode_missingScene_returnsEmptyList() {
        when(productDisplayConfigMapper.selectEnabledBySceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE,
                CommonStatusEnum.ENABLE.getStatus())).thenReturn(null);

        List<Long> categoryIds = service.getEnabledCategoryIdsBySceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE);

        assertEquals(List.of(), categoryIds);
    }

    @Test
    void getEnabledCategoryIdsBySceneCode_malformedConfig_filtersInvalidValues() {
        when(productDisplayConfigMapper.selectEnabledBySceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE,
                CommonStatusEnum.ENABLE.getStatus())).thenReturn(ProductDisplayConfigDO.builder()
                .sceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE)
                .categoryIds("10, bad, -1, 10, 20")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build());
        when(productCategoryMapper.selectListByIdAndStatus(eq(List.of(10L, 20L)),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of(
                ProductCategoryDO.builder().id(20L).status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ProductCategoryDO.builder().id(10L).status(CommonStatusEnum.ENABLE.getStatus()).build()));

        List<Long> categoryIds = service.getEnabledCategoryIdsBySceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE);

        assertEquals(List.of(10L, 20L), categoryIds);
    }

    @Test
    void getEnabledCategoryIdsBySceneCode_emotionCourseScene_returnsExistingEnabledCategories() {
        when(productDisplayConfigMapper.selectEnabledBySceneCode(ProductDisplayConfigService.SCENE_EMOTION_COURSE_PAGE,
                CommonStatusEnum.ENABLE.getStatus())).thenReturn(ProductDisplayConfigDO.builder()
                .sceneCode(ProductDisplayConfigService.SCENE_EMOTION_COURSE_PAGE)
                .categoryIds("94")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build());
        when(productCategoryMapper.selectListByIdAndStatus(eq(List.of(94L)),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of(
                ProductCategoryDO.builder().id(94L).status(CommonStatusEnum.ENABLE.getStatus()).build()));

        List<Long> categoryIds = service.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_EMOTION_COURSE_PAGE);

        assertEquals(List.of(94L), categoryIds);
    }

    @Test
    void getEnabledCategoryIdsBySceneCode_missingEmotionCourseScene_returnsEmptyList() {
        when(productDisplayConfigMapper.selectEnabledBySceneCode(ProductDisplayConfigService.SCENE_EMOTION_COURSE_PAGE,
                CommonStatusEnum.ENABLE.getStatus())).thenReturn(null);

        List<Long> categoryIds = service.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_EMOTION_COURSE_PAGE);

        assertEquals(List.of(), categoryIds);
    }

    @Test
    void getEnabledCategoryIdsBySceneCode_disabledEmotionCourseScene_returnsEmptyList() {
        when(productDisplayConfigMapper.selectEnabledBySceneCode(ProductDisplayConfigService.SCENE_EMOTION_COURSE_PAGE,
                CommonStatusEnum.ENABLE.getStatus())).thenReturn(null);

        List<Long> categoryIds = service.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_EMOTION_COURSE_PAGE);

        assertEquals(List.of(), categoryIds);
    }

    @Test
    void getEnabledCategoryIdsBySceneCode_offlineActivityScene_returnsExistingEnabledCategories() {
        when(productDisplayConfigMapper.selectEnabledBySceneCode(ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE,
                CommonStatusEnum.ENABLE.getStatus())).thenReturn(ProductDisplayConfigDO.builder()
                .sceneCode(ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE)
                .categoryIds("95")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build());
        when(productCategoryMapper.selectListByIdAndStatus(eq(List.of(95L)),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of(
                ProductCategoryDO.builder().id(95L).status(CommonStatusEnum.ENABLE.getStatus()).build()));

        List<Long> categoryIds = service.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE);

        assertEquals(List.of(95L), categoryIds);
    }

    @Test
    void getEnabledCategoryIdsBySceneCode_missingOfflineActivityScene_returnsEmptyList() {
        when(productDisplayConfigMapper.selectEnabledBySceneCode(ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE,
                CommonStatusEnum.ENABLE.getStatus())).thenReturn(null);

        List<Long> categoryIds = service.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE);

        assertEquals(List.of(), categoryIds);
    }

    @Test
    void getEnabledCategoryIdsBySceneCode_disabledOfflineActivityScene_returnsEmptyList() {
        when(productDisplayConfigMapper.selectEnabledBySceneCode(ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE,
                CommonStatusEnum.ENABLE.getStatus())).thenReturn(null);

        List<Long> categoryIds = service.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE);

        assertEquals(List.of(), categoryIds);
    }

    // ========== getDisplayConfigList 测试 ==========

    @Test
    void getDisplayConfigList_unconfigured_returnsAllScenesAsUnconfigured() {
        when(productDisplayConfigMapper.selectListBySceneCodes(anyCollection())).thenReturn(List.of());

        List<ProductDisplayConfigListRespVO> list = service.getDisplayConfigList();

        assertEquals(4, list.size());
        assertEquals("UNCONFIGURED", list.get(0).getConfigState());
        assertEquals(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), list.get(0).getSceneCode());
    }

    @Test
    void getDisplayConfigList_active_returnsCategoryNameAndState() {
        LocalDateTime updateTime = LocalDateTime.of(2026, 7, 10, 12, 0, 0);
        ProductDisplayConfigDO config = ProductDisplayConfigDO.builder()
                .sceneCode(ProductDisplaySceneEnum.MEMBER_PAGE.getCode())
                .categoryIds("90")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .sort(0)
                .remark("member")
                .build();
        config.setUpdateTime(updateTime);
        when(productDisplayConfigMapper.selectListBySceneCodes(anyCollection())).thenReturn(List.of(
                config));
        when(productCategoryMapper.selectByIds(anyCollection())).thenReturn(List.of(
                ProductCategoryDO.builder().id(90L).name("会员产品").parentId(89L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));

        List<ProductDisplayConfigListRespVO> list = service.getDisplayConfigList();

        ProductDisplayConfigListRespVO member = list.stream()
                .filter(item -> ProductDisplaySceneEnum.MEMBER_PAGE.getCode().equals(item.getSceneCode()))
                .findFirst().orElseThrow();
        assertEquals("ACTIVE", member.getConfigState());
        assertEquals(90L, member.getCategoryId());
        assertEquals("会员产品", member.getCategoryName());
        assertEquals(updateTime, member.getUpdateTime());
    }

    @Test
    void getDisplayConfigList_disabled_returnsDisabledState() {
        when(productDisplayConfigMapper.selectListBySceneCodes(anyCollection())).thenReturn(List.of(
                ProductDisplayConfigDO.builder()
                        .sceneCode(ProductDisplaySceneEnum.MEMBER_PAGE.getCode())
                        .categoryIds("90")
                        .status(CommonStatusEnum.DISABLE.getStatus())
                        .sort(0)
                        .build()));
        when(productCategoryMapper.selectByIds(anyCollection())).thenReturn(List.of(
                ProductCategoryDO.builder().id(90L).name("会员产品").parentId(89L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));

        List<ProductDisplayConfigListRespVO> list = service.getDisplayConfigList();

        ProductDisplayConfigListRespVO member = list.stream()
                .filter(item -> ProductDisplaySceneEnum.MEMBER_PAGE.getCode().equals(item.getSceneCode()))
                .findFirst().orElseThrow();
        assertEquals("DISABLED", member.getConfigState());
    }

    @Test
    void getDisplayConfigList_invalidCategory_returnsInvalidCategoryState() {
        when(productDisplayConfigMapper.selectListBySceneCodes(anyCollection())).thenReturn(List.of(
                ProductDisplayConfigDO.builder()
                        .sceneCode(ProductDisplaySceneEnum.MEMBER_PAGE.getCode())
                        .categoryIds("999")
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .sort(0)
                        .build()));
        when(productCategoryMapper.selectByIds(anyCollection())).thenReturn(List.of());

        List<ProductDisplayConfigListRespVO> list = service.getDisplayConfigList();

        ProductDisplayConfigListRespVO member = list.stream()
                .filter(item -> ProductDisplaySceneEnum.MEMBER_PAGE.getCode().equals(item.getSceneCode()))
                .findFirst().orElseThrow();
        assertEquals("INVALID_CATEGORY", member.getConfigState());
        assertEquals(999L, member.getCategoryId());
    }

    @Test
    void getDisplayConfigList_multipleCategories_returnsMultipleState() {
        when(productDisplayConfigMapper.selectListBySceneCodes(anyCollection())).thenReturn(List.of(
                ProductDisplayConfigDO.builder()
                        .sceneCode(ProductDisplaySceneEnum.MEMBER_PAGE.getCode())
                        .categoryIds("90,91")
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .sort(0)
                        .build()));
        when(productCategoryMapper.selectByIds(anyCollection())).thenReturn(List.of(
                ProductCategoryDO.builder().id(90L).name("会员产品").parentId(89L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));

        List<ProductDisplayConfigListRespVO> list = service.getDisplayConfigList();

        ProductDisplayConfigListRespVO member = list.stream()
                .filter(item -> ProductDisplaySceneEnum.MEMBER_PAGE.getCode().equals(item.getSceneCode()))
                .findFirst().orElseThrow();
        assertEquals("MULTIPLE_CATEGORIES", member.getConfigState());
        assertEquals(List.of(90L, 91L), member.getStoredCategoryIds());
    }

    @Test
    void getDisplayConfigList_lowercaseMatchmaker_returnsLowercaseSceneCode() {
        LocalDateTime updateTime = LocalDateTime.of(2026, 7, 10, 12, 0, 0);
        when(productDisplayConfigMapper.selectListBySceneCodes(anyCollection())).thenReturn(List.of(
                ProductDisplayConfigDO.builder()
                        .sceneCode(ProductDisplaySceneEnum.MATCHMAKER_SERVICE.getCode())
                        .categoryIds("104")
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .sort(30)
                        .build()));
        when(productCategoryMapper.selectByIds(anyCollection())).thenReturn(List.of(
                ProductCategoryDO.builder().id(104L).name("红娘服务").parentId(89L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));

        List<ProductDisplayConfigListRespVO> list = service.getDisplayConfigList();

        ProductDisplayConfigListRespVO matchmaker = list.stream()
                .filter(item -> ProductDisplaySceneEnum.MATCHMAKER_SERVICE.getCode().equals(item.getSceneCode()))
                .findFirst().orElseThrow();
        assertEquals(ProductDisplaySceneEnum.MATCHMAKER_SERVICE.getCode(), matchmaker.getSceneCode());
        assertEquals("ACTIVE", matchmaker.getConfigState());
        assertEquals(104L, matchmaker.getCategoryId());
    }

    // ========== updateDisplayConfig 测试 ==========

    @Test
    void updateDisplayConfig_create_success() {
        when(productDisplayConfigMapper.selectListBySceneCodes(eq(Collections.singleton(
                ProductDisplaySceneEnum.MEMBER_PAGE.getCode())))).thenReturn(List.of());
        mockSalesCategory(90L, "会员产品", 89L);

        ProductDisplayConfigUpdateReqVO reqVO = buildUpdateReqVO(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), 90L);

        ProductDisplayConfigListRespVO result = service.updateDisplayConfig(reqVO);

        assertEquals("ACTIVE", result.getConfigState());
        assertEquals(90L, result.getCategoryId());
        verify(productDisplayConfigMapper).insert(any(ProductDisplayConfigDO.class));
    }

    @Test
    void updateDisplayConfig_update_success() {
        LocalDateTime updateTime = LocalDateTime.of(2026, 7, 10, 12, 0, 0);
        ProductDisplayConfigDO config = ProductDisplayConfigDO.builder()
                .id(1L)
                .sceneCode(ProductDisplaySceneEnum.MEMBER_PAGE.getCode())
                .categoryIds("90")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .sort(0)
                .build();
        config.setUpdateTime(updateTime);
        when(productDisplayConfigMapper.selectListBySceneCodes(eq(Collections.singleton(
                ProductDisplaySceneEnum.MEMBER_PAGE.getCode())))).thenReturn(List.of(config));
        mockSalesCategory(91L, "新会员产品", 89L);

        ProductDisplayConfigUpdateReqVO reqVO = buildUpdateReqVO(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), 91L);
        reqVO.setUpdateTime(updateTime);

        ProductDisplayConfigListRespVO result = service.updateDisplayConfig(reqVO);

        assertEquals("ACTIVE", result.getConfigState());
        assertEquals(91L, result.getCategoryId());
        verify(productDisplayConfigMapper).updateById(any(ProductDisplayConfigDO.class));
    }

    @Test
    void updateDisplayConfig_unsupportedScene_throwsException() {
        ProductDisplayConfigUpdateReqVO reqVO = buildUpdateReqVO("UNKNOWN_SCENE", 90L);

        assertThrows(ServiceException.class, () -> service.updateDisplayConfig(reqVO));
    }

    @Test
    void updateDisplayConfig_disabledCategory_throwsException() {
        doThrow(new RuntimeException("disabled")).when(productCategoryService)
                .validateCategoryInDimension(eq(90L), anyLong());

        ProductDisplayConfigUpdateReqVO reqVO = buildUpdateReqVO(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), 90L);

        assertThrows(ServiceException.class, () -> service.updateDisplayConfig(reqVO));
    }

    @Test
    void updateDisplayConfig_missingCategory_throwsException() {
        doThrow(new RuntimeException("missing")).when(productCategoryService)
                .validateCategoryInDimension(eq(90L), anyLong());

        ProductDisplayConfigUpdateReqVO reqVO = buildUpdateReqVO(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), 90L);

        assertThrows(ServiceException.class, () -> service.updateDisplayConfig(reqVO));
    }

    @Test
    void updateDisplayConfig_nonSalesCategory_throwsException() {
        doThrow(new RuntimeException("warehouse")).when(productCategoryService)
                .validateCategoryInDimension(eq(50L), anyLong());

        ProductDisplayConfigUpdateReqVO reqVO = buildUpdateReqVO(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), 50L);

        assertThrows(ServiceException.class, () -> service.updateDisplayConfig(reqVO));
    }

    @Test
    void updateDisplayConfig_virtualSalesRoot_throwsException() {
        doThrow(new RuntimeException("virtual root")).when(productCategoryService)
                .validateCategoryInDimension(eq(1L), anyLong());

        assertThrows(ServiceException.class, () -> service.updateDisplayConfig(
                buildUpdateReqVO(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), 1L)));
    }

    @Test
    void updateDisplayConfig_staleUpdate_throwsException() {
        LocalDateTime updateTime = LocalDateTime.of(2026, 7, 10, 12, 0, 0);
        when(productDisplayConfigMapper.selectListBySceneCodes(eq(Collections.singleton(
                ProductDisplaySceneEnum.MEMBER_PAGE.getCode())))).thenReturn(List.of(
                ProductDisplayConfigDO.builder()
                        .id(1L)
                        .sceneCode(ProductDisplaySceneEnum.MEMBER_PAGE.getCode())
                        .categoryIds("90")
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .sort(0)
                        .build()));
        mockSalesCategory(91L, "新会员产品", 89L);

        ProductDisplayConfigUpdateReqVO reqVO = buildUpdateReqVO(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), 91L);
        reqVO.setUpdateTime(updateTime);

        assertThrows(ServiceException.class, () -> service.updateDisplayConfig(reqVO));
    }

    @Test
    void updateDisplayConfig_concurrentCreate_throwsException() {
        when(productDisplayConfigMapper.selectListBySceneCodes(eq(Collections.singleton(
                ProductDisplaySceneEnum.MEMBER_PAGE.getCode())))).thenReturn(List.of());
        mockSalesCategory(90L, "会员产品", 89L);
        doThrow(new org.apache.ibatis.exceptions.PersistenceException("Duplicate entry"))
                .when(productDisplayConfigMapper).insert(any(ProductDisplayConfigDO.class));

        ProductDisplayConfigUpdateReqVO reqVO = buildUpdateReqVO(ProductDisplaySceneEnum.MEMBER_PAGE.getCode(), 90L);

        assertThrows(ServiceException.class, () -> service.updateDisplayConfig(reqVO));
    }

    private void mockSalesCategory(Long id, String name, Long parentId) {
        doNothing().when(productCategoryService).validateCategoryInDimension(eq(id), anyLong());
        when(productCategoryMapper.selectById(id)).thenReturn(
                ProductCategoryDO.builder().id(id).name(name).parentId(parentId)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
    }

    private ProductDisplayConfigUpdateReqVO buildUpdateReqVO(String sceneCode, Long categoryId) {
        ProductDisplayConfigUpdateReqVO reqVO = new ProductDisplayConfigUpdateReqVO();
        reqVO.setSceneCode(sceneCode);
        reqVO.setCategoryId(categoryId);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setSort(0);
        return reqVO;
    }

}
