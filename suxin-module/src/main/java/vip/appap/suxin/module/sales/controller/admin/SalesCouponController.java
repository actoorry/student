package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponPageItemRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponSendReqVO;
import vip.appap.suxin.module.sales.convert.SalesCouponConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponDO;
import vip.appap.suxin.module.sales.service.SalesCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 优惠劵")
@RestController
@RequestMapping("/sales/promotion/coupon")
@Validated
public class SalesCouponController {

    @Resource
    private SalesCouponService couponService;
    @Resource
    private PartnerApi PartnerApi;

    @DeleteMapping("/delete")
    @Operation(summary = "回收优惠劵")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon:delete')")
    public CommonResult<Boolean> deleteCoupon(@RequestParam("id") Long id) {
        couponService.deleteCoupon(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得优惠劵分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon:query')")
    public CommonResult<PageResult<SalesCouponPageItemRespVO>> getCouponPage(@Valid SalesCouponPageReqVO pageVO) {
        PageResult<SalesCouponDO> pageResult = couponService.getCouponPage(pageVO);
        PageResult<SalesCouponPageItemRespVO> pageResulVO = SalesCouponConvert.INSTANCE.convertPage(pageResult);
        if (CollUtil.isEmpty(pageResulVO.getList())) {
            return success(pageResulVO);
        }

        // 读取用户信息，进行拼接
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(convertSet(pageResult.getList(), SalesCouponDO::getUserId));
        pageResulVO.getList().forEach(itemRespVO -> MapUtils.findAndThen(userMap, itemRespVO.getUserId(),
                userRespDTO -> itemRespVO.setNickname(userRespDTO.getNickname())));
        return success(pageResulVO);
    }

    @PostMapping("/send")
    @Operation(summary = "发送优惠劵")
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon:send')")
    public CommonResult<Boolean> sendCoupon(@Valid @RequestBody SalesCouponSendReqVO reqVO) {
        couponService.takeCouponByAdmin(reqVO.getTemplateId(), reqVO.getUserIds());
        return success(true);
    }

}
