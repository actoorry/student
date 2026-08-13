package vip.appap.suxin.module.sales.framework.aftersale.core.annotations;

import vip.appap.suxin.module.sales.enums.SalesAfterSaleOperateTypeEnum;
import vip.appap.suxin.module.sales.framework.aftersale.core.aop.SalesAfterSaleLogAspect;

import java.lang.annotation.*;

/**
 * 售后日志的注解
 *
 * 写在方法上时，会自动记录售后日志
 *
 * @author 陈賝
 * @since 2023/6/8 17:04
 * @see SalesAfterSaleLogAspect
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SalesAfterSaleLog {

    /**
     * 操作类型
     */
    SalesAfterSaleOperateTypeEnum operateType();

}
