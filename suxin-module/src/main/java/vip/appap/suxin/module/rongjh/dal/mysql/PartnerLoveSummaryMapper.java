package vip.appap.suxin.module.rongjh.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerLoveSummaryDO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface PartnerLoveSummaryMapper extends BaseMapperX<PartnerLoveSummaryDO> {

    default BigDecimal selectPlatformLoveValue() {
        return selectList().stream()
                .map(PartnerLoveSummaryDO::getTotalLoveValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
