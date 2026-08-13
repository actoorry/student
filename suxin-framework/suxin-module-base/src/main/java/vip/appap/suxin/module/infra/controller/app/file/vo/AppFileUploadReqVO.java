package vip.appap.suxin.module.infra.controller.app.file.vo;

import vip.appap.suxin.module.infra.controller.admin.file.vo.file.FileUploadReqVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "用户 App - 上传文件 Request VO")
@Data
public class AppFileUploadReqVO {

    @Schema(description = "文件附件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件附件不能为空")
    private MultipartFile file;

    @Schema(description = "文件目录", example = "XXX/YYY")
    private String directory;

    @Schema(description = "业务类型", example = "partner_image")
    private String bizType;

    @Schema(description = "业务ID（关联业务表主键）", example = "1024")
    private Long bizId;

    @AssertTrue(message = "文件目录不正确")
    @JsonIgnore
    public boolean isDirectoryValid() {
        return FileUploadReqVO.isDirectoryValid(directory);
    }

}
