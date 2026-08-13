package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.api.dto.SalesCouponRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponPageItemRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCouponPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponTemplateDO;
import vip.appap.suxin.module.sales.enums.SalesCouponStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesCouponTemplateValidityTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 优惠劵 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface SalesCouponConvert {

    SalesCouponConvert INSTANCE = Mappers.getMapper(SalesCouponConvert.class);

    PageResult<SalesCouponPageItemRespVO> convertPage(PageResult<SalesCouponDO> page);

    SalesCouponRespDTO convert(SalesCouponDO bean);

    default SalesCouponDO convert(SalesCouponTemplateDO template, Long userId) {
        SalesCouponDO coupon = new SalesCouponDO()
                .setTemplateId(template.getId())
                .setName(template.getName())
                .setTakeType(template.getTakeType())
                .setUsePrice(template.getUsePrice())
                .setProductScope(template.getProductScope())
                .setProductScopeValues(template.getProductScopeValues())
                .setDiscountType(template.getDiscountType())
                .setDiscountPercent(template.getDiscountPercent())
                .setDiscountPrice(template.getDiscountPrice())
                .setDiscountLimitPrice(template.getDiscountLimitPrice())
                .setStatus(SalesCouponStatusEnum.UNUSED.getStatus())
                .setUserId(userId);
        if (SalesCouponTemplateValidityTypeEnum.DATE.getType().equals(template.getValidityType())) {
            coupon.setValidStartTime(template.getValidStartTime());
            coupon.setValidEndTime(template.getValidEndTime());
        } else if (SalesCouponTemplateValidityTypeEnum.TERM.getType().equals(template.getValidityType())) {
            coupon.setValidStartTime(LocalDateTime.now().plusDays(template.getFixedStartTerm()));
            coupon.setValidEndTime(coupon.getValidStartTime().plusDays(template.getFixedEndTerm()));
        }
        return coupon;
    }

    SalesCouponPageReqVO convert(AppSalesCouponPageReqVO pageReqVO, Collection<Long> userIds);

}
