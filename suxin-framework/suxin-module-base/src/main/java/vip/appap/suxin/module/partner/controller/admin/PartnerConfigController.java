package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.tenant.core.context.TenantContextHolder;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerConfigRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerConfigSaveReqVO;
import vip.appap.suxin.module.system.api.dto.TenantPointTradeConfigRespDTO;
import vip.appap.suxin.module.system.service.TenantConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员配置")
@RestController
@RequestMapping("/partner/config")
@Validated
public class PartnerConfigController {

    @Resource
    private TenantConfigService tenantConfigService;

    @PutMapping("/save")
    @Operation(summary = "保存会员配置")
    @PreAuthorize("@ss.hasPermission('partner:config:save')")
    public CommonResult<Boolean> saveConfig(@Valid @RequestBody PartnerConfigSaveReqVO saveReqVO) {
        TenantPointTradeConfigRespDTO config = new TenantPointTradeConfigRespDTO();
        config.setPointTradeDeductEnable(saveReqVO.getPointTradeDeductEnable());
        config.setPointTradeDeductUnitPrice(saveReqVO.getPointTradeDeductUnitPrice());
        config.setPointTradeDeductMaxPrice(saveReqVO.getPointTradeDeductMaxPrice());
        config.setPointTradeGivePoint(saveReqVO.getPointTradeGivePoint());
        tenantConfigService.saveConfig(config);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员配置")
    @PreAuthorize("@ss.hasPermission('partner:config:query')")
    public CommonResult<PartnerConfigRespVO> getConfig() {
        TenantPointTradeConfigRespDTO config = tenantConfigService.getConfig();
        PartnerConfigRespVO respVO = new PartnerConfigRespVO();
        respVO.setId(TenantContextHolder.getRequiredTenantId());
        respVO.setPointTradeDeductEnable(config.getPointTradeDeductEnable());
        respVO.setPointTradeDeductUnitPrice(config.getPointTradeDeductUnitPrice());
        respVO.setPointTradeDeductMaxPrice(config.getPointTradeDeductMaxPrice());
        respVO.setPointTradeGivePoint(config.getPointTradeGivePoint());
        return success(respVO);
    }

}
