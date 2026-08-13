package vip.appap.suxin.module.accountant.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.dict.core.DictFrameworkUtils;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountRechargeCreateRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountRechargeRespVO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargeDO;
import vip.appap.suxin.module.accountant.enums.DictTypeConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface AccountRechargeConvert {

    AccountRechargeConvert INSTANCE = Mappers.getMapper(AccountRechargeConvert.class);

    @Mapping(target = "totalPrice", expression = "java( payPrice + bonusPrice)")
    AccountRechargeDO convert(Long accountId, Integer payPrice, Integer bonusPrice, Long packageId);

    AppAccountRechargeCreateRespVO convert(AccountRechargeDO bean);

    default PageResult<AppAccountRechargeRespVO> convertPage(PageResult<AccountRechargeDO> pageResult,
                                                               List<PayOrderDO> payOrderList) {
        PageResult<AppAccountRechargeRespVO> voPageResult = BeanUtils.toBean(pageResult, AppAccountRechargeRespVO.class);
        Map<Long, PayOrderDO> payOrderMap = CollectionUtils.convertMap(payOrderList, PayOrderDO::getId);
        voPageResult.getList().forEach(recharge -> {
            recharge.setPayChannelName(DictFrameworkUtils.parseDictDataLabel(
                    DictTypeConstants.CHANNEL_CODE, recharge.getPayChannelCode()));
            MapUtils.findAndThen(payOrderMap, recharge.getPayOrderId(),
                    order -> recharge.setPayOrderChannelOrderNo(order.getChannelOrderNo()));
        });
        return voPageResult;
    }

}

