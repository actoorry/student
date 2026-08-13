package vip.appap.suxin.module.accountant.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageCreateReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackagePageReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageUpdateReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargePackageDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 钱包充值套餐 Service 接口
 *
 * @author jason
 */
public interface AccountRechargePackageService {

    /**
     * 获取钱包充值套餐
     *
     * @param packageId 充值套餐编号
     */
    AccountRechargePackageDO getAccountRechargePackage(Long packageId);

    /**
     * 校验钱包充值套餐的有效性，无效的话抛出 ServiceException 异常
     *
     * @param packageId 充值套餐编号
     */
    AccountRechargePackageDO validAccountRechargePackage(Long packageId);

    /**
     * 创建充值套餐
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAccountRechargePackage(@Valid AccountRechargePackageCreateReqVO createReqVO);

    /**
     * 更新充值套餐
     *
     * @param updateReqVO 更新信息
     */
    void updateAccountRechargePackage(@Valid AccountRechargePackageUpdateReqVO updateReqVO);

    /**
     * 删除充值套餐
     *
     * @param id 编号
     */
    void deleteAccountRechargePackage(Long id);

    /**
     * 获得充值套餐分页
     *
     * @param pageReqVO 分页查询
     * @return 充值套餐分页
     */
    PageResult<AccountRechargePackageDO> getAccountRechargePackagePage(AccountRechargePackagePageReqVO pageReqVO);

    /**
     * 获得充值套餐列表
     *
     * @param status 状态
     * @return 充值套餐列表
     */
    List<AccountRechargePackageDO> getAccountRechargePackageList(Integer status);

}
