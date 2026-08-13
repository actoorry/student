package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleLogDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SalesAfterSaleLogMapper extends BaseMapperX<SalesAfterSaleLogDO> {

    default List<SalesAfterSaleLogDO> selectListByAfterSaleId(Long afterSaleId) {
        return selectList(new LambdaQueryWrapper<SalesAfterSaleLogDO>()
                .eq(SalesAfterSaleLogDO::getAfterSaleId, afterSaleId)
                .orderByDesc(SalesAfterSaleLogDO::getId));
    }

}
