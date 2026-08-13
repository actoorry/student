package vip.appap.suxin.module.marriage.service;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.im.service.websocket.ImWebSocketService;
import vip.appap.suxin.module.im.service.websocket.dto.AppInteractionNotificationDTO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationPageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationUnreadCountRespVO;
import vip.appap.suxin.module.marriage.dal.dataobject.AppInteractionNotificationDO;
import vip.appap.suxin.module.marriage.dal.mysql.AppInteractionNotificationMapper;
import vip.appap.suxin.module.marriage.enums.AppInteractionNotificationSceneEnum;
import vip.appap.suxin.module.marriage.service.bo.AppInteractionNotificationCreateBO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppInteractionNotificationServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppInteractionNotificationServiceImpl service;

    @Mock
    private AppInteractionNotificationMapper appInteractionNotificationMapper;
    @Mock
    private ImWebSocketService imWebSocketService;

    @Test
    void createNotification_whenBizKeyExists_returnsExistingAndSkipsInsert() {
        AppInteractionNotificationDO existing = AppInteractionNotificationDO.builder()
                .id(10L)
                .bizType(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE)
                .scene(AppInteractionNotificationSceneEnum.VIEW_ME_SUMMARY.getScene())
                .userId(100L)
                .bizKey("summary-key")
                .build();
        when(appInteractionNotificationMapper.selectByBizKey(
                existing.getBizType(), existing.getUserId(), existing.getScene(), existing.getBizKey()))
                .thenReturn(existing);

        AppInteractionNotificationDO result = service.createNotification(new AppInteractionNotificationCreateBO()
                .setBizType(existing.getBizType())
                .setScene(existing.getScene())
                .setUserId(existing.getUserId())
                .setBizKey(existing.getBizKey()));

        assertSame(existing, result);
        verify(appInteractionNotificationMapper, never()).insert(any(AppInteractionNotificationDO.class));
        verify(imWebSocketService, never()).sendAppInteractionNotificationAsync(
                any(Integer.class), any(Long.class), any(AppInteractionNotificationDTO.class));
    }

    @Test
    void createNotification_whenNew_insertsUnreadAndPushesWebSocket() {
        LocalDateTime eventTime = LocalDateTime.of(2026, 6, 16, 9, 30);

        AppInteractionNotificationDO result = service.createNotification(new AppInteractionNotificationCreateBO()
                .setBizType(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE)
                .setScene(AppInteractionNotificationSceneEnum.FOLLOW.getScene())
                .setUserId(200L)
                .setActorId(100L)
                .setTitle("follow")
                .setContent("A followed you")
                .setPayload(Map.of("fromPartnerId", 100L))
                .setEventTime(eventTime));

        ArgumentCaptor<AppInteractionNotificationDO> insertCaptor =
                ArgumentCaptor.forClass(AppInteractionNotificationDO.class);
        verify(appInteractionNotificationMapper).insert((AppInteractionNotificationDO) insertCaptor.capture());
        AppInteractionNotificationDO inserted = insertCaptor.getValue();
        assertSame(inserted, result);
        assertEquals(AppInteractionNotificationSceneEnum.FOLLOW.getScene(), inserted.getScene());
        assertEquals(200L, inserted.getUserId());
        assertEquals(100L, inserted.getActorId());
        assertFalse(inserted.getReadStatus());
        assertEquals(eventTime, inserted.getEventTime());

        verify(imWebSocketService).sendAppInteractionNotificationAsync(
                eq(UserTypeEnum.MEMBER.getValue()), eq(200L), any(AppInteractionNotificationDTO.class));
    }

    @Test
    void getUnreadCountGroupByScene_countsKnownScenes() {
        when(appInteractionNotificationMapper.selectUnreadList(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L))
                .thenReturn(List.of(
                        unread(AppInteractionNotificationSceneEnum.FOLLOW.getScene()),
                        unread(AppInteractionNotificationSceneEnum.FOLLOW.getScene()),
                        unread(AppInteractionNotificationSceneEnum.MUTUAL_FOLLOW.getScene())));

        List<AppInteractionNotificationUnreadCountRespVO> result = service.getUnreadCountGroupByScene(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L);

        assertEquals(3, result.size());
        assertEquals(2L, findCount(result, AppInteractionNotificationSceneEnum.FOLLOW.getScene()));
        assertEquals(1L, findCount(result, AppInteractionNotificationSceneEnum.MUTUAL_FOLLOW.getScene()));
        assertEquals(0L, findCount(result, AppInteractionNotificationSceneEnum.VIEW_ME_SUMMARY.getScene()));
    }

    @Test
    void getNotificationPage_delegatesBizTypeAndCurrentUser() {
        AppInteractionNotificationPageReqVO pageReqVO = new AppInteractionNotificationPageReqVO();
        pageReqVO.setScene(AppInteractionNotificationSceneEnum.FOLLOW.getScene());
        PageResult<AppInteractionNotificationDO> pageResult = new PageResult<>(List.of(unread(
                AppInteractionNotificationSceneEnum.FOLLOW.getScene())), 1L);
        when(appInteractionNotificationMapper.selectPage(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L, pageReqVO))
                .thenReturn(pageResult);

        PageResult<AppInteractionNotificationDO> result = service.getNotificationPage(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L, pageReqVO);

        assertSame(pageResult, result);
        verify(appInteractionNotificationMapper).selectPage(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L, pageReqVO);
    }

    @Test
    void getUnreadCount_delegatesBizTypeAndCurrentUser() {
        when(appInteractionNotificationMapper.selectUnreadCount(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L))
                .thenReturn(3L);

        Long result = service.getUnreadCount(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L);

        assertEquals(3L, result);
        verify(appInteractionNotificationMapper).selectUnreadCount(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L);
    }

    @Test
    void readByScene_marksOnlyRequestedScene() {
        when(appInteractionNotificationMapper.updateReadByScene(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L,
                AppInteractionNotificationSceneEnum.FOLLOW.getScene()))
                .thenReturn(2);

        int result = service.readByScene(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L,
                AppInteractionNotificationSceneEnum.FOLLOW.getScene());

        assertEquals(2, result);
        verify(appInteractionNotificationMapper).updateReadByScene(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L,
                AppInteractionNotificationSceneEnum.FOLLOW.getScene());
    }

    @Test
    void readAll_marksAllCurrentUserNotifications() {
        when(appInteractionNotificationMapper.updateReadAll(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L))
                .thenReturn(4);

        int result = service.readAll(AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L);

        assertEquals(4, result);
        verify(appInteractionNotificationMapper).updateReadAll(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, 200L);
    }

    private AppInteractionNotificationDO unread(String scene) {
        return AppInteractionNotificationDO.builder()
                .scene(scene)
                .readStatus(false)
                .build();
    }

    private Long findCount(List<AppInteractionNotificationUnreadCountRespVO> list, String scene) {
        return list.stream()
                .filter(item -> scene.equals(item.getScene()))
                .findFirst()
                .map(AppInteractionNotificationUnreadCountRespVO::getUnreadCount)
                .orElse(0L);
    }

}
