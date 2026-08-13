package vip.appap.suxin.module.accountant.dal.mysql;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.query.QueryWrapperX;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionPageReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountTransactionDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionPageReqVO.*;

@Mapper
public interface AccountTransactionMapper extends BaseMapperX<AccountTransactionDO> {

    default PageResult<AccountTransactionDO> selectPage(Long accountId, Integer type,
                                                          PageParam pageParam, LocalDateTime[] createTime) {
        LambdaQueryWrapperX<AccountTransactionDO> query = new LambdaQueryWrapperX<AccountTransactionDO>()
                .eqIfPresent(AccountTransactionDO::getAccountId, accountId);
        if (Objects.equals(type, TYPE_INCOME)) {
            query.gt(AccountTransactionDO::getPrice, 0);
        } else if (Objects.equals(type, TYPE_EXPENSE)) {
            query.lt(AccountTransactionDO::getPrice, 0);
        }
        query.betweenIfPresent(AccountTransactionDO::getCreateTime, createTime);
        query.orderByDesc(AccountTransactionDO::getId);
        return selectPage(pageParam, query);
    }

    default Integer selectPriceSum(Long accountId, Integer type, LocalDateTime[] createTime) {
        // SQL sum 鏌ヨ
        List<Map<String, Object>> result = selectMaps(new QueryWrapperX<AccountTransactionDO>()
                .select("SUM(price) AS priceSum")
                .gt(Objects.equals(type, TYPE_INCOME), "price", 0) // 鏀跺叆
                .lt(Objects.equals(type, TYPE_EXPENSE), "price", 0) // 鏀嚭
                .eq("account_id", accountId)
                .between("create_time", createTime[0], createTime[1]));
        // 鑾峰緱 sum 缁撴灉
        Map<String, Object> first = CollUtil.getFirst(result);
        return MapUtil.getInt(first, "priceSum", 0);
    }

    default AccountTransactionDO selectByNo(String no) {
        return selectOne(AccountTransactionDO::getNo, no);
    }

    default AccountTransactionDO selectByBiz(String bizId, Integer bizType) {
        return selectOne(AccountTransactionDO::getBizId, bizId,
                AccountTransactionDO::getBizType, bizType);
    }

}




