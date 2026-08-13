package vip.appap.suxin.module.crm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.annotations.VisibleForTesting;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmBusinessTransferReqVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContactTransferReqVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContractTransferReqVO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmBusinessDO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmContactDO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmContractDO;
import vip.appap.suxin.module.crm.enums.CrmBizTypeEnum;
import vip.appap.suxin.module.crm.enums.CrmPermissionLevelEnum;
import vip.appap.suxin.module.crm.enums.CrmSceneTypeEnum;
import vip.appap.suxin.module.crm.framework.permission.core.annotations.CrmPermission;
import vip.appap.suxin.module.crm.service.bo.CrmPermissionCreateReqBO;
import vip.appap.suxin.module.crm.service.bo.CrmPermissionTransferReqBO;
import vip.appap.suxin.module.partner.controller.admin.vo.*;
import vip.appap.suxin.module.partner.convert.PartnerConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesLimitConfigDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesPoolConfigDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.service.PartnerSalesLimitConfigService;
import vip.appap.suxin.module.partner.service.PartnerSalesPoolConfigService;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.partner.service.bo.PartnerSalesCreateReqBO;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.SmsCodeApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import vip.appap.suxin.module.system.api.dto.SmsCodeUseReqDTO;
import vip.appap.suxin.module.system.enums.SmsSceneEnum;
import vip.appap.suxin.module.system.service.AdminUserService;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Collections.singletonList;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.filterList;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;
import static vip.appap.suxin.module.crm.enums.ErrorCodeConstants.*;
import static vip.appap.suxin.module.crm.enums.LogRecordConstants.*;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.*;
import static vip.appap.suxin.module.partner.enums.PartnerSalesLimitConfigTypeEnum.CUSTOMER_LOCK_LIMIT;
import static vip.appap.suxin.module.partner.enums.PartnerSalesLimitConfigTypeEnum.CUSTOMER_OWNER_LIMIT;

/**
 * 合作伙伴 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Valid
@Slf4j
public class PartnerServiceImpl implements PartnerService {

    @Resource
    private PartnerMapper partnerMapper;

    // ==================== CRM 业务依赖 ====================
    @Resource
    private CrmPermissionService permissionService;
    @Resource
    private PartnerSalesLimitConfigService partnerSalesLimitConfigService;
    @Resource
    @Lazy
    private PartnerSalesPoolConfigService partnerSalesPoolConfigService;
    @Resource
    @Lazy
    private CrmContactService contactService;
    @Resource
    @Lazy
    private CrmBusinessService businessService;
    @Resource
    @Lazy
    private CrmContractService contractService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private SmsCodeApi smsCodeApi;
    @Resource
    @Lazy // 懒加载，避免与 AdminUserServiceImpl -> PartnerService 形成循环依赖
    private AdminUserService adminUserService;

    @Override
    public PartnerDO getPartnerByMobile(String mobile) {
        return partnerMapper.selectByMobile(mobile);
    }

    @Override
    public List<PartnerDO> getPartnerListByNickname(String nickname) {
        return partnerMapper.selectListByNicknameLike(nickname);
    }

    @Override
    public PartnerDO getPartner(Long id) {
        return partnerMapper.selectById(id);
    }

    @Override
    public List<PartnerDO> getPartnerList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return partnerMapper.selectByIds(ids);
    }

    @Override
    public Long createPartner(PartnerCreateReqVO createReqVO) {
        // 校验手机唯一
        validateMobileUnique(null, createReqVO.getMobile());
        // 校验邮箱唯一
        validateEmailUnique(null, createReqVO.getEmail());

        // 插入
        PartnerDO user = PartnerConvert.INSTANCE.convert(createReqVO);
        if (user.getStatus() == null) {
            user.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        partnerMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePartner(PartnerUpdateReqVO updateReqVO) {
        // 校验存在
        PartnerDO oldPartner = validatePartnerExists(updateReqVO.getId());
        // 校验手机唯一
        validateMobileUnique(updateReqVO.getId(), updateReqVO.getMobile());
        // 校验邮箱唯一
        validateEmailUnique(updateReqVO.getId(), updateReqVO.getEmail());

        // 更新
        PartnerDO updateObj = PartnerConvert.INSTANCE.convert(updateReqVO);
        partnerMapper.updateById(updateObj);
    }

    @Override
    public void updatePartnerProfile(Long userId, String nickname, String avatar) {
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(userId);
        updateObj.setNickname(nickname);
        updateObj.setAvatar(avatar);
        partnerMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePartnerMobile(Long userId, String mobile, String code) {
        // 校验当前用户存在
        PartnerDO partner = validatePartnerExists(userId);
        // 校验新手机号未被其他用户占用
        validateMobileUnique(userId, mobile);
        // 消费目标手机号对应的场景 2 验证码（同一事务，业务失败时消费一并回滚）
        smsCodeApi.useSmsCode(new SmsCodeUseReqDTO()
                .setMobile(mobile)
                .setCode(code)
                .setScene(SmsSceneEnum.MEMBER_UPDATE_MOBILE.getScene())
                .setUsedIp(getClientIP()));
        // 更新 partner 手机号
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(userId);
        updateObj.setMobile(mobile);
        partnerMapper.updateById(updateObj);
        // 同步共享主键 system user 的用户名：会员自动注册时 username = 手机号，
        // 若旧 username 仍为旧手机号，则同步为新手机号，保持会员账号 username=手机号 的不变式，
        // 避免后续使用旧手机号注册时命中 system_users.username 唯一性校验（USER_USERNAME_EXISTS）
        adminUserService.updateUserUsernameIfMobile(userId, partner.getMobile(), mobile);
    }

    @Override
    public void updatePartnerMobileByWeixin(Long userId, String code) {
        // TODO: 调用微信 API 获取手机号
        log.warn("[updatePartnerMobileByWeixin][userId({}) code({}) 微信手机号更新暂未实现]", userId, code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePartnerPassword(Long userId, String code, String newPassword) {
        // 校验当前用户存在
        PartnerDO partner = validatePartnerExists(userId);
        // 消费当前手机号对应的场景 3 验证码（同一事务，业务失败时消费一并回滚）
        smsCodeApi.useSmsCode(new SmsCodeUseReqDTO()
                .setMobile(partner.getMobile())
                .setCode(code)
                .setScene(SmsSceneEnum.MEMBER_UPDATE_PASSWORD.getScene())
                .setUsedIp(getClientIP()));
        // 更新共享主键 system user 的密码
        adminUserService.updateUserPassword(userId, newPassword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPartnerPassword(String mobile, String code, String newPassword) {
        // 按手机号定位 member partner
        PartnerDO partner = partnerMapper.selectByMobile(mobile);
        if (partner == null || !Boolean.TRUE.equals(partner.getIsMember())) {
            throw exception(USER_MOBILE_NOT_EXISTS);
        }
        // 消费手机号对应的场景 4 验证码（同一事务，业务失败时消费一并回滚）
        smsCodeApi.useSmsCode(new SmsCodeUseReqDTO()
                .setMobile(mobile)
                .setCode(code)
                .setScene(SmsSceneEnum.MEMBER_RESET_PASSWORD.getScene())
                .setUsedIp(getClientIP()));
        // 更新共享主键 system user 的密码
        adminUserService.updateUserPassword(partner.getId(), newPassword);
    }

    @Override
    public void deletePartner(Long id) {
        validatePartnerExists(id);
        partnerMapper.deleteById(id);
    }

    @VisibleForTesting
    PartnerDO validatePartnerExists(Long id) {
        if (id == null) {
            return null;
        }
        PartnerDO user = partnerMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        return user;
    }

    @Override
    @VisibleForTesting
    public void validateMobileUnique(Long id, String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return;
        }
        PartnerDO user = partnerMapper.selectByMobile(mobile);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_MOBILE_USED, mobile);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_MOBILE_USED, mobile);
        }
    }

    @Override
    public void validateEmailUnique(Long id, String email) {
        if (StrUtil.isBlank(email)) {
            return;
        }
        PartnerDO user = partnerMapper.selectByEmail(email);
        if (user == null) {
            return;
        }
        if (id == null) {
            throw exception(USER_EMAIL_USED, email);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_EMAIL_USED, email);
        }
    }

    @Override
    public PageResult<PartnerDO> getPartnerPage(PartnerPageReqVO pageReqVO) {
        return partnerMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PartnerDO> getPartnerListWithoutUser() {
        return partnerMapper.selectListWithoutUser();
    }

    @Override
    public Map<Long, PartnerDO> getPartnerMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<PartnerDO> users = partnerMapper.selectByIds(ids);
        return CollectionUtils.convertMap(users, PartnerDO::getId);
    }

    @Override
    public PartnerDO getCurrentPartner() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return null;
        }
        return getPartner(userId);
    }

    // ==================== CRM 客户业务方法（原 PartnerSalesServiceImpl） ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_CREATE_SUB_TYPE, bizNo = "{{#partner.id}}",
            success = CRM_CUSTOMER_CREATE_SUCCESS)
    public Long createPartner(PartnerSalesSaveReqVO createReqVO, Long userId) {
        createReqVO.setId(null);
        // 1. 校验拥有客户是否到达上限
        validatePartnerExceedOwnerLimit(createReqVO.getOwnerUserId(), 1);

        // 2. 插入 partner（含 CRM 业务字段，负责人用 ownerUserId）
        PartnerDO partner = initPartnerFromSales(createReqVO, createReqVO.getOwnerUserId());
        partner.setIsCustomer(true);
        partner.setIsCompany(true);
        partner.setStatus(CommonStatusEnum.ENABLE.getStatus());
        partnerMapper.insert(partner);

        // 3. 创建数据权限（赋给负责人，不是登录用户）
        permissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .setBizId(partner.getId()).setUserId(createReqVO.getOwnerUserId()).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        // 4. 记录操作日志上下文
        putPartnerLogContext(partner);
        return partner.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_CREATE_SUB_TYPE, bizNo = "{{#partner.id}}",
            success = CRM_CUSTOMER_CREATE_SUCCESS)
    public Long createPartner(PartnerSalesCreateReqBO createReqBO, Long userId) {
        // 1. 插入 partner（含 CRM 业务字段）
        PartnerDO partner = initPartnerFromSalesBO(createReqBO, userId);
        partner.setIsCustomer(true);
        partner.setIsCompany(true);
        partner.setStatus(CommonStatusEnum.ENABLE.getStatus());
        partnerMapper.insert(partner);

        // 2. 创建数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .setBizId(partner.getId()).setUserId(userId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        // 3. 记录操作日志上下文
        putPartnerLogContext(partner);
        return partner.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_CREATE_SUB_TYPE, bizNo = "{{#partner.id}}",
            success = CRM_CUSTOMER_CREATE_SUCCESS)
    public Long createPartner(PartnerSalesCreateReqBO createReqBO, Long userId, Long partnerId) {
        // 1. 查询已有 partner（线索转化场景，partner 已由 CrmClueServiceImpl 创建）
        PartnerDO partner = validatePartnerExists(partnerId);
        // 2. 设置 CRM 业务字段
        BeanUtils.copyProperties(createReqBO, partner);
        partner.setOwnerUserId(userId);
        partner.setOwnerTime(LocalDateTime.now());
        partnerMapper.updateById(partner);

        // 3. 创建数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .setBizId(partner.getId()).setUserId(userId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        // 4. 记录操作日志上下文
        putPartnerLogContext(partner);
        return partner.getId();
    }

    /**
     * 从 CRM 客户 VO 初始化 partner（新创建用）
     */
    private PartnerDO initPartnerFromSales(PartnerSalesSaveReqVO reqVO, Long ownerUserId) {
        PartnerDO partner = BeanUtils.toBean(reqVO, PartnerDO.class);
        partner.setId(null); // 自增主键
        partner.setOwnerUserId(ownerUserId);
        partner.setOwnerTime(LocalDateTime.now());
        partner.setFollowUpStatus(false);
        partner.setLockStatus(false);
        partner.setDealStatus(false);
        partner.setSalesLevel(reqVO.getLevel());
        partner.setSalesSource(reqVO.getSource());
        return partner;
    }

    /**
     * 从 CRM 客户 BO 初始化 partner（新创建用）
     */
    private PartnerDO initPartnerFromSalesBO(PartnerSalesCreateReqBO reqBO, Long ownerUserId) {
        PartnerDO partner = BeanUtils.toBean(reqBO, PartnerDO.class);
        partner.setId(null);
        partner.setOwnerUserId(ownerUserId);
        partner.setOwnerTime(LocalDateTime.now());
        partner.setFollowUpStatus(false);
        partner.setLockStatus(false);
        partner.setDealStatus(false);
        partner.setSalesLevel(reqBO.getLevel());
        partner.setSalesSource(reqBO.getSource());
        return partner;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_CUSTOMER_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updatePartner(PartnerSalesSaveReqVO updateReqVO) {
        Assert.notNull(updateReqVO.getId(), "客户编号不能为空");
        updateReqVO.setOwnerUserId(null);
        // 1. 校验存在
        PartnerDO oldPartner = validatePartnerExists(updateReqVO.getId());

        // 2. 更新 partner（含 CRM 业务字段）
        PartnerDO updateObj = BeanUtils.toBean(updateReqVO, PartnerDO.class);
        updateObj.setSalesLevel(updateReqVO.getLevel());
        updateObj.setSalesSource(updateReqVO.getSource());
        partnerMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        updateReqVO.setOwnerUserId(oldPartner.getOwnerUserId());
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, buildPartnerSalesSaveReqVO(updateReqVO.getId()));
        LogRecordContext.putVariable("customerName", getPartnerName(updateReqVO.getId()));
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_UPDATE_DEAL_STATUS_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CUSTOMER_UPDATE_DEAL_STATUS_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.WRITE)
    public void updatePartnerDealStatus(Long id, Boolean dealStatus) {
        PartnerDO partner = validatePartnerExists(id);
        if (Objects.equals(partner.getDealStatus(), dealStatus)) {
            throw exception(CUSTOMER_UPDATE_DEAL_STATUS_FAIL);
        }
        partnerMapper.updateById(new PartnerDO().setId(id).setDealStatus(dealStatus));
        LogRecordContext.putVariable("customerName", getPartnerName(id));
        LogRecordContext.putVariable("dealStatus", dealStatus);
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_FOLLOW_UP_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CUSTOMER_FOLLOW_UP_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.WRITE)
    public void updatePartnerFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent) {
        validatePartnerExists(id);
        partnerMapper.updateById(new PartnerDO().setId(id).setFollowUpStatus(true).setContactNextTime(contactNextTime)
                .setContactLastTime(LocalDateTime.now()).setContactLastContent(contactLastContent));
        LogRecordContext.putVariable("customerName", getPartnerName(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_TRANSFER_SUB_TYPE, bizNo = "{{#reqVO.id}}",
            success = CRM_CUSTOMER_TRANSFER_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#reqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void transferPartner(PartnerSalesTransferReqVO reqVO, Long userId) {
        PartnerDO partner = validatePartnerExists(reqVO.getId());
        validatePartnerExceedOwnerLimit(reqVO.getNewOwnerUserId(), 1);
        permissionService.transferPermission(new CrmPermissionTransferReqBO(userId, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                reqVO.getId(), reqVO.getNewOwnerUserId(), reqVO.getOldOwnerPermissionLevel()));
        partnerMapper.updateById(new PartnerDO().setId(reqVO.getId())
                .setOwnerUserId(reqVO.getNewOwnerUserId()).setOwnerTime(LocalDateTime.now()));

        if (CollUtil.isNotEmpty(reqVO.getToBizTypes())) {
            transferPartnerRelations(reqVO, userId);
        }
        putPartnerLogContext(partner);
        LogRecordContext.putVariable("customerName", getPartnerName(partner.getId()));
    }

    private void transferPartnerRelations(PartnerSalesTransferReqVO reqVO, Long userId) {
        if (reqVO.getToBizTypes().contains(CrmBizTypeEnum.CRM_CONTACT.getType())) {
            List<CrmContactDO> contactList = contactService.getContactListByCustomerIdOwnerUserId(reqVO.getId(), userId);
            contactList.forEach(item -> contactService.transferContact(new CrmContactTransferReqVO(item.getId(), reqVO.getNewOwnerUserId(),
                    reqVO.getOldOwnerPermissionLevel()), userId));
        }
        if (reqVO.getToBizTypes().contains(CrmBizTypeEnum.CRM_BUSINESS.getType())) {
            List<CrmBusinessDO> businessList = businessService.getBusinessListByCustomerIdOwnerUserId(reqVO.getId(), userId);
            businessList.forEach(item -> businessService.transferBusiness(new CrmBusinessTransferReqVO(item.getId(), reqVO.getNewOwnerUserId(),
                    reqVO.getOldOwnerPermissionLevel()), userId));
        }
        if (reqVO.getToBizTypes().contains(CrmBizTypeEnum.CRM_CONTRACT.getType())) {
            List<CrmContractDO> contractList = contractService.getContractListByCustomerIdOwnerUserId(reqVO.getId(), userId);
            contractList.forEach(item -> contractService.transferContract(new CrmContractTransferReqVO(item.getId(), reqVO.getNewOwnerUserId(),
                    reqVO.getOldOwnerPermissionLevel()), userId));
        }
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_LOCK_SUB_TYPE, bizNo = "{{#lockReqVO.id}}",
            success = CRM_CUSTOMER_LOCK_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#lockReqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void lockPartner(PartnerSalesLockReqVO lockReqVO, Long userId) {
        PartnerDO partner = validatePartnerExists(lockReqVO.getId());
        if (partner.getLockStatus().equals(lockReqVO.getLockStatus())) {
            throw exception(partner.getLockStatus() ? CUSTOMER_LOCK_FAIL_IS_LOCK : CUSTOMER_UNLOCK_FAIL_IS_UNLOCK);
        }
        if (lockReqVO.getLockStatus()) {
            validatePartnerExceedLockLimit(userId);
        }
        partnerMapper.updateById(BeanUtils.toBean(lockReqVO, PartnerDO.class));
        putPartnerLogContext(partner);
    }

    @Override
    public PageResult<PartnerDO> getPartnerSalesPage(PartnerSalesPageReqVO pageReqVO, Long userId) {
        return partnerMapper.selectSalesPage(pageReqVO, userId);
    }

    @Override
    public PageResult<PartnerDO> getPutPoolRemindPartnerPage(PartnerSalesPageReqVO pageVO, Long userId) {
        PartnerSalesPoolConfigDO poolConfig = partnerSalesPoolConfigService.getCustomerPoolConfig();
        if (ObjUtil.isNull(poolConfig) || Boolean.FALSE.equals(poolConfig.getEnabled())
                || Boolean.FALSE.equals(poolConfig.getNotifyEnabled())) {
            return PageResult.empty();
        }
        return partnerMapper.selectPutPoolRemindPartnerPage(pageVO, poolConfig, userId);
    }

    @Override
    public Long getPutPoolRemindPartnerCount(Long userId) {
        PartnerSalesPoolConfigDO poolConfig = partnerSalesPoolConfigService.getCustomerPoolConfig();
        if (ObjUtil.isNull(poolConfig) || Boolean.FALSE.equals(poolConfig.getEnabled())
                || Boolean.FALSE.equals(poolConfig.getNotifyEnabled())) {
            return 0L;
        }
        PartnerSalesPageReqVO pageVO = new PartnerSalesPageReqVO()
                .setPool(null).setContactStatus(PartnerSalesPageReqVO.CONTACT_TODAY)
                .setSceneType(CrmSceneTypeEnum.OWNER.getType());
        return partnerMapper.selectPutPoolRemindPartnerCount(pageVO, poolConfig, userId);
    }

    @Override
    public Long getTodayContactPartnerCount(Long userId) {
        return partnerMapper.selectCountByTodayContact(userId);
    }

    @Override
    public Long getFollowPartnerCount(Long userId) {
        return partnerMapper.selectCountByFollow(userId);
    }

    @Override
    public void validatePartner(Long id) {
        validatePartnerExists(id);
    }

    @Override
    public PartnerDO validatePartnerExistsAndEnable(Long id) {
        PartnerDO partner = validatePartnerExists(id);
        if (partner != null && !CommonStatusEnum.ENABLE.getStatus().equals(partner.getStatus())) {
            throw exception(PARTNER_IS_DISABLE);
        }
        return partner;
    }

    // ==================== 公海相关操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_POOL_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CUSTOMER_POOL_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void putPartnerPool(Long id) {
        PartnerDO partner = partnerMapper.selectById(id);
        if (partner == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        validatePartnerOwnerExists(partner, true);
        validatePartnerIsLocked(partner, true);
        doPutPartnerPool(partner);
        LogRecordContext.putVariable("customerName", getPartnerName(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receivePartner(List<Long> ids, Long ownerUserId, Boolean isReceive) {
        List<PartnerDO> partners = partnerMapper.selectByIds(ids);
        if (partners.size() != ids.size()) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        adminUserApi.validateUserList(singletonList(ownerUserId));
        partners.forEach(partner -> {
            validatePartnerOwnerExists(partner, false);
            validatePartnerIsLocked(partner, false);
            validatePartnerDeal(partner);
        });
        validatePartnerExceedOwnerLimit(ownerUserId, partners.size());

        List<PartnerDO> updatePartners = new ArrayList<>();
        List<CrmPermissionCreateReqBO> createPermissions = new ArrayList<>();
        partners.forEach(partner -> {
            updatePartners.add(new PartnerDO().setId(partner.getId())
                    .setOwnerUserId(ownerUserId).setOwnerTime(LocalDateTime.now()));
            createPermissions.add(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                    .setBizId(partner.getId()).setUserId(ownerUserId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        });
        partnerMapper.updateBatch(updatePartners);
        permissionService.createPermissionBatch(createPermissions);

        AdminUserRespDTO user = null;
        if (!isReceive) {
            user = adminUserApi.getUser(ownerUserId);
        }
        for (PartnerDO partner : partners) {
            getSelf().receivePartnerLog(partner, user == null ? null : user.getNickname());
        }
    }

    @Override
    public int autoPutPartnerPool() {
        PartnerSalesPoolConfigDO poolConfig = partnerSalesPoolConfigService.getCustomerPoolConfig();
        if (poolConfig == null || !poolConfig.getEnabled()) {
            return 0;
        }
        List<PartnerDO> partnerList = partnerMapper.selectListByAutoPool(poolConfig);
        int count = 0;
        for (PartnerDO partner : partnerList) {
            try {
                getSelf().doPutPartnerPool(partner);
                count++;
            } catch (Throwable e) {
                log.error("[autoPutPartnerPool][客户({}) 放入公海异常]", partner.getId(), e);
            }
        }
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void doPutPartnerPool(PartnerDO partner) {
        int updateOwnerUserIncr = partnerMapper.updateOwnerUserIdById(partner.getId(), null);
        if (updateOwnerUserIncr == 0) {
            throw exception(CUSTOMER_UPDATE_OWNER_USER_FAIL);
        }
        contactService.updateOwnerUserIdByCustomerId(partner.getId(), null);
        permissionService.deletePermission(CrmBizTypeEnum.CRM_CUSTOMER.getType(), partner.getId(),
                CrmPermissionLevelEnum.OWNER.getLevel());
    }

    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_RECEIVE_SUB_TYPE, bizNo = "{{#partner.id}}",
            success = CRM_CUSTOMER_RECEIVE_SUCCESS)
    public void receivePartnerLog(PartnerDO partner, String ownerUserName) {
        putPartnerLogContext(partner);
        LogRecordContext.putVariable("ownerUserName", ownerUserName);
    }

    // ==================== 导入相关操作 ====================

    @Override
    public PartnerSalesImportRespVO importPartnerList(List<PartnerSalesImportExcelVO> importPartners,
                                                       PartnerSalesImportReqVO importReqVO) {
        importPartners = filterList(importPartners, item -> Objects.nonNull(item.getName()));
        if (CollUtil.isEmpty(importPartners)) {
            throw exception(CUSTOMER_IMPORT_LIST_IS_EMPTY);
        }
        PartnerSalesImportRespVO respVO = PartnerSalesImportRespVO.builder().createCustomerNames(new ArrayList<>())
                .updateCustomerNames(new ArrayList<>()).failureCustomerNames(new LinkedHashMap<>()).build();
        importPartners.forEach(importPartner -> {
            try {
                validatePartnerForCreate(importPartner);
            } catch (ServiceException ex) {
                respVO.getFailureCustomerNames().put(importPartner.getName(), ex.getMessage());
                return;
            }
            PartnerDO existPartner = partnerMapper.selectByPartnerName(importPartner.getName());
            if (existPartner == null) {
                PartnerDO partner = BeanUtils.toBean(importPartner, PartnerDO.class);
                partner.setIsCustomer(true);
                partner.setIsCompany(true);
                partner.setStatus(CommonStatusEnum.ENABLE.getStatus());
                partner.setSalesLevel(importPartner.getLevel());
                partner.setSalesSource(importPartner.getSource());
                partner.setOwnerUserId(importReqVO.getOwnerUserId());
                partner.setOwnerTime(LocalDateTime.now());
                partner.setFollowUpStatus(false);
                partner.setLockStatus(false);
                partner.setDealStatus(false);
                partnerMapper.insert(partner);
                respVO.getCreateCustomerNames().add(importPartner.getName());
                if (importReqVO.getOwnerUserId() != null) {
                    permissionService.createPermission(new CrmPermissionCreateReqBO()
                            .setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                            .setBizId(partner.getId()).setUserId(importReqVO.getOwnerUserId())
                            .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
                }
                getSelf().importPartnerLog(partner, false);
                return;
            }
            if (!importReqVO.getUpdateSupport()) {
                respVO.getFailureCustomerNames().put(importPartner.getName(),
                        StrUtil.format(CUSTOMER_NAME_EXISTS.getMsg(), importPartner.getName()));
                return;
            }
            PartnerDO updatePartner = BeanUtils.toBean(importPartner, PartnerDO.class);
            updatePartner.setId(existPartner.getId());
            updatePartner.setSalesLevel(importPartner.getLevel());
            updatePartner.setSalesSource(importPartner.getSource());
            partnerMapper.updateById(updatePartner);
            respVO.getUpdateCustomerNames().add(importPartner.getName());
            getSelf().importPartnerLog(updatePartner, true);
        });
        return respVO;
    }

    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_IMPORT_SUB_TYPE, bizNo = "{{#partner.id}}",
            success = CRM_CUSTOMER_IMPORT_SUCCESS)
    public void importPartnerLog(PartnerDO partner, boolean isUpdate) {
        putPartnerLogContext(partner);
        LogRecordContext.putVariable("isUpdate", isUpdate);
        LogRecordContext.putVariable("customerName", getPartnerName(partner.getId()));
    }

    // ==================== 校验相关 ====================

    private void validatePartnerForCreate(PartnerSalesImportExcelVO importPartner) {
        if (StrUtil.isEmptyIfStr(importPartner.getName())) {
            throw exception(CUSTOMER_CREATE_NAME_NOT_NULL);
        }
    }

    private void validatePartnerOwnerExists(PartnerDO partner, Boolean pool) {
        if (partner == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        if (pool && partner.getOwnerUserId() == null) {
            throw exception(CUSTOMER_IN_POOL, getPartnerName(partner.getId()));
        }
        if (!pool && partner.getOwnerUserId() != null) {
            throw exception(CUSTOMER_OWNER_EXISTS, getPartnerName(partner.getId()));
        }
    }

    private void validatePartnerIsLocked(PartnerDO partner, Boolean pool) {
        if (Boolean.TRUE.equals(partner.getLockStatus())) {
            throw exception(pool ? CUSTOMER_LOCKED_PUT_POOL_FAIL : CUSTOMER_LOCKED, getPartnerName(partner.getId()));
        }
    }

    private void validatePartnerDeal(PartnerDO partner) {
        if (Boolean.TRUE.equals(partner.getDealStatus())) {
            throw exception(CUSTOMER_ALREADY_DEAL);
        }
    }

    private void validatePartnerExceedOwnerLimit(Long userId, int newCount) {
        List<PartnerSalesLimitConfigDO> limitConfigs = partnerSalesLimitConfigService.getCustomerLimitConfigListByUserId(
                CUSTOMER_OWNER_LIMIT.getType(), userId);
        if (CollUtil.isEmpty(limitConfigs)) {
            return;
        }
        Long ownerCount = partnerMapper.selectCountByDealStatusAndOwnerUserId(null, userId);
        Long dealOwnerCount = partnerMapper.selectCountByDealStatusAndOwnerUserId(true, userId);
        limitConfigs.forEach(limitConfig -> {
            long nowCount = limitConfig.getDealCountEnabled() ? ownerCount : ownerCount - dealOwnerCount;
            if (nowCount + newCount > limitConfig.getMaxCount()) {
                throw exception(CUSTOMER_OWNER_EXCEED_LIMIT);
            }
        });
    }

    private void validatePartnerExceedLockLimit(Long userId) {
        List<PartnerSalesLimitConfigDO> limitConfigs = partnerSalesLimitConfigService.getCustomerLimitConfigListByUserId(
                CUSTOMER_LOCK_LIMIT.getType(), userId);
        if (CollUtil.isEmpty(limitConfigs)) {
            return;
        }
        Long lockCount = partnerMapper.selectCountByLockStatusAndOwnerUserId(true, userId);
        Integer maxCount = CollectionUtils.getMaxValue(limitConfigs, PartnerSalesLimitConfigDO::getMaxCount);
        assert maxCount != null;
        if (lockCount >= maxCount) {
            throw exception(CUSTOMER_LOCK_EXCEED_LIMIT);
        }
    }

    // ==================== 辅助方法 ====================

    private String getPartnerName(Long partnerId) {
        if (partnerId == null) {
            return null;
        }
        PartnerDO partner = getPartner(partnerId);
        return partner != null ? partner.getName() : null;
    }

    private PartnerSalesSaveReqVO buildPartnerSalesSaveReqVO(Long id) {
        PartnerDO partner = partnerMapper.selectById(id);
        if (partner == null) {
            return null;
        }
        return BeanUtils.toBean(partner, PartnerSalesSaveReqVO.class);
    }

    private void putPartnerLogContext(PartnerDO partner) {
        Map<String, Object> logPartner = new HashMap<>();
        logPartner.put("id", partner.getId());
        logPartner.put("name", getPartnerName(partner.getId()));
        logPartner.put("lockStatus", partner.getLockStatus());
        logPartner.put("ownerUserId", partner.getOwnerUserId());
        LogRecordContext.putVariable("customer", logPartner);
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     */
    private PartnerServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    @Override
    public void initPartnerMember(Long partnerId, String registerIp, Integer registerTerminal) {
        PartnerDO partner = PartnerDO.builder()
                .id(partnerId)
                .point(0)
                .experience(0)
                .registerIp(registerIp)
                .registerTerminal(registerTerminal)
                .isMember(true)
                .build();
        partnerMapper.updateById(partner);
    }

    @Override
    public PageResult<PartnerDO> getPartnerMemberPage(PartnerMemberPageReqVO pageReqVO) {
        return partnerMapper.selectMemberPage(pageReqVO);
    }

    @Override
    public void updatePartnerMember(PartnerMemberUpdateReqVO updateReqVO) {
        validatePartnerExists(updateReqVO.getId());
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(updateReqVO.getId());
        updateObj.setCustomerLevel(updateReqVO.getCustomerLevel());
        updateObj.setGroupId(updateReqVO.getGroupId());
        updateObj.setTagIds(updateReqVO.getTagIds());
        partnerMapper.updateById(updateObj);
    }

    @Override
    public void updatePartnerPoint(Long id, Integer point) {
        validatePartnerExists(id);
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(id);
        updateObj.setPoint(point);
        partnerMapper.updateById(updateObj);
    }

    @Override
    public void updatePartnerLevel(Long id, Long levelId) {
        validatePartnerExists(id);
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(id);
        updateObj.setCustomerLevel(levelId);
        partnerMapper.updateById(updateObj);
    }

    @Override
    public void updatePartnerExperience(Long id, Integer experience) {
        validatePartnerExists(id);
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(id);
        updateObj.setExperience(experience);
        partnerMapper.updateById(updateObj);
    }

    @Override
    public void updatePartnerGroup(Long id, Long groupId) {
        validatePartnerExists(id);
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(id);
        updateObj.setGroupId(groupId);
        partnerMapper.updateById(updateObj);
    }

    @Override
    public void updatePartnerTagIds(Long id, String tagIds) {
        validatePartnerExists(id);
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(id);
        updateObj.setTagIds(tagIds);
        partnerMapper.updateById(updateObj);
    }

    @Override
    public Long getPartnerCountByGroupId(Long groupId) {
        return partnerMapper.selectCountByGroupId(groupId);
    }

    @Override
    public Long getPartnerCountByLevelId(Long levelId) {
        return partnerMapper.selectCountByCustomerLevel(levelId);
    }

    @Override
    public Long getPartnerCountByTagId(Long tagId) {
        return partnerMapper.selectCountByTagId(tagId);
    }

}
