package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.service.SalesBrokerageUserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppSalesBrokerageUserControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppSalesBrokerageUserController controller;

    @Mock
    private SalesBrokerageUserService brokerageUserService;

    @Test
    void getBrokerageUser_noRecord_returnsExistsFalseAndEnabledFalse() {
        when(brokerageUserService.getBrokerageUser(7L)).thenReturn(null);

        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(7L);

            AppSalesBrokerageUserRespVO result = controller.getBrokerageUser().getData();

            assertNotNull(result);
            assertFalse(result.getBrokerageUserExists());
            assertFalse(result.getBrokerageEnabled());
            assertEquals(0, result.getBrokeragePrice());
            assertEquals(0, result.getFrozenPrice());
        }
    }

    @Test
    void getBrokerageUser_disabledRecord_returnsExistsTrueAndEnabledFalse() {
        SalesBrokerageUserDO user = new SalesBrokerageUserDO().setId(7L).setBrokerageEnabled(false)
                .setBrokeragePrice(0).setFrozenPrice(0);
        when(brokerageUserService.getBrokerageUser(7L)).thenReturn(user);

        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(7L);

            AppSalesBrokerageUserRespVO result = controller.getBrokerageUser().getData();

            assertNotNull(result);
            assertTrue(result.getBrokerageUserExists());
            assertFalse(result.getBrokerageEnabled());
        }
    }

    @Test
    void getBrokerageUser_enabledRecord_returnsExistsTrueAndEnabledTrue() {
        SalesBrokerageUserDO user = new SalesBrokerageUserDO().setId(7L).setBrokerageEnabled(true)
                .setBrokeragePrice(100).setFrozenPrice(50);
        when(brokerageUserService.getBrokerageUser(7L)).thenReturn(user);

        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(7L);

            AppSalesBrokerageUserRespVO result = controller.getBrokerageUser().getData();

            assertNotNull(result);
            assertTrue(result.getBrokerageUserExists());
            assertTrue(result.getBrokerageEnabled());
            assertEquals(100, result.getBrokeragePrice());
            assertEquals(50, result.getFrozenPrice());
        }
    }

    @Test
    void getBrokerageUser_doesNotAutoCreate() {
        when(brokerageUserService.getBrokerageUser(7L)).thenReturn(null);

        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(7L);

            controller.getBrokerageUser();

            verify(brokerageUserService, never()).getOrCreateBrokerageUser(7L);
        }
    }

    @Test
    void applyBrokerageUser_delegatesToService() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(7L);

            Boolean result = controller.applyBrokerageUser().getData();

            assertTrue(result);
            verify(brokerageUserService).applyBrokerageUser(7L);
        }
    }

}
