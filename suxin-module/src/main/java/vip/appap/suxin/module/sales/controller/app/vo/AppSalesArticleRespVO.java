package vip.appap.suxin.module.sales.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "应用 App - 文章 Response VO")
@Data
public class AppSalesArticleRespVO {

    @Schema(description = "文章编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "文章标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "书心软件 - 促销模块")
    private String title;

    @Schema(description = "文章作者", requiredMode = Schema.RequiredMode.REQUIRED, example = "书心软件")
    private String author;

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long categoryId;

    @Schema(description = "省份区域编号（战友会使用）", example = "210000")
    private Integer stateId;

    @Schema(description = "图文封面", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.appap.vip/1.png")
    private String picUrl;

    @Schema(description = "文章简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是简介")
    private String introduction;

    @Schema(description = "文章内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是详细")
    private String content;

    @Schema(description = "发布时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "浏览量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer browseCount;

    @Schema(description = "关联的商品 SPU 编号", example = "1024")
    private Long spuId;

    @Schema(description = "帮扶金额（戎爱心公示）", example = "5000.00")
    private BigDecimal helpAmount;

    @Schema(description = "申请人（脱敏，戎爱心公示）", example = "李**")
    private String applicantName;

    @Schema(description = "执行状态（戎爱心公示）", example = "执行中")
    private String executionStatus;

}
