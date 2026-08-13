package vip.appap.suxin.module.sales.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 电子面单打印类型枚举
 *
 * 首期仅支持 HTML 与 IMAGE（浏览器预览/本地打印），不接入云打印机
 *
 * @author 书心软件
 */
@Getter
@AllArgsConstructor
public enum SalesElectronicWaybillPrintTypeEnum {

    HTML("HTML", "HTML 面单短链"),
    IMAGE("IMAGE", "图片面单短链");

    private final String type;
    private final String name;

    public static boolean isValid(String type) {
        return type != null && (HTML.getType().equals(type) || IMAGE.getType().equals(type));
    }

}
