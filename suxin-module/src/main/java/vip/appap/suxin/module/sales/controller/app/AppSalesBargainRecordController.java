package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainRecordCreateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainRecordDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainRecordRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainRecordSummaryRespVO;
import vip.appap.suxin.module.sales.convert.SalesBargainRecordConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainRecordDO;
import vip.appap.suxin.module.sales.enums.SalesBargainRecordStatusEnum;
import vip.appap.suxin.module.sales.service.SalesBargainActivityService;
import vip.appap.suxin.module.sales.service.SalesBargainHelpService;
import vip.appap.suxin.module.sales.service.SalesBargainRecordService;
import vip.appap.suxin.module.sales.api.SalesOrderApi;
import vip.appap.suxin.module.sales.api.dto.SalesOrderRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 砍价记录")
@RestController
@RequestMapping("/sales/promotion/bargain-record")
@Validated
public class AppSalesBargainRecordController {

    @Resource
    private SalesBargainHelpService bargainHelpService;
    @Resource
    private SalesBargainRecordService bargainRecordService;
    @Resource
    private SalesBargainActivityService bargainActivityService;

    @Resource
    private SalesOrderApi tradeOrderApi;
    @Resource
    private PartnerApi PartnerApi;
    @Resource
    private ProductSpuApi productSpuApi;

    @GetMapping("/get-summary")
    @Operation(summary = "获得砍价记录的概要信息", description = "用于小程序首页")
    @PermitAll
    public CommonResult<AppSalesBargainRecordSummaryRespVO> getBargainRecordSummary() {
        // 砍价成功的用户数量
        Integer successUserCount = bargainRecordService.getBargainRecordUserCount(
                SalesBargainRecordStatusEnum.SUCCESS.getStatus());
        if (successUserCount == 0) {
            return success(new AppSalesBargainRecordSummaryRespVO().setSuccessUserCount(0)
                    .setSuccessList(Collections.emptyList()));
        }
        // 砍价成功的用户列表
        List<SalesBargainRecordDO> successList = bargainRecordService.getBargainRecordList(
                SalesBargainRecordStatusEnum.SUCCESS.getStatus(), 7);
        List<SalesBargainActivityDO> activityList = bargainActivityService.getBargainActivityList(
                convertSet(successList, SalesBargainRecordDO::getActivityId));
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(
                convertSet(successList, SalesBargainRecordDO::getUserId));
        // 拼接返回
        return success(SalesBargainRecordConvert.INSTANCE.convert(successUserCount, successList, activityList, userMap));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得砍价记录的明细")
    @Parameters({
            @Parameter(name = "id", description = "砍价记录编号", example = "111"), // 场景一：查看指定的砍价记录
            @Parameter(name = "activityId", description = "砍价活动编号", example = "222") // 场景二：查看指定的砍价活动
    })
    @PermitAll
    public CommonResult<AppSalesBargainRecordDetailRespVO> getBargainRecordDetail(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "activityId", required = false) Long activityId) {
        // 1. 查询砍价记录 + 砍价活动
        Assert.isTrue(id != null || activityId != null, "砍价记录编号和活动编号不能同时为空");
        SalesBargainRecordDO record = id != null ? bargainRecordService.getBargainRecord(id)
                : bargainRecordService.getLastBargainRecord(getLoginUserId(), activityId);
        if (activityId == null || record != null) {
            activityId = record.getActivityId();
        }
        // 2. 查询助力记录
        Long userId = getLoginUserId();
        Integer helpAction = getHelpAction(userId, record, activityId);
        // 3. 如果是自己的订单，则查询订单信息
        SalesOrderRespDTO order = record != null && record.getOrderId() != null && record.getUserId().equals(getLoginUserId())
                ? tradeOrderApi.getOrder(record.getOrderId()) : null;
        // TODO 继续查询别的字段

        // 拼接返回
        return success(SalesBargainRecordConvert.INSTANCE.convert02(record, helpAction, order));
    }

    private Integer getHelpAction(Long userId, SalesBargainRecordDO record, Long activityId) {
        // 0.1 如果没有活动，无法帮砍
        if (activityId == null) {
            return null;
        }
        // 0.2 如果是自己的砍价记录，无法帮砍
        if (record != null && record.getUserId().equals(userId)) {
            return null;
        }

        // 1. 判断是否已经助力
        if (record != null
            && bargainHelpService.getBargainHelp(record.getId(), userId) != null) {
            return AppSalesBargainRecordDetailRespVO.HELP_ACTION_SUCCESS;
        }
        // 2. 判断是否满助力
        SalesBargainActivityDO activity = bargainActivityService.getBargainActivity(activityId);
        if (activity != null
            && bargainHelpService.getBargainHelpCountByActivity(activityId, userId) >= activity.getBargainCount()) {
            return AppSalesBargainRecordDetailRespVO.HELP_ACTION_FULL;
        }
        // 3. 允许助力
        return AppSalesBargainRecordDetailRespVO.HELP_ACTION_NONE;
    }

    @GetMapping("/page")
    @Operation(summary = "获得砍价记录的分页")
    public CommonResult<PageResult<AppSalesBargainRecordRespVO>> getBargainRecordPage(PageParam pageParam) {
        PageResult<SalesBargainRecordDO> pageResult = bargainRecordService.getBargainRecordPage(getLoginUserId(), pageParam);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        List<SalesBargainActivityDO> activityList = bargainActivityService.getBargainActivityList(
                convertSet(pageResult.getList(), SalesBargainRecordDO::getActivityId));
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(
                convertSet(pageResult.getList(), SalesBargainRecordDO::getSpuId));
        List<SalesOrderRespDTO> orderList = tradeOrderApi.getOrderList(
                convertSet(pageResult.getList(), SalesBargainRecordDO::getOrderId));
        return success(SalesBargainRecordConvert.INSTANCE.convertPage02(pageResult, activityList, spuList, orderList));
    }

    @PostMapping("/create")
    @Operation(summary = "创建砍价记录", description = "参与砍价活动")
    public CommonResult<Long> createBargainRecord(@RequestBody AppSalesBargainRecordCreateReqVO reqVO) {
        Long recordId = bargainRecordService.createBargainRecord(getLoginUserId(), reqVO);
        return success(recordId);
    }

}
