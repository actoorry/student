package vip.appap.suxin.module.crm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmCluePageReqVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmClueSaveReqVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmClueTransferReqVO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmClueDO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmFollowUpRecordDO;
import vip.appap.suxin.module.crm.dal.mysql.CrmClueMapper;
import vip.appap.suxin.module.crm.enums.CrmBizTypeEnum;
import vip.appap.suxin.module.crm.enums.CrmPermissionLevelEnum;
import vip.appap.suxin.module.crm.framework.permission.core.annotations.CrmPermission;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.partner.service.bo.PartnerSalesCreateReqBO;
import vip.appap.suxin.module.crm.service.bo.CrmFollowUpCreateReqBO;
import vip.appap.suxin.module.crm.service.bo.CrmPermissionCreateReqBO;
import vip.appap.suxin.module.crm.service.bo.CrmPermissionTransferReqBO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerUpdateReqVO;
import vip.appap.suxin.module.system.api.AdminUserApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.singleton;
import static vip.appap.suxin.module.crm.enums.ErrorCodeConstants.CLUE_NOT_EXISTS;
import static vip.appap.suxin.module.crm.enums.ErrorCodeConstants.CLUE_TRANSFORM_FAIL_ALREADY;
import static vip.appap.suxin.module.crm.enums.LogRecordConstants.*;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 线索 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Validated
public class CrmClueServiceImpl implements CrmClueService {

    @Resource
    private CrmClueMapper clueMapper;

    @Resource
    private PartnerService partnerService;
    @Resource
    private CrmPermissionService crmPermissionService;
    @Resource
    private CrmFollowUpRecordService followUpRecordService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CLUE_TYPE, subType = CRM_CLUE_CREATE_SUB_TYPE, bizNo = "{{#clue.id}}",
            success = CRM_CLUE_CREATE_SUCCESS)
    public Long createClue(CrmClueSaveReqVO createReqVO) {
        // 1.1 校验关联数据
        validateRelationDataExists(createReqVO);
        // 1.2 校验负责人是否存在
        adminUserApi.validateUser(createReqVO.getOwnerUserId());

        // 2. 创建 partner（线索基础信息）
        PartnerCreateReqVO partnerReqVO = new PartnerCreateReqVO();
        BeanUtils.copyProperties(createReqVO, partnerReqVO);
        Long partnerId = partnerService.createPartner(partnerReqVO);

        // 3. 插入线索（共享主键）
        CrmClueDO clue = BeanUtils.toBean(createReqVO, CrmClueDO.class);
        clue.setId(partnerId);
        clueMapper.insert(clue);

        // 4. 创建数据权限
        CrmPermissionCreateReqBO createReqBO = new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CLUE.getType())
                .setBizId(clue.getId()).setUserId(clue.getOwnerUserId()).setLevel(CrmPermissionLevelEnum.OWNER.getLevel());
        crmPermissionService.createPermission(createReqBO);

        // 5. 记录操作日志上下文
        LogRecordContext.putVariable("clue", clue);
        LogRecordContext.putVariable("clueName", createReqVO.getName());
        return clue.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CLUE_TYPE, subType = CRM_CLUE_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_CLUE_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CLUE, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void updateClue(CrmClueSaveReqVO updateReqVO) {
        Assert.notNull(updateReqVO.getId(), "线索编号不能为空");
        // 1.1 校验线索是否存在
        CrmClueDO oldClue = validateClueExists(updateReqVO.getId());
        // 1.2 校验关联数据
        validateRelationDataExists(updateReqVO);

        // 2. 更新 partner 基础信息
        PartnerUpdateReqVO partnerUpdateReqVO = new PartnerUpdateReqVO();
        BeanUtils.copyProperties(updateReqVO, partnerUpdateReqVO);
        partnerUpdateReqVO.setId(updateReqVO.getId()); // partner.id = clue.id
        partnerService.updatePartner(partnerUpdateReqVO);

        // 3. 更新线索业务字段
        CrmClueDO updateObj = BeanUtils.toBean(updateReqVO, CrmClueDO.class);
        clueMapper.updateById(updateObj);

        // 4. 记录操作日志上下文
        updateReqVO.setOwnerUserId(oldClue.getOwnerUserId()); // 避免操作日志出现"删除负责人"的情况
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldClue, CrmClueSaveReqVO.class));
        LogRecordContext.putVariable("clueName", getClueName(updateReqVO.getId()));
    }

    private void validateRelationDataExists(CrmClueSaveReqVO reqVO) {
        // 校验负责人
        if (Objects.nonNull(reqVO.getOwnerUserId()) &&
                Objects.isNull(adminUserApi.getUser(reqVO.getOwnerUserId()))) {
            throw exception(USER_NOT_EXISTS);
        }
    }

    @Override
    @LogRecord(type = CRM_CLUE_TYPE, subType = CRM_CLUE_FOLLOW_UP_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CLUE_FOLLOW_UP_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CLUE, bizId = "#id", level = CrmPermissionLevelEnum.WRITE)
    public void updateClueFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent) {
        // 校验线索是否存在
        validateClueExists(id);

        // 更新线索
        clueMapper.updateById(new CrmClueDO().setId(id).setFollowUpStatus(true).setContactNextTime(contactNextTime)
                .setContactLastTime(LocalDateTime.now()).setContactLastContent(contactLastContent));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("clueName", getClueName(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CLUE_TYPE, subType = CRM_CLUE_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CLUE_DELETE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CLUE, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteClue(Long id) {
        // 1. 校验存在
        validateClueExists(id);

        // 2. 删除
        clueMapper.deleteById(id);

        // 3. 删除数据权限
        crmPermissionService.deletePermission(CrmBizTypeEnum.CRM_CLUE.getType(), id);

        // 4. 删除跟进
        followUpRecordService.deleteFollowUpRecordByBiz(CrmBizTypeEnum.CRM_CLUE.getType(), id);

        // 5. 记录操作日志上下文
        LogRecordContext.putVariable("clueName", getClueName(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CLUE_TYPE, subType = CRM_CLUE_TRANSFER_SUB_TYPE, bizNo = "{{#reqVO.id}}",
            success = CRM_CLUE_TRANSFER_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CLUE, bizId = "#reqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void transferClue(CrmClueTransferReqVO reqVO, Long userId) {
        // 1 校验线索是否存在
        CrmClueDO clue = validateClueExists(reqVO.getId());

        // 2.1 数据权限转移
        crmPermissionService.transferPermission(new CrmPermissionTransferReqBO(userId, CrmBizTypeEnum.CRM_CLUE.getType(),
                        reqVO.getId(), reqVO.getNewOwnerUserId(), reqVO.getOldOwnerPermissionLevel()));
        // 2.2 设置新的负责人
        clueMapper.updateById(new CrmClueDO().setId(reqVO.getId()).setOwnerUserId(reqVO.getNewOwnerUserId()));

        // 3. 记录转移日志
        LogRecordContext.putVariable("clue", clue);
        LogRecordContext.putVariable("clueName", getClueName(clue.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CLUE_TYPE, subType = CRM_CLUE_TRANSLATE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CLUE_TRANSLATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CLUE, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void transformClue(Long id, Long userId) {
        // 1.1 校验线索都存在
        CrmClueDO clue = validateClueExists(id);
        // 1.2 存在已经转化的
        if (clue.getTransformStatus()) {
            throw exception(CLUE_TRANSFORM_FAIL_ALREADY);
        }

        // 2.1 创建客户（复用线索的 partner，共享主键：crm_customer.id = clue.id = partner.id）
        Long customerId = partnerService.createPartner(BeanUtils.toBean(clue, PartnerSalesCreateReqBO.class), userId, clue.getId());
        // 2.2 更新线索
        clueMapper.updateById(new CrmClueDO().setId(id).setTransformStatus(Boolean.TRUE).setCustomerId(customerId));
        // 2.3 复制跟进记录
        List<CrmFollowUpRecordDO> followUpRecords = followUpRecordService.getFollowUpRecordByBiz(
                CrmBizTypeEnum.CRM_CLUE.getType(), singleton(clue.getId()));
        if (CollUtil.isNotEmpty(followUpRecords)) {
            followUpRecordService.createFollowUpRecordBatch(convertList(followUpRecords, record ->
                    BeanUtils.toBean(record, CrmFollowUpCreateReqBO.class)
                            .setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType()).setBizId(customerId)));
        }

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("clueName", getClueName(id));
    }

    private CrmClueDO validateClueExists(Long id) {
        CrmClueDO crmClueDO = clueMapper.selectById(id);
        if (crmClueDO == null) {
            throw exception(CLUE_NOT_EXISTS);
        }
        return crmClueDO;
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CLUE, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmClueDO getClue(Long id) {
        return clueMapper.selectById(id);
    }

    @Override
    public PageResult<CrmClueDO> getCluePage(CrmCluePageReqVO pageReqVO, Long userId) {
        return clueMapper.selectPage(pageReqVO, userId);
    }

    @Override
    public Long getFollowClueCount(Long userId) {
        return clueMapper.selectCountByFollow(userId);
    }

    /**
     * 从 partner 表获取线索名称
     *
     * @param clueId 线索编号（等于 partner 编号）
     * @return 线索名称
     */
    private String getClueName(Long clueId) {
        if (clueId == null) {
            return null;
        }
        CrmClueDO clue = getClue(clueId);
        return clue != null ? clue.getName() : null;
    }

}
