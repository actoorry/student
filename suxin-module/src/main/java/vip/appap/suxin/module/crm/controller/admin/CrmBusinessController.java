package vip.appap.suxin.module.crm.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.number.NumberUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.crm.controller.admin.vo.*;
import vip.appap.suxin.module.crm.dal.dataobject.CrmBusinessDO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmBusinessProductDO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmBusinessStatusDO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmBusinessStatusTypeDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.crm.service.CrmBusinessService;
import vip.appap.suxin.module.crm.service.CrmBusinessStatusService;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.dal.mysql.ProductSkuMapper;
import vip.appap.suxin.module.product.dal.mysql.ProductSpuMapper;
import vip.appap.suxin.module.product.dal.mysql.ProductUnitMapper;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static vip.appap.suxin.module.crm.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS;

@Tag(name = "管理后台 - CRM 商机")
@RestController
@RequestMapping("/crm/business")
@Validated
public class CrmBusinessController {

    @Resource
    private CrmBusinessService businessService;
    @Resource
    private PartnerService partnerService;
    @Resource
    private CrmBusinessStatusService businessStatusTypeService;
    @Resource
    private CrmBusinessStatusService businessStatusService;

    @Resource
    private ProductSkuMapper skuMapper;
    @Resource
    private ProductSpuMapper spuMapper;
    @Resource
    private ProductUnitMapper unitMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建商机")
    @PreAuthorize("@ss.hasPermission('crm:business:create')")
    public CommonResult<Long> createBusiness(@Valid @RequestBody CrmBusinessSaveReqVO createReqVO) {
        return success(businessService.createBusiness(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商机")
    @PreAuthorize("@ss.hasPermission('crm:business:update')")
    public CommonResult<Boolean> updateBusiness(@Valid @RequestBody CrmBusinessSaveReqVO updateReqVO) {
        businessService.updateBusiness(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新商机状态")
    @PreAuthorize("@ss.hasPermission('crm:business:update')")
    public CommonResult<Boolean> updateBusinessStatus(@Valid @RequestBody CrmBusinessUpdateStatusReqVO updateStatusReqVO) {
        businessService.updateBusinessStatus(updateStatusReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商机")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:business:delete')")
    public CommonResult<Boolean> deleteBusiness(@RequestParam("id") Long id) {
        businessService.deleteBusiness(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商机")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<CrmBusinessRespVO> getBusiness(@RequestParam("id") Long id) {
        CrmBusinessDO business = businessService.getBusiness(id);
        return success(buildBusinessDetail(business));
    }

    private CrmBusinessRespVO buildBusinessDetail(CrmBusinessDO business) {
        if (business == null) {
            return null;
        }
        CrmBusinessRespVO businessVO = buildBusinessDetailList(Collections.singletonList(business)).get(0);
        // 拼接产品项
        List<CrmBusinessProductDO> businessProducts = businessService.getBusinessProductListByBusinessId(businessVO.getId());
        // 批量查询 SKU、SPU、单位信息
        Set<Long> skuIds = convertSet(businessProducts, CrmBusinessProductDO::getSkuId);
        List<ProductSkuDO> skuList = skuMapper.selectByIds(skuIds);
        Map<Long, ProductSkuDO> skuMap = convertMap(skuList, ProductSkuDO::getId);
        Set<Long> spuIds = skuList.stream().map(ProductSkuDO::getSpuId).collect(Collectors.toSet());
        Map<Long, ProductSpuDO> spuMap = convertMap(spuMapper.selectByIds(spuIds), ProductSpuDO::getId);
        Set<Long> unitIds = spuMap.values().stream().map(ProductSpuDO::getUnitId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ProductUnitDO> unitMap = convertMap(unitMapper.selectByIds(unitIds), ProductUnitDO::getId);

        businessVO.setProducts(BeanUtils.toBean(businessProducts, CrmBusinessRespVO.Product.class, businessProductVO ->
                MapUtils.findAndThen(skuMap, businessProductVO.getSkuId(), sku -> {
                    ProductSpuDO spu = spuMap.get(sku.getSpuId());
                    businessProductVO.setProductName(spu != null ? spu.getName() : null)
                            .setProductNo(sku.getBarCode());
                    if (spu != null && spu.getUnitId() != null) {
                        ProductUnitDO unit = unitMap.get(spu.getUnitId());
                        businessProductVO.setProductUnitName(unit != null ? unit.getName() : null);
                    }
                })));
        return businessVO;
    }

    @GetMapping("/simple-all-list")
    @Operation(summary = "获得商机的精简列表")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<List<CrmBusinessRespVO>> getSimpleContactList() {
        CrmBusinessPageReqVO reqVO = new CrmBusinessPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE); // 不分页
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPage(reqVO, getLoginUserId());
        return success(convertList(pageResult.getList(), business -> // 只返回 id、name 字段
                new CrmBusinessRespVO().setId(business.getId()).setName(business.getName())
                        .setCustomerId(business.getCustomerId())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商机分页")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPage(@Valid CrmBusinessPageReqVO pageVO) {
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPage(pageVO, getLoginUserId());
        return success(new PageResult<>(buildBusinessDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得商机分页，基于指定客户")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPageByCustomer(@Valid CrmBusinessPageReqVO pageReqVO) {
        if (pageReqVO.getCustomerId() == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPageByCustomerId(pageReqVO);
        return success(new PageResult<>(buildBusinessDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/page-by-contact")
    @Operation(summary = "获得联系人的商机分页")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessContactPage(@Valid CrmBusinessPageReqVO pageReqVO) {
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPageByContact(pageReqVO);
        return success(new PageResult<>(buildBusinessDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商机 Excel")
    @PreAuthorize("@ss.hasPermission('crm:business:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportBusinessExcel(@Valid CrmBusinessPageReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PAGE_SIZE_NONE);
        List<CrmBusinessDO> list = businessService.getBusinessPage(exportReqVO, getLoginUserId()).getList();
        // 导出 Excel
        ExcelUtils.write(response, "商机.xls", "数据", CrmBusinessRespVO.class,
                buildBusinessDetailList(list));
    }

    public List<CrmBusinessRespVO> buildBusinessDetailList(List<CrmBusinessDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.1 获取客户列表
        Map<Long, PartnerDO> customerPartnerMap = partnerService.getPartnerMap(
                convertSet(list, CrmBusinessDO::getCustomerId));
        // 1.2 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(list,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 1.3 获得商机状态组
        Map<Long, CrmBusinessStatusTypeDO> statusTypeMap = businessStatusTypeService.getBusinessStatusTypeMap(
                convertSet(list, CrmBusinessDO::getStatusTypeId));
        Map<Long, CrmBusinessStatusDO> statusMap = businessStatusService.getBusinessStatusMap(
                convertSet(list, CrmBusinessDO::getStatusId));
        // 2. 拼接数据
        return BeanUtils.toBean(list, CrmBusinessRespVO.class, businessVO -> {
            // 2.1 设置客户名称
            MapUtils.findAndThen(customerPartnerMap, businessVO.getCustomerId(), partner -> businessVO.setCustomerName(partner.getName()));
            // 2.2 设置创建人、负责人名称
            MapUtils.findAndThen(userMap, NumberUtils.parseLong(businessVO.getCreator()),
                    user -> businessVO.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, businessVO.getOwnerUserId(), user -> {
                businessVO.setOwnerUserName(user.getNickname());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> businessVO.setOwnerUserDeptName(dept.getName()));
            });
            // 2.3 设置商机状态
            MapUtils.findAndThen(statusTypeMap, businessVO.getStatusTypeId(), statusType -> businessVO.setStatusTypeName(statusType.getName()));
            MapUtils.findAndThen(statusMap, businessVO.getStatusId(), status -> businessVO.setStatusName(
                    businessService.getBusinessStatusName(businessVO.getEndStatus(), status)));
        });
    }

    @PutMapping("/transfer")
    @Operation(summary = "商机转移")
    @PreAuthorize("@ss.hasPermission('crm:business:update')")
    public CommonResult<Boolean> transferBusiness(@Valid @RequestBody CrmBusinessTransferReqVO reqVO) {
        businessService.transferBusiness(reqVO, getLoginUserId());
        return success(true);
    }

}
