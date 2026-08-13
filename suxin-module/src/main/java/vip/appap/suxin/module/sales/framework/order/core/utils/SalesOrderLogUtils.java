package vip.appap.suxin.module.sales.framework.order.core.utils;

import vip.appap.suxin.module.sales.framework.order.core.aop.SalesOrderLogAspect;

import java.util.Map;

/**
 * 交易订单的操作日志 Utils
 *
 * @author 书心软件
 */
public class SalesOrderLogUtils {

    public static void setOrderInfo(Long id, Integer beforeStatus, Integer afterStatus) {
        SalesOrderLogAspect.setOrderInfo(id, beforeStatus, afterStatus, null);
    }

    public static void setOrderInfo(Long id, Integer beforeStatus, Integer afterStatus,
                                    Map<String, Object> exts) {
        SalesOrderLogAspect.setOrderInfo(id, beforeStatus, afterStatus, exts);
    }

    public static void setUserInfo(Long userId, Integer userType) {
        SalesOrderLogAspect.setUserInfo(userId, userType);
    }

}
