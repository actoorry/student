package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.dict.core.DictFrameworkUtils;
import vip.appap.suxin.module.accountant.api.PayTransferApi;
import vip.appap.suxin.module.accountant.api.dto.PayTransferRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageWithdrawPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageWithdrawCreateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageWithdrawPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageWithdrawRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageWithdrawDO;
import vip.appap.suxin.module.sales.enums.DictTypeConstants;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawTypeEnum;
import vip.appap.suxin.module.sales.service.SalesBrokerageWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 分销提现")
@RestController
@RequestMapping("/sales/brokerage-withdraw")
@Validated
@Slf4j
public class AppSalesBrokerageWithdrawController {

    @Resource
    private SalesBrokerageWithdrawService brokerageWithdrawService;

    @Resource
    private PayTransferApi payTransferApi;

    @GetMapping("/page")
    @Operation(summary = "获得分销提现分页")
    public CommonResult<PageResult<AppSalesBrokerageWithdrawRespVO>> getBrokerageWithdrawPage(AppSalesBrokerageWithdrawPageReqVO pageReqVO) {
        PageResult<SalesBrokerageWithdrawDO> pageResult = brokerageWithdrawService.getBrokerageWithdrawPage(
                BeanUtils.toBean(pageReqVO, SalesBrokerageWithdrawPageReqVO.class).setUserId(getLoginUserId()));
        return success(BeanUtils.toBean(pageResult, AppSalesBrokerageWithdrawRespVO.class, withdrawVO ->
                withdrawVO.setTypeName(DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.BROKERAGE_WITHDRAW_TYPE, withdrawVO.getType()))
                        .setStatusName(DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.BROKERAGE_WITHDRAW_STATUS, withdrawVO.getStatus()))));
    }

    @GetMapping("/get")
    @Operation(summary = "获得佣金提现")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppSalesBrokerageWithdrawRespVO> getBrokerageWithdraw(@RequestParam("id") Long id) {
        SalesBrokerageWithdrawDO withdraw = brokerageWithdrawService.getBrokerageWithdraw(id);
        if (withdraw == null || ObjUtil.notEqual(withdraw.getUserId(), getLoginUserId())) {
            return success(null);
        }
        // 审核中（转账中），并且是微信转账，需要返回 mchId 用于确认收款
        AppSalesBrokerageWithdrawRespVO withdrawVO = BeanUtils.toBean(withdraw, AppSalesBrokerageWithdrawRespVO.class);
        if (Objects.equals(withdraw.getStatus(), SalesBrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus())
                && Objects.equals(withdraw.getType(), SalesBrokerageWithdrawTypeEnum.WECHAT_API.getType())
                && withdraw.getPayTransferId() != null) {
            PayTransferRespDTO transfer = payTransferApi.getTransfer(withdraw.getPayTransferId());
            if (transfer != null) {
                withdrawVO.setTransferChannelPackageInfo(transfer.getChannelPackageInfo())
                        .setTransferChannelMchId(transfer.getChannelMchId());
            }
        }
        return success(withdrawVO);
    }

    @PostMapping("/create")
    @Operation(summary = "创建分销提现")
    public CommonResult<Long> createBrokerageWithdraw(@RequestBody @Valid AppSalesBrokerageWithdrawCreateReqVO createReqVO) {
        return success(brokerageWithdrawService.createBrokerageWithdraw(getLoginUserId(), createReqVO));
    }

}

