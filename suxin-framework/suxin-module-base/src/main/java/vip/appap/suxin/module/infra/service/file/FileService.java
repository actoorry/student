package vip.appap.suxin.module.infra.service.file;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import vip.appap.suxin.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import vip.appap.suxin.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import vip.appap.suxin.module.infra.dal.dataobject.file.FileDO;
import jakarta.validation.constraints.NotEmpty;

import java.util.Collection;
import java.util.List;

/**
 * 文件 Service 接口
 *
 * @author 书心软件
 */
public interface FileService {

    /**
     * 获得文件分页
     *
     * @param pageReqVO 分页查询
     * @return 文件分页
     */
    PageResult<FileDO> getFilePage(FilePageReqVO pageReqVO);

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param content   文件内容
     * @param name      文件名称，允许空
     * @param directory 目录，允许空
     * @param type      文件的 MIME 类型，允许空
     * @return 文件路径
     */
    String createFile(@NotEmpty(message = "文件内容不能为空") byte[] content,
                      String name, String directory, String type);

    /**
     * 保存文件，并返回文件的访问路径（带业务关联）
     *
     * @param content   文件内容
     * @param name      文件名称，允许空
     * @param directory 目录，允许空
     * @param type      文件的 MIME 类型，允许空
     * @param bizType   业务类型
     * @param bizId     业务ID（关联业务表主键）
     * @return 文件路径
     */
    String createFile(@NotEmpty(message = "文件内容不能为空") byte[] content,
                      String name, String directory, String type,
                      String bizType, Long bizId);

    /**
     * 生成文件预签名地址信息，用于上传
     *
     * @param name      文件名
     * @param directory 目录
     * @return 预签名地址信息
     */
    FilePresignedUrlRespVO presignPutUrl(@NotEmpty(message = "文件名不能为空") String name,
                                         String directory);
    /**
     * 生成文件预签名地址信息，用于读取
     *
     * @param url 完整的文件访问地址
     * @param expirationSeconds 访问有效期，单位秒
     * @return 文件预签名地址
     */
    String presignGetUrl(String url, Integer expirationSeconds);

    /**
     * 创建文件
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFile(FileCreateReqVO createReqVO);
    FileDO getFile(Long id);

    /**
     * 删除文件
     *
     * @param id 编号
     */
    void deleteFile(Long id) throws Exception;

    /**
     * 批量删除文件
     *
     * @param ids 编号列表
     */
    void deleteFileList(List<Long> ids) throws Exception;

    /**
     * 获得文件内容
     *
     * @param configId 配置编号
     * @param path     文件路径
     * @return 文件内容
     */
    byte[] getFileContent(Long configId, String path) throws Exception;

    /**
     * 根据业务类型和业务ID查询文件列表
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 文件列表
     */
    List<FileDO> getFileListByBiz(String bizType, Long bizId);

    /**
     * 根据业务类型和多个业务ID批量查询文件列表
     *
     * @param bizType 业务类型
     * @param bizIds  业务ID列表
     * @return 文件列表
     */
    List<FileDO> getFileListByBizTypeAndBizIds(String bizType, Collection<Long> bizIds);

    /**
     * 鏍规嵁 URL 缁戝畾鏂囦欢鐨勪笟鍔″叧鑱?
     *
     * @param bizType 涓氬姟绫诲瀷
     * @param bizId   涓氬姟ID
     * @param urls    鏂囦欢 URL 鍒楄〃
     * @return 鏇存柊鏁伴噺
     */
    int bindFileBizByUrls(String bizType, Long bizId, Collection<String> urls);

    /**
     * 根据 trace_id 更新文件审核状态
     *
     * @param traceId     微信审核 trace_id
     * @param auditStatus 审核状态
     * @param auditReason 审核原因
     * @param auditTime   审核时间
     */
    void updateFileAuditStatusByTraceId(String traceId, Integer auditStatus, String auditReason,
                                         java.time.LocalDateTime auditTime);

    /**
     * 根据 URL 获取文件审核状态
     *
     * @param url 文件 URL
     * @return 审核状态
     */
    Integer getFileAuditStatusByUrl(String url);

    /**
     * 根据 URL 获取文件
     *
     * @param url 文件 URL
     * @return 文件信息
     */
    FileDO getFileByUrl(String url);

}
