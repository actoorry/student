package vip.appap.suxin.module.partner.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.service.PartnerService;

/**
 * CRM 客户的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class CrmCustomerParseFunction implements IParseFunction {

    public static final String NAME = "getCustomerById";

    @Resource
    private PartnerService partnerService;

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
        PartnerDO partner = partnerService.getPartner(Long.parseLong(value.toString()));
        if (partner == null) {
            return "";
        }
        return StrUtil.nullToEmpty(partner.getName());
    }

}
