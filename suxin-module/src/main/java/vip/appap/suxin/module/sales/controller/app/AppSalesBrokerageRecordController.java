package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.dict.core.DictFrameworkUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageRecordPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageProductPriceRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageRecordPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageRecordRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageRecordDO;
import vip.appap.suxin.module.sales.enums.DictTypeConstants;
import vip.appap.suxin.module.sales.service.SalesBrokerageRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 分销用户")
@RestController
@RequestMapping("/sales/brokerage-record")
@Validated
@Slf4j
public class AppSalesBrokerageRecordController {

    @Resource
    private SalesBrokerageRecordService brokerageRecordService;

    @GetMapping("/page")
    @Operation(summary = "获得分销记录分页")
    public CommonResult<PageResult<AppSalesBrokerageRecordRespVO>> getBrokerageRecordPage(@Valid AppSalesBrokerageRecordPageReqVO pageReqVO) {
        PageResult<SalesBrokerageRecordDO> pageResult = brokerageRecordService.getBrokerageRecordPage(
                BeanUtils.toBean(pageReqVO, SalesBrokerageRecordPageReqVO.class).setUserId(getLoginUserId()));
        return success(BeanUtils.toBean(pageResult, AppSalesBrokerageRecordRespVO.class, recordVO ->
                recordVO.setStatusName(DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.BROKERAGE_RECORD_STATUS, recordVO.getStatus()))));
    }

    @GetMapping("/get-product-brokerage-price")
    @Operation(summary = "获得商品的分销金额")
    public CommonResult<AppSalesBrokerageProductPriceRespVO> getProductBrokeragePrice(@RequestParam("spuId") Long spuId) {
        return success(brokerageRecordService.calculateProductBrokeragePrice(getLoginUserId(), spuId));
    }

}