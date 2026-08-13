package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesBrokerageUserConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawStatusEnum;
import vip.appap.suxin.module.sales.service.SalesBrokerageRecordService;
import vip.appap.suxin.module.sales.service.SalesBrokerageUserService;
import vip.appap.suxin.module.sales.service.SalesBrokerageWithdrawService;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageWithdrawSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageUserSummaryRespBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static java.util.Arrays.asList;

@Tag(name = "管理后台 - 分销用户")
@RestController
@RequestMapping("/sales/brokerage-user")
@Validated
public class SalesBrokerageUserController {

    @Resource
    private SalesBrokerageUserService brokerageUserService;
    @Resource
    private SalesBrokerageRecordService brokerageRecordService;
    @Resource
    private SalesBrokerageWithdrawService brokerageWithdrawService;

    @Resource
    private PartnerApi PartnerApi;

    @PostMapping("/create")
    @Operation(summary = "创建分销用户")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_user:create')")
    public CommonResult<Long> createBrokerageUser(@Valid @RequestBody SalesBrokerageUserCreateReqVO createReqVO) {
        return success(brokerageUserService.createBrokerageUser(createReqVO));
    }

    @PutMapping("/update-bind-user")
    @Operation(summary = "修改推广员")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_user:update-bind-user')")
    public CommonResult<Boolean> updateBindUser(@Valid @RequestBody SalesBrokerageUserUpdateBrokerageUserReqVO updateReqVO) {
        brokerageUserService.updateBrokerageUserId(updateReqVO.getId(), updateReqVO.getBindUserId());
        return success(true);
    }

    @PutMapping("/clear-bind-user")
    @Operation(summary = "清除推广员")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_user:clear-bind-user')")
    public CommonResult<Boolean> clearBindUser(@Valid @RequestBody SalesBrokerageUserClearBrokerageUserReqVO updateReqVO) {
        brokerageUserService.updateBrokerageUserId(updateReqVO.getId(), null);
        return success(true);
    }

    @PutMapping("/update-brokerage-enable")
    @Operation(summary = "修改推广资格")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_user:update-brokerage-enable')")
    public CommonResult<Boolean> updateBrokerageEnabled(@Valid @RequestBody SalesBrokerageUserUpdateBrokerageEnabledReqVO updateReqVO) {
        brokerageUserService.updateBrokerageUserEnabled(updateReqVO.getId(), updateReqVO.getEnabled());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得分销用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_user:query')")
    public CommonResult<SalesBrokerageUserRespVO> getBrokerageUser(@RequestParam("id") Long id) {
        SalesBrokerageUserDO brokerageUser = brokerageUserService.getBrokerageUser(id);
        // TODO @疯狂：是不是搞成一个统一的 convert？
        SalesBrokerageUserRespVO respVO = SalesBrokerageUserConvert.INSTANCE.convert(brokerageUser);
        return success(SalesBrokerageUserConvert.INSTANCE.copyTo(PartnerApi.getUser(id), respVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得分销用户分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_user:query')")
    public CommonResult<PageResult<SalesBrokerageUserRespVO>> getBrokerageUserPage(@Valid SalesBrokerageUserPageReqVO pageVO) {
        // 分页查询
        PageResult<SalesBrokerageUserDO> pageResult = brokerageUserService.getBrokerageUserPage(pageVO);

        // 查询用户信息
        Set<Long> userIds = convertSet(pageResult.getList(), SalesBrokerageUserDO::getId);
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(userIds);
        // 合计分佣的推广订单
        Map<Long, SalesBrokerageUserSummaryRespBO> brokerageOrderSummaryMap = brokerageRecordService.getUserBrokerageSummaryMapByUserId(
                userIds, SalesBrokerageRecordBizTypeEnum.ORDER.getType(), SalesBrokerageRecordStatusEnum.SETTLEMENT.getStatus());
        // 合计分佣的推广用户
        // TODO @疯狂：转成 map 批量读取
        Map<Long, Long> brokerageUserCountMap = convertMap(userIds,
                userId -> userId,
                userId -> brokerageUserService.getBrokerageUserCountByBindUserId(userId, null));
        // 合计分佣的提现
        // TODO @疯狂：如果未来支持了打款这个动作，可能 status 会不对；
        Map<Long, SalesBrokerageWithdrawSummaryRespBO> withdrawMap = brokerageWithdrawService.getWithdrawSummaryMapByUserId(
                userIds, asList(SalesBrokerageWithdrawStatusEnum.AUDIT_SUCCESS, SalesBrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS));
        // 拼接返回
        return success(SalesBrokerageUserConvert.INSTANCE.convertPage(pageResult, userMap, brokerageUserCountMap,
                brokerageOrderSummaryMap, withdrawMap));
    }

}
