package vip.appap.suxin.module.partner.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.ratelimiter.core.annotation.NaturalPeriod;
import vip.appap.suxin.framework.ratelimiter.core.annotation.NaturalRateLimit;
import vip.appap.suxin.framework.web.core.util.WebFrameworkUtils;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerNameCheckReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerNameCheckRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.appap.suxin.module.partner.service.PartnerCertificationRecordService;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

/**
 * App - 合作伙伴实名认证
 *
 * @author 书心软件
 */
@Tag(name = "App - 合作伙伴实名认证")
@RestController
@RequestMapping("/partner/name-check")
@Validated
public class AppPartnerNameCheckController {

    @Resource
    private PartnerCertificationRecordService partnerCertificationRecordService;

    /**
     * 实名认证
     *
     * 限流规则：每个用户每天最多 3 次（可通过配置文件 suxin.rate-limit.real-name.day-count 调整）
     */
    @NaturalRateLimit(
            period = NaturalPeriod.DAY,
            configKey = "suxin.rate-limit.real-name.day-count"
    )
    @PostMapping("/verify")
    @Operation(summary = "实名认证")
    public CommonResult<PartnerNameCheckRespVO> nameCheck(@Valid @RequestBody PartnerNameCheckReqVO reqVO) {
        Long partnerId = WebFrameworkUtils.getLoginUserId();
        return success(partnerCertificationRecordService.nameCheck(partnerId, reqVO));
    }
}
