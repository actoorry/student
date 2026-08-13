package vip.appap.suxin.module.partner.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerSignInConfigRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInConfigDO;
import vip.appap.suxin.module.partner.service.PartnerSignInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 签到配置")
@RestController
@RequestMapping("/partner/sign-in/config")
@Validated
public class AppPartnerSignInConfigController {

    @Resource
    private PartnerSignInService signInService;

    @GetMapping("/list")
    @Operation(summary = "获得启用签到配置列表")
    public CommonResult<List<AppPartnerSignInConfigRespVO>> getSignInConfigList() {
        List<PartnerSignInConfigDO> list = signInService.getEnabledSignInConfigList();
        return success(BeanUtils.toBean(list, AppPartnerSignInConfigRespVO.class));
    }

}
