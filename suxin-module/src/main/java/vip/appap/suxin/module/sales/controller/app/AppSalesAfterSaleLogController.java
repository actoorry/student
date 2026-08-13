package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSaleLogRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleLogDO;
import vip.appap.suxin.module.sales.service.SalesAfterSaleLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 售后日志")
@RestController
@RequestMapping("/sales/after-sale-log")
@Validated
@Slf4j
public class AppSalesAfterSaleLogController {

    @Resource
    private SalesAfterSaleLogService afterSaleLogService;

    @GetMapping("/list")
    @Operation(summary = "获得售后日志列表")
    @Parameter(name = "afterSaleId", description = "售后编号", required = true, example = "1")
    public CommonResult<List<AppSalesAfterSaleLogRespVO>> getAfterSaleLogList(
            @RequestParam("afterSaleId") Long afterSaleId) {
        List<SalesAfterSaleLogDO> logs = afterSaleLogService.getAfterSaleLogList(afterSaleId);
        return success(BeanUtils.toBean(logs, AppSalesAfterSaleLogRespVO.class));
    }

}
