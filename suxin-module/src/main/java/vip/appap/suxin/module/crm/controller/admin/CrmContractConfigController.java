package vip.appap.suxin.module.crm.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContractConfigRespVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContractConfigSaveReqVO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmContractConfigDO;
import vip.appap.suxin.module.crm.service.CrmContractConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - CRM 合同配置")
@RestController
@RequestMapping("/crm/contract-config")
@Validated
public class CrmContractConfigController {

    @Resource
    private CrmContractConfigService contractConfigService;

    @GetMapping("/get")
    @Operation(summary = "获取合同配置")
    @PreAuthorize("@ss.hasPermission('crm:contract-config:query')")
    public CommonResult<CrmContractConfigRespVO> getCustomerPoolConfig() {
        CrmContractConfigDO config = contractConfigService.getContractConfig();
        return success(BeanUtils.toBean(config, CrmContractConfigRespVO.class));
    }

    @PutMapping("/save")
    @Operation(summary = "更新合同配置")
    @PreAuthorize("@ss.hasPermission('crm:contract-config:update')")
    public CommonResult<Boolean> saveCustomerPoolConfig(@Valid @RequestBody CrmContractConfigSaveReqVO updateReqVO) {
        contractConfigService.saveContractConfig(updateReqVO);
        return success(true);
    }

}
