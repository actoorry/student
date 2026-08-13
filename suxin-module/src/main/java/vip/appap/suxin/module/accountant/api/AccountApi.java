package vip.appap.suxin.module.accountant.api;

import vip.appap.suxin.module.accountant.api.dto.AccountAddBalanceReqDTO;
import vip.appap.suxin.module.accountant.api.dto.AccountRespDTO;

/**
 * 钱包 API 接口
 *
 * @author liurulin
 */
public interface AccountApi {

    /**
     * 添加钱包余额
     *
     * @param reqDTO 增加余额请求
     */
    void addAccountBalance(AccountAddBalanceReqDTO reqDTO);

    /**
     * 获取钱包信息
     *
     * @param userId 用户编号
     * @return 钱包信息
     */
    AccountRespDTO getOrCreateAccount(Long userId);

}

