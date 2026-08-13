package vip.appap.suxin.module.partner.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.ratelimiter.core.annotation.NaturalPeriod;
import vip.appap.suxin.framework.ratelimiter.core.annotation.NaturalRateLimit;
import vip.appap.suxin.framework.web.core.util.WebFrameworkUtils;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerMarriageCheckReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerMarriageCheckRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerMarriageCheckStatusRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.appap.suxin.module.partner.service.PartnerCertificationRecordService;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

/**
 * App - 合作伙伴婚姻认证
 *
 * @author 书心软件
 */
@Tag(name = "App - 合作伙伴婚姻认证")
@RestController
@RequestMapping("/partner/marriage-check")
@Validated
public class AppPartnerMarriageCheckController {

    @Resource
    private PartnerCertificationRecordService partnerCertificationRecordService;

    /**
     * 婚姻认证
     *
     * 限流规则：每个用户每月最多 1 次（可通过配置文件 suxin.rate-limit.marriage.month-count 调整）
     */
    @NaturalRateLimit(
            period = NaturalPeriod.MONTH,
            count = 1,
            configKey = "suxin.rate-limit.marriage.month-count"
    )
    @PostMapping("/verify")
    @Operation(summary = "婚姻认证")
    public CommonResult<PartnerMarriageCheckRespVO> marriageCheck(@Valid @RequestBody PartnerMarriageCheckReqVO reqVO) {
        Long partnerId = WebFrameworkUtils.getLoginUserId();
        return success(partnerCertificationRecordService.marriageCheck(partnerId, reqVO));
    }

    @GetMapping("/status")
    @Operation(summary = "获取婚姻认证结果及本月额度状态")
    public CommonResult<PartnerMarriageCheckStatusRespVO> getMarriageCheckStatus() {
        Long partnerId = WebFrameworkUtils.getLoginUserId();
        return success(partnerCertificationRecordService.getMarriageCheckStatus(partnerId));
    }

}
