package vip.appap.suxin.server.controller;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.servlet.ServletUtils;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;

/**
 * 默认 Controller，解决部分 module 未开启时的 404 提示。
 * 例如说，/bpm/** 路径，工作流
 *
 * @author 书心软件
 */
@RestController
@Slf4j
public class DefaultController {

//    @RequestMapping("/admin-api/bpm/**")
//    public CommonResult<Boolean> bpm404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[工作流模块 suxin-module-bpm - 已禁用][参考 https://doc.appap.vip/bpm/ 开启]");
//    }
//
//    @RequestMapping("/admin-api/mp/**")
//    public CommonResult<Boolean> mp404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[微信公众号 suxin-module-mp - 已禁用][参考 https://doc.appap.vip/mp/build/ 开启]");
//    }
//
//    @RequestMapping(value = { "/admin-api/product/**", // 商品中心
//            "/admin-api/trade/**", // 交易中心
//            "/admin-api/promotion/**" }) // 营销中心
//    public CommonResult<Boolean> mall404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[商城系统 suxin-module-mall - 已禁用][参考 https://doc.appap.vip/mall/build/ 开启]");
//    }
//
//    @RequestMapping("/admin-api/erp/**")
//    public CommonResult<Boolean> erp404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[ERP 模块 suxin-module-erp - 已禁用][参考 https://doc.appap.vip/erp/build/ 开启]");
//    }
//
//    @RequestMapping("/admin-api/crm/**")
//    public CommonResult<Boolean> crm404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[CRM 模块 suxin-module-crm - 已禁用][参考 https://doc.appap.vip/crm/build/ 开启]");
//    }
//
//    @RequestMapping(value = { "/admin-api/report/**"})
//    public CommonResult<Boolean> report404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[报表模块 suxin-module-report - 已禁用][参考 https://doc.appap.vip/report/ 开启]");
//    }
//
//    @RequestMapping(value = { "/admin-api/ai/**"})
//    public CommonResult<Boolean> ai404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[AI 大模型 suxin-module-ai - 已禁用][参考 https://doc.appap.vip/ai/build/ 开启]");
//    }
//
//    @RequestMapping(value = { "/admin-api/iot/**"})
//    public CommonResult<Boolean> iot404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[IoT 物联网 suxin-module-iot - 已禁用][参考 https://doc.appap.vip/iot/build/ 开启]");
//    }

    /**
     * 测试接口：打印 query、header、body
     */
    @RequestMapping(value = { "/test" })
    @PermitAll
    public CommonResult<Boolean> test(HttpServletRequest request) {
        // 打印查询参数
        log.info("Query: {}", ServletUtils.getParamMap(request));
        // 打印请求头
        log.info("Header: {}", ServletUtils.getHeaderMap(request));
        // 打印请求体
        log.info("Body: {}", ServletUtils.getBody(request));
        return CommonResult.success(true);
    }

}
