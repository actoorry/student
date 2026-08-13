package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainHelpCreateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainHelpRespVO;
import vip.appap.suxin.module.sales.convert.SalesBargainHelpConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainHelpDO;
import vip.appap.suxin.module.sales.service.SalesBargainHelpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 砍价助力")
@RestController
@RequestMapping("/sales/promotion/bargain-help")
@Validated
public class AppSalesBargainHelpController {

    @Resource
    private SalesBargainHelpService bargainHelpService;

    @Resource
    private PartnerApi PartnerApi;

    @PostMapping("/create")
    @Operation(summary = "创建砍价助力", description = "给拼团记录砍一刀") // 返回结果为砍价金额，单位：分
    public CommonResult<Integer> createBargainHelp(@RequestBody AppSalesBargainHelpCreateReqVO reqVO) {
        SalesBargainHelpDO help = bargainHelpService.createBargainHelp(getLoginUserId(), reqVO);
        return success(help.getReducePrice());
    }

    @GetMapping("/list")
    @Operation(summary = "获得砍价助力列表")
    @Parameter(name = "recordId", description = "砍价记录编号", required = true, example = "111")
    public CommonResult<List<AppSalesBargainHelpRespVO>> getBargainHelpList(@RequestParam("recordId") Long recordId) {
        List<SalesBargainHelpDO> helps = bargainHelpService.getBargainHelpListByRecordId(recordId);
        if (CollUtil.isEmpty(helps)) {
            return success(Collections.emptyList());
        }
        helps.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())); // 倒序展示

        // 拼接数据
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(
                convertSet(helps, SalesBargainHelpDO::getUserId));
        return success(SalesBargainHelpConvert.INSTANCE.convertList(helps, userMap));
    }

}
