package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;
import vip.appap.suxin.module.sales.enums.SalesElectronicWaybillStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SalesElectronicWaybillMapper extends BaseMapperX<SalesElectronicWaybillDO> {

    /**
     * 查询订单最新的电子面单记录（含失败、有效、作废）
     *
     * @param orderId 订单编号
     * @return 最新的电子面单记录，可能为 null
     */
    default SalesElectronicWaybillDO selectLatestByOrderId(Long orderId) {
        return selectOne(new LambdaQueryWrapper<SalesElectronicWaybillDO>()
                .eq(SalesElectronicWaybillDO::getOrderId, orderId)
                .orderByDesc(SalesElectronicWaybillDO::getId)
                .last("LIMIT 1"));
    }

    default SalesElectronicWaybillDO selectOldestUnknownByOrderId(Long orderId) {
        return selectOne(new LambdaQueryWrapper<SalesElectronicWaybillDO>()
                .eq(SalesElectronicWaybillDO::getOrderId, orderId)
                .eq(SalesElectronicWaybillDO::getStatus, SalesElectronicWaybillStatusEnum.UNKNOWN.getStatus())
                .orderByAsc(SalesElectronicWaybillDO::getId)
                .last("LIMIT 1"));
    }

    /**
     * 查询订单的有效电子面单记录
     *
     * @param orderId 订单编号
     * @return 有效电子面单记录，可能为 null
     */
    default SalesElectronicWaybillDO selectValidByOrderId(Long orderId) {
        return selectOne(new LambdaQueryWrapper<SalesElectronicWaybillDO>()
                .eq(SalesElectronicWaybillDO::getOrderId, orderId)
                .eq(SalesElectronicWaybillDO::getStatus, SalesElectronicWaybillStatusEnum.VALID.getStatus())
                .last("LIMIT 1"));
    }

    /**
     * 批量查询订单的有效电子面单记录（用于订单列表/详情展示）
     *
     * @param orderIds 订单编号集合
     * @return 有效电子面单记录列表
     */
    default List<SalesElectronicWaybillDO> selectValidListByOrderIds(Collection<Long> orderIds) {
        return selectList(new LambdaQueryWrapperX<SalesElectronicWaybillDO>()
                .in(SalesElectronicWaybillDO::getOrderId, orderIds)
                .eq(SalesElectronicWaybillDO::getStatus, SalesElectronicWaybillStatusEnum.VALID.getStatus()));
    }

    /**
     * 查询账户关联的电子面单记录（用于账户删除前引用校验）
     *
     * @param accountId 账户编号
     * @return 记录列表
     */
    default List<SalesElectronicWaybillDO> selectListByAccountId(Long accountId) {
        return selectList(new LambdaQueryWrapper<SalesElectronicWaybillDO>()
                .eq(SalesElectronicWaybillDO::getAccountId, accountId)
                .eq(SalesElectronicWaybillDO::getStatus, SalesElectronicWaybillStatusEnum.VALID.getStatus()));
    }

}
