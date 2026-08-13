package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.codec.Base64;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.app.vo.*;
import vip.appap.suxin.module.sales.convert.SalesBrokerageRecordConvert;
import vip.appap.suxin.module.sales.convert.SalesBrokerageUserConvert;
import vip.appap.suxin.module.system.api.SocialClientApi;
import vip.appap.suxin.module.system.api.dto.SocialWxQrcodeReqDTO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawStatusEnum;
import vip.appap.suxin.module.sales.service.SalesBrokeragePosterService;
import vip.appap.suxin.module.sales.service.SalesBrokerageRecordService;
import vip.appap.suxin.module.sales.service.SalesBrokerageUserService;
import vip.appap.suxin.module.sales.service.SalesBrokerageWithdrawService;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageWithdrawSummaryRespBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static java.util.Arrays.asList;

@Tag(name = "用户 APP - 分销用户")
@RestController
@RequestMapping("/sales/brokerage-user")
@Validated
@Slf4j
public class AppSalesBrokerageUserController {

    @Resource
    private SalesBrokerageUserService brokerageUserService;
    @Resource
    private SalesBrokerageRecordService brokerageRecordService;
    @Resource
    private SalesBrokerageWithdrawService brokerageWithdrawService;
    @Resource
    private SalesBrokeragePosterService brokeragePosterService;
    @Resource
    private PartnerApi PartnerApi;
    @Resource
    private SocialClientApi socialClientApi;

    @GetMapping("/get")
    @Operation(summary = "获得个人分销信息")
    public CommonResult<AppSalesBrokerageUserRespVO> getBrokerageUser() {
        Optional<SalesBrokerageUserDO> user = Optional.ofNullable(brokerageUserService.getBrokerageUser(getLoginUserId()));
        // 返回数据
        AppSalesBrokerageUserRespVO respVO = new AppSalesBrokerageUserRespVO()
                .setBrokerageUserExists(user.isPresent())
                .setBrokerageEnabled(user.map(SalesBrokerageUserDO::getBrokerageEnabled).orElse(false))
                .setBrokeragePrice(user.map(SalesBrokerageUserDO::getBrokeragePrice).orElse(0))
                .setFrozenPrice(user.map(SalesBrokerageUserDO::getFrozenPrice).orElse(0));
        return success(respVO);
    }

    @PostMapping("/apply")
    @Operation(summary = "申请成为分销用户")
    public CommonResult<Boolean> applyBrokerageUser() {
        brokerageUserService.applyBrokerageUser(getLoginUserId());
        return success(true);
    }

    @PutMapping("/bind")
    @Operation(summary = "绑定推广员")
    public CommonResult<Boolean> bindBrokerageUser(@Valid @RequestBody AppSalesBrokerageUserBindReqVO reqVO) {
        return success(brokerageUserService.bindBrokerageUser(getLoginUserId(), reqVO.getBindUserId()));
    }

    @GetMapping("/get-summary")
    @Operation(summary = "获得个人分销统计")
    public CommonResult<AppSalesBrokerageUserMySummaryRespVO> getBrokerageUserSummary() {
        // 查询当前登录用户信息
        Long userId = getLoginUserId();
        SalesBrokerageUserDO brokerageUser = brokerageUserService.getBrokerageUser(userId);
        // 统计用户昨日的佣金
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(yesterday);
        LocalDateTime endTime = LocalDateTimeUtil.endOfDay(yesterday);
        Integer yesterdayPrice = brokerageRecordService.getSummaryPriceByUserId(userId,
                SalesBrokerageRecordBizTypeEnum.ORDER, SalesBrokerageRecordStatusEnum.SETTLEMENT, beginTime, endTime);
        // 统计用户提现的佣金
        Integer withdrawPrice = brokerageWithdrawService.getWithdrawSummaryListByUserId(Collections.singleton(userId),
                        asList(SalesBrokerageWithdrawStatusEnum.AUDIT_SUCCESS, SalesBrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS)).stream()
                .findFirst().map(SalesBrokerageWithdrawSummaryRespBO::getPrice).orElse(0);
        // 统计分销用户数量（一级）
        Long firstBrokerageUserCount = brokerageUserService.getBrokerageUserCountByBindUserId(userId, 1);
        // 统计分销用户数量（二级）
        Long secondBrokerageUserCount = brokerageUserService.getBrokerageUserCountByBindUserId(userId, 2);

        // 拼接返回
        return success(SalesBrokerageUserConvert.INSTANCE.convert(yesterdayPrice, withdrawPrice, firstBrokerageUserCount, secondBrokerageUserCount, brokerageUser));
    }

    @GetMapping("/rank-page-by-user-count")
    @Operation(summary = "获得分销用户排行分页（基于用户量）")
    public CommonResult<PageResult<AppSalesBrokerageUserRankByUserCountRespVO>> getBrokerageUserRankPageByUserCount(AppSalesBrokerageUserRankPageReqVO pageReqVO) {
        // 分页查询
        PageResult<AppSalesBrokerageUserRankByUserCountRespVO> pageResult = brokerageUserService.getBrokerageUserRankPageByUserCount(pageReqVO);
        // 拼接数据
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(convertSet(pageResult.getList(), AppSalesBrokerageUserRankByUserCountRespVO::getId));
        return success(SalesBrokerageUserConvert.INSTANCE.convertPage03(pageResult, userMap));
    }

    @GetMapping("/rank-page-by-price")
    @Operation(summary = "获得分销用户排行分页（基于佣金）")
    public CommonResult<PageResult<AppSalesBrokerageUserRankByPriceRespVO>> getBrokerageUserChildSummaryPageByPrice(AppSalesBrokerageUserRankPageReqVO pageReqVO) {
        // 分页查询
        PageResult<AppSalesBrokerageUserRankByPriceRespVO> pageResult = brokerageRecordService.getBrokerageUserChildSummaryPageByPrice(pageReqVO);
        // 拼接数据
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(convertSet(pageResult.getList(), AppSalesBrokerageUserRankByPriceRespVO::getId));
        return success(SalesBrokerageRecordConvert.INSTANCE.convertPage03(pageResult, userMap));
    }

    @GetMapping("/child-summary-page")
    @Operation(summary = "获得下级分销统计分页")
    public CommonResult<PageResult<AppSalesBrokerageUserChildSummaryRespVO>> getBrokerageUserChildSummaryPage(
            AppSalesBrokerageUserChildSummaryPageReqVO pageReqVO) {
        PageResult<AppSalesBrokerageUserChildSummaryRespVO> pageResult = brokerageUserService.getBrokerageUserChildSummaryPage(pageReqVO, getLoginUserId());
        return success(pageResult);
    }

    @GetMapping("/get-rank-by-price")
    @Operation(summary = "获得分销用户排行（基于佣金）")
    @Parameter(name = "times", description = "时间段", required = true)
    public CommonResult<Integer> getRankByPrice(
            @RequestParam("times") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime[] times) {
        return success(brokerageRecordService.getUserRankByPrice(getLoginUserId(), times));
    }

    @GetMapping("/generate-qrcode")
    @Operation(summary = "生成分销二维码")
    public CommonResult<String> generateQrcode() {
        Long userId = getLoginUserId();
        // 构建请求参数（分享后进入首页，scene 传递 bindUserId）
        SocialWxQrcodeReqDTO reqDTO = new SocialWxQrcodeReqDTO()
                .setScene("bindUserId=" + String.valueOf(userId))
                .setPath("pages/index/index")
                .setWidth(430)
                .setAutoColor(true)
                .setCheckPath(false)
                .setHyaline(true);
        // 调用微信接口生成小程序码
        byte[] qrcode = socialClientApi.getWxaQrcode(reqDTO);
        // 将字节数组转换为 Base64 字符串
        String base64 = Base64.encode(qrcode);
        return success(base64);
    }

    @GetMapping("/generate-poster")
    @Operation(summary = "生成分销海报")
    public CommonResult<String> generatePoster() {
        return success(brokeragePosterService.generateBrokeragePoster(getLoginUserId()));
    }

}
