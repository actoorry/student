package vip.appap.suxin.module.rongjh.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppHelpSubmitReqVO;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppHelpTotalAmountRespVO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerHelpDO;
import vip.appap.suxin.module.rongjh.service.HelpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 爱心帮扶")
@RestController
@RequestMapping("/rongjh/help")
@Validated
public class HelpController {

    @Resource
    private HelpService helpService;

    @PostMapping("/create")
    @Operation(summary = "提交帮扶申请")
    public CommonResult<Boolean> create(@Valid @RequestBody AppHelpSubmitReqVO reqVO) {
        helpService.createHelp(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "撤销帮扶申请")
    @Parameter(name = "id", description = "申请编号", required = true)
    public CommonResult<Boolean> cancel(@RequestParam("id") Long id) {
        helpService.cancelHelp(getLoginUserId(), id);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "我的帮扶申请列表")
    public CommonResult<List<PartnerHelpDO>> list() {
        return success(helpService.getHelpList(getLoginUserId()));
    }

    @GetMapping("/get")
    @Operation(summary = "帮扶申请详情")
    @Parameter(name = "id", description = "申请编号", required = true)
    public CommonResult<PartnerHelpDO> get(@RequestParam("id") Long id) {
        return success(helpService.getHelp(getLoginUserId(), id));
    }

    @GetMapping("/total-amount")
    @Operation(summary = "帮扶累计金额统计")
    public CommonResult<AppHelpTotalAmountRespVO> getTotalAmount() {
        return success(helpService.getTotalAmount(getLoginUserId()));
    }

}
