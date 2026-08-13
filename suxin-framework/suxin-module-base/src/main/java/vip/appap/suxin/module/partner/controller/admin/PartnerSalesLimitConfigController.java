package vip.appap.suxin.module.partner.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesLimitConfigPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesLimitConfigRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesLimitConfigSaveReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesLimitConfigDO;
import vip.appap.suxin.module.partner.service.PartnerSalesLimitConfigService;
import vip.appap.suxin.module.system.api.DeptApi;
import vip.appap.suxin.module.system.api.dto.DeptRespDTO;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - CRM 客户限制配置")
@RestController
@RequestMapping("/partner/sales-limit-config")
@Validated
public class PartnerSalesLimitConfigController {

    @Resource
    private PartnerSalesLimitConfigService partnerSalesLimitConfigService;

    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建客户限制配置")
    @PreAuthorize("@ss.hasPermission('partner:sales-limit-config:create')")
    public CommonResult<Long> createCustomerLimitConfig(@Valid @RequestBody PartnerSalesLimitConfigSaveReqVO createReqVO) {
        return success(partnerSalesLimitConfigService.createCustomerLimitConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户限制配置")
    @PreAuthorize("@ss.hasPermission('partner:sales-limit-config:update')")
    public CommonResult<Boolean> updateCustomerLimitConfig(@Valid @RequestBody PartnerSalesLimitConfigSaveReqVO updateReqVO) {
        partnerSalesLimitConfigService.updateCustomerLimitConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户限制配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('partner:sales-limit-config:delete')")
    public CommonResult<Boolean> deleteCustomerLimitConfig(@RequestParam("id") Long id) {
        partnerSalesLimitConfigService.deleteCustomerLimitConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户限制配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('partner:sales-limit-config:query')")
    public CommonResult<PartnerSalesLimitConfigRespVO> getCustomerLimitConfig(@RequestParam("id") Long id) {
        PartnerSalesLimitConfigDO limitConfig = partnerSalesLimitConfigService.getCustomerLimitConfig(id);
        // 拼接数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(limitConfig.getUserIds());
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(limitConfig.getDeptIds());
        return success(BeanUtils.toBean(limitConfig, PartnerSalesLimitConfigRespVO.class, configVO -> {
            configVO.setUsers(CollectionUtils.convertList(configVO.getUserIds(), userMap::get));
            configVO.setDepts(CollectionUtils.convertList(configVO.getDeptIds(), deptMap::get));
        }));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户限制配置分页")
    @PreAuthorize("@ss.hasPermission('partner:sales-limit-config:query')")
    public CommonResult<PageResult<PartnerSalesLimitConfigRespVO>> getCustomerLimitConfigPage(@Valid PartnerSalesLimitConfigPageReqVO pageVO) {
        PageResult<PartnerSalesLimitConfigDO> pageResult = partnerSalesLimitConfigService.getCustomerLimitConfigPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 拼接数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(pageResult.getList(), PartnerSalesLimitConfigDO::getUserIds, Collection::stream));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSetByFlatMap(pageResult.getList(), PartnerSalesLimitConfigDO::getDeptIds, Collection::stream));
        return success(BeanUtils.toBean(pageResult, PartnerSalesLimitConfigRespVO.class, configVO -> {
            configVO.setUsers(CollectionUtils.convertList(configVO.getUserIds(), userMap::get));
            configVO.setDepts(CollectionUtils.convertList(configVO.getDeptIds(), deptMap::get));
        }));
    }

}
