package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.api.PartnerAddressApi;
import vip.appap.suxin.module.partner.api.dto.PartnerAddressRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesElectronicWaybillAccountMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesElectronicWaybillMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 电子面单账户服务测试
 *
 * 覆盖：凭据必填、名称唯一、地址可见性、更新空值保留原凭据、账户可下单校验。
 */
class SalesElectronicWaybillAccountServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesElectronicWaybillAccountServiceImpl service;

    @Mock
    private SalesElectronicWaybillAccountMapper waybillAccountMapper;
    @Mock
    private SalesElectronicWaybillMapper waybillMapper;
    @Mock
    private SalesDeliveryExpressService deliveryExpressService;
    @Mock
    private PartnerAddressApi addressApi;

    private SalesElectronicWaybillAccountDO account;

    @BeforeEach
    void setUp() {
        account = new SalesElectronicWaybillAccountDO();
        account.setId(1L).setName("顺丰月结").setExpressId(1L).setExpressCode("shunfeng")
                .setKey("key").setSecret("secret").setPartnerId("P001").setPartnerKey("pk").setPartnerSecret("ps")
                .setTempId("t1").setDefaultAddressId(10L).setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    private PartnerAddressRespDTO senderAddress() {
        PartnerAddressRespDTO address = new PartnerAddressRespDTO();
        address.setId(10L).setUserId(0L).setType(1).setName("公司").setMobile("13800000000")
                .setAreaId(4371).setDetailAddress("测试路1号");
        return address;
    }

    @Test
    void create_missingCredentials_rejects() {
        when(addressApi.getAddress(anyLong())).thenReturn(senderAddress());
        SalesDeliveryExpressDO express = new SalesDeliveryExpressDO();
        express.setId(1L).setCode("shunfeng").setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(deliveryExpressService.validateDeliveryExpress(1L)).thenReturn(express);
        when(waybillAccountMapper.selectByName("顺丰月结")).thenReturn(null);

        SalesElectronicWaybillAccountCreateReqVO reqVO = new SalesElectronicWaybillAccountCreateReqVO();
        reqVO.setName("顺丰月结").setExpressId(1L).setKey("").setSecret("").setTempId("t1")
                .setDefaultAddressId(10L).setStatus(CommonStatusEnum.ENABLE.getStatus());
        ServiceException ex = assertThrows(ServiceException.class, () -> service.createWaybillAccount(reqVO));
        assertEquals(1_011_004_209, ex.getCode()); // WAYBILL_ACCOUNT_CREDENTIAL_MISSING
    }

    @Test
    void create_senderAddressMustBeType1() {
        PartnerAddressRespDTO memberAddress = new PartnerAddressRespDTO();
        memberAddress.setId(10L).setType(0).setName("会员").setMobile("13800000000")
                .setAreaId(4371).setDetailAddress("测试路1号");
        when(addressApi.getAddress(10L)).thenReturn(memberAddress);

        SalesElectronicWaybillAccountCreateReqVO reqVO = new SalesElectronicWaybillAccountCreateReqVO();
        reqVO.setName("顺丰月结").setExpressId(1L).setKey("k").setSecret("s").setTempId("t1")
                .setDefaultAddressId(10L).setStatus(CommonStatusEnum.ENABLE.getStatus());
        ServiceException ex = assertThrows(ServiceException.class, () -> service.createWaybillAccount(reqVO));
        assertEquals(1_011_004_208, ex.getCode()); // WAYBILL_ACCOUNT_ADDRESS_NOT_VISIBLE
    }

    @Test
    void create_nameDuplicate_rejects() {
        when(waybillAccountMapper.selectByName("顺丰月结")).thenReturn(account);
        SalesElectronicWaybillAccountCreateReqVO reqVO = new SalesElectronicWaybillAccountCreateReqVO();
        reqVO.setName("顺丰月结").setExpressId(1L).setKey("k").setSecret("s").setTempId("t1")
                .setDefaultAddressId(10L).setStatus(CommonStatusEnum.ENABLE.getStatus());
        ServiceException ex = assertThrows(ServiceException.class, () -> service.createWaybillAccount(reqVO));
        assertEquals(1_011_004_201, ex.getCode()); // WAYBILL_ACCOUNT_NAME_DUPLICATE
    }

    @Test
    void update_blankCredentialsKeepsOriginal() {
        when(waybillAccountMapper.selectById(1L)).thenReturn(account);
        when(waybillAccountMapper.selectByName("顺丰月结")).thenReturn(account); // 同名自身

        SalesElectronicWaybillAccountUpdateReqVO reqVO = new SalesElectronicWaybillAccountUpdateReqVO();
        reqVO.setId(1L).setName("顺丰月结").setExpressId(1L).setKey("").setSecret("").setPartnerKey("")
                .setPartnerSecret("").setTempId("t1").setDefaultAddressId(10L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        service.updateWaybillAccount(reqVO);

        verify(waybillAccountMapper).updateById(Mockito.<SalesElectronicWaybillAccountDO>argThat(updateObj ->
                updateObj.getKey() == null && updateObj.getSecret() == null
                        && updateObj.getPartnerKey() == null && updateObj.getPartnerSecret() == null
                        && updateObj.getExpressId() != null));
    }

    @Test
    void delete_referencedByValidWaybill_rejects() {
        when(waybillAccountMapper.selectById(1L)).thenReturn(account);
        // 存在有效面单引用该账户
        when(waybillMapper.selectListByAccountId(1L))
                .thenReturn(List.of(new SalesElectronicWaybillDO().setId(99L).setOrderId(1L)));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteWaybillAccount(1L));
        assertEquals(1_011_004_210, ex.getCode()); // WAYBILL_ACCOUNT_REFERENCED
        verify(waybillAccountMapper, Mockito.never()).deleteById(anyLong());
    }

    @Test
    void delete_notReferenced_succeeds() {
        when(waybillAccountMapper.selectById(1L)).thenReturn(account);
        when(waybillMapper.selectListByAccountId(1L)).thenReturn(List.of());
        service.deleteWaybillAccount(1L);
        verify(waybillAccountMapper).deleteById(1L);
    }

    @Test
    void validateForOrder_accountDisabled_rejects() {
        account.setStatus(CommonStatusEnum.DISABLE.getStatus());
        when(waybillAccountMapper.selectById(1L)).thenReturn(account);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.validateWaybillAccountForOrder(1L, null));
        assertEquals(1_011_004_202, ex.getCode()); // WAYBILL_ACCOUNT_STATUS_NOT_ENABLE
    }

    @Test
    void validateForOrder_expressMismatch_rejects() {
        when(waybillAccountMapper.selectById(1L)).thenReturn(account);
        SalesDeliveryExpressDO express = new SalesDeliveryExpressDO();
        express.setId(1L).setCode("shunfeng").setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(deliveryExpressService.validateDeliveryExpress(1L)).thenReturn(express);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.validateWaybillAccountForOrder(1L, 2L));
        assertEquals(1_011_004_204, ex.getCode()); // WAYBILL_ACCOUNT_EXPRESS_NOT_MATCH
    }

    @Test
    void validateForOrder_ok() {
        when(waybillAccountMapper.selectById(1L)).thenReturn(account);
        SalesDeliveryExpressDO express = new SalesDeliveryExpressDO();
        express.setId(1L).setCode("shunfeng").setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(deliveryExpressService.validateDeliveryExpress(1L)).thenReturn(express);
        when(addressApi.getAddress(10L)).thenReturn(senderAddress());
        SalesElectronicWaybillAccountDO result = service.validateWaybillAccountForOrder(1L, 1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void validateSenderAddress_incomplete_rejects() {
        PartnerAddressRespDTO incomplete = new PartnerAddressRespDTO();
        incomplete.setId(10L).setType(1).setName("公司").setMobile("").setAreaId(null).setDetailAddress("");
        when(addressApi.getAddress(10L)).thenReturn(incomplete);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.validateSenderAddress(10L));
        assertEquals(1_011_004_206, ex.getCode()); // WAYBILL_ACCOUNT_ADDRESS_INCOMPLETE
    }

}
