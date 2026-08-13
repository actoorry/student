package vip.appap.suxin.module.hr.integration.zhiye;



import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

import org.springframework.validation.annotation.Validated;



/**

 * 智业 HIS 集成配置

 *

 * yaml 路径：suxin.hr.zhiye.*

 */

@Component

@ConfigurationProperties(prefix = "suxin.hr.zhiye")

@Validated

@Data

public class ZhiyeHisProperties {



    /**

     * 是否启用人员同步；本地开发可设为 false

     */

    private Boolean enabled = false;



    private String queryUrl = "http://222.222.22.218:8693/Select-service?wsdl";



    private String updateUrl = "http://222.222.22.218:8693/Common-service?wsdl";



    private Integer connectTimeoutMs = 5000;



    private Integer readTimeoutMs = 30000;



    /**

     * 与旧 OA {@code providerInfoRegisterIn} 一致：仅替换 7 个业务字段，其余走模板写死值

     */

    private Boolean oaCompatible = true;



    /**

     * 是否去掉嵌入 SOAP 的内层 {@code <?xml ?>} 声明；OA 原版 FtXmlUtil 不剥离，默认 false

     */

    private Boolean stripXmlProlog = false;



    /** 院区代码（OA organization_info.branch_code） */

    private String branchCode = "01";



    /** 组织机构代码（OA organization_info.origin_code） */

    private String originCode = "12340421485396401Y";



    /** 申请者工号（OA ProviderInfoRegisterEntity.applyId） */

    private String applyId = "263913";



    /** 出生地编码（extended 模式用；OA 模板 body 中 birthplace 为空） */
    private String birthPlace = "340000";

    /** 智业 HIP 鉴权证书（OA head.xml certificate，与 OA 默认值一致） */
    private String certificate = "q/FfngQ8T9Yvz6nFyFgydOMnphlsOSZVjDhNRq/Ba4P7tdQnePqJ4A==";

}

