package vip.appap.suxin.module.partner.controller.app;

import org.junit.jupiter.api.Test;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerMarriageCheckRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerMarriageCheckStatusRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerNameCheckRespVO;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PartnerCertificationAppResponseTest {

    private static final Set<String> FORBIDDEN_FIELDS = Set.of(
            "requestParams", "responseBody", "idCard", "apiKey", "nameCheckApiKey", "singleCheckApiKey");

    @Test
    void appCertificationResponsesDoNotExposeProviderSnapshotsOrFullIdentityFields() {
        assertSafe(PartnerNameCheckRespVO.class);
        assertSafe(PartnerMarriageCheckRespVO.class);
        assertSafe(PartnerMarriageCheckStatusRespVO.class);
    }

    private void assertSafe(Class<?> type) {
        Set<String> fieldNames = Arrays.stream(type.getDeclaredFields()).map(field -> field.getName()).collect(java.util.stream.Collectors.toSet());
        FORBIDDEN_FIELDS.forEach(field -> assertFalse(fieldNames.contains(field),
                () -> type.getSimpleName() + " must not expose " + field));
    }
}
