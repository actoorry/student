package vip.appap.suxin.module.sales.dal.mysql;


import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateChargeDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SalesDeliveryExpressTemplateChargeMapper extends BaseMapperX<SalesDeliveryExpressTemplateChargeDO> {

    default List<SalesDeliveryExpressTemplateChargeDO> selectListByTemplateId(Long templateId){
        return selectList(new LambdaQueryWrapper<SalesDeliveryExpressTemplateChargeDO>()
                .eq(SalesDeliveryExpressTemplateChargeDO::getTemplateId, templateId));
    }

    default int deleteByTemplateId(Long templateId){
       return delete(new LambdaQueryWrapper<SalesDeliveryExpressTemplateChargeDO>()
               .eq(SalesDeliveryExpressTemplateChargeDO::getTemplateId, templateId));
    }

    default List<SalesDeliveryExpressTemplateChargeDO> selectByTemplateIds(Collection<Long> templateIds) {
        return selectList(SalesDeliveryExpressTemplateChargeDO::getTemplateId, templateIds);
    }

}




