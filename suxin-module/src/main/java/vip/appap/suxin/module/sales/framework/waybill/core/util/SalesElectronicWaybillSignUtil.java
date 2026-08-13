package vip.appap.suxin.module.sales.framework.waybill.core.util;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.experimental.UtilityClass;

/**
 * 快递100电子面单签名工具
 * <p>
 * 官方公式（对应 Demo {@code SignUtils.printSign}）：sign = MD5(param + t + key + secret)，结果转大写。
 *
 * @author 书心软件
 */
@UtilityClass
public class SalesElectronicWaybillSignUtil {

    /**
     * 下单/复打/取消签名
     *
     * @param param  业务参数 JSON
     * @param t      当前请求时间戳
     * @param key    快递100授权 key
     * @param secret 快递100电子面单密钥
     * @return 大写 MD5 签名
     */
    public static String printSign(String param, String t, String key, String secret) {
        String plainText = param + t + key + secret;
        return DigestUtil.md5Hex(plainText).toUpperCase();
    }

}
