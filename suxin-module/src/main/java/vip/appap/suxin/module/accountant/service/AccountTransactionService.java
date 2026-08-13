package vip.appap.suxin.module.accountant.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountTransactionPageReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionPageReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionSummaryRespVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountTransactionDO;
import vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum;
import vip.appap.suxin.module.accountant.service.bo.AccountTransactionCreateReqBO;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

/**
 * 钱包余额流水 Service 接口
 *
 * @author jason
 */
public interface AccountTransactionService {

    /**
     * 查询钱包余额流水分页
     *
     * @param userId   用户编号
     * @param pageVO   分页查询参数
     */
    PageResult<AccountTransactionDO> getAccountTransactionPage(Long userId,
                                                                AppAccountTransactionPageReqVO pageVO);

    /**
     * 查询钱包余额流水分页
     *
     * @param pageVO   分页查询参数
     */
    PageResult<AccountTransactionDO> getAccountTransactionPage(AccountTransactionPageReqVO pageVO);

    /**
     * 新增钱包余额流水
     *
     * @param bo 创建钱包流水 bo
     * @return 新建的钱包 do
     */
    AccountTransactionDO createAccountTransaction(@Valid AccountTransactionCreateReqBO bo);

    /**
     * 根据 no，获取钱包余流水
     *
     * @param no 流水号
     */
    AccountTransactionDO getAccountTransactionByNo(String no);

    /**
     * 获取钱包流水
     *
     * @param bizId 业务编号
     * @param type  业务类型
     * @return 钱包流水
     */
    AccountTransactionDO getAccountTransaction(String bizId, AccountBizTypeEnum type);

    /**
     * 获得钱包流水统计
     *
     * @param userId 用户编号
     * @param createTime 时间段
     * @return 钱包流水统计
     */
    AppAccountTransactionSummaryRespVO getaccountTransactionSummary(Long userId,
                                                                     LocalDateTime[] createTime);

}

