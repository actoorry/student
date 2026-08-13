package vip.appap.suxin.module.product.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.controller.admin.vo.ProductBrandCreateReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductBrandRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductBrandSimpleRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductBrandUpdateReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductBrandDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 品牌 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface ProductBrandConvert {

    ProductBrandConvert INSTANCE = Mappers.getMapper(ProductBrandConvert.class);

    ProductBrandDO convert(ProductBrandCreateReqVO bean);

    ProductBrandDO convert(ProductBrandUpdateReqVO bean);

    ProductBrandRespVO convert(ProductBrandDO bean);

    List<ProductBrandSimpleRespVO> convertList1(List<ProductBrandDO> list);

    List<ProductBrandRespVO> convertList(List<ProductBrandDO> list);

    PageResult<ProductBrandRespVO> convertPage(PageResult<ProductBrandDO> page);

}
