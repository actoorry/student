package vip.appap.suxin.module.marriage.service;

import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.marriage.enums.AppInteractionNotificationSceneEnum;
import vip.appap.suxin.module.marriage.service.bo.AppInteractionNotificationCreateBO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerRelPartnerDO;
import vip.appap.suxin.module.partner.service.PartnerRelPartnerService;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.service.NotifyMessageService;
import vip.appap.suxin.module.system.service.NotifyTemplateService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarriageInteractionServiceImplTest extends BaseMockitoUnitTest {

    private static final String RELATION_TYPE_ATTENTION = "attention";
    private static final String RELATION_TYPE_LOOK = "look";

    @InjectMocks
    private MarriageInteractionServiceImpl service;

    @Mock
    private PartnerService partnerService;
    @Mock
    private PartnerRelPartnerService partnerRelPartnerService;
    @Mock
    private PartnerMarriageProfileService partnerMarriageProfileService;
    @Mock
    private NotifyTemplateService notifyTemplateService;
    @Mock
    private NotifyMessageService notifyMessageService;
    @Mock
    private AppInteractionNotificationService appInteractionNotificationService;

    @Test
    void follow_whenFirstAttention_createsFollowNotification() {
        when(partnerService.getPartner(100L)).thenReturn(partner(100L, "Alice"));
        when(partnerService.getPartner(200L)).thenReturn(partner(200L, "Bob"));

        service.follow(100L, 200L);

        verify(partnerRelPartnerService).createPartnerRelPartner(100L, 200L, RELATION_TYPE_ATTENTION);
        ArgumentCaptor<AppInteractionNotificationCreateBO> captor =
                ArgumentCaptor.forClass(AppInteractionNotificationCreateBO.class);
        verify(appInteractionNotificationService).createNotification(captor.capture());
        AppInteractionNotificationCreateBO reqVO = captor.getValue();
        assertEquals(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, reqVO.getBizType());
        assertEquals(AppInteractionNotificationSceneEnum.FOLLOW.getScene(), reqVO.getScene());
        assertEquals(200L, reqVO.getUserId());
        assertEquals(100L, reqVO.getActorId());
        assertNull(reqVO.getBizKey());
        verify(notifyMessageService, never()).createNotifyMessage(any(), any(), any(), any(), any());
    }

    @Test
    void follow_whenAlreadyAttention_doesNotCreateDuplicateNotification() {
        when(partnerService.getPartner(100L)).thenReturn(partner(100L, "Alice"));
        when(partnerService.getPartner(200L)).thenReturn(partner(200L, "Bob"));
        when(partnerRelPartnerService.getPartnerRelPartner(100L, 200L, RELATION_TYPE_ATTENTION))
                .thenReturn(PartnerRelPartnerDO.builder()
                        .partnerId(100L)
                        .relPartnerId(200L)
                        .type(RELATION_TYPE_ATTENTION)
                        .build());

        service.follow(100L, 200L);

        verify(partnerRelPartnerService, never()).createPartnerRelPartner(any(), any(), any());
        verify(appInteractionNotificationService, never()).createNotification(any());
    }

    @Test
    void follow_whenMutualAttention_createsNotificationsForBothUsers() {
        when(partnerService.getPartner(100L)).thenReturn(partner(100L, "Alice"));
        when(partnerService.getPartner(200L)).thenReturn(partner(200L, "Bob"));
        when(partnerRelPartnerService.getPartnerRelPartner(100L, 200L, RELATION_TYPE_ATTENTION))
                .thenReturn(null);
        when(partnerRelPartnerService.getPartnerRelPartner(200L, 100L, RELATION_TYPE_ATTENTION))
                .thenReturn(PartnerRelPartnerDO.builder()
                        .partnerId(200L)
                        .relPartnerId(100L)
                        .type(RELATION_TYPE_ATTENTION)
                        .build());

        service.follow(100L, 200L);

        ArgumentCaptor<AppInteractionNotificationCreateBO> captor =
                ArgumentCaptor.forClass(AppInteractionNotificationCreateBO.class);
        verify(appInteractionNotificationService, org.mockito.Mockito.times(2)).createNotification(captor.capture());
        List<AppInteractionNotificationCreateBO> notifications = captor.getAllValues();
        assertEquals(AppInteractionNotificationSceneEnum.MUTUAL_FOLLOW.getScene(), notifications.get(0).getScene());
        assertEquals(100L, notifications.get(0).getUserId());
        assertEquals(200L, notifications.get(0).getActorId());
        assertEquals(AppInteractionNotificationSceneEnum.MUTUAL_FOLLOW.getScene(), notifications.get(1).getScene());
        assertEquals(200L, notifications.get(1).getUserId());
        assertEquals(100L, notifications.get(1).getActorId());
    }

    @Test
    void generateViewMeSummaryNotifications_groupsByReceiverAndUsesWindowBizKey() {
        LocalDateTime startTime = LocalDateTime.of(2026, 6, 15, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 6, 16, 0, 0);
        PartnerRelPartnerDO relation1 = relation(100L, 200L, startTime.plusHours(2));
        PartnerRelPartnerDO relation2 = relation(101L, 200L, startTime.plusHours(3));
        when(partnerRelPartnerService.getPartnerRelPartnerListByTypeAndCreateTimeBetween(
                RELATION_TYPE_LOOK, startTime, endTime))
                .thenReturn(List.of(relation1, relation2));
        when(partnerService.getPartner(100L)).thenReturn(partner(100L, "Alice"));
        when(partnerService.getPartner(101L)).thenReturn(partner(101L, "Carol"));

        int count = service.generateViewMeSummaryNotifications(startTime, endTime);

        assertEquals(1, count);
        ArgumentCaptor<AppInteractionNotificationCreateBO> captor =
                ArgumentCaptor.forClass(AppInteractionNotificationCreateBO.class);
        verify(appInteractionNotificationService).createNotification(captor.capture());
        AppInteractionNotificationCreateBO reqVO = captor.getValue();
        assertEquals(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, reqVO.getBizType());
        assertEquals(AppInteractionNotificationSceneEnum.VIEW_ME_SUMMARY.getScene(), reqVO.getScene());
        assertEquals(200L, reqVO.getUserId());
        assertEquals("marriage:view-me:" + startTime + ":" + endTime, reqVO.getBizKey());
        assertEquals(endTime, reqVO.getEventTime());
        assertEquals(2, reqVO.getPayload().get("viewerCount"));
    }

    private PartnerDO partner(Long id, String nickname) {
        return PartnerDO.builder()
                .id(id)
                .nickname(nickname)
                .avatar("avatar-" + id)
                .build();
    }

    private PartnerRelPartnerDO relation(Long partnerId, Long relPartnerId, LocalDateTime createTime) {
        PartnerRelPartnerDO relation = PartnerRelPartnerDO.builder()
                .partnerId(partnerId)
                .relPartnerId(relPartnerId)
                .type(RELATION_TYPE_LOOK)
                .build();
        relation.setCreateTime(createTime);
        return relation;
    }

}
