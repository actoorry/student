package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.SortablePageParam;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesProductStatisticsReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesProductStatisticsRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesProductStatisticsDO;
import vip.appap.suxin.module.sales.service.SalesProductStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 商品统计")
@RestController
@RequestMapping("/sales/statistics/product")
@Validated
public class SalesProductStatisticsController {

    @Resource
    private SalesProductStatisticsService productStatisticsService;

    @Resource
    private ProductSpuApi productSpuApi;

    @GetMapping("/analyse")
    @Operation(summary = "获得商品统计分析")
    @PreAuthorize("@ss.hasPermission('sales:sales_product_statistics:query')")
    public CommonResult<SalesStatisticsDataComparisonRespVO<SalesProductStatisticsRespVO>> getProductStatisticsAnalyse(SalesProductStatisticsReqVO reqVO) {
        return success(productStatisticsService.getProductStatisticsAnalyse(reqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "获得商品统计明细（日期维度）")
    @PreAuthorize("@ss.hasPermission('sales:sales_product_statistics:query')")
    public CommonResult<List<SalesProductStatisticsRespVO>> getProductStatisticsList(SalesProductStatisticsReqVO reqVO) {
        List<SalesProductStatisticsDO> list = productStatisticsService.getProductStatisticsList(reqVO);
        return success(BeanUtils.toBean(list, SalesProductStatisticsRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出获得商品统计明细 Excel（日期维度）")
    @PreAuthorize("@ss.hasPermission('sales:sales_product_statistics:export')")
    public void exportProductStatisticsExcel(SalesProductStatisticsReqVO reqVO, HttpServletResponse response) throws IOException {
        List<SalesProductStatisticsDO> list = productStatisticsService.getProductStatisticsList(reqVO);
        // 导出 Excel
        List<SalesProductStatisticsRespVO> voList = BeanUtils.toBean(list, SalesProductStatisticsRespVO.class);
        ExcelUtils.write(response, "商品状况.xls", "数据", SalesProductStatisticsRespVO.class, voList);
    }

    @GetMapping("/rank-page")
    @Operation(summary = "获得商品统计排行榜分页（商品维度）")
    @PreAuthorize("@ss.hasPermission('sales:sales_product_statistics:query')")
    public CommonResult<PageResult<SalesProductStatisticsRespVO>> getProductStatisticsRankPage(@Valid SalesProductStatisticsReqVO reqVO,
                                                                                          @Valid SortablePageParam pageParam) {
        PageResult<SalesProductStatisticsDO> pageResult = productStatisticsService.getProductStatisticsRankPage(reqVO, pageParam);
        // 处理商品信息
        Set<Long> spuIds = convertSet(pageResult.getList(), SalesProductStatisticsDO::getSpuId);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(productSpuApi.getSpuList(spuIds), ProductSpuRespDTO::getId);
        return success(BeanUtils.toBean(pageResult, SalesProductStatisticsRespVO.class,
                item -> Optional.ofNullable(spuMap.get(item.getSpuId()))
                        .ifPresent(spu -> item.setName(spu.getName()).setPicUrl(spu.getPicUrl()))));
    }

}