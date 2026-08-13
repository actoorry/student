package vip.appap.suxin.module.sales.dal.mysql;


import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressTemplatePageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesDeliveryExpressTemplateMapper extends BaseMapperX<SalesDeliveryExpressTemplateDO> {

    default PageResult<SalesDeliveryExpressTemplateDO> selectPage(SalesDeliveryExpressTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesDeliveryExpressTemplateDO>()
                .likeIfPresent(SalesDeliveryExpressTemplateDO::getName, reqVO.getName())
                .eqIfPresent(SalesDeliveryExpressTemplateDO::getChargeMode, reqVO.getChargeMode())
                .betweenIfPresent(SalesDeliveryExpressTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(SalesDeliveryExpressTemplateDO::getSort));
    }

    default SalesDeliveryExpressTemplateDO selectByName(String name) {
        return selectOne(SalesDeliveryExpressTemplateDO::getName,name);
    }

}
