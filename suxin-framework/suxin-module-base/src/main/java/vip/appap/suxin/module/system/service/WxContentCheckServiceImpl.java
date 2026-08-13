package vip.appap.suxin.module.system.service;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 微信内容安全 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Slf4j
public class WxContentCheckServiceImpl implements WxContentCheckService {

    @Resource
    private SocialClientService socialClientService;

    @Override
    public String mediaCheckAsync(String openid, String mediaUrl, Integer mediaType, Integer scene) {
        // 调用微信接口，使用会员用户类型
        return socialClientService.mediaCheckAsync(
                UserTypeEnum.MEMBER.getValue(), openid, mediaUrl, mediaType, scene);
    }

    @Override
    public boolean isValidAuditImageUrl(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            return false;
        }
        if (StrUtil.startWith(imageUrl, "wxfile://")
                || StrUtil.startWith(imageUrl, "file://")
                || StrUtil.startWith(imageUrl, "blob:")
                || StrUtil.startWith(imageUrl, "/")
                || StrUtil.startWith(imageUrl, "http://tmp/")
                || StrUtil.startWith(imageUrl, "https://tmp/")
                || StrUtil.startWith(imageUrl, "http://store/")
                || StrUtil.startWith(imageUrl, "https://store/")) {
            return false;
        }
        return StrUtil.startWith(imageUrl, "http://") || StrUtil.startWith(imageUrl, "https://");
    }

    @Override
    public boolean isSupportedImageType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return ArrayUtil.contains(SUPPORTED_IMAGE_TYPES, mimeType.toLowerCase());
    }

    @Override
    public boolean isFileSizeValid(Long size) {
        if (size == null) {
            return false;
        }
        return size <= MAX_FILE_SIZE;
    }

}
