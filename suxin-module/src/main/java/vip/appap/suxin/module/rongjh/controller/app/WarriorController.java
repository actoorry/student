package vip.appap.suxin.module.rongjh.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppWarriorStatusRespVO;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppWarriorSubmitReqVO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerWarriorDO;
import vip.appap.suxin.module.rongjh.service.WarriorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 战友会")
@RestController
@RequestMapping("/rongjh/warrior")
@Validated
public class WarriorController {

    @Resource
    private WarriorService warriorService;

    @PostMapping("/submit")
    @Operation(summary = "提交战友会/三属身份信息")
    public CommonResult<Boolean> submit(@Valid @RequestBody AppWarriorSubmitReqVO reqVO) {
        warriorService.submitWarrior(getLoginUserId(), reqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取战友会身份信息")
    public CommonResult<PartnerWarriorDO> get() {
        return success(warriorService.getWarrior(getLoginUserId()));
    }

    @GetMapping("/status")
    @Operation(summary = "获取战友会入会/申请状态及统计")
    @Parameter(name = "state_id", description = "省份区域编号，用于统计当前省成员数", example = "210000")
    public CommonResult<AppWarriorStatusRespVO> status(
            @RequestParam(value = "state_id", required = false) Integer stateId) {
        return success(warriorService.getWarriorStatus(getLoginUserId(), stateId));
    }

}
