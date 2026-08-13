package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import vip.appap.suxin.module.infra.api.file.FileApi;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.system.api.SocialClientApi;
import vip.appap.suxin.module.system.api.dto.SocialWxQrcodeReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_POSTER_CONFIG_NOT_EXISTS;

/**
 * 分销海报 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SalesBrokeragePosterServiceImpl implements SalesBrokeragePosterService {

    private static final int POSTER_WIDTH = 750;
    private static final int POSTER_HEIGHT = 1250;
    private static final int CARD_X = 210;
    private static final int CARD_Y = 890;
    private static final int CARD_WIDTH = 290;
    private static final int CARD_HEIGHT = 330;
    private static final int CARD_RADIUS = 32;
    private static final int QR_SIZE = 210;
    private static final int QR_X = 250;
    private static final int QR_Y = 930;

    @Resource
    private SalesConfigService tradeConfigService;
    @Resource
    private SocialClientApi socialClientApi;
    @Resource
    private FileApi fileApi;

    @Override
    public String generateBrokeragePoster(Long userId) {
        SalesConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        List<String> posterUrls = tradeConfig != null ? tradeConfig.getBrokeragePosterUrls() : null;
        if (CollUtil.isEmpty(posterUrls)) {
            throw exception(BROKERAGE_POSTER_CONFIG_NOT_EXISTS);
        }
        String posterUrl = posterUrls.get(0);
        byte[] qrcodeBytes = generateQrcode(userId);
        byte[] posterBytes = composePoster(posterUrl, qrcodeBytes);
        String fileName = "brokerage-poster-" + userId + ".png";
        return fileApi.createFile(posterBytes, fileName, "brokerage/poster", FileUtil.getMimeType(fileName));
    }

    private byte[] generateQrcode(Long userId) {
        SocialWxQrcodeReqDTO reqDTO = new SocialWxQrcodeReqDTO()
                .setScene("bindUserId=" + userId)
                .setPath("pages/index/index")
                .setWidth(430)
                .setAutoColor(true)
                .setCheckPath(false)
                .setHyaline(true);
        return socialClientApi.getWxaQrcode(reqDTO);
    }

    private byte[] composePoster(String posterUrl, byte[] qrcodeBytes) {
        try {
            BufferedImage backgroundImage = readImageFromUrl(posterUrl);
            BufferedImage qrcodeImage = readImage(qrcodeBytes);
            BufferedImage posterImage = new BufferedImage(POSTER_WIDTH, POSTER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = posterImage.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setColor(new Color(255, 247, 242));
                graphics.fillRect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);
                graphics.drawImage(backgroundImage, 0, 0, POSTER_WIDTH, POSTER_HEIGHT, null);
                graphics.setColor(new Color(255, 255, 255, 245));
                graphics.fill(new RoundRectangle2D.Float(CARD_X, CARD_Y, CARD_WIDTH, CARD_HEIGHT, CARD_RADIUS, CARD_RADIUS));
                graphics.drawImage(qrcodeImage, QR_X, QR_Y, QR_SIZE, QR_SIZE, null);
                graphics.setColor(new Color(122, 103, 96));
                graphics.setFont(new Font("Microsoft YaHei", Font.PLAIN, 28));
                graphics.drawString("微信扫码进入", 268, 1180);
            } finally {
                graphics.dispose();
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(posterImage, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            log.error("[composePoster][生成分销海报失败][posterUrl={}]", posterUrl, ex);
            throw invalidParamException("生成分销海报失败");
        }
    }

    private BufferedImage readImageFromUrl(String posterUrl) throws IOException {
        try (InputStream inputStream = new URL(posterUrl).openStream()) {
            byte[] imageBytes = IoUtil.readBytes(inputStream);
            return readImage(imageBytes);
        }
    }

    private BufferedImage readImage(byte[] imageBytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("image decode failed");
        }
        return image;
    }

}
