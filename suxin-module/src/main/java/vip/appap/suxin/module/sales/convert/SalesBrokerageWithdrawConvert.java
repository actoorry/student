package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageWithdrawRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageWithdrawDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 佣金提现 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface SalesBrokerageWithdrawConvert {

    SalesBrokerageWithdrawConvert INSTANCE = Mappers.getMapper(SalesBrokerageWithdrawConvert.class);

    SalesBrokerageWithdrawRespVO convert(SalesBrokerageWithdrawDO bean);

    List<SalesBrokerageWithdrawRespVO> convertList(List<SalesBrokerageWithdrawDO> list);

    PageResult<SalesBrokerageWithdrawRespVO> convertPage(PageResult<SalesBrokerageWithdrawDO> page);

    default PageResult<SalesBrokerageWithdrawRespVO> convertPage(PageResult<SalesBrokerageWithdrawDO> pageResult, Map<Long, PartnerRespDTO> userMap) {
        PageResult<SalesBrokerageWithdrawRespVO> result = convertPage(pageResult);
        for (SalesBrokerageWithdrawRespVO vo : result.getList()) {
            vo.setUserNickname(Optional.ofNullable(userMap.get(vo.getUserId())).map(PartnerRespDTO::getNickname).orElse(null));
        }
        return result;
    }

}
