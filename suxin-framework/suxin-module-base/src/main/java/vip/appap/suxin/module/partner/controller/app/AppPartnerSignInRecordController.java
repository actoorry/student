package vip.appap.suxin.module.partner.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerSignInRecordRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerSignInSummaryRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInRecordDO;
import vip.appap.suxin.module.partner.service.PartnerSignInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 签到记录")
@RestController
@RequestMapping("/partner/sign-in/record")
@Validated
public class AppPartnerSignInRecordController {

    @Resource
    private PartnerSignInService signInService;

    @PostMapping("/create")
    @Operation(summary = "签到")
    public CommonResult<AppPartnerSignInRecordRespVO> createSignInRecord() {
        PartnerSignInRecordDO record = signInService.signIn(getLoginUserId());
        return success(BeanUtils.toBean(record, AppPartnerSignInRecordRespVO.class));
    }

    @GetMapping("/get-today")
    @Operation(summary = "获得今日签到记录")
    public CommonResult<AppPartnerSignInRecordRespVO> getTodaySignInRecord() {
        PartnerSignInRecordDO record = signInService.getTodaySignInRecord(getLoginUserId());
        return success(BeanUtils.toBean(record, AppPartnerSignInRecordRespVO.class));
    }

    @GetMapping("/get-summary")
    @Operation(summary = "获得签到统计")
    public CommonResult<AppPartnerSignInSummaryRespVO> getSignInRecordSummary() {
        return success(signInService.getSignInSummary(getLoginUserId()));
    }

}
