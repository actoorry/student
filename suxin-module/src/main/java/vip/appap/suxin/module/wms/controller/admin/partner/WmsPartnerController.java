package vip.appap.suxin.module.wms.controller.admin.partner;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;
import java.util.stream.Collectors;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.wms.service.partner.WmsPartnerService;

@Tag(name = "管理后台 - WMS 客商查询（跨模块调用）")
@RestController
@RequestMapping("/wms/partner")
@Validated
public class WmsPartnerController {

    @Resource
    private WmsPartnerService wmsPartnerService;

    @GetMapping("/supplier-list")
    @Operation(summary = "获取供应商列表（下拉选择用）")
    @PreAuthorize("@ss.hasPermission('wms:order:query')")
    public CommonResult<List<Map<String, Object>>> getSupplierList() {
        List<PartnerRespDTO> list = wmsPartnerService.getSupplierList();
        return success(convertToSelect(list));
    }

    @GetMapping("/customer-list")
    @Operation(summary = "获取客户列表（下拉选择用）")
    @PreAuthorize("@ss.hasPermission('wms:order:query')")
    public CommonResult<List<Map<String, Object>>> getCustomerList() {
        List<PartnerRespDTO> list = wmsPartnerService.getCustomerList();
        return success(convertToSelect(list));
    }

    @GetMapping("/company-list")
    @Operation(summary = "获取公司列表（仓库负责人下拉选择用，仅筛选 isCompany=true）")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:query')")
    public CommonResult<List<Map<String, Object>>> getCompanyList() {
        List<PartnerRespDTO> list = wmsPartnerService.getCompanyList();
        return success(convertToSelect(list));
    }

    @GetMapping("/get")
    @Operation(summary = "查询客商信息")
    @PreAuthorize("@ss.hasPermission('wms:order:query')")
    public CommonResult<PartnerRespDTO> getPartner(@RequestParam("id") Long id) {
        return success(wmsPartnerService.getPartner(id));
    }

    /**
     * 转换为下拉选择器格式：{id, name}
     */
    private List<Map<String, Object>> convertToSelect(List<PartnerRespDTO> list) {
        return list.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getNickname());
            return item;
        }).collect(Collectors.toList());
    }

}