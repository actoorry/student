package vip.appap.suxin.module.infra.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件审核状态枚举
 *
 * @author 书心软件
 */
@Getter
@AllArgsConstructor
public enum FileAuditStatusEnum {

    NO_AUDIT(0, "无需审核"),
    AUDITING(1, "审核中"),
    AUDIT_PASS(2, "审核通过"),
    AUDIT_REJECT(3, "审核不通过"),
    AUDIT_REVIEW(4, "待复审");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名称
     */
    private final String name;

    /**
     * 判断是否是图片类型
     *
     * @param type MIME 类型
     * @return 是否是图片
     */
    public static boolean isImageType(String type) {
        if (type == null) {
            return false;
        }
        return type.startsWith("image/");
    }

}
