package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesConfigRespVO;
import vip.appap.suxin.module.sales.convert.SalesConfigConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.service.SalesConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 交易配置")
@RestController
@RequestMapping("/sales/config")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AppSalesConfigController {

    @Resource
    private SalesConfigService tradeConfigService;

    @Value("${suxin.tencent-lbs-key}")
    private String tencentLbsKey;

    @GetMapping("/get")
    @Operation(summary = "获得交易配置")
    @PermitAll
    public CommonResult<AppSalesConfigRespVO> getTradeConfig() {
        SalesConfigDO config = ObjUtil.defaultIfNull(tradeConfigService.getTradeConfig(), new SalesConfigDO());
        return success(SalesConfigConvert.INSTANCE.convert02(config).setTencentLbsKey(tencentLbsKey));
    }

}
