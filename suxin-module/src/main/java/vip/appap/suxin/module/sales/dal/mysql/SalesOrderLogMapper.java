package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderLogDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SalesOrderLogMapper extends BaseMapperX<SalesOrderLogDO> {

    default List<SalesOrderLogDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapper<SalesOrderLogDO>()
                .eq(SalesOrderLogDO::getOrderId, orderId)
                .orderByDesc(SalesOrderLogDO::getCreateTime));
    }

}
