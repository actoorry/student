package vip.appap.suxin.module.system.service;

/**
 * 微信内容安全 Service 接口
 *
 * @author 书心软件
 */
public interface WxContentCheckService {

    /**
     * 支持的图片格式
     */
    String[] SUPPORTED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/bmp", "image/gif"};

    /**
     * 最大文件大小（10M）
     */
    long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 异步校验图片是否含有违法违规内容
     *
     * @param openid    用户 openid
     * @param mediaUrl  媒体资源 URL
     * @param mediaType 媒体类型（1-音频，2-图片）
     * @param scene     场景值（1-资料，2-评论，3-论坛，4-社交日志）
     * @return trace_id，用于匹配异步推送结果
     */
    String mediaCheckAsync(String openid, String mediaUrl, Integer mediaType, Integer scene);

    /**
     * 校验图片地址是否可用于微信内容安全审核
     * 必须是 http/https 服务器地址，不能是本地临时路径
     *
     * @param imageUrl 图片地址
     * @return 是否有效
     */
    boolean isValidAuditImageUrl(String imageUrl);

    /**
     * 校验图片格式是否支持
     *
     * @param mimeType MIME 类型
     * @return 是否支持
     */
    boolean isSupportedImageType(String mimeType);

    /**
     * 校验文件大小是否在限制范围内
     *
     * @param size 文件大小（字节）
     * @return 是否在限制范围内
     */
    boolean isFileSizeValid(Long size);

}
