package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuConversationDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客服会话 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesKeFuConversationMapper extends BaseMapperX<SalesKeFuConversationDO> {

    default List<SalesKeFuConversationDO> selectConversationList() {
        return selectList(new LambdaQueryWrapperX<SalesKeFuConversationDO>()
                .eq(SalesKeFuConversationDO::getAdminDeleted, Boolean.FALSE)
                .orderByDesc(SalesKeFuConversationDO::getCreateTime));
    }

    default void updateAdminUnreadMessageCountIncrement(Long id) {
        update(new LambdaUpdateWrapper<SalesKeFuConversationDO>()
                .eq(SalesKeFuConversationDO::getId, id)
                .setSql("admin_unread_message_count = admin_unread_message_count + 1"));
    }

    default SalesKeFuConversationDO selectByUserId(Long userId) {
        return selectOne(SalesKeFuConversationDO::getUserId, userId);
    }

}