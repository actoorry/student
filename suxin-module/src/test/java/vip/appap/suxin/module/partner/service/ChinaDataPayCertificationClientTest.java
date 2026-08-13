package vip.appap.suxin.module.partner.service;

import org.apache.http.NameValuePair;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChinaDataPayCertificationClientTest {

    @Test
    void missingCredentialsFailClosedWithoutMockSuccess() throws Exception {
        ChinaDataPayCertificationClient client = new ChinaDataPayCertificationClient();

        PartnerCertificationCallResult realName = client.realNameCheck("张三", "110101199001010011", "13800138000");
        PartnerCertificationCallResult marriage = client.marriageCheck("张三", "110101199001010011");

        assertEquals("CONFIG_MISSING", realName.getCode());
        assertEquals("3", realName.getState());
        assertFalse(client.isRealNameCharged(realName));
        assertEquals("CONFIG_MISSING", marriage.getCode());
        assertFalse(client.isMarriageCharged(marriage));
    }

    @Test
    void providerFailureStatusesAndMalformedPayloadCannotBeChargedOrVerified() throws Exception {
        StubClient client = new StubClient();
        ReflectionTestUtils.setField(client, "nameCheckApiKey", "configured");
        ReflectionTestUtils.setField(client, "singleCheckApiKey", "configured");

        client.next = PartnerCertificationCallResult.builder().code("503").state("0").build();
        assertFalse(client.isRealNameCharged(client.realNameCheck("张三", "110101199001010011", "13800138000")));
        client.next = PartnerCertificationCallResult.builder().code("TIMEOUT").build();
        assertFalse(client.isMarriageCharged(client.marriageCheck("张三", "110101199001010011")));
        client.next = PartnerCertificationCallResult.builder().code("10000").state("UNKNOWN").build();
        assertFalse(client.isRealNameCharged(client.realNameCheck("张三", "110101199001010011", "13800138000")));
        assertFalse(client.isMarriageCharged(client.marriageCheck("张三", "110101199001010011")));
        PartnerCertificationCallResult malformed = client.parseResult("not-json");
        assertEquals("PARSE_ERROR", malformed.getCode());
        assertFalse(client.isRealNameCharged(malformed));
        assertFalse(client.isMarriageCharged(malformed));
    }

    private static class StubClient extends ChinaDataPayCertificationClient {
        private PartnerCertificationCallResult next;

        @Override
        protected PartnerCertificationCallResult executeFormPost(String url, List<NameValuePair> params,
                                                                 String fallbackState) {
            return next;
        }
    }
}
