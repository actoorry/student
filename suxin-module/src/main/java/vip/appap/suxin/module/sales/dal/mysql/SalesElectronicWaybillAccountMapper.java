package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SalesElectronicWaybillAccountMapper extends BaseMapperX<SalesElectronicWaybillAccountDO> {

    default PageResult<SalesElectronicWaybillAccountDO> selectPage(SalesElectronicWaybillAccountPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesElectronicWaybillAccountDO>()
                .likeIfPresent(SalesElectronicWaybillAccountDO::getName, reqVO.getName())
                .eqIfPresent(SalesElectronicWaybillAccountDO::getExpressId, reqVO.getExpressId())
                .eqIfPresent(SalesElectronicWaybillAccountDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesElectronicWaybillAccountDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesElectronicWaybillAccountDO::getId));
    }

    default SalesElectronicWaybillAccountDO selectByName(String name) {
        return selectOne(new LambdaQueryWrapper<SalesElectronicWaybillAccountDO>()
                .eq(SalesElectronicWaybillAccountDO::getName, name));
    }

    /**
     * 查询指定快递公司下启用的账户列表（电子面单模式下按快递公司匹配加载）
     *
     * @param expressId 快递公司编号
     * @return 账户列表
     */
    default List<SalesElectronicWaybillAccountDO> selectListByExpressIdAndStatus(Long expressId, Integer status) {
        return selectList(new LambdaQueryWrapperX<SalesElectronicWaybillAccountDO>()
                .eqIfPresent(SalesElectronicWaybillAccountDO::getExpressId, expressId)
                .eq(SalesElectronicWaybillAccountDO::getStatus, status)
                .orderByAsc(SalesElectronicWaybillAccountDO::getId));
    }

    /**
     * 查询当前租户启用的账户列表（按状态过滤）
     *
     * @param status 状态
     * @return 账户列表
     */
    default List<SalesElectronicWaybillAccountDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<SalesElectronicWaybillAccountDO>()
                .eq(SalesElectronicWaybillAccountDO::getStatus, status)
                .orderByAsc(SalesElectronicWaybillAccountDO::getId));
    }

}
