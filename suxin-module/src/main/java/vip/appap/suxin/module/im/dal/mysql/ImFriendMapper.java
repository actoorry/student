package vip.appap.suxin.module.im.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.im.controller.admin.vo.ImFriendManagerPageReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImFriendDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IM 好友关系 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImFriendMapper extends BaseMapperX<ImFriendDO> {

    default ImFriendDO selectByUserIdAndFriendUserId(Long userId, Long friendUserId) {
        return selectOne(new LambdaQueryWrapperX<ImFriendDO>()
                .eq(ImFriendDO::getUserId, userId)
                .eq(ImFriendDO::getFriendUserId, friendUserId));
    }

    default List<ImFriendDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<ImFriendDO>()
                .eq(ImFriendDO::getUserId, userId)
                .orderByDesc(ImFriendDO::getId));
    }
    default List<ImFriendDO> selectListByUserIdAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ImFriendDO>()
                .eq(ImFriendDO::getUserId, userId)
                .eq(ImFriendDO::getStatus, status)
                .orderByDesc(ImFriendDO::getId));
    }

    default List<ImFriendDO> selectListByUserIdAndFriendUserIdsAndStatus(Long userId,
                                                                        Collection<Long> friendUserIds,
                                                                        Integer status) {
        return selectList(new LambdaQueryWrapperX<ImFriendDO>()
                .eq(ImFriendDO::getUserId, userId)
                .in(ImFriendDO::getFriendUserId, friendUserIds)
                .eq(ImFriendDO::getStatus, status));
    }

    default PageResult<ImFriendDO> selectPage(ImFriendManagerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImFriendDO>()
                .eqIfPresent(ImFriendDO::getUserId, reqVO.getUserId())
                .eqIfPresent(ImFriendDO::getFriendUserId, reqVO.getFriendUserId())
                .eqIfPresent(ImFriendDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ImFriendDO::getSilent, reqVO.getSilent())
                .betweenIfPresent(ImFriendDO::getAddTime, reqVO.getAddTime())
                .orderByDesc(ImFriendDO::getId));
    }

}
