package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesConfigRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesConfigSaveReqVO;
import vip.appap.suxin.module.sales.convert.SalesConfigConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.service.SalesConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 交易中心配置")
@RestController
@RequestMapping("/sales/config")
@Validated
public class SalesConfigController {

    @Resource
    private SalesConfigService tradeConfigService;

    @Value("${suxin.tencent-lbs-key}")
    private String tencentLbsKey;

    @PutMapping("/save")
    @Operation(summary = "更新交易中心配置")
    @PreAuthorize("@ss.hasPermission('sales:sales_config:save')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody SalesConfigSaveReqVO updateReqVO) {
        tradeConfigService.saveTradeConfig(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得交易中心配置")
    @PreAuthorize("@ss.hasPermission('sales:sales_config:query')")
    public CommonResult<SalesConfigRespVO> getConfig() {
        SalesConfigDO config = tradeConfigService.getTradeConfig();
        SalesConfigRespVO configVO = SalesConfigConvert.INSTANCE.convert(config);
        if (configVO != null) {
            configVO.setTencentLbsKey(tencentLbsKey);
        }
        return success(configVO);
    }

}
