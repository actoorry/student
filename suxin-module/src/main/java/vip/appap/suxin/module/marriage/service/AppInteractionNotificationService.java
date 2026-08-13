package vip.appap.suxin.module.marriage.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationPageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationUnreadCountRespVO;
import vip.appap.suxin.module.marriage.dal.dataobject.AppInteractionNotificationDO;
import vip.appap.suxin.module.marriage.service.bo.AppInteractionNotificationCreateBO;

import java.util.List;

public interface AppInteractionNotificationService {

    AppInteractionNotificationDO createNotification(AppInteractionNotificationCreateBO createBO);

    PageResult<AppInteractionNotificationDO> getNotificationPage(String bizType, Long userId,
                                                                 AppInteractionNotificationPageReqVO pageReqVO);

    Long getUnreadCount(String bizType, Long userId);

    List<AppInteractionNotificationUnreadCountRespVO> getUnreadCountGroupByScene(String bizType, Long userId);

    int readByScene(String bizType, Long userId, String scene);

    int readAll(String bizType, Long userId);

}
