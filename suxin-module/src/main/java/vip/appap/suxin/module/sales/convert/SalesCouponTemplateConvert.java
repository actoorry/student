package vip.appap.suxin.module.sales.convert;

import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponTemplateCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponTemplatePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponTemplateRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponTemplateUpdateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCouponTemplatePageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCouponTemplateRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponTemplateDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 优惠劵模板 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface SalesCouponTemplateConvert {

    SalesCouponTemplateConvert INSTANCE = Mappers.getMapper(SalesCouponTemplateConvert.class);

    SalesCouponTemplateDO convert(SalesCouponTemplateCreateReqVO bean);

    SalesCouponTemplateDO convert(SalesCouponTemplateUpdateReqVO bean);

    SalesCouponTemplateRespVO convert(SalesCouponTemplateDO bean);

    PageResult<SalesCouponTemplateRespVO> convertPage(PageResult<SalesCouponTemplateDO> page);

    SalesCouponTemplatePageReqVO convert(AppSalesCouponTemplatePageReqVO pageReqVO, List<Integer> canTakeTypes, Integer productScope, Long productScopeValue);

    PageResult<AppSalesCouponTemplateRespVO> convertAppPage(PageResult<SalesCouponTemplateDO> pageResult);

    List<AppSalesCouponTemplateRespVO> convertAppList(List<SalesCouponTemplateDO> list);

    default PageResult<AppSalesCouponTemplateRespVO> convertAppPage(PageResult<SalesCouponTemplateDO> pageResult, Map<Long, Boolean> userCanTakeMap) {
        PageResult<AppSalesCouponTemplateRespVO> result = convertAppPage(pageResult);
        copyTo(result.getList(), userCanTakeMap);
        return result;
    }

    default List<AppSalesCouponTemplateRespVO> convertAppList(List<SalesCouponTemplateDO> list, Map<Long, Boolean> userCanTakeMap) {
        List<AppSalesCouponTemplateRespVO> result = convertAppList(list);
        copyTo(result, userCanTakeMap);
        return result;
    }

    default void copyTo(List<AppSalesCouponTemplateRespVO> list, Map<Long, Boolean> userCanTakeMap) {
        for (AppSalesCouponTemplateRespVO template : list) {
            // 检查已领取数量是否超过限领数量
            template.setCanTake(MapUtil.getBool(userCanTakeMap, template.getId(), false));
        }
    }

    List<SalesCouponTemplateRespVO> convertList(List<SalesCouponTemplateDO> list);

}
