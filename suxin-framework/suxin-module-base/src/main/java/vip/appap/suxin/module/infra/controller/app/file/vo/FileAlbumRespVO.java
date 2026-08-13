package vip.appap.suxin.module.infra.controller.app.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 相册文件 Response VO")
@Data
public class FileAlbumRespVO {

    @Schema(description = "文件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "文件 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.appap.vip/yudao.jpg")
    private String url;

    @Schema(description = "原文件名", example = "suxin.jpg")
    private String name;

}
