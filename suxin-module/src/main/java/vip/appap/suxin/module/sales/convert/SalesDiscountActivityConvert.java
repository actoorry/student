package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityBaseVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 限时折扣活动 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface SalesDiscountActivityConvert {

    SalesDiscountActivityConvert INSTANCE = Mappers.getMapper(SalesDiscountActivityConvert.class);

    SalesDiscountActivityDO convert(SalesDiscountActivityCreateReqVO bean);

    SalesDiscountActivityDO convert(SalesDiscountActivityUpdateReqVO bean);

    SalesDiscountActivityRespVO convert(SalesDiscountActivityDO bean);

    List<SalesDiscountActivityRespVO> convertList(List<SalesDiscountActivityDO> list);

    List<SalesDiscountActivityBaseVO.Product> convertList2(List<SalesDiscountProductDO> list);

    PageResult<SalesDiscountActivityRespVO> convertPage(PageResult<SalesDiscountActivityDO> page);

    default PageResult<SalesDiscountActivityRespVO> convertPage(PageResult<SalesDiscountActivityDO> page,
                                                           List<SalesDiscountProductDO> discountProductDOList) {
        PageResult<SalesDiscountActivityRespVO> pageResult = convertPage(page);
        pageResult.getList().forEach(item -> item.setProducts(convertList2(discountProductDOList)));
        return pageResult;
    }

    SalesDiscountProductDO convert(SalesDiscountActivityBaseVO.Product bean);

    default SalesDiscountActivityRespVO convert(SalesDiscountActivityDO activity, List<SalesDiscountProductDO> products) {
        return BeanUtils.toBean(activity, SalesDiscountActivityRespVO.class).setProducts(convertList2(products));
    }

}