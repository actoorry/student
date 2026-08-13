package vip.appap.suxin.module.accountant.dal.mysql;

import vip.appap.suxin.module.accountant.dal.dataobject.PayNotifyLogDO;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PayNotifyLogMapper extends BaseMapperX<PayNotifyLogDO> {

    default List<PayNotifyLogDO> selectListByTaskId(Long taskId) {
        return selectList(PayNotifyLogDO::getTaskId, taskId);
    }

}

