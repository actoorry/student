package vip.appap.suxin.module.hr.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * HR 模块全局配置
 *
 * yaml 路径：suxin.hr.certificate.*
 *
 * @author admin
 */
@Component
@ConfigurationProperties(prefix = "suxin.hr")
@Validated
@Data
public class HrProperties {

    private Certificate certificate = new Certificate();

    private Appointment appointment = new Appointment();

    /**
     * 证书模块配置
     */
    @Data
    public static class Certificate {

        /**
         * 站内信推送对象 userId 列表。
         * 为空时自动查询拥有 hr:certificate:query 权限的管理员。
         */
        private List<Long> notifyUserIds;

    }

    @Data
    public static class Appointment {

        /** 聘期预警 HR 管理员 userId，为空则按 hr:appointment:query 权限反查 */
        private List<Long> notifyUserIds;

    }

}
