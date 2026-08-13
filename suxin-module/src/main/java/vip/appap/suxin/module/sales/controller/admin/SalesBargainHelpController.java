package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainHelpPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainHelpRespVO;
import vip.appap.suxin.module.sales.convert.SalesBargainHelpConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainHelpDO;
import vip.appap.suxin.module.sales.service.SalesBargainHelpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 砍价助力")
@RestController
@RequestMapping("/sales/promotion/bargain-help")
@Validated
public class SalesBargainHelpController {

    @Resource
    private SalesBargainHelpService bargainHelpService;

    @Resource
    private PartnerApi PartnerApi;

    @GetMapping("/page")
    @Operation(summary = "获得砍价助力分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_bargain_help:query')")
    public CommonResult<PageResult<SalesBargainHelpRespVO>> getBargainHelpPage(@Valid SalesBargainHelpPageReqVO pageVO) {
        PageResult<SalesBargainHelpDO> pageResult = bargainHelpService.getBargainHelpPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(
                convertSet(pageResult.getList(), SalesBargainHelpDO::getUserId));
        return success(SalesBargainHelpConvert.INSTANCE.convertPage(pageResult, userMap));
    }

}
