package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainHelpRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainHelpRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainHelpDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 砍价助力 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface SalesBargainHelpConvert {

    SalesBargainHelpConvert INSTANCE = Mappers.getMapper(SalesBargainHelpConvert.class);

    default PageResult<SalesBargainHelpRespVO> convertPage(PageResult<SalesBargainHelpDO> page,
                                                      Map<Long, PartnerRespDTO> userMap) {
        PageResult<SalesBargainHelpRespVO> pageResult = convertPage(page);
        // 拼接数据
        pageResult.getList().forEach(record ->
                MapUtils.findAndThen(userMap, record.getUserId(),
                        user -> record.setNickname(user.getNickname()).setAvatar(user.getAvatar())));
        return pageResult;
    }
    PageResult<SalesBargainHelpRespVO> convertPage(PageResult<SalesBargainHelpDO> page);

    default List<AppSalesBargainHelpRespVO> convertList(List<SalesBargainHelpDO> helps,
                                                   Map<Long, PartnerRespDTO> userMap) {
        List<AppSalesBargainHelpRespVO> helpVOs = convertList02(helps);
        helpVOs.forEach(help ->
                MapUtils.findAndThen(userMap, help.getUserId(),
                        user -> help.setNickname(user.getNickname()).setAvatar(user.getAvatar())));
        return helpVOs;
    }
    List<AppSalesBargainHelpRespVO> convertList02(List<SalesBargainHelpDO> helps);

}
