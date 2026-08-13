package vip.appap.suxin.module.system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WxContentCheckServiceImplTest {

    private final WxContentCheckServiceImpl service = new WxContentCheckServiceImpl();

    @Test
    void isValidAuditImageUrl_whenHttpServerUrl_returnsTrue() {
        assertTrue(service.isValidAuditImageUrl("http://example.com/avatar.jpg"));
    }

    @Test
    void isValidAuditImageUrl_whenHttpsServerUrl_returnsTrue() {
        assertTrue(service.isValidAuditImageUrl("https://cdn.example.com/image.png"));
    }

    @Test
    void isValidAuditImageUrl_whenBlank_returnsFalse() {
        assertFalse(service.isValidAuditImageUrl(null));
        assertFalse(service.isValidAuditImageUrl(""));
        assertFalse(service.isValidAuditImageUrl("   "));
    }

    @Test
    void isValidAuditImageUrl_whenLocalTempPath_returnsFalse() {
        assertFalse(service.isValidAuditImageUrl("wxfile://tmp/avatar.jpg"));
        assertFalse(service.isValidAuditImageUrl("http://tmp/5iG1WpIxTaJf3ece38692a337dc06df7eb69ecb49c6b.jpeg"));
        assertFalse(service.isValidAuditImageUrl("file:///storage/emulated/0/Pictures/image.jpg"));
        assertFalse(service.isValidAuditImageUrl("blob:http://localhost:9000/f74ab6b8-a14d-4cb6-a10d-fcf4511a0de5"));
    }

    @Test
    void isValidAuditImageUrl_whenRelativePath_returnsFalse() {
        assertFalse(service.isValidAuditImageUrl("/upload/avatar.jpg"));
    }
}
