package vip.appap.suxin.module.product.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitCreateReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitSimpleRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitUpdateReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 产品单位 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface ProductUnitConvert {

    ProductUnitConvert INSTANCE = Mappers.getMapper(ProductUnitConvert.class);

    ProductUnitDO convert(ProductUnitCreateReqVO bean);

    ProductUnitDO convert(ProductUnitUpdateReqVO bean);

    ProductUnitRespVO convert(ProductUnitDO bean);

    List<ProductUnitSimpleRespVO> convertList1(List<ProductUnitDO> list);

    List<ProductUnitRespVO> convertList(List<ProductUnitDO> list);

    PageResult<ProductUnitRespVO> convertPage(PageResult<ProductUnitDO> page);

}
