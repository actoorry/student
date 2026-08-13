package vip.appap.suxin.module.sales.framework.aftersale.core.utils;


import vip.appap.suxin.module.sales.enums.SalesAfterSaleOperateTypeEnum;
import vip.appap.suxin.module.sales.framework.aftersale.core.aop.SalesAfterSaleLogAspect;

import java.util.Map;

/**
 * 操作日志工具类
 * 目前主要的作用，是提供给业务代码，记录操作明细和拓展字段
 *
 * @author 书心软件
 */
public class SalesAfterSaleLogUtils {

    public static void setAfterSaleOperateType(SalesAfterSaleOperateTypeEnum operateType) {
        SalesAfterSaleLogAspect.setAfterSaleOperateType(operateType);
    }

    public static void setAfterSaleInfo(Long id, Integer beforeStatus, Integer afterStatus) {
        setAfterSaleInfo(id, beforeStatus, afterStatus, null);
    }

    public static void setAfterSaleInfo(Long id, Integer beforeStatus, Integer afterStatus,
                                        Map<String, Object> exts) {
        SalesAfterSaleLogAspect.setAfterSale(id, beforeStatus, afterStatus, exts);
    }

}
