package vip.appap.suxin.module.accountant.api;

import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.accountant.api.dto.AccountAddBalanceReqDTO;
import vip.appap.suxin.module.accountant.api.dto.AccountRespDTO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum;
import vip.appap.suxin.module.accountant.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 钱包 API 实现类
 *
 * @author 书心软件
 */
@Service
public class AccountApiImpl implements AccountApi {

    @Resource
    private AccountService AccountService;

    @Override
    public void addAccountBalance(AccountAddBalanceReqDTO reqDTO) {
        // 创建或获取钱包
        AccountDO account = AccountService.getOrCreateAccount(reqDTO.getUserId());
        Assert.notNull(account, "钱包({})不存在", reqDTO.getUserId());

        // 增加余额
        AccountBizTypeEnum bizType = AccountBizTypeEnum.valueOf(reqDTO.getBizType());
        AccountService.addAccountBalance(account.getId(), reqDTO.getBizId(), bizType, reqDTO.getPrice());
    }

    @Override
    public AccountRespDTO getOrCreateAccount(Long userId) {
        AccountDO account = AccountService.getOrCreateAccount(userId);
        return BeanUtils.toBean(account, AccountRespDTO.class);
    }

}

