package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressExportReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SalesDeliveryExpressMapper extends BaseMapperX<SalesDeliveryExpressDO> {

    default PageResult<SalesDeliveryExpressDO> selectPage(SalesDeliveryExpressPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesDeliveryExpressDO>()
                .likeIfPresent(SalesDeliveryExpressDO::getCode, reqVO.getCode())
                .likeIfPresent(SalesDeliveryExpressDO::getName, reqVO.getName())
                .eqIfPresent(SalesDeliveryExpressDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesDeliveryExpressDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(SalesDeliveryExpressDO::getSort));
    }

    default List<SalesDeliveryExpressDO> selectList(SalesDeliveryExpressExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SalesDeliveryExpressDO>()
                .likeIfPresent(SalesDeliveryExpressDO::getCode, reqVO.getCode())
                .likeIfPresent(SalesDeliveryExpressDO::getName, reqVO.getName())
                .eqIfPresent(SalesDeliveryExpressDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesDeliveryExpressDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(SalesDeliveryExpressDO::getSort));
    }

    default SalesDeliveryExpressDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapper<SalesDeliveryExpressDO>()
                .eq(SalesDeliveryExpressDO::getCode, code));
    }

    default List<SalesDeliveryExpressDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<SalesDeliveryExpressDO>()
                .eq(SalesDeliveryExpressDO::getStatus, status)
                .orderByAsc(SalesDeliveryExpressDO::getSort));
    }

}




