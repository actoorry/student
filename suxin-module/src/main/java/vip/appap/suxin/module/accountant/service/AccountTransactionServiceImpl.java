package vip.appap.suxin.module.accountant.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountTransactionPageReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionPageReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionSummaryRespVO;
import vip.appap.suxin.module.accountant.convert.AccountTransactionConvert;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountTransactionDO;
import vip.appap.suxin.module.accountant.dal.mysql.AccountTransactionMapper;
import vip.appap.suxin.module.accountant.dal.redis.no.PayNoRedisDAO;
import vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum;
import vip.appap.suxin.module.accountant.service.bo.AccountTransactionCreateReqBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionPageReqVO.TYPE_EXPENSE;
import static vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionPageReqVO.TYPE_INCOME;

/**
 * 钱包流水 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
@Validated
public class AccountTransactionServiceImpl implements AccountTransactionService {

    private static final String WALLET_NO_PREFIX = "W";

    @Resource
    private AccountService AccountService;
    @Resource
    private AccountTransactionMapper AccountTransactionMapper;
    @Resource
    private PayNoRedisDAO noRedisDAO;

    @Override
    public PageResult<AccountTransactionDO> getAccountTransactionPage(Long userId,
                                                                       AppAccountTransactionPageReqVO pageVO) {
        AccountDO account = AccountService.getOrCreateAccount(userId);
        return AccountTransactionMapper.selectPage(account.getId(), pageVO.getType(), pageVO, pageVO.getCreateTime());
    }

    @Override
    public PageResult<AccountTransactionDO> getAccountTransactionPage(AccountTransactionPageReqVO pageVO) {
        // 基于 userId 查询钱包
        if (pageVO.getAccountId() == null && pageVO.getUserId() != null) {
            AccountDO account = AccountService.getOrCreateAccount(pageVO.getUserId());
            if (account != null) {
                pageVO.setAccountId(account.getId());
            }
        }

        // 查询分页
        return AccountTransactionMapper.selectPage(pageVO.getAccountId(), null, pageVO, null);
    }

    @Override
    public AccountTransactionDO createAccountTransaction(AccountTransactionCreateReqBO bo) {
        AccountTransactionDO transaction = AccountTransactionConvert.INSTANCE.convert(bo)
                .setNo(noRedisDAO.generate(WALLET_NO_PREFIX));
        AccountTransactionMapper.insert(transaction);
        return transaction;
    }

    @Override
    public AccountTransactionDO getAccountTransactionByNo(String no) {
        return AccountTransactionMapper.selectByNo(no);
    }

    @Override
    public AccountTransactionDO getAccountTransaction(String bizId, AccountBizTypeEnum type) {
        return AccountTransactionMapper.selectByBiz(bizId, type.getType());
    }

    @Override
    public AppAccountTransactionSummaryRespVO getaccountTransactionSummary(Long userId, LocalDateTime[] createTime) {
        AccountDO account = AccountService.getOrCreateAccount(userId);
        return new AppAccountTransactionSummaryRespVO()
                .setTotalExpense(AccountTransactionMapper.selectPriceSum(account.getId(), TYPE_EXPENSE, createTime))
                .setTotalIncome(AccountTransactionMapper.selectPriceSum(account.getId(), TYPE_INCOME, createTime));
    }

}
