package vip.appap.suxin.module.sales.dal.dataobject;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.framework.mybatis.core.type.EncryptTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 快递100电子面单账户 DO
 *
 * 租户隔离的电子面单账户主数据。敏感凭据（partnerKey / partnerSecret）由
 * {@link EncryptTypeHandler} 使用 mybatis-plus.encryptor.password 加密落库；
 * 响应统一脱敏，更新时空值表示保留原凭据。
 *
 * @author 书心软件
 */
@TableName(value = "sales_electronic_waybill_account", autoResultMap = true)
@KeySequence("sales_electronic_waybill_account_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesElectronicWaybillAccountDO extends BaseDO {

    /**
     * 编号，自增
     */
    @TableId
    private Long id;

    /**
     * 账户名称（租户内唯一）
     */
    private String name;

    /**
     * 快递公司编号
     *
     * 关联 {@link SalesDeliveryExpressDO#getId()}
     */
    private Long expressId;

    /**
     * 快递公司编码快照
     *
     * 关联 {@link SalesDeliveryExpressDO#getCode()}
     */
    private String expressCode;

    /**
     * 快递100平台授权 key（AES 加密落库，签名与表单使用）
     *
     * 列名使用 auth_key 而非 key：key 是 MySQL 保留字，MyBatis-Plus 生成的 SQL 不带
     * 引号，直接映射 key 列会在运行时语法报错；auth_key 与既有快递100配置表
     * sales_express_provider_config.auth_key 命名保持一致。Java 字段名保持 key，
     * 前端/API 契约与掩码逻辑不受影响。
     */
    @TableField(value = "auth_key", typeHandler = EncryptTypeHandler.class)
    private String key;

    /**
     * 快递100平台授权密钥（AES 加密落库，签名使用）
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String secret;

    /**
     * 电子面单月结账号（需向当地快递公司网点申请）
     */
    private String partnerId;

    /**
     * 电子面单密码（AES 加密落库）
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String partnerKey;

    /**
     * 电子面单密钥（AES 加密落库）
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String partnerSecret;

    /**
     * 电子面单网点（由当地网点分配；淘宝授权填 taobao，菜鸟授权填 cainiao）
     */
    private String net;

    /**
     * 电子面单承载编号
     */
    private String code;

    /**
     * 电子面单客户账户名称
     */
    private String partnerName;

    /** 业务员编码（部分快递公司取消面单必填） */
    private String checkMan;

    /** 产品业务类型（部分快递公司取消面单必填） */
    private String expType;

    /**
     * 快递100模板 ID（必填）
     */
    private String tempId;

    /**
     * 默认寄件地址编号
     *
     * 关联 {@link vip.appap.suxin.module.partner.dal.dataobject.PartnerAddressDO#getId()}
     */
    private Long defaultAddressId;

    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
