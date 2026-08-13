package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationRecordPageItemRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationRecordReqPageVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationRecordSummaryVO;
import vip.appap.suxin.module.sales.convert.SalesCombinationActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationProductDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationRecordDO;
import vip.appap.suxin.module.sales.enums.SalesCombinationRecordStatusEnum;
import vip.appap.suxin.module.sales.service.SalesCombinationActivityService;
import vip.appap.suxin.module.sales.service.SalesCombinationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 拼团记录")
@RestController
@RequestMapping("/sales/promotion/combination-record")
@Validated
public class SalesCombinationRecordController {

    @Resource
    private SalesCombinationActivityService combinationActivityService;
    @Resource
    @Lazy
    private SalesCombinationRecordService combinationRecordService;

    @GetMapping("/page")
    @Operation(summary = "获得拼团记录分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_combination_record:query')")
    public CommonResult<PageResult<SalesCombinationRecordPageItemRespVO>> getCombinationRecordPage(
            @Valid SalesCombinationRecordReqPageVO pageVO) {
        PageResult<SalesCombinationRecordDO> recordPage = combinationRecordService.getCombinationRecordPage(pageVO);
        // 拼接数据
        List<SalesCombinationActivityDO> activities = combinationActivityService.getCombinationActivityListByIds(
                convertSet(recordPage.getList(), SalesCombinationRecordDO::getActivityId));
        List<SalesCombinationProductDO> products = combinationActivityService.getCombinationProductListByActivityIds(
                convertSet(recordPage.getList(), SalesCombinationRecordDO::getActivityId));
        return success(SalesCombinationActivityConvert.INSTANCE.convert(recordPage, activities, products));
    }

    @GetMapping("/get-summary")
    @Operation(summary = "获得拼团记录的概要信息", description = "用于拼团记录页面展示")
    @PreAuthorize("@ss.hasPermission('sales:sales_combination_record:query')")
    public CommonResult<SalesCombinationRecordSummaryVO> getCombinationRecordSummary() {
        SalesCombinationRecordSummaryVO summaryVO = new SalesCombinationRecordSummaryVO();
        summaryVO.setUserCount(combinationRecordService.getCombinationUserCount()); // 获取拼团用户参与数量
        summaryVO.setSuccessCount(combinationRecordService.getCombinationRecordCount( // 获取成团记录
                SalesCombinationRecordStatusEnum.SUCCESS.getStatus(), null, SalesCombinationRecordDO.HEAD_ID_GROUP));
        summaryVO.setVirtualGroupCount(combinationRecordService.getCombinationRecordCount(// 获取虚拟成团记录
                null, Boolean.TRUE, SalesCombinationRecordDO.HEAD_ID_GROUP));
        return success(summaryVO);
    }

}
