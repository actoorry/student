package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesPoolConfigRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesPoolConfigSaveReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesPoolConfigDO;
import vip.appap.suxin.module.partner.service.PartnerSalesPoolConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - CRM 客户公海配置")
@RestController
@RequestMapping("/partner/sales-pool-config")
@Validated
public class PartnerSalesPoolConfigController {

    @Resource
    private PartnerSalesPoolConfigService partnerSalesPoolConfigService;

    @GetMapping("/get")
    @Operation(summary = "获取客户公海规则设置")
    @PreAuthorize("@ss.hasPermission('partner:sales-pool-config:query')")
    public CommonResult<PartnerSalesPoolConfigRespVO> getCustomerPoolConfig() {
        PartnerSalesPoolConfigDO poolConfig = partnerSalesPoolConfigService.getCustomerPoolConfig();
        return success(BeanUtils.toBean(poolConfig, PartnerSalesPoolConfigRespVO.class));
    }

    @PutMapping("/save")
    @Operation(summary = "更新客户公海规则设置")
    @PreAuthorize("@ss.hasPermission('partner:sales-pool-config:update')")
    public CommonResult<Boolean> saveCustomerPoolConfig(@Valid @RequestBody PartnerSalesPoolConfigSaveReqVO updateReqVO) {
        partnerSalesPoolConfigService.saveCustomerPoolConfig(updateReqVO);
        return success(true);
    }

}
