package vip.appap.suxin.module.accountant.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.module.accountant.dal.dataobject.PayChannelDO;
import vip.appap.suxin.module.accountant.dal.mysql.PayChannelMapper;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.*;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsRefreshReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsSyncReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.mysql.ProductSkuMapper;
import vip.appap.suxin.module.product.dal.mysql.ProductSpuMapper;
import vip.appap.suxin.module.product.enums.ProductSkuWechatVirtualStatusEnum;
import vip.appap.suxin.module.system.dal.dataobject.SocialClientDO;
import vip.appap.suxin.module.system.dal.mysql.SocialClientMapper;
import vip.appap.suxin.module.system.enums.SocialTypeEnum;
import vip.appap.suxin.module.system.service.SocialClientService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.*;

/**
 * 微信虚拟支付道具生命周期服务。
 */
@Service
@Validated
@Slf4j
public class ProductWechatVirtualGoodsServiceImpl implements ProductWechatVirtualGoodsService {

    /**
     * 任务运行中占位标识（官方协议不返回 task_id，一个 app+env 同时只能有一个任务）。
     */
    private static final String TASK_ID_RUNNING = "RUNNING";
    private static final Duration IMAGE_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration IMAGE_REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient IMAGE_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(IMAGE_CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Resource
    private ProductSpuMapper productSpuMapper;
    @Resource
    private ProductSkuMapper productSkuMapper;
    @Resource
    private PayChannelMapper payChannelMapper;
    @Resource
    private SocialClientMapper socialClientMapper;
    @Resource
    private SocialClientService socialClientService;


    /**
     * 允许子类/测试覆盖 HTTP 客户端。
     */
    public WxVirtualPayHttpClient createWxVirtualPayHttpClient() {
        return new WxVirtualPayHttpClient();
    }

    /**
     * 将图片地址转换为微信可解析的 ASCII URI，并用与微信一致的 GET 语义验证资源。
     */
    public String normalizeAndValidateImageUrl(String imageUrl) {
        URI normalizedUri = normalizeImageUri(imageUrl);
        ImageResourceMetadata metadata = probeImageResource(normalizedUri);
        if (metadata.statusCode() < 200 || metadata.statusCode() >= 300) {
            throw new IllegalArgumentException("图片 GET 返回 HTTP " + metadata.statusCode());
        }
        String contentType = StrUtil.blankToDefault(metadata.contentType(), "")
                .split(";", 2)[0]
                .trim()
                .toLowerCase(Locale.ROOT);
        if (!Objects.equals(contentType, "image/png")
                && !Objects.equals(contentType, "image/jpeg")) {
            throw new IllegalArgumentException("图片 Content-Type 不受支持："
                    + StrUtil.blankToDefault(contentType, "未返回"));
        }
        return normalizedUri.toASCIIString();
    }

    private URI normalizeImageUri(String imageUrl) {
        String candidate = StrUtil.trim(imageUrl);
        if (StrUtil.isBlank(candidate)) {
            throw new IllegalArgumentException("图片 URL 为空");
        }
        try {
            URI uri = new URI(encodeUriWhitespace(candidate));
            String scheme = StrUtil.blankToDefault(uri.getScheme(), "").toLowerCase(Locale.ROOT);
            if (!Objects.equals(scheme, "http") && !Objects.equals(scheme, "https")) {
                throw new IllegalArgumentException("图片 URL 仅支持 HTTP/HTTPS");
            }
            if (StrUtil.isBlank(uri.getHost()) || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("图片 URL 主机无效");
            }
            return URI.create(uri.toASCIIString());
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("图片 URL 格式无效");
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("图片 URL")) {
                throw ex;
            }
            throw new IllegalArgumentException("图片 URL 格式无效");
        }
    }

    private String encodeUriWhitespace(String value) {
        StringBuilder result = new StringBuilder(value.length());
        value.codePoints().forEach(codePoint -> {
            if (Character.isWhitespace(codePoint) || Character.isISOControl(codePoint)) {
                byte[] bytes = new String(Character.toChars(codePoint)).getBytes(StandardCharsets.UTF_8);
                for (byte current : bytes) {
                    result.append('%');
                    result.append(String.format("%02X", current & 0xff));
                }
            } else {
                result.appendCodePoint(codePoint);
            }
        });
        return result.toString();
    }

    /**
     * 只读取响应头并关闭响应流，避免把完整图片载入内存。
     */
    public ImageResourceMetadata probeImageResource(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(IMAGE_REQUEST_TIMEOUT)
                .header("Accept", "image/png,image/jpeg")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response = IMAGE_HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream ignored = response.body()) {
                return new ImageResourceMetadata(response.statusCode(),
                        response.headers().firstValue("Content-Type").orElse(null));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("图片 GET 校验被中断");
        } catch (IOException ex) {
            throw new IllegalStateException("图片 GET 请求失败，请检查公网访问、HTTPS 证书和网络");
        }
    }

    public record ImageResourceMetadata(int statusCode, String contentType) {
    }

    @Override
    public List<ProductWechatVirtualGoodsRespVO> getWechatVirtualGoodsList(Long spuId) {
        ProductSpuDO spu = validateSupportedSpu(spuId);
        return productSkuMapper.selectListBySpuId(spu.getId()).stream()
                .map(sku -> buildRespVO(sku, null, null, null))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProductWechatVirtualGoodsRespVO> syncWechatVirtualGoods(ProductWechatVirtualGoodsSyncReqVO reqVO) {
        ProductSpuDO spu = validateSupportedSpu(reqVO.getSpuId());
        ChannelContext channel = resolveChannel(reqVO.getAppId());
        List<ProductSkuDO> skus = productSkuMapper.selectListBySpuId(spu.getId());
        if (skus.stream().anyMatch(sku -> isProcessing(sku.getWechatVirtualUploadStatus()))) {
            return skus.stream()
                    .map(sku -> buildRespVO(sku, "sync", false,
                            isProcessing(sku.getWechatVirtualUploadStatus())
                                    ? "上传任务正在处理中，请先刷新状态"
                                    : "当前 SPU 已有上传批任务，请先刷新状态后再同步"))
                    .toList();
        }
        return startUploadBatch(spu, skus, channel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProductWechatVirtualGoodsRespVO> publishWechatVirtualGoods(ProductWechatVirtualGoodsSyncReqVO reqVO) {
        ProductSpuDO spu = validateSupportedSpu(reqVO.getSpuId());
        ChannelContext channel = resolveChannel(reqVO.getAppId());
        List<ProductSkuDO> skus = productSkuMapper.selectListBySpuId(spu.getId());
        if (skus.stream().anyMatch(sku -> isProcessing(sku.getWechatVirtualPublishStatus()))) {
            return skus.stream()
                    .map(sku -> buildRespVO(sku, "publish", false,
                            isProcessing(sku.getWechatVirtualPublishStatus())
                                    ? "发布任务正在处理中，请先刷新状态"
                                    : "当前 SPU 已有发布批任务，请先刷新状态后再发布"))
                    .toList();
        }
        return startPublishBatch(skus, channel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProductWechatVirtualGoodsRespVO> refreshWechatVirtualGoods(ProductWechatVirtualGoodsRefreshReqVO reqVO) {
        ProductSpuDO spu = validateSupportedSpu(reqVO.getSpuId());
        ChannelContext channel = resolveChannel(reqVO.getAppId());
        List<ProductSkuDO> skus = productSkuMapper.selectListBySpuId(spu.getId()).stream()
                .map(sku -> selectSkuById(sku.getId()))
                .toList();
        boolean queryUpload = skus.stream().anyMatch(sku -> isProcessing(sku.getWechatVirtualUploadStatus()));
        boolean queryPublish = skus.stream().anyMatch(sku -> !isProcessing(sku.getWechatVirtualUploadStatus())
                && isProcessing(sku.getWechatVirtualPublishStatus()));

        WxVirtualPayQueryUploadGoodsResponse uploadResponse = null;
        String uploadQueryError = null;
        WxVirtualPayQueryPublishGoodsResponse publishResponse = null;
        String publishQueryError = null;
        WxVirtualPayHttpClient client = createWxVirtualPayHttpClient();
        if (queryUpload) {
            WxVirtualPayQueryUploadGoodsRequest request = new WxVirtualPayQueryUploadGoodsRequest();
            request.setEnv(channel.config.getEnv());
            try {
                uploadResponse = WxVirtualPayAccessTokenUtils.execute(socialClientService,
                        accessToken -> client.queryUploadGoods(accessToken, channel.config, request));
                if (uploadResponse.getStatus() == null) {
                    uploadQueryError = "微信未返回上传任务状态";
                }
            } catch (WxVirtualPayApiException ex) {
                uploadQueryError = sanitizeError(ex.getErrcode(), ex.getMessage());
            } catch (Exception ex) {
                uploadQueryError = sanitizeError(null, ex.getMessage());
            }
        }
        if (queryPublish) {
            WxVirtualPayQueryPublishGoodsRequest request = new WxVirtualPayQueryPublishGoodsRequest();
            request.setEnv(channel.config.getEnv());
            try {
                publishResponse = WxVirtualPayAccessTokenUtils.execute(socialClientService,
                        accessToken -> client.queryPublishGoods(accessToken, channel.config, request));
                if (publishResponse.getStatus() == null) {
                    publishQueryError = "微信未返回发布任务状态";
                }
            } catch (WxVirtualPayApiException ex) {
                publishQueryError = sanitizeError(ex.getErrcode(), ex.getMessage());
            } catch (Exception ex) {
                publishQueryError = sanitizeError(null, ex.getMessage());
            }
        }

        List<ProductWechatVirtualGoodsRespVO> results = new ArrayList<>(skus.size());
        for (ProductSkuDO sku : skus) {
            if (isProcessing(sku.getWechatVirtualUploadStatus())) {
                results.add(uploadQueryError != null
                        ? buildRespVO(sku, "refresh", false, uploadQueryError)
                        : applyUploadQueryResult(sku, uploadResponse));
            } else if (isProcessing(sku.getWechatVirtualPublishStatus())) {
                results.add(publishQueryError != null
                        ? buildRespVO(sku, "refresh", false, publishQueryError)
                        : applyPublishQueryResult(sku, publishResponse));
            } else {
                results.add(buildRespVO(sku, "refresh", true, "当前没有处理中的任务"));
            }
        }
        return results;
    }

    // ==================== 批任务处理 ====================

    private List<ProductWechatVirtualGoodsRespVO> startUploadBatch(ProductSpuDO spu, List<ProductSkuDO> skus,
                                                                   ChannelContext channel) {
        List<ProductWechatVirtualGoodsRespVO> results = new ArrayList<>(skus.size());
        List<ProductSkuDO> candidates = new ArrayList<>();
        List<WxVirtualPayUploadItem> items = new ArrayList<>();
        List<Integer> resultIndexes = new ArrayList<>();
        for (ProductSkuDO sku : skus) {
            String dataError = validateSkuData(spu, sku);
            if (dataError != null) {
                results.add(buildRespVO(sku, "sync", false, "SKU 数据不完整：" + dataError));
                continue;
            }
            try {
                String imageUrl = normalizeAndValidateImageUrl(
                        StrUtil.blankToDefault(sku.getPicUrl(), spu.getPicUrl()));
                candidates.add(sku);
                items.add(buildUploadItem(spu, sku, imageUrl));
                resultIndexes.add(results.size());
                results.add(null);
            } catch (Exception ex) {
                String sanitized = "道具图片校验失败：" + sanitizeError(null, ex.getMessage());
                setUploadFailed(sku.getId(), sanitized);
                results.add(buildRespVO(selectSkuById(sku.getId()), "sync", false, sanitized));
            }
        }
        if (candidates.isEmpty()) {
            return results;
        }

        candidates.forEach(sku -> setUploadProcessing(sku.getId()));
        try {
            WxVirtualPayAccessTokenUtils.execute(socialClientService,
                    accessToken -> createWxVirtualPayHttpClient().startUploadGoods(accessToken, channel.config,
                            buildUploadRequest(items, channel)));
            for (int i = 0; i < candidates.size(); i++) {
                ProductSkuDO sku = candidates.get(i);
                productSkuMapper.updateWechatVirtualGoodsFields(sku.getId(), new ProductSkuDO()
                        .setWechatVirtualUploadTaskId(TASK_ID_RUNNING)
                        .setWechatVirtualLastSyncTime(LocalDateTime.now()));
                results.set(resultIndexes.get(i), buildRespVO(selectSkuById(sku.getId()), "sync", true,
                        "上传批任务已提交，请稍后刷新状态"));
            }
        } catch (Exception ex) {
            String sanitized = ex instanceof WxVirtualPayApiException apiException
                    ? sanitizeError(apiException.getErrcode(), apiException.getMessage())
                    : sanitizeError(null, ex.getMessage());
            for (int i = 0; i < candidates.size(); i++) {
                ProductSkuDO sku = candidates.get(i);
                setUploadFailed(sku.getId(), sanitized);
                results.set(resultIndexes.get(i), buildRespVO(selectSkuById(sku.getId()), "sync", false, sanitized));
            }
        }
        return results;
    }

    private List<ProductWechatVirtualGoodsRespVO> startPublishBatch(List<ProductSkuDO> skus,
                                                                    ChannelContext channel) {
        List<ProductWechatVirtualGoodsRespVO> results = new ArrayList<>(skus.size());
        List<ProductSkuDO> candidates = new ArrayList<>();
        List<WxVirtualPayPublishItem> items = new ArrayList<>();
        List<Integer> resultIndexes = new ArrayList<>();
        for (ProductSkuDO sku : skus) {
            ProductSkuDO current = selectSkuById(sku.getId());
            if (StrUtil.isBlank(current.getWechatVirtualProductId())) {
                results.add(buildRespVO(current, "publish", false, "尚未同步成功，请先同步或刷新"));
                continue;
            }
            if (!isSuccess(current.getWechatVirtualUploadStatus())) {
                results.add(buildRespVO(current, "publish", false, "上传状态不是成功，不能发布"));
                continue;
            }
            candidates.add(current);
            items.add(buildPublishItem(current));
            resultIndexes.add(results.size());
            results.add(null);
        }
        if (candidates.isEmpty()) {
            return results;
        }

        candidates.forEach(sku -> setPublishProcessing(sku.getId()));
        try {
            WxVirtualPayAccessTokenUtils.execute(socialClientService,
                    accessToken -> createWxVirtualPayHttpClient().startPublishGoods(accessToken, channel.config,
                            buildPublishRequest(items, channel)));
            for (int i = 0; i < candidates.size(); i++) {
                ProductSkuDO sku = candidates.get(i);
                productSkuMapper.updateWechatVirtualGoodsFields(sku.getId(), new ProductSkuDO()
                        .setWechatVirtualPublishTaskId(TASK_ID_RUNNING)
                        .setWechatVirtualLastSyncTime(LocalDateTime.now()));
                results.set(resultIndexes.get(i), buildRespVO(selectSkuById(sku.getId()), "publish", true,
                        "发布批任务已提交，请稍后刷新状态"));
            }
        } catch (Exception ex) {
            String sanitized = ex instanceof WxVirtualPayApiException apiException
                    ? sanitizeError(apiException.getErrcode(), apiException.getMessage())
                    : sanitizeError(null, ex.getMessage());
            for (int i = 0; i < candidates.size(); i++) {
                ProductSkuDO sku = candidates.get(i);
                setPublishFailed(sku.getId(), sanitized);
                results.set(resultIndexes.get(i), buildRespVO(selectSkuById(sku.getId()), "publish", false, sanitized));
            }
        }
        return results;
    }

    private ProductWechatVirtualGoodsRespVO applyUploadQueryResult(ProductSkuDO sku,
                                                                   WxVirtualPayQueryUploadGoodsResponse response) {
        // status: 0-无任务 1-运行中 2-失败/部分失败 3-成功
        int status = response.getStatus();
        if (status == 1) {
            productSkuMapper.updateWechatVirtualGoodsFields(sku.getId(), new ProductSkuDO()
                    .setWechatVirtualUploadTaskId(TASK_ID_RUNNING)
                    .setWechatVirtualLastSyncTime(LocalDateTime.now()));
            return buildRespVO(selectSkuById(sku.getId()), "refresh", true,
                    "上传任务仍在处理中");
        }

        WxVirtualPayQueryUploadGoodsResponse.UploadItemResult matched = matchUploadItem(sku, response.getUploadItem());
        if (status == 3) {
            // 任务整体成功
            if (matched == null) {
                String reason = "当前微信上传批次不包含该 SKU，请重新同步";
                setUploadFailed(sku.getId(), reason);
                return buildRespVO(selectSkuById(sku.getId()), "refresh", false, reason);
            }
            Integer uploadStatus = matched.getUploadStatus();
            if (Objects.equals(uploadStatus, 2)) {
                // 上传成功
                productSkuMapper.updateWechatVirtualGoodsFieldsAndClearReviewFailReason(sku.getId(), new ProductSkuDO()
                        .setWechatVirtualProductId(matched.getId())
                        .setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                        .setWechatVirtualReviewStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                        .setWechatVirtualUploadTaskId(null)
                        .setWechatVirtualReviewFailReason(null)
                        .setWechatVirtualLastSyncTime(LocalDateTime.now()));
                return buildRespVO(selectSkuById(sku.getId()), "refresh", true,
                        "上传成功，ProductId=" + matched.getId());
            }
            if (Objects.equals(uploadStatus, 1)) {
                // id 已经存在
                productSkuMapper.updateWechatVirtualGoodsFieldsAndClearReviewFailReason(sku.getId(), new ProductSkuDO()
                        .setWechatVirtualProductId(matched.getId())
                        .setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                        .setWechatVirtualReviewStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                        .setWechatVirtualUploadTaskId(null)
                        .setWechatVirtualReviewFailReason(null)
                        .setWechatVirtualLastSyncTime(LocalDateTime.now()));
                return buildRespVO(selectSkuById(sku.getId()), "refresh", true,
                        "道具 ID 已存在，ProductId=" + matched.getId());
            }
            // 其它状态视为失败
            String reason = matched.getErrmsg() != null ? matched.getErrmsg() : "上传失败";
            setUploadFailed(sku.getId(), reason);
            return buildRespVO(selectSkuById(sku.getId()), "refresh", false, reason);
        }

        if (status == 2) {
            // 任务整体失败或部分失败
            String reason = matched != null && StrUtil.isNotBlank(matched.getErrmsg())
                    ? matched.getErrmsg() : "上传任务失败或部分失败";
            setUploadFailed(sku.getId(), reason);
            return buildRespVO(selectSkuById(sku.getId()), "refresh", false, reason);
        }

        // status == 0 无任务在运行
        setUploadFailed(sku.getId(), "微信端无运行中的上传任务");
        return buildRespVO(selectSkuById(sku.getId()), "refresh", false,
                "微信端无运行中的上传任务");
    }

    private ProductWechatVirtualGoodsRespVO applyPublishQueryResult(ProductSkuDO sku,
                                                                    WxVirtualPayQueryPublishGoodsResponse response) {
        int status = response.getStatus();
        if (status == 1) {
            productSkuMapper.updateWechatVirtualGoodsFields(sku.getId(), new ProductSkuDO()
                    .setWechatVirtualPublishTaskId(TASK_ID_RUNNING)
                    .setWechatVirtualLastSyncTime(LocalDateTime.now()));
            return buildRespVO(selectSkuById(sku.getId()), "refresh", true,
                    "发布任务仍在处理中");
        }

        WxVirtualPayQueryPublishGoodsResponse.PublishItemResult matched = matchPublishItem(sku, response.getPublishItem());
        if (status == 3) {
            if (matched == null) {
                String reason = "当前微信发布批次不包含该 SKU，请重新发布";
                setPublishFailed(sku.getId(), reason);
                return buildRespVO(selectSkuById(sku.getId()), "refresh", false, reason);
            }
            Integer publishStatus = matched.getPublishStatus();
            if (Objects.equals(publishStatus, 2)) {
                productSkuMapper.updateWechatVirtualGoodsFieldsAndClearReviewFailReason(sku.getId(), new ProductSkuDO()
                        .setWechatVirtualPublishStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                        .setWechatVirtualReviewStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                        .setWechatVirtualPublishTaskId(null)
                        .setWechatVirtualReviewFailReason(null)
                        .setWechatVirtualLastSyncTime(LocalDateTime.now()));
                return buildRespVO(selectSkuById(sku.getId()), "refresh", true,
                        "发布成功");
            }
            if (Objects.equals(publishStatus, 1)) {
                productSkuMapper.updateWechatVirtualGoodsFieldsAndClearReviewFailReason(sku.getId(), new ProductSkuDO()
                        .setWechatVirtualPublishStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                        .setWechatVirtualReviewStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                        .setWechatVirtualPublishTaskId(null)
                        .setWechatVirtualReviewFailReason(null)
                        .setWechatVirtualLastSyncTime(LocalDateTime.now()));
                return buildRespVO(selectSkuById(sku.getId()), "refresh", true,
                        "发布成功（道具 ID 已存在）");
            }
            String reason = matched.getErrmsg() != null ? matched.getErrmsg() : "发布失败";
            setPublishFailed(sku.getId(), reason);
            return buildRespVO(selectSkuById(sku.getId()), "refresh", false, reason);
        }

        if (status == 2) {
            String reason = matched != null && StrUtil.isNotBlank(matched.getErrmsg())
                    ? matched.getErrmsg() : "发布任务失败或部分失败";
            setPublishFailed(sku.getId(), reason);
            return buildRespVO(selectSkuById(sku.getId()), "refresh", false, reason);
        }

        setPublishFailed(sku.getId(), "微信端无运行中的发布任务");
        return buildRespVO(selectSkuById(sku.getId()), "refresh", false,
                "微信端无运行中的发布任务");
    }

    // ==================== 请求构造 ====================

    private WxVirtualPayUploadItem buildUploadItem(ProductSpuDO spu, ProductSkuDO sku, String imageUrl) {
        WxVirtualPayUploadItem item = new WxVirtualPayUploadItem();
        item.setId(StrUtil.blankToDefault(sku.getWechatVirtualProductId(), "sku_" + sku.getId()));
        item.setName(buildWechatGoodsName(spu, sku));
        item.setPrice(sku.getPrice());
        item.setRemark(StrUtil.subPre(buildGoodsName(spu, sku), 1024));
        item.setItemUrl(imageUrl);
        return item;
    }

    private WxVirtualPayUploadGoodsRequest buildUploadRequest(List<WxVirtualPayUploadItem> items,
                                                              ChannelContext channel) {
        WxVirtualPayUploadGoodsRequest request = new WxVirtualPayUploadGoodsRequest();
        request.setUploadItem(items);
        request.setEnv(channel.config.getEnv());
        return request;
    }

    private WxVirtualPayPublishItem buildPublishItem(ProductSkuDO sku) {
        WxVirtualPayPublishItem item = new WxVirtualPayPublishItem();
        item.setId(sku.getWechatVirtualProductId());
        return item;
    }

    private WxVirtualPayPublishGoodsRequest buildPublishRequest(List<WxVirtualPayPublishItem> items,
                                                                ChannelContext channel) {
        WxVirtualPayPublishGoodsRequest request = new WxVirtualPayPublishGoodsRequest();
        request.setPublishItem(items);
        request.setEnv(channel.config.getEnv());
        return request;
    }

    // ==================== 渠道与配置 ====================

    private record ChannelContext(PayChannelDO channel, WxVirtualPayClientConfig config) {
    }

    private ChannelContext resolveChannel(Long appId) {
        PayChannelDO channel;
        if (appId != null) {
            channel = payChannelMapper.selectByAppIdAndCode(appId, PayChannelEnum.WX_VIRTUAL_LITE.getCode());
        } else {
            List<PayChannelDO> channels = payChannelMapper.selectListByCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode());
            List<PayChannelDO> enabled = channels.stream()
                    .filter(c -> Objects.equals(c.getStatus(), CommonStatusEnum.ENABLE.getStatus()))
                    .toList();
            if (enabled.isEmpty()) {
                throw exception(WECHAT_VIRTUAL_GOODS_CHANNEL_CONFIG_ERROR);
            }
            if (enabled.size() > 1) {
                throw exception(WECHAT_VIRTUAL_GOODS_CHANNEL_AMBIGUOUS);
            }
            channel = enabled.get(0);
        }
        if (channel == null || !Objects.equals(channel.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(WECHAT_VIRTUAL_GOODS_CHANNEL_CONFIG_ERROR);
        }
        WxVirtualPayClientConfig config = getVirtualConfig(channel);
        validateChannelAppId(config);
        return new ChannelContext(channel, config);
    }

    private WxVirtualPayClientConfig getVirtualConfig(PayChannelDO channel) {
        if (!(channel.getConfig() instanceof WxVirtualPayClientConfig config)) {
            throw exception(WECHAT_VIRTUAL_GOODS_CHANNEL_CONFIG_ERROR);
        }
        if (StrUtil.isBlank(config.getAppid()) || StrUtil.isBlank(config.getOfferId())
                || config.getEnv() == null || StrUtil.isBlank(config.getAppKey())) {
            throw exception(WECHAT_VIRTUAL_GOODS_CHANNEL_CONFIG_ERROR);
        }
        return config;
    }

    private void validateChannelAppId(WxVirtualPayClientConfig config) {
        SocialClientDO client = socialClientMapper.selectBySocialTypeAndUserType(
                SocialTypeEnum.WECHAT_MINI_PROGRAM.getType(), UserTypeEnum.MEMBER.getValue());
        if (client == null || !Objects.equals(client.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(WECHAT_VIRTUAL_GOODS_CHANNEL_APP_ID_MISMATCH, "未找到启用的小程序社交客户端");
        }
        if (!Objects.equals(client.getClientId(), config.getAppid())) {
            throw exception(WECHAT_VIRTUAL_GOODS_CHANNEL_APP_ID_MISMATCH,
                    "小程序社交客户端 AppID 与虚拟支付渠道 AppID 不一致");
        }
    }

    // ==================== 校验与工具 ====================

    private ProductSpuDO validateSupportedSpu(Long spuId) {
        ProductSpuDO spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw exception(SPU_NOT_EXISTS);
        }
        if (!Boolean.TRUE.equals(spu.getIsWechatMiniappVirtualGoods())) {
            throw exception(WECHAT_VIRTUAL_GOODS_UNSUPPORTED_PRODUCT_TYPE);
        }
        return spu;
    }

    private String validateSkuData(ProductSpuDO spu, ProductSkuDO sku) {
        if (StrUtil.isBlank(buildWechatGoodsName(spu, sku))) {
            return "道具名称为空";
        }
        if (sku.getPrice() == null || sku.getPrice() <= 0) {
            return "道具价格必须大于 0";
        }
        String picUrl = StrUtil.blankToDefault(sku.getPicUrl(), spu.getPicUrl());
        if (StrUtil.isBlank(picUrl)) {
            return "SKU 或 SPU 图片为空";
        }
        return null;
    }

    private WxVirtualPayQueryUploadGoodsResponse.UploadItemResult matchUploadItem(
            ProductSkuDO sku, List<WxVirtualPayQueryUploadGoodsResponse.UploadItemResult> items) {
        if (CollUtil.isEmpty(items)) {
            return null;
        }
        String localId = "sku_" + sku.getId();
        String productId = sku.getWechatVirtualProductId();
        return items.stream()
                .filter(item -> Objects.equals(item.getId(), localId)
                        || Objects.equals(item.getId(), productId))
                .findFirst()
                .orElse(null);
    }

    private WxVirtualPayQueryPublishGoodsResponse.PublishItemResult matchPublishItem(
            ProductSkuDO sku, List<WxVirtualPayQueryPublishGoodsResponse.PublishItemResult> items) {
        if (CollUtil.isEmpty(items)) {
            return null;
        }
        String productId = sku.getWechatVirtualProductId();
        return items.stream()
                .filter(item -> Objects.equals(item.getId(), productId))
                .findFirst()
                .orElse(null);
    }

    private ProductSkuDO selectSkuById(Long skuId) {
        ProductSkuDO sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
        return sku;
    }

    private boolean isProcessing(Integer status) {
        return Objects.equals(status, ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus());
    }

    private boolean isSuccess(Integer status) {
        return Objects.equals(status, ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus());
    }

    private void setUploadProcessing(Long skuId) {
        productSkuMapper.updateWechatVirtualGoodsFieldsAndClearReviewFailReason(skuId, new ProductSkuDO()
                .setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus())
                .setWechatVirtualReviewStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus())
                .setWechatVirtualReviewFailReason(null)
                .setWechatVirtualLastSyncTime(LocalDateTime.now()));
    }

    private void setUploadFailed(Long skuId, String reason) {
        productSkuMapper.updateWechatVirtualGoodsFields(skuId, new ProductSkuDO()
                .setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.FAILED.getStatus())
                .setWechatVirtualReviewStatus(ProductSkuWechatVirtualStatusEnum.FAILED.getStatus())
                .setWechatVirtualUploadTaskId(null)
                .setWechatVirtualReviewFailReason(StrUtil.subPre(reason, 500))
                .setWechatVirtualLastSyncTime(LocalDateTime.now()));
    }

    private void setPublishProcessing(Long skuId) {
        productSkuMapper.updateWechatVirtualGoodsFieldsAndClearReviewFailReason(skuId, new ProductSkuDO()
                .setWechatVirtualPublishStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus())
                .setWechatVirtualReviewStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus())
                .setWechatVirtualReviewFailReason(null)
                .setWechatVirtualLastSyncTime(LocalDateTime.now()));
    }

    private void setPublishFailed(Long skuId, String reason) {
        productSkuMapper.updateWechatVirtualGoodsFields(skuId, new ProductSkuDO()
                .setWechatVirtualPublishStatus(ProductSkuWechatVirtualStatusEnum.FAILED.getStatus())
                .setWechatVirtualReviewStatus(ProductSkuWechatVirtualStatusEnum.FAILED.getStatus())
                .setWechatVirtualPublishTaskId(null)
                .setWechatVirtualReviewFailReason(StrUtil.subPre(reason, 500))
                .setWechatVirtualLastSyncTime(LocalDateTime.now()));
    }

    private String sanitizeError(Integer errcode, String message) {
        // 仅返回错误码和脱敏后的官方错误信息，不暴露密钥、token、URL
        String text = StrUtil.nullToDefault(message, "");
        // 移除可能包含 secrets 的片段
        text = text.replaceAll("(?i)(access_token|pay_sig|signature|app_key|appkey|session_key)=[^&\\s]*", "$1=***");
        text = StrUtil.subPre(text, 500);
        if (errcode != null) {
            return "[" + errcode + "] " + text;
        }
        return text;
    }

    // ==================== VO 构造 ====================

    private ProductWechatVirtualGoodsRespVO buildRespVO(ProductSkuDO sku, String action,
                                                        Boolean actionSuccess, String actionMessage) {
        ProductWechatVirtualGoodsRespVO vo = buildRespVO(sku);
        vo.setAction(action);
        vo.setActionSuccess(actionSuccess);
        vo.setActionMessage(actionMessage);
        return vo;
    }

    private ProductWechatVirtualGoodsRespVO buildRespVO(ProductSkuDO sku) {
        return new ProductWechatVirtualGoodsRespVO()
                .setSkuId(sku.getId())
                .setSpuId(sku.getSpuId())
                .setSkuName(buildSkuName(sku))
                .setWechatVirtualProductId(sku.getWechatVirtualProductId())
                .setWechatVirtualUploadTaskId(sku.getWechatVirtualUploadTaskId())
                .setWechatVirtualUploadStatus(sku.getWechatVirtualUploadStatus())
                .setWechatVirtualPublishTaskId(sku.getWechatVirtualPublishTaskId())
                .setWechatVirtualPublishStatus(sku.getWechatVirtualPublishStatus())
                .setWechatVirtualReviewStatus(sku.getWechatVirtualReviewStatus())
                .setWechatVirtualReviewFailReason(sku.getWechatVirtualReviewFailReason())
                .setWechatVirtualLastSyncTime(sku.getWechatVirtualLastSyncTime());
    }

    private String buildGoodsName(ProductSpuDO spu, ProductSkuDO sku) {
        String skuName = buildSkuName(sku);
        if (StrUtil.isBlank(skuName) || "默认".equals(skuName)) {
            return spu.getName();
        }
        return spu.getName() + " " + skuName;
    }

    private String buildWechatGoodsName(ProductSpuDO spu, ProductSkuDO sku) {
        String name = buildGoodsName(spu, sku)
                .replaceAll("[^\\p{IsHan}A-Za-z0-9\\-_*·]", "");
        if (StrUtil.isBlank(name)) {
            name = "sku_" + sku.getId();
        }
        return StrUtil.subPre(name, 20);
    }

    private String buildSkuName(ProductSkuDO sku) {
        if (CollUtil.isEmpty(sku.getProperties())) {
            return "默认";
        }
        return sku.getProperties().stream()
                .map(ProductSkuDO.Property::getValueName)
                .filter(StrUtil::isNotBlank)
                .reduce((left, right) -> left + "/" + right)
                .orElse("默认");
    }

}
