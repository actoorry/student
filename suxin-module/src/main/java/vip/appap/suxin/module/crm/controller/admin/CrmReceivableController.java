package vip.appap.suxin.module.crm.controller.admin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.number.NumberUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContractRespVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmReceivablePageReqVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmReceivableRespVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmReceivableSaveReqVO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmContractDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmReceivableDO;
import vip.appap.suxin.module.crm.service.CrmContractService;
import vip.appap.suxin.module.crm.service.CrmReceivableService;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.api.DeptApi;
import vip.appap.suxin.module.system.api.dto.DeptRespDTO;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - CRM 回款")
@RestController
@RequestMapping("/crm/receivable")
@Validated
public class CrmReceivableController {

    @Resource
    private CrmReceivableService receivableService;
    @Resource
    private CrmContractService contractService;
    @Resource
    private PartnerService partnerService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建回款")
    @PreAuthorize("@ss.hasPermission('crm:receivable:create')")
    public CommonResult<Long> createReceivable(@Valid @RequestBody CrmReceivableSaveReqVO createReqVO) {
        return success(receivableService.createReceivable(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新回款")
    @PreAuthorize("@ss.hasPermission('crm:receivable:update')")
    public CommonResult<Boolean> updateReceivable(@Valid @RequestBody CrmReceivableSaveReqVO updateReqVO) {
        receivableService.updateReceivable(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除回款")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:receivable:delete')")
    public CommonResult<Boolean> deleteReceivable(@RequestParam("id") Long id) {
        receivableService.deleteReceivable(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得回款")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:receivable:query')")
    public CommonResult<CrmReceivableRespVO> getReceivable(@RequestParam("id") Long id) {
        CrmReceivableDO receivable = receivableService.getReceivable(id);
        return success(buildReceivableDetail(receivable));
    }

    private CrmReceivableRespVO buildReceivableDetail(CrmReceivableDO receivable) {
        if (receivable == null) {
            return null;
        }
        return buildReceivableDetailList(Collections.singletonList(receivable)).get(0);
    }

    @GetMapping("/page")
    @Operation(summary = "获得回款分页")
    @PreAuthorize("@ss.hasPermission('crm:receivable:query')")
    public CommonResult<PageResult<CrmReceivableRespVO>> getReceivablePage(@Valid CrmReceivablePageReqVO pageReqVO) {
        PageResult<CrmReceivableDO> pageResult = receivableService.getReceivablePage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildReceivableDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得回款分页，基于指定客户")
    public CommonResult<PageResult<CrmReceivableRespVO>> getReceivablePageByCustomer(@Valid CrmReceivablePageReqVO pageReqVO) {
        Assert.notNull(pageReqVO.getCustomerId(), "客户编号不能为空");
        PageResult<CrmReceivableDO> pageResult = receivableService.getReceivablePageByCustomerId(pageReqVO);
        return success(new PageResult<>(buildReceivableDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出回款 Excel")
    @PreAuthorize("@ss.hasPermission('crm:receivable:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReceivableExcel(@Valid CrmReceivablePageReqVO exportReqVO,
                                      HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PAGE_SIZE_NONE);
        List<CrmReceivableDO> list = receivableService.getReceivablePage(exportReqVO, getLoginUserId()).getList();
        // 导出 Excel
        ExcelUtils.write(response, "回款.xls", "数据", CrmReceivableRespVO.class,
                buildReceivableDetailList(list));
    }

    private List<CrmReceivableRespVO> buildReceivableDetailList(List<CrmReceivableDO> receivableList) {
        if (CollUtil.isEmpty(receivableList)) {
            return Collections.emptyList();
        }
        // 1.1 获取客户列表
        Map<Long, PartnerDO> customerPartnerMap = partnerService.getPartnerMap(
                convertSet(receivableList, CrmReceivableDO::getCustomerId));
        // 1.2 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(receivableList,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 1.3 获得合同列表
        Map<Long, CrmContractDO> contractMap = contractService.getContractMap(
                convertSet(receivableList, CrmReceivableDO::getContractId));
        // 2. 拼接结果
        return BeanUtils.toBean(receivableList, CrmReceivableRespVO.class, (receivableVO) -> {
            // 2.1 拼接客户名称
            findAndThen(customerPartnerMap, receivableVO.getCustomerId(), partner -> receivableVO.setCustomerName(partner.getName()));
            // 2.2 拼接负责人、创建人名称
            MapUtils.findAndThen(userMap, NumberUtils.parseLong(receivableVO.getCreator()),
                    user -> receivableVO.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, receivableVO.getOwnerUserId(), user -> {
                receivableVO.setOwnerUserName(user.getNickname());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> receivableVO.setOwnerUserDeptName(dept.getName()));
            });
            // 2.3 拼接合同信息
            findAndThen(contractMap, receivableVO.getContractId(), contract ->
                    receivableVO.setContract(BeanUtils.toBean(contract, CrmContractRespVO.class)));
        });
    }

    @PutMapping("/submit")
    @Operation(summary = "提交回款审批")
    @PreAuthorize("@ss.hasPermission('crm:receivable:update')")
    public CommonResult<Boolean> submitContract(@RequestParam("id") Long id) {
        receivableService.submitReceivable(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/audit-count")
    @Operation(summary = "获得待审核回款数量")
    @PreAuthorize("@ss.hasPermission('crm:receivable:query')")
    public CommonResult<Long> getAuditReceivableCount() {
        return success(receivableService.getAuditReceivableCount(getLoginUserId()));
    }

}
