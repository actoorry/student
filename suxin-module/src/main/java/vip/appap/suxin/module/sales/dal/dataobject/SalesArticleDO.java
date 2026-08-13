package vip.appap.suxin.module.sales.dal.dataobject;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 文章管理 DO
 *
 * @author HUIHUI
 */
@TableName("sales_article")
@KeySequence("sales_article_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesArticleDO extends BaseDO {

    /**
     * 文章管理编号
     */
    @TableId
    private Long id;
    /**
     * 分类编号 SalesArticleCategoryDO#id
     */
    private Long categoryId;
    /**
     * 省份区域编号（战友会使用）
     */
    private Integer stateId;
    /**
     * 关联商品编号 ProductSpuDO#id
     */
    private Long spuId;
    /**
     * 文章标题
     */
    private String title;
    /**
     * 文章作者
     */
    private String author;
    /**
     * 文章封面图片地址
     */
    private String picUrl;
    /**
     * 文章简介
     */
    private String introduction;
    /**
     * 浏览次数
     */
    private Integer browseCount;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 是否热门(小程序)
     */
    private Boolean recommendHot;
    /**
     * 是否轮播图(小程序)
     */
    private Boolean recommendBanner;
    /**
     * 文章内容
     */
    private String content;

    /**
     * 帮扶金额（戎爱心公示 categoryId=5）
     */
    private BigDecimal helpAmount;

    /**
     * 申请人（脱敏，戎爱心公示）
     */
    private String applicantName;

    /**
     * 执行状态（戎爱心公示）
     */
    private String executionStatus;

}
