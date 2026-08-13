package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 我的会员 Response VO")
@Data
public class AppPartnerMemberRespVO {

    @Schema(description = "是否有效", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean active;

    @Schema(description = "当前会员类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer memberType;

    @Schema(description = "会员过期时间")
    private LocalDateTime memberExpireTime;

    @Schema(description = "有效会员记录")
    private List<Member> members;

    @Schema(description = "用户 App - 会员记录")
    @Data
    public static class Member {

        @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "会员类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer memberType;

        @Schema(description = "开始时间")
        private LocalDateTime startTime;

        @Schema(description = "结束时间")
        private LocalDateTime endTime;

        @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer sourceType;

        @Schema(description = "会员时长（月）", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
        private BigDecimal durationQuantity;

        @Schema(description = "浼氬憳鏃堕暱鍗曚綅缂栧彿", requiredMode = Schema.RequiredMode.REQUIRED, example = "102")
        private Long durationUnitId;

    }

}
