package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainRecordPageItemRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainRecordPageReqVO;
import vip.appap.suxin.module.sales.convert.SalesBargainRecordConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainRecordDO;
import vip.appap.suxin.module.sales.service.SalesBargainActivityService;
import vip.appap.suxin.module.sales.service.SalesBargainHelpService;
import vip.appap.suxin.module.sales.service.SalesBargainRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 砍价记录")
@RestController
@RequestMapping("/sales/promotion/bargain-record")
@Validated
public class SalesBargainRecordController {

    @Resource
    private SalesBargainRecordService bargainRecordService;
    @Resource
    private SalesBargainActivityService bargainActivityService;
    @Resource
    private SalesBargainHelpService bargainHelpService;

    @Resource
    private PartnerApi PartnerApi;

    @GetMapping("/page")
    @Operation(summary = "获得砍价记录分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_bargain_record:query')")
    public CommonResult<PageResult<SalesBargainRecordPageItemRespVO>> getBargainRecordPage(@Valid SalesBargainRecordPageReqVO pageVO) {
        PageResult<SalesBargainRecordDO> pageResult = bargainRecordService.getBargainRecordPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(
                convertSet(pageResult.getList(), SalesBargainRecordDO::getUserId));
        List<SalesBargainActivityDO> activityList = bargainActivityService.getBargainActivityList(
                convertSet(pageResult.getList(), SalesBargainRecordDO::getActivityId));
        Map<Long, Integer> helpCountMap = bargainHelpService.getBargainHelpUserCountMapByRecord(
                convertSet(pageResult.getList(), SalesBargainRecordDO::getId));
        return success(SalesBargainRecordConvert.INSTANCE.convertPage(pageResult, helpCountMap, activityList, userMap));
    }

}
