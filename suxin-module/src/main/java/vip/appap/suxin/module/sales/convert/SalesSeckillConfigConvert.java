package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillConfigCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillConfigRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillConfigSimpleRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillConfigUpdateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillConfigRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 秒杀时段 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface SalesSeckillConfigConvert {

    SalesSeckillConfigConvert INSTANCE = Mappers.getMapper(SalesSeckillConfigConvert.class);

    SalesSeckillConfigDO convert(SalesSeckillConfigCreateReqVO bean);

    SalesSeckillConfigDO convert(SalesSeckillConfigUpdateReqVO bean);

    SalesSeckillConfigRespVO convert(SalesSeckillConfigDO bean);

    List<SalesSeckillConfigRespVO> convertList(List<SalesSeckillConfigDO> list);

    List<SalesSeckillConfigSimpleRespVO> convertList1(List<SalesSeckillConfigDO> list);

    PageResult<SalesSeckillConfigRespVO> convertPage(PageResult<SalesSeckillConfigDO> page);

    List<AppSalesSeckillConfigRespVO> convertList2(List<SalesSeckillConfigDO> list);

    AppSalesSeckillConfigRespVO convert1(SalesSeckillConfigDO filteredConfig);
}
