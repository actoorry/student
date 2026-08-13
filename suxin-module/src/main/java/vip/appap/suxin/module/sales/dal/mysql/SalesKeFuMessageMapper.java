package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.QueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageListReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuMessageDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 客服消息 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesKeFuMessageMapper extends BaseMapperX<SalesKeFuMessageDO> {

    /**
     * 获得消息列表
     * 1. 第一次查询时，不带时间，默认查询最新的十条消息
     * 2. 第二次查询时，带时间，查询历史消息
     *
     * @param reqVO 列表请求
     * @return 消息列表
     */
    default List<SalesKeFuMessageDO> selectList(SalesKeFuMessageListReqVO reqVO) {
        return selectList(new QueryWrapperX<SalesKeFuMessageDO>()
                .eqIfPresent("conversation_id", reqVO.getConversationId())
                .ltIfPresent("create_time", reqVO.getCreateTime())
                .orderByDesc("create_time")
                .limitN(reqVO.getLimit()));
    }

    default List<SalesKeFuMessageDO> selectListByConversationIdAndUserTypeAndReadStatus(Long conversationId, Integer userType,
                                                                                   Boolean readStatus) {
        return selectList(new LambdaQueryWrapper<SalesKeFuMessageDO>()
                .eq(SalesKeFuMessageDO::getConversationId, conversationId)
                .ne(SalesKeFuMessageDO::getSenderType, userType) // 管理员：查询出未读的会员消息，会员：查询出未读的客服消息
                .eq(SalesKeFuMessageDO::getReadStatus, readStatus));
    }

    default void updateReadStatusBatchByIds(Collection<Long> ids, SalesKeFuMessageDO keFuMessageDO) {
        update(keFuMessageDO, new LambdaUpdateWrapper<SalesKeFuMessageDO>()
                .in(SalesKeFuMessageDO::getId, ids));
    }

}