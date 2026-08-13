package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillConfigRespVO;
import vip.appap.suxin.module.sales.convert.SalesSeckillConfigConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillConfigDO;
import vip.appap.suxin.module.sales.service.SalesSeckillConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 秒杀时间段")
@RestController
@RequestMapping("/sales/promotion/seckill-config")
@Validated
public class AppSalesSeckillConfigController {
    @Resource
    private SalesSeckillConfigService configService;

    @GetMapping("/list")
    @Operation(summary = "获得秒杀时间段列表")
    @PermitAll
    public CommonResult<List<AppSalesSeckillConfigRespVO>> getSeckillConfigList() {
        List<SalesSeckillConfigDO> list = configService.getSeckillConfigListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(SalesSeckillConfigConvert.INSTANCE.convertList2(list));
    }

}
