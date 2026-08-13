package vip.appap.suxin.module.infra.dal.dataobject.file;

import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 文件表
 * 每次文件上传，都会记录一条记录到该表中
 *
 * @author 书心软件
 */
@TableName("infra_file")
@KeySequence("infra_file_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDO extends TenantBaseDO {

    /**
     * 编号，数据库自增
     */
    private Long id;
    /**
     * 配置编号
     *
     * 关联 {@link FileConfigDO#getId()}
     */
    private Long configId;
    /**
     * 原文件名
     */
    private String name;
    /**
     * 路径，即文件名
     */
    private String path;
    /**
     * 访问地址
     */
    private String url;
    /**
     * 文件的 MIME 类型，例如 "application/octet-stream"
     */
    private String type;
    /**
     * 文件大小
     */
    private Long size;

    /**
     * 业务类型（如 partner_image、partner_avatar 等），为空表示无业务关联
     */
    private String bizType;
    /**
     * 业务ID（关联业务表主键），为空表示无业务关联
     */
    private Long bizId;

    /**
     * 审核状态
     *
     * 0-无需审核 1-审核中 2-审核通过 3-审核不通过 4-待复审
     */
    private Integer auditStatus;
    /**
     * 微信审核 trace_id
     */
    private String auditTraceId;
    /**
     * 审核原因
     */
    private String auditReason;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

}
