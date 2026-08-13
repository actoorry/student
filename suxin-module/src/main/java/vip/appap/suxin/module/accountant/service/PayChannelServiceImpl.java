package vip.appap.suxin.module.accountant.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.framework.pay.core.client.PayClient;
import vip.appap.suxin.module.accountant.framework.pay.core.client.PayClientConfig;
import vip.appap.suxin.module.accountant.framework.pay.core.client.PayClientFactory;
import vip.appap.suxin.module.accountant.controller.admin.vo.PayChannelCreateReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.PayChannelUpdateReqVO;
import vip.appap.suxin.module.accountant.convert.PayChannelConvert;
import vip.appap.suxin.module.accountant.dal.dataobject.PayChannelDO;
import vip.appap.suxin.module.accountant.dal.mysql.PayChannelMapper;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.NonePayClientConfig;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxPayClientConfig;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxVirtualPayClientConfig;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.accountant.enums.ErrorCodeConstants.*;

/**
 * 支付渠道 Service 实现类
 *
 * @author aquan
 */
@Service
@Slf4j
@Validated
public class PayChannelServiceImpl implements PayChannelService {

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayChannelMapper payChannelMapper;

    @Resource
    private Validator validator;

    @Override
    public Long createChannel(PayChannelCreateReqVO reqVO) {
        // 断言是否有重复的
        PayChannelDO dbChannel = getChannelByAppIdAndCode(reqVO.getAppId(), reqVO.getCode());
        if (dbChannel != null) {
            throw exception(CHANNEL_EXIST_SAME_CHANNEL_ERROR);
        }

        // 新增渠道
        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(reqVO)
                .setConfig(parseConfig(reqVO.getCode(), reqVO.getConfig()));
        payChannelMapper.insert(channel);
        return channel.getId();
    }

    @Override
    public void updateChannel(PayChannelUpdateReqVO updateReqVO) {
        // 校验存在
        PayChannelDO dbChannel = validateChannelExists(updateReqVO.getId());

        // 更新
        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(updateReqVO)
                .setConfig(parseConfig(dbChannel.getCode(), updateReqVO.getConfig()));
        payChannelMapper.updateById(channel);
    }

    /**
     * 解析并校验配置
     *
     * @param code      渠道编码
     * @param configStr 配置
     * @return 支付配置
     */
    private PayClientConfig parseConfig(String code, String configStr) {
        // 解析配置
        Class<? extends PayClientConfig> payClass = PayChannelEnum.WX_VIRTUAL_LITE.getCode().equals(code) ? WxVirtualPayClientConfig.class
                : PayChannelEnum.isAlipay(code) ? AlipayPayClientConfig.class
                : PayChannelEnum.isWeixin(code) ? WxPayClientConfig.class
                : NonePayClientConfig.class;
        if (ObjectUtil.isNull(payClass)) {
            throw exception(CHANNEL_NOT_FOUND);
        }
        PayClientConfig config = JsonUtils.parseObject2(configStr, payClass);
        Assert.notNull(config);

        // 验证参数
        config.validate(validator);
        return config;
    }

    @Override
    public void deleteChannel(Long id) {
        // 校验存在
        validateChannelExists(id);

        // 删除
        payChannelMapper.deleteById(id);
    }

    private PayChannelDO validateChannelExists(Long id) {
        PayChannelDO channel = payChannelMapper.selectById(id);
        if (channel == null) {
            throw exception(CHANNEL_NOT_FOUND);
        }
        return channel;
    }

    @Override
    public PayChannelDO getChannel(Long id) {
        return payChannelMapper.selectById(id);
    }

    @Override
    public List<PayChannelDO> getChannelListByAppIds(Collection<Long> appIds) {
        return payChannelMapper.selectListByAppIds(appIds);
    }

    @Override
    public PayChannelDO getChannelByAppIdAndCode(Long appId, String code) {
        return payChannelMapper.selectByAppIdAndCode(appId, code);
    }

    @Override
    public PayChannelDO validPayChannel(Long id) {
        PayChannelDO channel = payChannelMapper.selectById(id);
        validPayChannel(channel);
        return channel;
    }

    @Override
    public PayChannelDO validPayChannel(Long appId, String code) {
        PayChannelDO channel = payChannelMapper.selectByAppIdAndCode(appId, code);
        validPayChannel(channel);
        return channel;
    }

    private void validPayChannel(PayChannelDO channel) {
        if (channel == null) {
            throw exception(CHANNEL_NOT_FOUND);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(channel.getStatus())) {
            throw exception(CHANNEL_IS_DISABLE);
        }
    }

    @Override
    public List<PayChannelDO> getEnableChannelList(Long appId) {
        return payChannelMapper.selectListByAppId(appId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public PayClient getPayClient(Long id) {
        PayChannelDO channel = validPayChannel(id);
        return payClientFactory.createOrUpdatePayClient(id, channel.getCode(), channel.getConfig());
    }

}

