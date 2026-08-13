package vip.appap.suxin.module.system.service;

import vip.appap.suxin.framework.tenant.core.context.TenantContextHolder;
import vip.appap.suxin.module.system.api.dto.TenantPointTradeConfigRespDTO;
import vip.appap.suxin.module.system.dal.dataobject.TenantDO;
import vip.appap.suxin.module.system.dal.mysql.TenantMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.TENANT_NOT_EXISTS;

/**
 * 租户积分交易配置 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class TenantConfigServiceImpl implements TenantConfigService {

    /**
     * 积分交易字段的最大有效值
     */
    private static final int MAX_CONFIG_VALUE = 2_147_483_647;

    @Resource
    private TenantMapper tenantMapper;

    @Override
    public TenantPointTradeConfigRespDTO getConfig() {
        TenantDO tenant = getRequiredCurrentTenant();
        TenantPointTradeConfigRespDTO config = new TenantPointTradeConfigRespDTO();
        config.setPointTradeDeductEnable(tenant.getPointTradeDeductEnable());
        config.setPointTradeDeductUnitPrice(tenant.getPointTradeDeductUnitPrice());
        config.setPointTradeDeductMaxPrice(tenant.getPointTradeDeductMaxPrice());
        config.setPointTradeGivePoint(tenant.getPointTradeGivePoint());
        return config;
    }

    @Override
    public void saveConfig(TenantPointTradeConfigRespDTO config) {
        validateConfig(config);
        TenantDO tenant = getRequiredCurrentTenant();
        tenantMapper.updateById(new TenantDO()
                .setId(tenant.getId())
                .setPointTradeDeductEnable(config.getPointTradeDeductEnable())
                .setPointTradeDeductUnitPrice(config.getPointTradeDeductUnitPrice())
                .setPointTradeDeductMaxPrice(config.getPointTradeDeductMaxPrice())
                .setPointTradeGivePoint(config.getPointTradeGivePoint()));
    }

    /**
     * 获取当前租户（必须存在且有效）
     */
    private TenantDO getRequiredCurrentTenant() {
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        TenantDO tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw exception(TENANT_NOT_EXISTS);
        }
        return tenant;
    }

    /**
     * 校验配置值：非空、非负、不超过 Integer.MAX_VALUE
     */
    private void validateConfig(TenantPointTradeConfigRespDTO config) {
        if (config.getPointTradeDeductEnable() == null) {
            throw new IllegalArgumentException("积分抵扣开关不能为空");
        }
        validateNumericField(config.getPointTradeDeductUnitPrice(), "积分抵扣单价");
        validateNumericField(config.getPointTradeDeductMaxPrice(), "积分抵扣最大值");
        validateNumericField(config.getPointTradeGivePoint(), "每元赠送积分");
    }

    private void validateNumericField(Integer value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + "不能为负数");
        }
        if (value > MAX_CONFIG_VALUE) {
            throw new IllegalArgumentException(fieldName + "不能超过 " + MAX_CONFIG_VALUE);
        }
    }

}
