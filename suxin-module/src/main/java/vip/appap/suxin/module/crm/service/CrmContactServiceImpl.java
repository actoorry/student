package vip.appap.suxin.module.crm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContactBusinessReqVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContactPageReqVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContactSaveReqVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmContactTransferReqVO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmContactBusinessDO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmContactDO;
import vip.appap.suxin.module.crm.dal.mysql.CrmContactMapper;
import vip.appap.suxin.module.crm.enums.CrmBizTypeEnum;
import vip.appap.suxin.module.crm.enums.CrmPermissionLevelEnum;
import vip.appap.suxin.module.crm.framework.permission.core.annotations.CrmPermission;
import vip.appap.suxin.module.crm.service.bo.CrmPermissionCreateReqBO;
import vip.appap.suxin.module.crm.service.bo.CrmPermissionTransferReqBO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerUpdateReqVO;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.api.AdminUserApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.module.crm.enums.ErrorCodeConstants.*;
import static vip.appap.suxin.module.crm.enums.LogRecordConstants.*;
import static java.util.Collections.singletonList;

/**
 * CRM 联系人 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class CrmContactServiceImpl implements CrmContactService {

    @Resource
    private CrmContactMapper contactMapper;

    @Resource
    private PartnerService partnerService;
    @Resource
    private CrmPermissionService permissionService;
    @Resource
    @Lazy
    private CrmContractService contractService;
    @Resource
    private CrmContactBusinessService contactBusinessService;
    @Resource
    private CrmBusinessService businessService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_CREATE_SUB_TYPE, bizNo = "{{#contact.id}}",
            success = CRM_CONTACT_CREATE_SUCCESS)
    public Long createContact(CrmContactSaveReqVO createReqVO, Long userId) {
        createReqVO.setId(null);
        // 1. 校验关联数据
        validateRelationDataExists(createReqVO);

        // 2. 创建 partner（联系人基础信息）
        PartnerCreateReqVO partnerReqVO = new PartnerCreateReqVO();
        BeanUtils.copyProperties(createReqVO, partnerReqVO);
        Long partnerId = partnerService.createPartner(partnerReqVO);

        // 3. 插入联系人（共享主键）
        CrmContactDO contact = BeanUtils.toBean(createReqVO, CrmContactDO.class);
        contact.setId(partnerId);
        contactMapper.insert(contact);

        // 4. 创建数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setUserId(userId)
                .setBizType(CrmBizTypeEnum.CRM_CONTACT.getType()).setBizId(contact.getId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        // 5. 如果有关联商机，则需要创建关联
        if (createReqVO.getBusinessId() != null) {
            contactBusinessService.createContactBusinessList(new CrmContactBusinessReqVO()
                    .setContactId(contact.getId()).setBusinessIds(singletonList(createReqVO.getBusinessId())));
        }

        // 6. 记录操作日志
        LogRecordContext.putVariable("contact", contact);
        return contact.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_CONTACT_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateContact(CrmContactSaveReqVO updateReqVO) {
        // 1.1 校验存在
        CrmContactDO oldContact = validateContactExists(updateReqVO.getId());
        // 1.2 校验关联数据
        validateRelationDataExists(updateReqVO);

        // 2. 更新 partner 基础信息
        PartnerUpdateReqVO partnerUpdateReqVO = new PartnerUpdateReqVO();
        BeanUtils.copyProperties(updateReqVO, partnerUpdateReqVO);
        partnerUpdateReqVO.setId(updateReqVO.getId()); // partner.id = contact.id
        partnerService.updatePartner(partnerUpdateReqVO);

        // 3. 更新联系人业务字段
        CrmContactDO updateObj = BeanUtils.toBean(updateReqVO, CrmContactDO.class);
        contactMapper.updateById(updateObj);

        // 4. 记录操作日志
        updateReqVO.setOwnerUserId(oldContact.getOwnerUserId()); // 避免操作日志出现"删除负责人"的情况
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldContact, CrmContactSaveReqVO.class));
        LogRecordContext.putVariable("contactName", getContactName(updateReqVO.getId()));
    }

    /**
     * 校验关联的数据都存在
     *
     * @param saveReqVO 新增/修改请求 VO
     */
    private void validateRelationDataExists(CrmContactSaveReqVO saveReqVO) {
        // 1. 校验客户
        if (saveReqVO.getCustomerId() != null && partnerService.getPartner(saveReqVO.getCustomerId()) == null) {
            partnerService.validatePartner(saveReqVO.getCustomerId());
        }
        // 2. 校验负责人
        if (saveReqVO.getOwnerUserId() != null) {
            adminUserApi.validateUser(saveReqVO.getOwnerUserId());
        }
        // 3. 直属上级
        if (saveReqVO.getParentId() != null) {
            validateContactExists(saveReqVO.getParentId());
        }
        // 4. 如果有关联商机，则需要校验存在
        if (saveReqVO.getBusinessId() != null && businessService.getBusiness(saveReqVO.getBusinessId()) == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CONTACT_DELETE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteContact(Long id) {
        // 1.1 校验存在
        validateContactExists(id);
        // 1.2 校验是否关联合同
        if (contractService.getContractCountByContactId(id) > 0) {
            throw exception(CONTACT_DELETE_FAIL_CONTRACT_LINK_EXISTS);
        }

        // 2. 删除联系人
        contactMapper.deleteById(id);

        // 4.1 删除商机关联
        contactBusinessService.deleteContactBusinessByContactId(id);
        // 4.2 删除数据权限
        permissionService.deletePermission(CrmBizTypeEnum.CRM_CONTACT.getType(), id);

        // 记录操作日志上下文
        LogRecordContext.putVariable("contactName", getContactName(id));
    }

    private CrmContactDO validateContactExists(Long id) {
        CrmContactDO contact = contactMapper.selectById(id);
        if (contact == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
        return contact;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_TRANSFER_SUB_TYPE, bizNo = "{{#reqVO.id}}",
            success = CRM_CONTACT_TRANSFER_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#reqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void transferContact(CrmContactTransferReqVO reqVO, Long userId) {
        // 1 校验联系人是否存在
        CrmContactDO contact = validateContactExists(reqVO.getId());

        // 2.1 数据权限转移
        permissionService.transferPermission(new CrmPermissionTransferReqBO(userId, CrmBizTypeEnum.CRM_CONTACT.getType(),
                reqVO.getId(), reqVO.getNewOwnerUserId(), reqVO.getOldOwnerPermissionLevel()));
        // 2.2 设置新的负责人
        contactMapper.updateById(new CrmContactDO().setId(reqVO.getId()).setOwnerUserId(reqVO.getNewOwnerUserId()));

        // 3. 记录转移日志
        LogRecordContext.putVariable("contact", contact);
        LogRecordContext.putVariable("contactName", getContactName(contact.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#customerId", level = CrmPermissionLevelEnum.OWNER)
    public void updateOwnerUserIdByCustomerId(Long customerId, Long ownerUserId) {
        // 1. 校验存在
        List<CrmContactDO> contacts = contactMapper.selectListByCustomerId(customerId);
        if (CollUtil.isEmpty(contacts)) {
            return;
        }
        int count = contactMapper.updateOwnerUserIdByCustomerId(customerId, ownerUserId);
        if (count == 0) {
            throw exception(CONTACT_UPDATE_OWNER_USER_FAIL);
        }

        // 2. 记录操作日志
        for (CrmContactDO contact : contacts) {
            receiveContactLog(contact, ownerUserId);
        }
    }

    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_UPDATE_OWNER_USER_SUB_TYPE, bizNo = "{{#contact.id}}",
            success = CRM_CONTACT_UPDATE_OWNER_USER_SUCCESS)
    public void receiveContactLog(CrmContactDO contact, Long ownerUserId) {
        // 记录操作日志上下文
        LogRecordContext.putVariable("contact", contact);
        LogRecordContext.putVariable("ownerUserId", ownerUserId);
    }

    @Override
    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_FOLLOW_UP_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CONTACT_FOLLOW_UP_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#id", level = CrmPermissionLevelEnum.WRITE)
    public void updateContactFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent) {
        // 1. 校验存在
        validateContactExists(id);

        // 2. 更新联系人的跟进信息
        contactMapper.updateById(new CrmContactDO().setId(id).setContactNextTime(contactNextTime)
                .setContactLastTime(LocalDateTime.now()).setContactLastContent(contactLastContent));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("contactName", getContactName(id));
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#ids", level = CrmPermissionLevelEnum.WRITE)
    public void updateContactContactNextTime(Collection<Long> ids, LocalDateTime contactNextTime) {
        contactMapper.updateBatch(convertList(ids, id -> new CrmContactDO().setId(id).setContactNextTime(contactNextTime)));
    }

    //======================= 查询相关 =======================

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmContactDO getContact(Long id) {
        return contactMapper.selectById(id);
    }

    @Override
    public void validateContact(Long id) {
        validateContactExists(id);
    }

    @Override
    public List<CrmContactDO> getContactList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return contactMapper.selectByIds(ids);
    }

    @Override
    public List<CrmContactDO> getContactList(Long userId) {
        CrmContactPageReqVO reqVO = new CrmContactPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE); // 不分页
        return contactMapper.selectPage(reqVO, userId).getList();
    }

    @Override
    public PageResult<CrmContactDO> getContactPage(CrmContactPageReqVO pageReqVO, Long userId) {
        return contactMapper.selectPage(pageReqVO, userId);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#pageVO.customerId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmContactDO> getContactPageByCustomerId(CrmContactPageReqVO pageVO) {
        return contactMapper.selectPageByCustomerId(pageVO);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#pageVO.businessId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmContactDO> getContactPageByBusinessId(CrmContactPageReqVO pageVO) {
        List<CrmContactBusinessDO> contactBusinessList = contactBusinessService.getContactBusinessListByBusinessId(pageVO.getBusinessId());
        if (CollUtil.isEmpty(contactBusinessList)) {
            return PageResult.empty();
        }
        return contactMapper.selectPageByBusinessId(pageVO, convertSet(contactBusinessList, CrmContactBusinessDO::getContactId));
    }

    @Override
    public Long getContactCountByCustomerId(Long customerId) {
        return contactMapper.selectCount(CrmContactDO::getCustomerId, customerId);
    }

    @Override
    public List<CrmContactDO> getContactListByCustomerIdOwnerUserId(Long customerId, Long ownerUserId) {
        return contactMapper.selectListByCustomerIdOwnerUserId(customerId, ownerUserId);
    }

    /**
     * 从 partner 表获取联系人名称
     *
     * @param contactId 联系人编号（等于 partner 编号）
     * @return 联系人名称
     */
    private String getContactName(Long contactId) {
        if (contactId == null) {
            return null;
        }
        CrmContactDO contact = getContact(contactId);
        return contact != null ? contact.getName() : null;
    }

}
