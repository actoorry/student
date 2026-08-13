package vip.appap.suxin.module.sales.convert;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.number.MoneyUtils;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageRecordRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRankByPriceRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageRecordDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 佣金记录 Convert
 *
 * @author owen
 */
@Mapper
public interface SalesBrokerageRecordConvert {

    SalesBrokerageRecordConvert INSTANCE = Mappers.getMapper(SalesBrokerageRecordConvert.class);

    SalesBrokerageRecordRespVO convert(SalesBrokerageRecordDO bean);

    List<SalesBrokerageRecordRespVO> convertList(List<SalesBrokerageRecordDO> list);

    PageResult<SalesBrokerageRecordRespVO> convertPage(PageResult<SalesBrokerageRecordDO> page);

    default SalesBrokerageRecordDO convert(SalesBrokerageUserDO user, SalesBrokerageRecordBizTypeEnum bizType, String bizId,
                                      Integer brokerageFrozenDays, int brokeragePrice, LocalDateTime unfreezeTime,
                                      String title, Long sourceUserId, Integer sourceUserLevel) {
        brokerageFrozenDays = ObjectUtil.defaultIfNull(brokerageFrozenDays, 0);
        // 不冻结时，佣金直接就是结算状态
        Integer status = brokerageFrozenDays > 0
                ? SalesBrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus()
                : SalesBrokerageRecordStatusEnum.SETTLEMENT.getStatus();
        return new SalesBrokerageRecordDO().setUserId(user.getId())
                .setBizType(bizType.getType()).setBizId(bizId)
                .setPrice(brokeragePrice).setTotalPrice(user.getBrokeragePrice())
                .setTitle(title)
                .setDescription(StrUtil.format(bizType.getDescription(), MoneyUtils.fenToYuanStr(Math.abs(brokeragePrice))))
                .setStatus(status).setFrozenDays(brokerageFrozenDays).setUnfreezeTime(unfreezeTime)
                .setSourceUserLevel(sourceUserLevel).setSourceUserId(sourceUserId);
    }

    default PageResult<SalesBrokerageRecordRespVO> convertPage(PageResult<SalesBrokerageRecordDO> pageResult, Map<Long, PartnerRespDTO> userMap) {
        PageResult<SalesBrokerageRecordRespVO> result = convertPage(pageResult);
        for (SalesBrokerageRecordRespVO respVO : result.getList()) {
            Optional.ofNullable(userMap.get(respVO.getUserId())).ifPresent(user ->
                    respVO.setUserNickname(user.getNickname()).setUserAvatar(user.getAvatar()));
            Optional.ofNullable(userMap.get(respVO.getSourceUserId())).ifPresent(user ->
                    respVO.setSourceUserNickname(user.getNickname()).setSourceUserAvatar(user.getAvatar()));
        }
        return result;
    }

    default PageResult<AppSalesBrokerageUserRankByPriceRespVO> convertPage03(PageResult<AppSalesBrokerageUserRankByPriceRespVO> pageResult, Map<Long, PartnerRespDTO> userMap) {
        for (AppSalesBrokerageUserRankByPriceRespVO vo : pageResult.getList()) {
            copyTo(userMap.get(vo.getId()), vo);
        }
        return pageResult;
    }

    void copyTo(PartnerRespDTO from, @MappingTarget AppSalesBrokerageUserRankByPriceRespVO to);
}
