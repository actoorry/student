package vip.appap.suxin.module.marriage.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationPageReqVO;
import vip.appap.suxin.module.marriage.dal.dataobject.AppInteractionNotificationDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AppInteractionNotificationMapper extends BaseMapperX<AppInteractionNotificationDO> {

    default PageResult<AppInteractionNotificationDO> selectPage(String bizType, Long userId,
                                                                AppInteractionNotificationPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<AppInteractionNotificationDO>()
                .eq(AppInteractionNotificationDO::getBizType, bizType)
                .eq(AppInteractionNotificationDO::getUserId, userId)
                .eqIfPresent(AppInteractionNotificationDO::getScene, pageReqVO.getScene())
                .eqIfPresent(AppInteractionNotificationDO::getReadStatus, pageReqVO.getReadStatus())
                .orderByDesc(AppInteractionNotificationDO::getEventTime));
    }

    default Long selectUnreadCount(String bizType, Long userId) {
        return selectCount(new LambdaQueryWrapperX<AppInteractionNotificationDO>()
                .eq(AppInteractionNotificationDO::getBizType, bizType)
                .eq(AppInteractionNotificationDO::getUserId, userId)
                .eq(AppInteractionNotificationDO::getReadStatus, false));
    }

    default List<AppInteractionNotificationDO> selectUnreadList(String bizType, Long userId) {
        return selectList(new LambdaQueryWrapperX<AppInteractionNotificationDO>()
                .eq(AppInteractionNotificationDO::getBizType, bizType)
                .eq(AppInteractionNotificationDO::getUserId, userId)
                .eq(AppInteractionNotificationDO::getReadStatus, false));
    }

    default AppInteractionNotificationDO selectByBizKey(String bizType, Long userId, String scene, String bizKey) {
        if (bizKey == null || bizKey.isEmpty()) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<AppInteractionNotificationDO>()
                .eq(AppInteractionNotificationDO::getBizType, bizType)
                .eq(AppInteractionNotificationDO::getUserId, userId)
                .eq(AppInteractionNotificationDO::getScene, scene)
                .eq(AppInteractionNotificationDO::getBizKey, bizKey));
    }

    default int updateReadByScene(String bizType, Long userId, String scene) {
        return update(new AppInteractionNotificationDO().setReadStatus(true).setReadTime(LocalDateTime.now()),
                new LambdaQueryWrapperX<AppInteractionNotificationDO>()
                        .eq(AppInteractionNotificationDO::getBizType, bizType)
                        .eq(AppInteractionNotificationDO::getUserId, userId)
                        .eq(AppInteractionNotificationDO::getScene, scene)
                        .eq(AppInteractionNotificationDO::getReadStatus, false));
    }

    default int updateReadAll(String bizType, Long userId) {
        return update(new AppInteractionNotificationDO().setReadStatus(true).setReadTime(LocalDateTime.now()),
                new LambdaQueryWrapperX<AppInteractionNotificationDO>()
                        .eq(AppInteractionNotificationDO::getBizType, bizType)
                        .eq(AppInteractionNotificationDO::getUserId, userId)
                        .eq(AppInteractionNotificationDO::getReadStatus, false));
    }

}
