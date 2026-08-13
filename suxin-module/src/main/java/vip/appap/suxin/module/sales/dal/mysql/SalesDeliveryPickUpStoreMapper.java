package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryPickUpStorePageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryPickUpStoreDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SalesDeliveryPickUpStoreMapper extends BaseMapperX<SalesDeliveryPickUpStoreDO> {

    default PageResult<SalesDeliveryPickUpStoreDO> selectPage(SalesDeliveryPickUpStorePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesDeliveryPickUpStoreDO>()
                .likeIfPresent(SalesDeliveryPickUpStoreDO::getName, reqVO.getName())
                .eqIfPresent(SalesDeliveryPickUpStoreDO::getPhone, reqVO.getPhone())
                .eqIfPresent(SalesDeliveryPickUpStoreDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(SalesDeliveryPickUpStoreDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesDeliveryPickUpStoreDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesDeliveryPickUpStoreDO::getId));
    }

    default List<SalesDeliveryPickUpStoreDO> selectListByStatus(Integer status) {
        return selectList(SalesDeliveryPickUpStoreDO::getStatus, status);
    }

}




