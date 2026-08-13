package vip.appap.suxin.module.product.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSkuSaveReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSpuSaveReqVO;
import vip.appap.suxin.module.product.controller.app.vo.AppProductSpuPageReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.dal.mysql.ProductSpuMapper;
import vip.appap.suxin.module.product.enums.ErrorCodeConstants;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.product.service.ProductBrandService;
import vip.appap.suxin.module.product.service.ProductCategoryService;
import vip.appap.suxin.module.product.service.ProductDeliveryTemplateValidator;
import vip.appap.suxin.module.product.service.ProductSkuService;
import vip.appap.suxin.module.product.service.ProductUnitService;
import vip.appap.suxin.module.sales.enums.SalesDeliveryTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static vip.appap.suxin.module.product.enums.ProductCategoryDimensionConstants.SALES_ROOT_ID;
import static vip.appap.suxin.module.product.enums.ProductCategoryDimensionConstants.WAREHOUSE_ROOT_ID;

class ProductSpuServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ProductSpuServiceImpl service;

    @Mock
    private ProductSpuMapper productSpuMapper;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private ProductBrandService brandService;
    @Mock
    private ProductCategoryService categoryService;
    @Mock
    private ProductUnitService productUnitService;
    @Mock
    private ProductDeliveryTemplateValidator productDeliveryTemplateValidator;

    @BeforeEach
    void initTableInfo() {
        // selectPage_* 测试通过 CALLS_REAL_METHODS 直接执行 Mapper 默认方法，
        // 其 LambdaQueryWrapperX 的 lambda 列解析需要 MyBatis-Plus TableInfo 缓存。
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                ProductSpuDO.class);
    }

    @Test
    void createSpu_memberProductWithoutUnit_saves() {
        mockBaseValidation();
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.MEMBER.getValue(), null, new BigDecimal("30"));

        assertDoesNotThrow(() -> service.createSpu(reqVO));

        verify(productSpuMapper).insert(any(ProductSpuDO.class));
        verify(productSkuService).createSkuList(any(), eq(reqVO.getSkus()));
    }

    @Test
    void createSpu_memberProductWithoutQuantity_rejectsSave() {
        mockBaseValidation();
        when(productUnitService.getUnit(102L)).thenReturn(dayUnit());
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.MEMBER.getValue(), 102L, null);

        assertThrows(RuntimeException.class, () -> service.createSpu(reqVO));
    }

    @Test
    void createSpu_memberProductWithNonPositiveQuantity_rejectsSave() {
        mockBaseValidation();
        when(productUnitService.getUnit(102L)).thenReturn(dayUnit());
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.MEMBER.getValue(), 102L, BigDecimal.ZERO);

        assertThrows(RuntimeException.class, () -> service.createSpu(reqVO));
    }

    @Test
    void createSpu_memberProductWithUnitAndPositiveQuantity_saves() {
        mockBaseValidation();
        when(productUnitService.getUnit(102L)).thenReturn(dayUnit());
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.MEMBER.getValue(), 102L, new BigDecimal("30"));

        assertDoesNotThrow(() -> service.createSpu(reqVO));

        verify(productSpuMapper).insert(any(ProductSpuDO.class));
        verify(productSkuService).createSkuList(any(), eq(reqVO.getSkus()));
    }

    @Test
    void createSpu_onlineDeliveryWithoutTemplate_saves() {
        mockBaseValidation();
        when(productUnitService.getUnit(102L)).thenReturn(dayUnit());
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.SERVICE.getValue(), 102L, new BigDecimal("30"));
        reqVO.setDeliveryTypes(List.of(SalesDeliveryTypeEnum.ONLINE.getType()));
        reqVO.setDeliveryTemplateId(null);

        assertDoesNotThrow(() -> service.createSpu(reqVO));

        verify(productSpuMapper).insert(any(ProductSpuDO.class));
        verify(productSkuService).createSkuList(any(), eq(reqVO.getSkus()));
    }

    @Test
    void createSpu_memberProductWithOnlineDelivery_saves() {
        // 配送方式不再根据商品类型过滤，会员商品也可选择线上发货
        mockBaseValidation();
        when(productUnitService.getUnit(102L)).thenReturn(dayUnit());
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.MEMBER.getValue(), 102L, new BigDecimal("30"));
        reqVO.setDeliveryTypes(List.of(SalesDeliveryTypeEnum.ONLINE.getType()));

        assertDoesNotThrow(() -> service.createSpu(reqVO));

        verify(productSpuMapper).insert(any(ProductSpuDO.class));
        verify(productSkuService).createSkuList(any(), eq(reqVO.getSkus()));
    }

    @Test
    void createSpu_memberProductWithAutoDelivery_saves() {
        mockBaseValidation();
        when(productUnitService.getUnit(102L)).thenReturn(dayUnit());
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.MEMBER.getValue(), 102L, new BigDecimal("30"));
        reqVO.setDeliveryTypes(List.of(SalesDeliveryTypeEnum.AUTO.getType()));

        assertDoesNotThrow(() -> service.createSpu(reqVO));

        verify(productSpuMapper).insert(any(ProductSpuDO.class));
        verify(productSkuService).createSkuList(any(), eq(reqVO.getSkus()));
    }

    @Test
    void createSpu_entityProductWithOnlineDelivery_saves() {
        // 配送方式不再根据商品类型过滤，实体商品也可选择线上发货
        mockBaseValidation();
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.ENTITY.getValue(), null, null);
        reqVO.setDeliveryTypes(List.of(SalesDeliveryTypeEnum.ONLINE.getType()));

        assertDoesNotThrow(() -> service.createSpu(reqVO));

        verify(productSpuMapper).insert(any(ProductSpuDO.class));
        verify(productSkuService).createSkuList(any(), eq(reqVO.getSkus()));
    }

    @Test
    void createSpu_wechatMiniappVirtualWithAutoDelivery_saves() {
        mockBaseValidation();
        when(productUnitService.getUnit(102L)).thenReturn(dayUnit());
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.SERVICE.getValue(), 102L, new BigDecimal("30"));
        reqVO.setIsWechatMiniappVirtualGoods(true);
        reqVO.setDeliveryTypes(List.of(SalesDeliveryTypeEnum.AUTO.getType()));

        assertDoesNotThrow(() -> service.createSpu(reqVO));

        verify(productSpuMapper).insert(any(ProductSpuDO.class));
        verify(productSkuService).createSkuList(any(), eq(reqVO.getSkus()));
    }

    @Test
    void createSpu_validatesSalesAndWarehouseDimensions() {
        mockBaseValidation();
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.ENTITY.getValue(), null, null);
        reqVO.setCategoryStore(20L);

        assertDoesNotThrow(() -> service.createSpu(reqVO));

        verify(categoryService).validateCategoryInDimension(10L, SALES_ROOT_ID);
        verify(categoryService).validateCategoryInDimension(20L, WAREHOUSE_ROOT_ID);
    }

    @Test
    void createSpu_virtualOrWarehouseSalesCategory_rejectsSave() {
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.ENTITY.getValue(), null, null);
        doThrow(new RuntimeException("invalid dimension")).when(categoryService)
                .validateCategoryInDimension(10L, SALES_ROOT_ID);

        assertThrows(RuntimeException.class, () -> service.createSpu(reqVO));
    }

    @Test
    void createSpu_salesCategoryBelowMinimumVisibleDepth_rejectsSave() {
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.ENTITY.getValue(), null, null);
        when(categoryService.getCategoryLevel(10L)).thenReturn(2);

        assertThrows(RuntimeException.class, () -> service.createSpu(reqVO));
    }

    @Test
    void createSpu_invalidDeliveryType_rejectsSave() {
        mockBaseValidation();
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.ENTITY.getValue(), null, null);
        reqVO.setDeliveryTypes(List.of(99));

        assertThrows(RuntimeException.class, () -> service.createSpu(reqVO));
    }

    @Test
    void createSpu_expressDeliveryWithoutTemplate_rejectsSave() {
        mockBaseValidation();
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.ENTITY.getValue(), null, null);
        reqVO.setDeliveryTypes(List.of(SalesDeliveryTypeEnum.EXPRESS.getType()));
        reqVO.setDeliveryTemplateId(null);

        assertThrows(RuntimeException.class, () -> service.createSpu(reqVO));
    }

    @Test
    void createSpu_expressDeliveryWithTemplate_saves() {
        mockBaseValidation();
        ProductSpuSaveReqVO reqVO = spu(ProductTypeEnum.ENTITY.getValue(), null, null);
        reqVO.setDeliveryTypes(List.of(SalesDeliveryTypeEnum.EXPRESS.getType()));
        reqVO.setDeliveryTemplateId(1L);

        assertDoesNotThrow(() -> service.createSpu(reqVO));

        verify(productSpuMapper).insert(any(ProductSpuDO.class));
        verify(productSkuService).createSkuList(any(), eq(reqVO.getSkus()));
    }

    private void mockBaseValidation() {
        when(categoryService.getCategoryLevel(10L)).thenReturn(3);
    }

    private ProductUnitDO dayUnit() {
        return ProductUnitDO.builder().id(102L).name("天").status(CommonStatusEnum.ENABLE.getStatus()).build();
    }

    private ProductSpuSaveReqVO spu(Integer type, Long unitId, BigDecimal quantity) {
        ProductSpuSaveReqVO reqVO = new ProductSpuSaveReqVO();
        reqVO.setName("会员商品");
        reqVO.setKeyword("会员");
        reqVO.setIntroduction("会员套餐");
        reqVO.setDescription("会员套餐");
        reqVO.setCategorySales(10L);
        reqVO.setBrandId(1L);
        reqVO.setPicUrl("https://example.com/a.png");
        reqVO.setSliderPicUrls(List.of("https://example.com/a.png"));
        reqVO.setSort(1);
        reqVO.setType(type);
        reqVO.setUnitId(unitId);
        reqVO.setIsSale(true);
        reqVO.setIsPurchase(false);
        reqVO.setIsMes(false);
        reqVO.setSpecType(false);
        reqVO.setDeliveryTypes(List.of(SalesDeliveryTypeEnum.AUTO.getType()));
        reqVO.setGiveIntegral(0);
        reqVO.setSubCommissionType(false);
        reqVO.setSkus(List.of(sku(quantity)));
        return reqVO;
    }

    private ProductSkuSaveReqVO sku(BigDecimal quantity) {
        ProductSkuSaveReqVO sku = new ProductSkuSaveReqVO();
        sku.setName("30天");
        sku.setPrice(9900);
        sku.setMarketPrice(12900);
        sku.setCostPrice(0);
        sku.setPicUrl("https://example.com/a.png");
        sku.setStock(100);
        sku.setQuantity(quantity);
        return sku;
    }

    // ========== App 商品分页（全国特产/同城特产） ==========

    @Test
    void getSpuPage_provinceTianjin_expandsToCityId120100() {
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setProvinceId(120000); // 天津
        when(productSpuMapper.selectPage(any(), any(), ArgumentMatchers.<Set<Integer>>any()))
                .thenReturn(new PageResult<>());

        service.getSpuPage(reqVO);

        // 天津 120000 的市级节点包含 120100（天津市），应展开进 city_id IN 集合
        verify(productSpuMapper).selectPage(eq(reqVO), any(),
                argThat((Set<Integer> cityIds) -> cityIds != null && cityIds.contains(120100)));
    }

    @Test
    void getSpuPage_provinceXinjiang_expandsToCityId650500() {
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setProvinceId(650000); // 新疆维吾尔自治区
        when(productSpuMapper.selectPage(any(), any(), ArgumentMatchers.<Set<Integer>>any()))
                .thenReturn(new PageResult<>());

        service.getSpuPage(reqVO);

        // 新疆 650000 的市级节点包含 650500（哈密市），应展开进 city_id IN 集合
        verify(productSpuMapper).selectPage(eq(reqVO), any(),
                argThat((Set<Integer> cityIds) -> cityIds != null && cityIds.contains(650500)));
    }

    @Test
    void getSpuPage_provinceAndCityBothSet_rejects() {
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setProvinceId(120000);
        reqVO.setCityId(120100);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.getSpuPage(reqVO));
        assertEquals(ErrorCodeConstants.SPU_PROVINCE_CITY_CONFLICT.getCode(), ex.getCode());
    }

    @Test
    void getSpuPage_provinceAndAreaAliasBothSet_rejects() {
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setProvinceId(120000);
        reqVO.setAreaId(120100);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.getSpuPage(reqVO));
        assertEquals(ErrorCodeConstants.SPU_PROVINCE_CITY_CONFLICT.getCode(), ex.getCode());
    }

    @Test
    void getSpuPage_cityOnly_keepsExactCityFilter() {
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setCityId(120100); // 同城特产：天津市
        when(productSpuMapper.selectPage(any(), any(), ArgumentMatchers.<Set<Integer>>any()))
                .thenReturn(new PageResult<>());

        service.getSpuPage(reqVO);

        // 同城特产不展开省份，provinceCityIds 应为 null
        verify(productSpuMapper).selectPage(eq(reqVO), any(),
                argThat((Set<Integer> cityIds) -> cityIds == null));
    }

    @Test
    void getSpuPage_noAreaParams_keepsNormalList() {
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        when(productSpuMapper.selectPage(any(), any(), ArgumentMatchers.<Set<Integer>>any()))
                .thenReturn(new PageResult<>());

        service.getSpuPage(reqVO);

        // 无地区参数：省份展开集合为 null、分类集合为空，保持普通商城列表行为
        verify(productSpuMapper).selectPage(eq(reqVO),
                argThat((Set<Long> categoryIds) -> categoryIds.isEmpty()),
                argThat((Set<Integer> cityIds) -> cityIds == null));
    }

    // ========== Mapper App 分页 SQL 组装（全国特产/同城特产） ==========

    @Test
    void selectPage_provinceCityIds_appendsCityIdInClause() {
        ProductSpuMapper mapper = mock(ProductSpuMapper.class, CALLS_REAL_METHODS);
        lenient().doReturn(new Page<>(1, 10)).when(mapper).selectPage(any(Page.class), any());
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();

        mapper.selectPage(reqVO, Collections.emptySet(), Set.of(120100, 650500));

        String sql = captureSelectPageSql(mapper);
        assertTrue(sql.contains("city_id IN"));
        assertFalse(sql.contains("city_id IS NOT NULL")); // specialtyOnly 未设置
        assertFalse(sql.contains("city_id =")); // 未传 cityId，不精确匹配
    }

    @Test
    void selectPage_specialtyOnly_appendsCityIdNotNull() {
        ProductSpuMapper mapper = mock(ProductSpuMapper.class, CALLS_REAL_METHODS);
        lenient().doReturn(new Page<>(1, 10)).when(mapper).selectPage(any(Page.class), any());
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setSpecialtyOnly(true);

        mapper.selectPage(reqVO, Collections.<Long>emptySet(), (Set<Integer>) null);

        String sql = captureSelectPageSql(mapper);
        // 全国特产「全部」：未绑定地区（cityId 为空）的商品不返回
        assertTrue(sql.contains("city_id IS NOT NULL"));
    }

    @Test
    void selectPage_cityId_keepsExactClause() {
        ProductSpuMapper mapper = mock(ProductSpuMapper.class, CALLS_REAL_METHODS);
        lenient().doReturn(new Page<>(1, 10)).when(mapper).selectPage(any(Page.class), any());
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setCityId(120100);

        mapper.selectPage(reqVO, Collections.<Long>emptySet(), (Set<Integer>) null);

        String sql = captureSelectPageSql(mapper);
        // 同城特产：精确筛选 city_id = 120100
        assertTrue(sql.contains("city_id ="));
        assertFalse(sql.contains("city_id IN"));
        assertFalse(sql.contains("city_id IS NOT NULL"));
    }

    @Test
    void getSpuPage_isMilitaryTrue_keepsMilitaryFilterParam() {
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setIsMilitary(true);
        when(productSpuMapper.selectPage(any(), any(), ArgumentMatchers.<Set<Integer>>any()))
                .thenReturn(new PageResult<>());

        service.getSpuPage(reqVO);

        // 军创区筛选：reqVO 透传至 Mapper，由 Mapper 组装 is_military = true 等值条件
        verify(productSpuMapper).selectPage(eq(reqVO),
                ArgumentMatchers.<Set<Long>>any(), ArgumentMatchers.<Set<Integer>>any());
    }

    @Test
    void getSpuPage_isMilitaryNotSet_keepsNormalQuery() {
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        when(productSpuMapper.selectPage(any(), any(), ArgumentMatchers.<Set<Integer>>any()))
                .thenReturn(new PageResult<>());

        service.getSpuPage(reqVO);

        // 未传 isMilitary：不因军创区改造增加过滤，保持普通商品查询行为
        verify(productSpuMapper).selectPage(eq(reqVO),
                argThat((Set<Long> categoryIds) -> categoryIds.isEmpty()),
                argThat((Set<Integer> cityIds) -> cityIds == null));
    }

    @Test
    void selectPage_isMilitaryTrue_appendsIsMilitaryClause() {
        ProductSpuMapper mapper = mock(ProductSpuMapper.class, CALLS_REAL_METHODS);
        lenient().doReturn(new Page<>(1, 10)).when(mapper).selectPage(any(Page.class), any());
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setIsMilitary(true);

        mapper.selectPage(reqVO, Collections.<Long>emptySet(), (Set<Integer>) null);

        String sql = captureSelectPageSql(mapper);
        // 军创区：等值筛选 is_military = true
        assertTrue(sql.contains("is_military ="));
    }

    @Test
    void selectPage_isMilitaryNotSet_omitsIsMilitaryClause() {
        ProductSpuMapper mapper = mock(ProductSpuMapper.class, CALLS_REAL_METHODS);
        lenient().doReturn(new Page<>(1, 10)).when(mapper).selectPage(any(Page.class), any());
        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();

        mapper.selectPage(reqVO, Collections.<Long>emptySet(), (Set<Integer>) null);

        String sql = captureSelectPageSql(mapper);
        // 未传 isMilitary：SQL 不出现军创区条件，普通商品与军创区商品均按原有条件返回
        assertFalse(sql.contains("is_military"));
    }

    @SuppressWarnings("unchecked")
    private String captureSelectPageSql(ProductSpuMapper mapper) {
        ArgumentCaptor<Wrapper<ProductSpuDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectPage(any(Page.class), captor.capture());
        return ((LambdaQueryWrapperX<ProductSpuDO>) captor.getValue()).getCustomSqlSegment();
    }

}
