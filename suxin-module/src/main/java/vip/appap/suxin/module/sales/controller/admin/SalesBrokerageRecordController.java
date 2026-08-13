package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageRecordPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageRecordRespVO;
import vip.appap.suxin.module.sales.convert.SalesBrokerageRecordConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageRecordDO;
import vip.appap.suxin.module.sales.service.SalesBrokerageRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 佣金记录")
@RestController
@RequestMapping("/sales/brokerage-record")
@Validated
public class SalesBrokerageRecordController {

    @Resource
    private SalesBrokerageRecordService brokerageRecordService;

    @Resource
    private PartnerApi PartnerApi;

    @GetMapping("/get")
    @Operation(summary = "获得佣金记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_record:query')")
    public CommonResult<SalesBrokerageRecordRespVO> getBrokerageRecord(@RequestParam("id") Long id) {
        SalesBrokerageRecordDO brokerageRecord = brokerageRecordService.getBrokerageRecord(id);
        return success(SalesBrokerageRecordConvert.INSTANCE.convert(brokerageRecord));
    }

    @GetMapping("/page")
    @Operation(summary = "获得佣金记录分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_brokerage_record:query')")
    public CommonResult<PageResult<SalesBrokerageRecordRespVO>> getBrokerageRecordPage(@Valid SalesBrokerageRecordPageReqVO pageVO) {
        PageResult<SalesBrokerageRecordDO> pageResult = brokerageRecordService.getBrokerageRecordPage(pageVO);

        // 查询用户信息
        Set<Long> userIds = convertSet(pageResult.getList(), SalesBrokerageRecordDO::getUserId);
        userIds.addAll(convertList(pageResult.getList(), SalesBrokerageRecordDO::getSourceUserId));
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(userIds);
        // 拼接数据
        return success(SalesBrokerageRecordConvert.INSTANCE.convertPage(pageResult, userMap));
    }

}
