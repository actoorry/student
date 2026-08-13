package vip.appap.suxin.module.partner.controller.admin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils;
import vip.appap.suxin.framework.common.util.number.NumberUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.*;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesPoolConfigDO;
import vip.appap.suxin.module.partner.service.PartnerSalesPoolConfigService;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.api.DeptApi;
import vip.appap.suxin.module.system.api.dto.DeptRespDTO;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static java.util.Collections.singletonList;

@Tag(name = "管理后台 - CRM 客户")
@RestController
@RequestMapping("/partner/sales")
@Validated
public class PartnerSalesController {

    @Resource
    private PartnerService partnerService;
    @Resource
    private PartnerSalesPoolConfigService partnerSalesPoolConfigService;

    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建客户")
    @PreAuthorize("@ss.hasPermission('partner:sales:create')")
    public CommonResult<Long> createPartnerSales(@Valid @RequestBody PartnerSalesSaveReqVO createReqVO) {
        return success(partnerService.createPartner(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户")
    @PreAuthorize("@ss.hasPermission('partner:sales:update')")
    public CommonResult<Boolean> updatePartnerSales(@Valid @RequestBody PartnerSalesSaveReqVO updateReqVO) {
        partnerService.updatePartner(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-deal-status")
    @Operation(summary = "更新客户的成交状态")
    @Parameters({
            @Parameter(name = "id", description = "客户编号", required = true),
            @Parameter(name = "dealStatus", description = "成交状态", required = true)
    })
    public CommonResult<Boolean> updateCustomerDealStatus(@RequestParam("id") Long id,
                                                          @RequestParam("dealStatus") Boolean dealStatus) {
        partnerService.updatePartnerDealStatus(id, dealStatus);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户")
    @Parameter(name = "id", description = "客户编号", required = true)
    @PreAuthorize("@ss.hasPermission('partner:sales:delete')")
    public CommonResult<Boolean> deletePartnerSales(@RequestParam("id") Long id) {
        partnerService.deletePartner(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('partner:sales:query')")
    public CommonResult<PartnerSalesRespVO> getPartnerSales(@RequestParam("id") Long id) {
        PartnerDO partner = partnerService.getPartner(id);
        return success(buildCustomerDetail(partner));
    }

    public PartnerSalesRespVO buildCustomerDetail(PartnerDO partner) {
        if (partner == null) {
            return null;
        }
        return buildCustomerDetailList(singletonList(partner)).get(0);
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户分页")
    @PreAuthorize("@ss.hasPermission('partner:sales:query')")
    public CommonResult<PageResult<PartnerSalesRespVO>> getPartnerSalesPage(@Valid PartnerSalesPageReqVO pageVO) {
        PageResult<PartnerDO> pageResult = partnerService.getPartnerSalesPage(pageVO, getLoginUserId());
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        return success(new PageResult<>(buildCustomerDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    /**
     * 构建客户详情列表（所有字段来自 partner 表，无需二次查询）
     */
    public List<PartnerSalesRespVO> buildCustomerDetailList(List<PartnerDO> list) {
        if (CollUtil.isEmpty(list)) {
            return java.util.Collections.emptyList();
        }
        // 1.1 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(list,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 1.2 获取距离进入公海的时间
        Map<Long, Long> poolDayMap = getPoolDayMap(list);
        // 2. 转换成 VO
        List<PartnerSalesRespVO> voList = BeanUtils.toBean(list, PartnerSalesRespVO.class, vo -> {
            vo.setAreaName(AreaUtils.format(vo.getAreaId()));
            // 设置创建人、负责人名称
            MapUtils.findAndThen(userMap, NumberUtils.parseLong(vo.getCreator()),
                    user -> vo.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, vo.getOwnerUserId(), user -> {
                vo.setOwnerUserName(user.getNickname());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> vo.setOwnerUserDeptName(dept.getName()));
            });
            // 设置距离进入公海的时间
            if (vo.getOwnerUserId() != null) {
                vo.setPoolDay(poolDayMap.get(vo.getId()));
            }
        });
        // 字段名映射：PartnerDO.salesLevel/salesSource → PartnerSalesRespVO.level/source
        for (int i = 0; i < voList.size(); i++) {
            voList.get(i).setLevel(list.get(i).getSalesLevel());
            voList.get(i).setSource(list.get(i).getSalesSource());
        }
        return voList;
    }

    @GetMapping("/put-pool-remind-page")
    @Operation(summary = "获得待进入公海客户分页")
    @PreAuthorize("@ss.hasPermission('partner:sales:query')")
    public CommonResult<PageResult<PartnerSalesRespVO>> getPutPoolRemindCustomerPage(@Valid PartnerSalesPageReqVO pageVO) {
        PageResult<PartnerDO> pageResult = partnerService.getPutPoolRemindPartnerPage(pageVO, getLoginUserId());
        return success(new PageResult<>(buildCustomerDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/put-pool-remind-count")
    @Operation(summary = "获得待进入公海客户数量")
    @PreAuthorize("@ss.hasPermission('partner:sales:query')")
    public CommonResult<Long> getPutPoolRemindCustomerCount() {
        return success(partnerService.getPutPoolRemindPartnerCount(getLoginUserId()));
    }

    @GetMapping("/today-contact-count")
    @Operation(summary = "获得今日需联系客户数量")
    @PreAuthorize("@ss.hasPermission('partner:sales:query')")
    public CommonResult<Long> getTodayContactCustomerCount() {
        return success(partnerService.getTodayContactPartnerCount(getLoginUserId()));
    }

    @GetMapping("/follow-count")
    @Operation(summary = "获得分配给我、待跟进的线索数量的客户数量")
    @PreAuthorize("@ss.hasPermission('partner:sales:query')")
    public CommonResult<Long> getFollowCustomerCount() {
        return success(partnerService.getFollowPartnerCount(getLoginUserId()));
    }

    private Map<Long, Long> getPoolDayMap(List<PartnerDO> list) {
        PartnerSalesPoolConfigDO poolConfig = partnerSalesPoolConfigService.getCustomerPoolConfig();
        if (poolConfig == null || !poolConfig.getEnabled()) {
            return MapUtil.empty();
        }
        list = CollectionUtils.filterList(list, customer -> {
            if (customer.getOwnerUserId() == null) {
                return false;
            }
            return !customer.getDealStatus() && !customer.getLockStatus();
        });
        return convertMap(list, PartnerDO::getId, customer -> {
            long dealExpireDay = poolConfig.getDealExpireDays() - LocalDateTimeUtils.between(customer.getOwnerTime());
            LocalDateTime lastTime = customer.getOwnerTime();
            if (customer.getContactLastTime() != null && customer.getContactLastTime().isAfter(lastTime)) {
                lastTime = customer.getContactLastTime();
            }
            long contactExpireDay = poolConfig.getContactExpireDays() - LocalDateTimeUtils.between(lastTime);
            long poolDay = Math.min(dealExpireDay, contactExpireDay);
            return poolDay > 0 ? poolDay : 0;
        });
    }

    @GetMapping(value = "/simple-list")
    @Operation(summary = "获取客户精简信息列表", description = "只包含有读权限的客户，主要用于前端的下拉选项")
    public CommonResult<List<PartnerSalesRespVO>> getCustomerSimpleList() {
        PartnerSalesPageReqVO reqVO = new PartnerSalesPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<PartnerDO> list = partnerService.getPartnerSalesPage(reqVO, getLoginUserId()).getList();
        return success(convertList(list, customer ->
                new PartnerSalesRespVO().setId(customer.getId()).setName(customer.getName())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出客户 Excel")
    @PreAuthorize("@ss.hasPermission('partner:sales:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCustomerExcel(@Valid PartnerSalesPageReqVO pageVO,
                                    HttpServletResponse response) throws IOException {
        pageVO.setPageSize(PAGE_SIZE_NONE);
        List<PartnerDO> list = partnerService.getPartnerSalesPage(pageVO, getLoginUserId()).getList();
        ExcelUtils.write(response, "客户.xls", "数据", PartnerSalesRespVO.class,
                buildCustomerDetailList(list));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入客户模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        List<PartnerSalesImportExcelVO> list = Arrays.asList(
                PartnerSalesImportExcelVO.builder().name("书心").industryId(1).level(1).source(1)
                        .mobile("15601691300").telephone("").qq("").wechat("").email("yunai@iocoder.cn")
                        .areaId(null).detailAddress("").remark("").build(),
                PartnerSalesImportExcelVO.builder().name("软件").industryId(1).level(1).source(1)
                        .mobile("15601691300").telephone("").qq("").wechat("").email("yunai@iocoder.cn")
                        .areaId(null).detailAddress("").remark("").build()
        );
        ExcelUtils.write(response, "客户导入模板.xls", "客户列表", PartnerSalesImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入客户")
    @PreAuthorize("@ss.hasPermission('partner:sales:import')")
    public CommonResult<PartnerSalesImportRespVO> importExcel(@Valid PartnerSalesImportReqVO importReqVO)
            throws Exception {
        List<PartnerSalesImportExcelVO> list = ExcelUtils.read(importReqVO.getFile(), PartnerSalesImportExcelVO.class);
        return success(partnerService.importPartnerList(list, importReqVO));
    }

    @PutMapping("/transfer")
    @Operation(summary = "转移客户")
    @PreAuthorize("@ss.hasPermission('partner:sales:update')")
    public CommonResult<Boolean> transferCustomer(@Valid @RequestBody PartnerSalesTransferReqVO reqVO) {
        partnerService.transferPartner(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/lock")
    @Operation(summary = "锁定/解锁客户")
    @PreAuthorize("@ss.hasPermission('partner:sales:update')")
    public CommonResult<Boolean> lockCustomer(@Valid @RequestBody PartnerSalesLockReqVO lockReqVO) {
        partnerService.lockPartner(lockReqVO, getLoginUserId());
        return success(true);
    }

    // ==================== 公海相关操作 ====================

    @PutMapping("/put-pool")
    @Operation(summary = "数据放入公海")
    @Parameter(name = "id", description = "客户编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('partner:sales:update')")
    public CommonResult<Boolean> putCustomerPool(@RequestParam("id") Long id) {
        partnerService.putPartnerPool(id);
        return success(true);
    }

    @PutMapping("/receive")
    @Operation(summary = "领取公海客户")
    @Parameter(name = "ids", description = "编号数组", required = true, example = "1,2,3")
    @PreAuthorize("@ss.hasPermission('partner:sales:receive')")
    public CommonResult<Boolean> receiveCustomer(@RequestParam(value = "ids") List<Long> ids) {
        partnerService.receivePartner(ids, getLoginUserId(), Boolean.TRUE);
        return success(true);
    }

    @PutMapping("/distribute")
    @Operation(summary = "分配公海给对应负责人")
    @PreAuthorize("@ss.hasPermission('partner:sales:distribute')")
    public CommonResult<Boolean> distributeCustomer(@Valid @RequestBody PartnerSalesDistributeReqVO distributeReqVO) {
        partnerService.receivePartner(distributeReqVO.getIds(), distributeReqVO.getOwnerUserId(), Boolean.FALSE);
        return success(true);
    }

}
