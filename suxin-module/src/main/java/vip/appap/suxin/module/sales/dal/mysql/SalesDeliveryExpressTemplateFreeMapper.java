package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateFreeDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SalesDeliveryExpressTemplateFreeMapper extends BaseMapperX<SalesDeliveryExpressTemplateFreeDO> {

    default List<SalesDeliveryExpressTemplateFreeDO> selectListByTemplateId(Long templateId) {
        return selectList(new LambdaQueryWrapper<SalesDeliveryExpressTemplateFreeDO>()
                .eq(SalesDeliveryExpressTemplateFreeDO::getTemplateId, templateId));
    }

    default int deleteByTemplateId(Long templateId) {
        return delete(new LambdaQueryWrapper<SalesDeliveryExpressTemplateFreeDO>()
                .eq(SalesDeliveryExpressTemplateFreeDO::getTemplateId, templateId));
    }

    default List<SalesDeliveryExpressTemplateFreeDO> selectListByTemplateIds(Collection<Long> templateIds) {
        return selectList(SalesDeliveryExpressTemplateFreeDO::getTemplateId, templateIds);
    }
}




