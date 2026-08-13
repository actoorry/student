package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInConfigRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInConfigSaveReqVO;
import vip.appap.suxin.module.partner.convert.PartnerSignInConfigConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInConfigDO;
import vip.appap.suxin.module.partner.service.PartnerSignInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员签到配置")
@RestController
@RequestMapping("/partner/sign-in-config")
@Validated
public class PartnerSignInConfigController {

    @Resource
    private PartnerSignInService signInService;

    @PutMapping("/save")
    @Operation(summary = "保存签到配置")
    @PreAuthorize("@ss.hasPermission('partner:sign-in-config:save')")
    public CommonResult<Boolean> saveSignInConfig(@Valid @RequestBody List<PartnerSignInConfigSaveReqVO> saveReqVOList) {
        signInService.saveSignInConfigList(saveReqVOList);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得签到配置列表")
    @PreAuthorize("@ss.hasPermission('partner:sign-in-config:query')")
    public CommonResult<List<PartnerSignInConfigRespVO>> getSignInConfigList() {
        List<PartnerSignInConfigDO> list = signInService.getSignInConfigList();
        return success(PartnerSignInConfigConvert.INSTANCE.convertList(list));
    }

}
