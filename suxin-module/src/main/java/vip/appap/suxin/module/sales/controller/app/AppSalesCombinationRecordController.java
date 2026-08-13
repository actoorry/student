package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationRecordDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationRecordPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationRecordRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationRecordSummaryRespVO;
import vip.appap.suxin.module.sales.convert.SalesCombinationActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationRecordDO;
import vip.appap.suxin.module.sales.service.SalesCombinationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 拼团活动")
@RestController
@RequestMapping("/sales/promotion/combination-record")
@Validated
public class AppSalesCombinationRecordController {

    @Resource
    private SalesCombinationRecordService combinationRecordService;

    @GetMapping("/get-summary")
    @Operation(summary = "获得拼团记录的概要信息", description = "用于小程序首页")
    @PermitAll
    public CommonResult<AppSalesCombinationRecordSummaryRespVO> getCombinationRecordSummary() {
        AppSalesCombinationRecordSummaryRespVO summary = new AppSalesCombinationRecordSummaryRespVO();
        // 1. 获得拼团参与用户数量
        Long userCount = combinationRecordService.getCombinationUserCount();
        if (userCount == 0) {
            summary.setAvatars(Collections.emptyList());
            summary.setUserCount(userCount);
            return success(summary);
        }
        summary.setUserCount(userCount);

        // 2. 获得拼团记录头像
        List<SalesCombinationRecordDO> records = combinationRecordService.getLatestCombinationRecordList(
                AppSalesCombinationRecordSummaryRespVO.AVATAR_COUNT);
        summary.setAvatars(convertList(records, SalesCombinationRecordDO::getAvatar));
        return success(summary);
    }

    @GetMapping("/get-head-list")
    @Operation(summary = "获得最近 n 条拼团记录（团长发起的）")
    @Parameters({
            @Parameter(name = "activityId", description = "拼团活动编号"),
            @Parameter(name = "status", description = "拼团状态"), // 对应 SalesCombinationRecordStatusEnum 枚举
            @Parameter(name = "count", description = "数量")
    })
    @PermitAll
    public CommonResult<List<AppSalesCombinationRecordRespVO>> getHeadCombinationRecordList(
            @RequestParam(value = "activityId", required = false) Long activityId,
            @RequestParam("status") Integer status,
            @RequestParam(value = "count", defaultValue = "20") @Max(20) Integer count) {
        List<SalesCombinationRecordDO> list = combinationRecordService.getHeadCombinationRecordList(activityId, status, count);
        return success(BeanUtils.toBean(list, AppSalesCombinationRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得我的拼团记录分页")
    public CommonResult<PageResult<AppSalesCombinationRecordRespVO>> getCombinationRecordPage(
            @Valid AppSalesCombinationRecordPageReqVO pageReqVO) {
        PageResult<SalesCombinationRecordDO> pageResult = combinationRecordService.getCombinationRecordPage(
                getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppSalesCombinationRecordRespVO.class));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得拼团记录明细")
    @Parameter(name = "id", description = "拼团记录编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<AppSalesCombinationRecordDetailRespVO> getCombinationRecordDetail(@RequestParam("id") Long id) {
        // 1. 查找这条拼团记录
        SalesCombinationRecordDO record = combinationRecordService.getCombinationRecordById(id);
        if (record == null) {
            return success(null);
        }

        // 2. 查找该拼团的参团记录
        SalesCombinationRecordDO headRecord;
        List<SalesCombinationRecordDO> memberRecords;
        if (Objects.equals(record.getHeadId(), SalesCombinationRecordDO.HEAD_ID_GROUP)) { // 情况一：团长
            headRecord = record;
            memberRecords = combinationRecordService.getCombinationRecordListByHeadId(record.getId());
        } else { // 情况二：团员
            headRecord = combinationRecordService.getCombinationRecordById(record.getHeadId());
            memberRecords = combinationRecordService.getCombinationRecordListByHeadId(headRecord.getId());
        }

        // 3. 拼接数据
        return success(SalesCombinationActivityConvert.INSTANCE.convert(getLoginUserId(), headRecord, memberRecords));
    }

}
