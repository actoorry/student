package vip.appap.suxin.module.accountant.service;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageCreateReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackagePageReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageUpdateReqVO;
import vip.appap.suxin.module.accountant.convert.AccountRechargePackageConvert;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargePackageDO;
import vip.appap.suxin.module.accountant.dal.mysql.AccountRechargePackageMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.accountant.enums.ErrorCodeConstants.*;

/**
 * 钱包充值套餐 Service 实现类
 *
 * @author jason
 */
@Service
public class AccountRechargePackageServiceImpl implements AccountRechargePackageService {

    @Resource
    private AccountRechargePackageMapper accountRechargePackageMapper;

    @Override
    public AccountRechargePackageDO getAccountRechargePackage(Long packageId) {
        return accountRechargePackageMapper.selectById(packageId);
    }

    @Override
    public AccountRechargePackageDO validAccountRechargePackage(Long packageId) {
        AccountRechargePackageDO rechargePackageDO = accountRechargePackageMapper.selectById(packageId);
        if (rechargePackageDO == null) {
            throw exception(WALLET_RECHARGE_PACKAGE_NOT_FOUND);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(rechargePackageDO.getStatus())) {
            throw exception(WALLET_RECHARGE_PACKAGE_IS_DISABLE);
        }
        return rechargePackageDO;
    }

    @Override
    public Long createAccountRechargePackage(AccountRechargePackageCreateReqVO createReqVO) {
        // 校验套餐名是否唯一
        validateRechargePackageNameUnique(null, createReqVO.getName());

        // 插入
        AccountRechargePackageDO accountRechargePackage = AccountRechargePackageConvert.INSTANCE.convert(createReqVO);
        accountRechargePackageMapper.insert(accountRechargePackage);
        // 返回
        return accountRechargePackage.getId();
    }

    @Override
    public void updateAccountRechargePackage(AccountRechargePackageUpdateReqVO updateReqVO) {
        // 校验存在
        validateaccountRechargePackageExists(updateReqVO.getId());
        // 校验套餐名是否唯一
        validateRechargePackageNameUnique(updateReqVO.getId(), updateReqVO.getName());

        // 更新
        AccountRechargePackageDO updateObj = AccountRechargePackageConvert.INSTANCE.convert(updateReqVO);
        accountRechargePackageMapper.updateById(updateObj);
    }

    private void validateRechargePackageNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        AccountRechargePackageDO rechargePackage = accountRechargePackageMapper.selectByName(name);
        if (rechargePackage == null) {
            return ;
        }
        if (id == null) {
            throw exception(WALLET_RECHARGE_PACKAGE_NAME_EXISTS);
        }
        if (!id.equals(rechargePackage.getId())) {
            throw exception(WALLET_RECHARGE_PACKAGE_NAME_EXISTS);
        }
    }

    @Override
    public void deleteAccountRechargePackage(Long id) {
        // 校验存在
        validateaccountRechargePackageExists(id);
        // 删除
        accountRechargePackageMapper.deleteById(id);
    }

    private void validateaccountRechargePackageExists(Long id) {
        if (accountRechargePackageMapper.selectById(id) == null) {
            throw exception(WALLET_RECHARGE_PACKAGE_NOT_FOUND);
        }
    }

    @Override
    public PageResult<AccountRechargePackageDO> getAccountRechargePackagePage(AccountRechargePackagePageReqVO pageReqVO) {
        return accountRechargePackageMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AccountRechargePackageDO> getAccountRechargePackageList(Integer status) {
        return accountRechargePackageMapper.selectListByStatus(status);
    }

}

