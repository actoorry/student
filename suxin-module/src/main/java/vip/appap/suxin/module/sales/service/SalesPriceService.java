package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.controller.app.vo.AppSalesProductSettlementRespVO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 价格计算 Service 接口
 *
 * @author 书心软件
 */
public interface SalesPriceService {

    /**
     * 【订单】价格计算
     *
     * @param calculateReqDTO 计算信息
     * @return 计算结果
     */
    SalesPriceCalculateRespBO calculateOrderPrice(@Valid SalesPriceCalculateReqBO calculateReqDTO);

    /**
     * 【商品】价格计算，用于商品列表、商品详情
     *
     * @param userId 用户编号，允许为空
     * @param spuIds 商品 SPU 编号数组
     * @return 计算结果
     */
    List<AppSalesProductSettlementRespVO> calculateProductPrice(Long userId, List<Long> spuIds);

}
