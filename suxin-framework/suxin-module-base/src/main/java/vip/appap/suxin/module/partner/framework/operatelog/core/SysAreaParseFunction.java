package vip.appap.suxin.module.partner.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;

/**
 * 地名的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Slf4j
@Component
public class SysAreaParseFunction implements IParseFunction {

    public static final String NAME = "getArea";

    @Override
    public boolean executeBefore() {
        return true; // 先转换值后对比
    }

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }
        return AreaUtils.format(Integer.parseInt(value.toString()));
    }

}
