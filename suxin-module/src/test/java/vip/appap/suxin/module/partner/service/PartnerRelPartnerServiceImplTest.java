package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerRelPartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerRelPartnerMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PartnerRelPartnerServiceImplTest extends BaseMockitoUnitTest {

    private static final String RELATION_TYPE_ATTENTION = "attention";

    @InjectMocks
    private PartnerRelPartnerServiceImpl service;

    @Mock
    private PartnerRelPartnerMapper partnerRelPartnerMapper;

    @Test
    void createPartnerRelPartner_whenDeletedRelationExists_resurrectsAndSkipsInsert() {
        PartnerRelPartnerDO existing = PartnerRelPartnerDO.builder()
                .id(10L)
                .partnerId(367L)
                .relPartnerId(346L)
                .type(RELATION_TYPE_ATTENTION)
                .build();
        when(partnerRelPartnerMapper.resurrectDeletedRelation(367L, 346L, RELATION_TYPE_ATTENTION)).thenReturn(1);
        when(partnerRelPartnerMapper.selectByPartnerIdAndRelPartnerIdAndType(367L, 346L, RELATION_TYPE_ATTENTION))
                .thenReturn(existing);

        PartnerRelPartnerDO result = service.createPartnerRelPartner(367L, 346L, RELATION_TYPE_ATTENTION);

        assertSame(existing, result);
        verify(partnerRelPartnerMapper, never()).insert(any(PartnerRelPartnerDO.class));
    }

    @Test
    void createPartnerRelPartner_whenConcurrentInsertHappened_returnsExistingRelation() {
        PartnerRelPartnerDO existing = PartnerRelPartnerDO.builder()
                .id(11L)
                .partnerId(367L)
                .relPartnerId(346L)
                .type(RELATION_TYPE_ATTENTION)
                .build();
        when(partnerRelPartnerMapper.resurrectDeletedRelation(367L, 346L, RELATION_TYPE_ATTENTION)).thenReturn(0);
        when(partnerRelPartnerMapper.insert(any(PartnerRelPartnerDO.class))).thenThrow(new DuplicateKeyException("duplicate"));
        when(partnerRelPartnerMapper.selectByPartnerIdAndRelPartnerIdAndType(367L, 346L, RELATION_TYPE_ATTENTION))
                .thenReturn(existing);

        PartnerRelPartnerDO result = service.createPartnerRelPartner(367L, 346L, RELATION_TYPE_ATTENTION);

        assertSame(existing, result);
        assertEquals(11L, result.getId());
    }

}
