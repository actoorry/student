package vip.appap.suxin.module.system.controller.app;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.system.controller.app.vo.AppTenantRespVO;
import vip.appap.suxin.module.system.dal.dataobject.TenantDO;
import vip.appap.suxin.module.system.service.TenantService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AppTenantControllerTest {

    @Test
    void returnsNoTenantForExpiredIdentity() throws Exception {
        TenantDO expired = tenant(303L, LocalDateTime.now().minusSeconds(1));

        CommonResult<AppTenantRespVO> result = controllerFor(expired).getTenantByWebsite("wxc6e16a1b05f5ddaa");

        assertNull(result.getData());
    }

    private static AppTenantController controllerFor(TenantDO tenant) throws Exception {
        AppTenantController controller = new AppTenantController();
        TenantService tenantService = (TenantService) Proxy.newProxyInstance(
                TenantService.class.getClassLoader(), new Class<?>[]{TenantService.class},
                (proxy, method, args) -> "getTenantByWebsite".equals(method.getName()) ? tenant : null);
        setField(controller, "tenantService", tenantService);
        return controller;
    }

    private static TenantDO tenant(Long id, LocalDateTime expireTime) {
        return TenantDO.builder().id(id).name("测试租户").status(CommonStatusEnum.ENABLE.getStatus())
                .expireTime(expireTime).build();
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
