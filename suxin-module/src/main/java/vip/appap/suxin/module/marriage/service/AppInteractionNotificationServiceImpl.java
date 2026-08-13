package vip.appap.suxin.module.marriage.service;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.im.service.websocket.ImWebSocketService;
import vip.appap.suxin.module.im.service.websocket.dto.AppInteractionNotificationDTO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationPageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationUnreadCountRespVO;
import vip.appap.suxin.module.marriage.dal.dataobject.AppInteractionNotificationDO;
import vip.appap.suxin.module.marriage.dal.mysql.AppInteractionNotificationMapper;
import vip.appap.suxin.module.marriage.enums.AppInteractionNotificationSceneEnum;
import vip.appap.suxin.module.marriage.service.bo.AppInteractionNotificationCreateBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
public class AppInteractionNotificationServiceImpl implements AppInteractionNotificationService {

    @Resource
    private AppInteractionNotificationMapper appInteractionNotificationMapper;
    @Resource
    private ImWebSocketService imWebSocketService;

    @Override
    public AppInteractionNotificationDO createNotification(AppInteractionNotificationCreateBO createBO) {
        AppInteractionNotificationDO existing = appInteractionNotificationMapper.selectByBizKey(
                createBO.getBizType(), createBO.getUserId(), createBO.getScene(), createBO.getBizKey());
        if (existing != null) {
            return existing;
        }
        AppInteractionNotificationDO notification = AppInteractionNotificationDO.builder()
                .bizType(createBO.getBizType())
                .scene(createBO.getScene())
                .userId(createBO.getUserId())
                .actorId(createBO.getActorId())
                .title(createBO.getTitle())
                .content(createBO.getContent())
                .payload(createBO.getPayload())
                .bizKey(createBO.getBizKey())
                .readStatus(false)
                .eventTime(createBO.getEventTime() != null ? createBO.getEventTime() : LocalDateTime.now())
                .build();
        appInteractionNotificationMapper.insert(notification);
        imWebSocketService.sendAppInteractionNotificationAsync(UserTypeEnum.MEMBER.getValue(),
                notification.getUserId(), AppInteractionNotificationDTO.of(notification));
        return notification;
    }

    @Override
    public PageResult<AppInteractionNotificationDO> getNotificationPage(String bizType, Long userId,
                                                                        AppInteractionNotificationPageReqVO pageReqVO) {
        return appInteractionNotificationMapper.selectPage(bizType, userId, pageReqVO);
    }

    @Override
    public Long getUnreadCount(String bizType, Long userId) {
        return appInteractionNotificationMapper.selectUnreadCount(bizType, userId);
    }

    @Override
    public List<AppInteractionNotificationUnreadCountRespVO> getUnreadCountGroupByScene(String bizType, Long userId) {
        Map<String, Long> countMap = appInteractionNotificationMapper.selectUnreadList(bizType, userId).stream()
                .collect(Collectors.groupingBy(AppInteractionNotificationDO::getScene, Collectors.counting()));
        return List.of(
                buildUnreadCount(AppInteractionNotificationSceneEnum.FOLLOW.getScene(), countMap),
                buildUnreadCount(AppInteractionNotificationSceneEnum.MUTUAL_FOLLOW.getScene(), countMap),
                buildUnreadCount(AppInteractionNotificationSceneEnum.VIEW_ME_SUMMARY.getScene(), countMap));
    }

    @Override
    public int readByScene(String bizType, Long userId, String scene) {
        return appInteractionNotificationMapper.updateReadByScene(bizType, userId, scene);
    }

    @Override
    public int readAll(String bizType, Long userId) {
        return appInteractionNotificationMapper.updateReadAll(bizType, userId);
    }

    private AppInteractionNotificationUnreadCountRespVO buildUnreadCount(String scene, Map<String, Long> countMap) {
        return new AppInteractionNotificationUnreadCountRespVO(scene, countMap.getOrDefault(scene, 0L));
    }

}
