package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerMemberConfigRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberGrantReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerMemberDO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMemberMapper;
import vip.appap.suxin.module.partner.enums.ErrorCodeConstants;
import vip.appap.suxin.module.partner.enums.MemberSourceTypeEnum;
import vip.appap.suxin.module.partner.enums.MemberTypeEnum;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.product.service.ProductDisplayConfigService;
import vip.appap.suxin.module.product.service.ProductSkuService;
import vip.appap.suxin.module.product.service.ProductSpuService;
import vip.appap.suxin.module.product.service.ProductUnitService;
import vip.appap.suxin.module.sales.service.PartnerMemberServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PartnerMemberServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PartnerMemberServiceImpl service;

    @Mock
    private PartnerMapper partnerMapper;
    @Mock
    private PartnerMemberMapper partnerMemberMapper;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private ProductSpuService productSpuService;
    @Mock
    private ProductUnitService productUnitService;
    @Mock
    private ProductDisplayConfigService productDisplayConfigService;

    @Test
    void activateMemberByPaidOrderItems_firstPurchase_insertsThirtyDayRecord() {
        mockMembershipOrder(new BigDecimal("30"), 1);
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).isMember(false).build());

        LocalDateTime before = LocalDateTime.now();
        service.activateMemberByPaidOrderItems(1L, List.of(orderItem()));
        LocalDateTime after = LocalDateTime.now();

        PartnerMemberDO member = captureInsertedMember();
        assertEquals(new BigDecimal("30"), member.getDurationQuantity());
        assertEquals(102L, member.getDurationUnitId());
        assertTrue(member.getStartTime().isAfter(before.minusSeconds(1)));
        assertTrue(member.getEndTime().isAfter(before.plusDays(29)));
        assertTrue(member.getEndTime().isBefore(after.plusDays(31)));
        assertEquals(22L, member.getOrderId());
        assertEquals(11L, member.getOrderItemId());
        assertEquals(200L, member.getSpuId());
        assertEquals(100L, member.getSkuId());
    }

    @Test
    void activateMemberByPaidOrderItems_multiQuantity_multipliesSkuQuantityByCount() {
        mockMembershipOrder(new BigDecimal("30"), 2);
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).build());

        service.activateMemberByPaidOrderItems(1L, List.of(orderItem(2)));

        PartnerMemberDO member = captureInsertedMember();
        assertEquals(new BigDecimal("60"), member.getDurationQuantity());
    }

    @Test
    void activateMemberByPaidOrderItems_activeMember_stacksFromLatestEndTime() {
        mockMembershipOrder(new BigDecimal("30"), 1);
        LocalDateTime currentExpireTime = LocalDateTime.now().plusDays(10);
        PartnerMemberDO latest = new PartnerMemberDO();
        latest.setEndTime(currentExpireTime);
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).build());
        when(partnerMemberMapper.selectLatestActive(1L, MemberTypeEnum.MARRIAGE_ADVANCED.getType())).thenReturn(latest);

        service.activateMemberByPaidOrderItems(1L, List.of(orderItem()));

        PartnerMemberDO member = captureInsertedMember();
        assertEquals(currentExpireTime, member.getStartTime());
        assertEquals(currentExpireTime.plusDays(30), member.getEndTime());
    }

    @Test
    void activateMemberByPaidOrderItems_expiredRepurchase_startsFromNow() {
        mockMembershipOrder(new BigDecimal("30"), 1);
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).build());
        when(partnerMemberMapper.selectLatestActive(1L, MemberTypeEnum.MARRIAGE_ADVANCED.getType())).thenReturn(null);

        LocalDateTime before = LocalDateTime.now();
        service.activateMemberByPaidOrderItems(1L, List.of(orderItem()));

        PartnerMemberDO member = captureInsertedMember();
        assertTrue(member.getStartTime().isAfter(before.minusSeconds(1)));
    }

    @Test
    void activateMemberByPaidOrderItems_duplicateCallback_doesNotInsertAgain() {
        mockMembershipOrder(new BigDecimal("30"), 1);
        PartnerMemberDO exists = new PartnerMemberDO();
        exists.setId(99L);
        when(partnerMemberMapper.selectByOrderItemId(11L, MemberTypeEnum.MARRIAGE_ADVANCED.getType())).thenReturn(exists);

        service.activateMemberByPaidOrderItems(1L, List.of(orderItem()));

        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
        verify(partnerMapper, never()).updateById(any(PartnerDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_propertiesAreIgnoredForFulfillmentDuration() {
        SalesOrderItemDO orderItem = orderItem();
        SalesOrderItemDO.Property property = new SalesOrderItemDO.Property();
        property.setPropertyName("会员时长");
        property.setValueName("999个月");
        orderItem.setProperties(List.of(property));
        mockMembershipOrder(new BigDecimal("30"), 1);
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).build());

        service.activateMemberByPaidOrderItems(1L, List.of(orderItem));

        assertEquals(new BigDecimal("30"), captureInsertedMember().getDurationQuantity());
    }

    @Test
    void activateMemberByPaidOrderItems_nonMemberSpu_doesNothing() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).categorySales(999L).type(ProductTypeEnum.ENTITY.getValue()).build());

        service.activateMemberByPaidOrderItems(1L, List.of(orderItem()));

        verify(productSkuService, never()).getSku(any(), any(Boolean.class));
        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_memberTitleOnPersistedEntitySpu_doesNothing() {
        SalesOrderItemDO item = orderItem();
        item.setSpuName("婚恋高级会员套餐");
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).name("婚恋高级会员套餐").type(ProductTypeEnum.ENTITY.getValue()).build());

        service.activateMemberByPaidOrderItems(1L, List.of(item));

        verify(productSkuService, never()).getSku(any(), any(Boolean.class));
        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_mixedOrder_onlyPersistsMemberSpuItem() {
        SalesOrderItemDO memberItem = orderItem();
        SalesOrderItemDO serviceItem = new SalesOrderItemDO();
        serviceItem.setId(12L);
        serviceItem.setOrderId(22L);
        serviceItem.setSpuId(201L);
        serviceItem.setSkuId(101L);
        serviceItem.setCount(1);
        mockMembershipOrder(new BigDecimal("30"), 1);
        when(productSpuService.getSpu(201L, true)).thenReturn(ProductSpuDO.builder()
                .id(201L).type(ProductTypeEnum.SERVICE.getValue()).build());
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).build());

        service.activateMemberByPaidOrderItems(1L, List.of(memberItem, serviceItem));

        PartnerMemberDO member = captureInsertedMember();
        assertEquals(11L, member.getOrderItemId());
        verify(productSkuService, never()).getSku(101L, true);
    }

    @Test
    void activateMemberByPaidOrderItems_missingDeletedOrCrossTenantUnit_rejectsBeforeInsert() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).type(ProductTypeEnum.MEMBER.getValue()).unitId(115L).build());
        when(productSkuService.getSku(100L, true)).thenReturn(ProductSkuDO.builder()
                .id(100L).spuId(200L).quantity(BigDecimal.ONE).build());
        when(productUnitService.getUnit(115L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.activateMemberByPaidOrderItems(1L, List.of(orderItem())));

        assertEquals(ErrorCodeConstants.MEMBER_GRANT_DURATION_INVALID.getCode(), exception.getCode());
        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
        verify(partnerMapper, never()).updateById(any(PartnerDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_disabledTimeUnit_rejectsBeforeInsert() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).type(ProductTypeEnum.MEMBER.getValue()).unitId(102L).build());
        when(productSkuService.getSku(100L, true)).thenReturn(ProductSkuDO.builder()
                .id(100L).spuId(200L).quantity(BigDecimal.ONE).build());
        when(productUnitService.getUnit(102L)).thenReturn(ProductUnitDO.builder()
                .id(102L).name("天").status(CommonStatusEnum.DISABLE.getStatus()).build());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.activateMemberByPaidOrderItems(1L, List.of(orderItem())));

        assertEquals(ErrorCodeConstants.MEMBER_GRANT_DURATION_INVALID.getCode(), exception.getCode());
        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_nonTimeUnit_rejectsBeforeInsert() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).type(ProductTypeEnum.MEMBER.getValue()).unitId(104L).build());
        when(productSkuService.getSku(100L, true)).thenReturn(ProductSkuDO.builder()
                .id(100L).spuId(200L).quantity(BigDecimal.ONE).build());
        when(productUnitService.getUnit(104L)).thenReturn(ProductUnitDO.builder()
                .id(104L).name("千克").status(CommonStatusEnum.ENABLE.getStatus())
                .type(2).relativeFactor("1000").build());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.activateMemberByPaidOrderItems(1L, List.of(orderItem())));

        assertEquals(ErrorCodeConstants.MEMBER_GRANT_DURATION_INVALID.getCode(), exception.getCode());
        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_fractionalTimeUnit_convertsToWholeSeconds() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).type(ProductTypeEnum.MEMBER.getValue()).unitId(103L).build());
        when(productSkuService.getSku(100L, true)).thenReturn(ProductSkuDO.builder()
                .id(100L).spuId(200L).quantity(new BigDecimal("0.5")).build());
        when(productUnitService.getUnit(103L)).thenReturn(ProductUnitDO.builder()
                .id(103L).name("小时").status(CommonStatusEnum.ENABLE.getStatus())
                .type(5).relativeFactor("3600").build());
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).build());

        service.activateMemberByPaidOrderItems(1L, List.of(orderItem()));

        PartnerMemberDO member = captureInsertedMember();
        assertEquals(new BigDecimal("0.5"), member.getDurationQuantity());
        assertEquals(member.getStartTime().plusSeconds(1800), member.getEndTime());
    }

    @Test
    void activateMemberByPaidOrderItems_fractionalSecond_rejectsBeforeInsert() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).type(ProductTypeEnum.MEMBER.getValue()).unitId(106L).build());
        when(productSkuService.getSku(100L, true)).thenReturn(ProductSkuDO.builder()
                .id(100L).spuId(200L).quantity(new BigDecimal("0.5")).build());
        mockSecondBaseUnit();

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.activateMemberByPaidOrderItems(1L, List.of(orderItem())));

        assertEquals(ErrorCodeConstants.MEMBER_GRANT_DURATION_INVALID.getCode(), exception.getCode());
        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_secondBaseUnit_usesFactorOne() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).type(ProductTypeEnum.MEMBER.getValue()).unitId(106L).build());
        when(productSkuService.getSku(100L, true)).thenReturn(ProductSkuDO.builder()
                .id(100L).spuId(200L).quantity(new BigDecimal("30")).build());
        mockSecondBaseUnit();
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).build());

        service.activateMemberByPaidOrderItems(1L, List.of(orderItem()));

        PartnerMemberDO member = captureInsertedMember();
        assertEquals(106L, member.getDurationUnitId());
        assertEquals(member.getStartTime().plusSeconds(30), member.getEndTime());
    }

    @Test
    void activateMemberByPaidOrderItems_invalidTimeFactor_rejectsBeforeInsert() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).type(ProductTypeEnum.MEMBER.getValue()).unitId(117L).build());
        when(productSkuService.getSku(100L, true)).thenReturn(ProductSkuDO.builder()
                .id(100L).spuId(200L).quantity(BigDecimal.ONE).build());
        when(productUnitService.getUnit(117L)).thenReturn(ProductUnitDO.builder()
                .id(117L).name("年").status(CommonStatusEnum.ENABLE.getStatus())
                .type(5).relativeFactor("invalid").build());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.activateMemberByPaidOrderItems(1L, List.of(orderItem())));

        assertEquals(ErrorCodeConstants.MEMBER_GRANT_DURATION_INVALID.getCode(), exception.getCode());
        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_invalidSecondItem_propagatesForOuterTransactionRollback() {
        SalesOrderItemDO validItem = orderItem();
        SalesOrderItemDO invalidItem = new SalesOrderItemDO();
        invalidItem.setId(12L);
        invalidItem.setOrderId(22L);
        invalidItem.setSpuId(201L);
        invalidItem.setSkuId(101L);
        invalidItem.setCount(1);
        mockMembershipOrder(new BigDecimal("30"), 1);
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).build());
        when(productSpuService.getSpu(201L, true)).thenReturn(ProductSpuDO.builder()
                .id(201L).type(ProductTypeEnum.MEMBER.getValue()).unitId(102L).build());
        when(productSkuService.getSku(101L, true)).thenReturn(ProductSkuDO.builder()
                .id(101L).spuId(201L).quantity(BigDecimal.ZERO).build());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.activateMemberByPaidOrderItems(1L, List.of(validItem, invalidItem)));

        assertEquals(ErrorCodeConstants.MEMBER_GRANT_DURATION_INVALID.getCode(), exception.getCode());
        verify(partnerMemberMapper).insert(any(PartnerMemberDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_displayConfigCategory_doesNotTriggerMembership() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).categorySales(999L).type(ProductTypeEnum.ENTITY.getValue()).build());

        service.activateMemberByPaidOrderItems(1L, List.of(orderItem()));

        verify(productDisplayConfigService, never()).getEnabledCategoryIdsBySceneCode(any());
        verify(productSkuService, never()).getSku(any(), any(Boolean.class));
        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
    }

    @Test
    void activateMemberByPaidOrderItems_configuredParentCategoryChildButServiceType_doesNothing() {
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).categorySales(90L).type(ProductTypeEnum.SERVICE.getValue()).unitId(102L).build());

        service.activateMemberByPaidOrderItems(1L, List.of(orderItem()));

        verify(productSkuService, never()).getSku(any(), any(Boolean.class));
        verify(partnerMemberMapper, never()).insert(any(PartnerMemberDO.class));
    }

    @Test
    void revokeMemberByRefundedOrderItem_subtractsConvertedDurationSeconds() {
        mockMembershipOrder(new BigDecimal("30"), 1);
        LocalDateTime currentExpireTime = LocalDateTime.now().plusDays(60);
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder()
                .id(1L).isMember(true).memberExpireTime(currentExpireTime).build());

        service.revokeMemberByRefundedOrderItem(1L, orderItem());

        PartnerDO updateObj = capturePartnerUpdate();
        assertTrue(updateObj.getIsMember());
        assertEquals(currentExpireTime.minusDays(30), updateObj.getMemberExpireTime());
    }

    @Test
    void revokeMemberByRefundedOrderItem_expiredAfterSubtract_clearsMembership() {
        mockMembershipOrder(new BigDecimal("30"), 1);
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder()
                .id(1L).isMember(true).memberExpireTime(LocalDateTime.now().plusDays(10)).build());

        service.revokeMemberByRefundedOrderItem(1L, orderItem());

        PartnerDO updateObj = capturePartnerUpdate();
        assertFalse(updateObj.getIsMember());
        assertNull(updateObj.getMemberExpireTime());
    }

    @Test
    void grantMember_insertsStructuredTimeDurationRecord() {
        mockDayUnit();
        when(partnerMapper.selectByIdForUpdate(1L)).thenReturn(PartnerDO.builder().id(1L).isMember(false).build());
        PartnerMemberGrantReqVO reqVO = new PartnerMemberGrantReqVO();
        reqVO.setUserId(1L);
        reqVO.setMemberType(MemberTypeEnum.MARRIAGE_ADVANCED.getType());
        reqVO.setDurationQuantity(new BigDecimal("365"));
        reqVO.setDurationUnitId(102L);
        reqVO.setRemark("运营赠送");

        service.grantMember(reqVO);

        PartnerMemberDO member = captureInsertedMember();
        assertEquals(MemberSourceTypeEnum.ADMIN_GRANT.getType(), member.getSourceType());
        assertEquals(new BigDecimal("365"), member.getDurationQuantity());
        assertEquals(102L, member.getDurationUnitId());
    }

    @Test
    void getMemberConfig_usesDisplayConfigCategory() {
        when(productDisplayConfigService.getEnabledCategoryIdsBySceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE))
                .thenReturn(List.of(10L, 11L));

        AppPartnerMemberConfigRespVO respVO = service.getMemberConfig();

        assertEquals(MemberTypeEnum.MARRIAGE_ADVANCED.getType(), respVO.getMemberType());
        assertEquals(10L, respVO.getMemberCategoryId());
    }

    @Test
    void getMemberConfig_whenDisplayConfigMissing_throws() {
        when(productDisplayConfigService.getEnabledCategoryIdsBySceneCode(ProductDisplayConfigService.SCENE_MEMBER_PAGE))
                .thenReturn(List.of());

        ServiceException exception = assertThrows(ServiceException.class, () -> service.getMemberConfig());

        assertEquals(ErrorCodeConstants.MEMBER_CONFIG_NOT_FOUND.getCode(), exception.getCode());
    }

    private void mockMembershipOrder(BigDecimal quantity, Integer count) {
        mockDayUnit();
        when(productSpuService.getSpu(200L, true)).thenReturn(ProductSpuDO.builder()
                .id(200L).categorySales(10L).type(ProductTypeEnum.MEMBER.getValue()).unitId(102L).build());
        when(productSkuService.getSku(100L, true)).thenReturn(ProductSkuDO.builder()
                .id(100L).spuId(200L).quantity(quantity).build());
    }

    private void mockDayUnit() {
        when(productUnitService.getUnit(102L)).thenReturn(ProductUnitDO.builder()
                .id(102L).name("天").status(CommonStatusEnum.ENABLE.getStatus())
                .type(5).relativeFactor("86400").build());
    }

    private void mockSecondBaseUnit() {
        when(productUnitService.getUnit(106L)).thenReturn(ProductUnitDO.builder()
                .id(106L).name("秒").status(CommonStatusEnum.ENABLE.getStatus())
                .type(0).build());
    }

    private SalesOrderItemDO orderItem() {
        return orderItem(1);
    }

    private SalesOrderItemDO orderItem(Integer count) {
        SalesOrderItemDO orderItem = new SalesOrderItemDO();
        orderItem.setId(11L);
        orderItem.setOrderId(22L);
        orderItem.setSpuId(200L);
        orderItem.setSkuId(100L);
        orderItem.setCount(count);
        return orderItem;
    }

    private PartnerMemberDO captureInsertedMember() {
        ArgumentCaptor<PartnerMemberDO> captor = ArgumentCaptor.forClass(PartnerMemberDO.class);
        verify(partnerMemberMapper).insert(captor.capture());
        return captor.getValue();
    }

    private PartnerDO capturePartnerUpdate() {
        ArgumentCaptor<PartnerDO> captor = ArgumentCaptor.forClass(PartnerDO.class);
        verify(partnerMapper).updateById(captor.capture());
        return captor.getValue();
    }

}
