package vip.appap.suxin.module.sales.convert;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageUserRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserChildSummaryRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserMySummaryRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRankByUserCountRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageWithdrawSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageUserSummaryRespBO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 分销用户 Convert
 *
 * @author owen
 */
@Mapper
public interface SalesBrokerageUserConvert {

    SalesBrokerageUserConvert INSTANCE = Mappers.getMapper(SalesBrokerageUserConvert.class);

    SalesBrokerageUserRespVO convert(SalesBrokerageUserDO bean);

    List<SalesBrokerageUserRespVO> convertList(List<SalesBrokerageUserDO> list);

    PageResult<SalesBrokerageUserRespVO> convertPage(PageResult<SalesBrokerageUserDO> page, Map<Long, PartnerRespDTO> userMap, Map<Long, Long> brokerageUserCountMap, Map<Long, SalesBrokerageUserSummaryRespBO> userOrderSummaryMap);

    default PageResult<SalesBrokerageUserRespVO> convertPage(PageResult<SalesBrokerageUserDO> pageResult,
                                                        Map<Long, PartnerRespDTO> userMap,
                                                        Map<Long, Long> brokerageUserCountMap,
                                                        Map<Long, SalesBrokerageUserSummaryRespBO> userOrderSummaryMap,
                                                        Map<Long, SalesBrokerageWithdrawSummaryRespBO> withdrawMap) {
        PageResult<SalesBrokerageUserRespVO> result = convertPage(pageResult, userMap, brokerageUserCountMap, userOrderSummaryMap);
        for (SalesBrokerageUserRespVO userVO : result.getList()) {
            // 用户信息
            copyTo(userMap.get(userVO.getId()), userVO);
            // 推广用户数量
            userVO.setBrokerageUserCount(MapUtil.getInt(brokerageUserCountMap, userVO.getId(), 0));
            // 推广订单数量、推广订单金额
            Optional<SalesBrokerageUserSummaryRespBO> orderSummaryOptional = Optional.ofNullable(userOrderSummaryMap.get(userVO.getId()));
            userVO.setBrokerageOrderCount(orderSummaryOptional.map(SalesBrokerageUserSummaryRespBO::getCount).orElse(0))
                    .setBrokerageOrderPrice(orderSummaryOptional.map(SalesBrokerageUserSummaryRespBO::getPrice).orElse(0));
            // 已提现次数、已提现金额
            Optional<SalesBrokerageWithdrawSummaryRespBO> withdrawSummaryOptional = Optional.ofNullable(withdrawMap.get(userVO.getId()));
            userVO.setWithdrawCount(withdrawSummaryOptional.map(SalesBrokerageWithdrawSummaryRespBO::getCount).orElse(0))
                    .setWithdrawPrice(withdrawSummaryOptional.map(SalesBrokerageWithdrawSummaryRespBO::getPrice).orElse(0));
        }
        return result;
    }

    default SalesBrokerageUserRespVO copyTo(PartnerRespDTO source, SalesBrokerageUserRespVO target) {
        if (target == null) {
            return null;
        }
        Optional.ofNullable(source).ifPresent(
                user -> target.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
        return target;
    }

    default PageResult<AppSalesBrokerageUserRankByUserCountRespVO> convertPage03(PageResult<AppSalesBrokerageUserRankByUserCountRespVO> pageResult,
                                                                            Map<Long, PartnerRespDTO> userMap) {
        pageResult.getList().forEach(vo -> copyTo(userMap.get(vo.getId()), vo));
        return pageResult;
    }

    void copyTo(PartnerRespDTO from, @MappingTarget AppSalesBrokerageUserRankByUserCountRespVO to);

    default AppSalesBrokerageUserMySummaryRespVO convert(Integer yesterdayPrice, Integer withdrawPrice,
                                                    Long firstBrokerageUserCount, Long secondBrokerageUserCount,
                                                    SalesBrokerageUserDO brokerageUser) {
        AppSalesBrokerageUserMySummaryRespVO respVO = new AppSalesBrokerageUserMySummaryRespVO()
                .setYesterdayPrice(ObjUtil.defaultIfNull(yesterdayPrice, 0))
                .setWithdrawPrice(ObjUtil.defaultIfNull(withdrawPrice, 0))
                .setBrokeragePrice(0).setFrozenPrice(0)
                .setFirstBrokerageUserCount(ObjUtil.defaultIfNull(firstBrokerageUserCount, 0L))
                .setSecondBrokerageUserCount(ObjUtil.defaultIfNull(secondBrokerageUserCount, 0L));
        // 设置 brokeragePrice、frozenPrice 字段
        Optional.ofNullable(brokerageUser)
                .ifPresent(user -> respVO.setBrokeragePrice(user.getBrokeragePrice()).setFrozenPrice(user.getFrozenPrice()));
        return respVO;
    }

    default void copyTo(List<AppSalesBrokerageUserChildSummaryRespVO> list, Map<Long, PartnerRespDTO> userMap) {
        for (AppSalesBrokerageUserChildSummaryRespVO vo : list) {
            Optional.ofNullable(userMap.get(vo.getId())).ifPresent(user ->
                    vo.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
        }
    }
}
