package vip.appap.suxin.module.sales.dal.dataobject;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.module.sales.enums.SalesBannerPositionEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * banner DO
 *
 * @author xia
 */
@TableName("sales_banner")
@KeySequence("sales_banner_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesBannerDO extends BaseDO {

    /**
     * 编号
     */
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 跳转链接
     */
    private String url;
    /**
     * 图片链接
     */
    private String picUrl;
    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 定位 {@link SalesBannerPositionEnum}
     */
    private Integer position;

    /**
     * 备注
     */
    private String memo;

    /**
     * 点击次数
     */
    private Integer browseCount;

}
