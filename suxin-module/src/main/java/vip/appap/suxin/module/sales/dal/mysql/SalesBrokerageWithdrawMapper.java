package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.bean.BeanUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageWithdrawPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageWithdrawDO;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageWithdrawSummaryRespBO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 佣金提现 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface SalesBrokerageWithdrawMapper extends BaseMapperX<SalesBrokerageWithdrawDO> {

    default PageResult<SalesBrokerageWithdrawDO> selectPage(SalesBrokerageWithdrawPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesBrokerageWithdrawDO>()
                .eqIfPresent(SalesBrokerageWithdrawDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SalesBrokerageWithdrawDO::getType, reqVO.getType())
                .likeIfPresent(SalesBrokerageWithdrawDO::getUserName, reqVO.getUserName())
                .likeIfPresent(SalesBrokerageWithdrawDO::getUserAccount, reqVO.getUserAccount())
                .likeIfPresent(SalesBrokerageWithdrawDO::getBankName, reqVO.getBankName())
                .eqIfPresent(SalesBrokerageWithdrawDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesBrokerageWithdrawDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesBrokerageWithdrawDO::getId));
    }

    default int updateByIdAndStatus(Long id, Integer whereStatus, SalesBrokerageWithdrawDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<SalesBrokerageWithdrawDO>()
                .eq(SalesBrokerageWithdrawDO::getId, id)
                .eq(SalesBrokerageWithdrawDO::getStatus, whereStatus));
    }

    default List<SalesBrokerageWithdrawSummaryRespBO> selectCountAndSumPriceByUserIdAndStatus(Collection<Long> userIds,
                                                                                         Collection<Integer> status) {
        List<Map<String, Object>> list = selectMaps(new MPJLambdaWrapper<SalesBrokerageWithdrawDO>()
                .select(SalesBrokerageWithdrawDO::getUserId)
                .selectCount(SalesBrokerageWithdrawDO::getId, SalesBrokerageWithdrawSummaryRespBO::getCount)
                .selectSum(SalesBrokerageWithdrawDO::getPrice)
                .in(SalesBrokerageWithdrawDO::getUserId, userIds)
                .in(SalesBrokerageWithdrawDO::getStatus, status)
                .groupBy(SalesBrokerageWithdrawDO::getUserId));
        return BeanUtil.copyToList(list, SalesBrokerageWithdrawSummaryRespBO.class);
        // selectJoinList有BUG，会与租户插件冲突：解析SQL时，发生异常 https://gitee.com/best_handsome/mybatis-plus-join/issues/I84GYW
//        return selectJoinList(UserWithdrawSummaryBO.class, new MPJLambdaWrapper<SalesBrokerageWithdrawDO>()
//                .select(SalesBrokerageWithdrawDO::getUserId)
//                    .selectCount(SalesBrokerageWithdrawDO::getId, UserWithdrawSummaryBO::getCount)
//                .selectSum(SalesBrokerageWithdrawDO::getPrice)
//                .in(SalesBrokerageWithdrawDO::getUserId, userIds)
//                .eq(SalesBrokerageWithdrawDO::getStatus, status)
//                .groupBy(SalesBrokerageWithdrawDO::getUserId));
    }

}
