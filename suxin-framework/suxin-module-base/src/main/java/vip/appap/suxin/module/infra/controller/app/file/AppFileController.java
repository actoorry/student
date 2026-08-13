package vip.appap.suxin.module.infra.controller.app.file;

import cn.hutool.core.io.IoUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import vip.appap.suxin.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import vip.appap.suxin.module.infra.controller.app.file.vo.AppFileUploadReqVO;
import vip.appap.suxin.module.infra.controller.app.file.vo.FileAlbumRespVO;
import vip.appap.suxin.module.infra.dal.dataobject.file.FileDO;
import vip.appap.suxin.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static vip.appap.suxin.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static vip.appap.suxin.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 文件存储")
@RestController
@RequestMapping("/infra/file")
@Validated
@Slf4j
public class AppFileController {

    private static final String PARTNER_ALBUM_BIZ_TYPE = "partner_album";

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @Parameter(name = "file", description = "文件附件", required = true,
            schema = @Schema(type = "string", format = "binary"))
    @PermitAll
    public CommonResult<String> uploadFile(AppFileUploadReqVO uploadReqVO) throws Exception {
        MultipartFile file = uploadReqVO.getFile();
        byte[] content = IoUtil.readBytes(file.getInputStream());
        Long bizId = uploadReqVO.getBizId();
        if (PARTNER_ALBUM_BIZ_TYPE.equals(uploadReqVO.getBizType())) {
            bizId = getRequiredLoginUserId();
        }
        return success(fileService.createFile(content, file.getOriginalFilename(),
                uploadReqVO.getDirectory(), file.getContentType(),
                uploadReqVO.getBizType(), bizId));
    }

    @GetMapping("/audit-status")
    @Operation(summary = "获取文件审核状态")
    @Parameter(name = "url", description = "文件URL", required = true)
    public CommonResult<Integer> getFileAuditStatus(@RequestParam("url") String url) {
        return success(fileService.getFileAuditStatusByUrl(url));
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "获取文件预签名地址（上传）", description = "模式二：前端上传文件：用于前端直接上传七牛、阿里云 OSS 等文件存储器")
    @Parameters({
            @Parameter(name = "name", description = "文件名称", required = true),
            @Parameter(name = "directory", description = "文件目录")
    })
    public CommonResult<FilePresignedUrlRespVO> getFilePresignedUrl(
            @RequestParam("name") String name,
            @RequestParam(value = "directory", required = false) String directory) {
        return success(fileService.presignPutUrl(name, directory));
    }

    @PostMapping("/create")
    @Operation(summary = "创建文件", description = "模式二：前端上传文件：配合 presigned-url 接口，记录上传了上传的文件")
    @PermitAll
    public CommonResult<Long> createFile(@Valid @RequestBody FileCreateReqVO createReqVO) {
        return success(fileService.createFile(createReqVO));
    }

    @GetMapping("/get-album-images")
    @Operation(summary = "获取相册图片列表")
    @Parameter(name = "bizType", description = "业务类型", required = true)
    public CommonResult<List<FileAlbumRespVO>> getAlbumImages(@RequestParam("bizType") String bizType) {
        Long userId = getRequiredLoginUserId();
        List<FileDO> files = fileService.getFileListByBiz(bizType, userId);
        List<FileAlbumRespVO> result = convertList(files, file -> {
            FileAlbumRespVO vo = new FileAlbumRespVO();
            vo.setId(file.getId());
            vo.setUrl(file.getUrl());
            vo.setName(file.getName());
            return vo;
        });
        return success(result);
    }

    @DeleteMapping("/delete-album-image")
    @Operation(summary = "删除我的相册图片")
    @Parameter(name = "id", description = "文件编号", required = true)
    public CommonResult<Boolean> deleteAlbumImage(@RequestParam("id") Long id) throws Exception {
        Long userId = getRequiredLoginUserId();
        FileDO file = fileService.getFile(id);
        if (file == null) {
            throw exception(NOT_FOUND);
        }
        if (!PARTNER_ALBUM_BIZ_TYPE.equals(file.getBizType()) || !userId.equals(file.getBizId())) {
            throw exception(FORBIDDEN);
        }
        fileService.deleteFile(id);
        return success(Boolean.TRUE);
    }

    private Long getRequiredLoginUserId() {
        Long userId = getLoginUserId();
        if (userId == null) {
            throw exception(UNAUTHORIZED);
        }
        return userId;
    }

}
