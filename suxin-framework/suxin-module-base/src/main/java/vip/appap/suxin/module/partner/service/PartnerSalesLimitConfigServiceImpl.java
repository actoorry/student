package vip.appap.suxin.module.partner.service;

import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesLimitConfigPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesLimitConfigSaveReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesLimitConfigDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerSalesLimitConfigMapper;
import vip.appap.suxin.module.partner.enums.PartnerSalesLimitConfigTypeEnum;
import vip.appap.suxin.module.system.api.DeptApi;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;

import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_SALES_LIMIT_CONFIG_NOT_EXISTS;
import static vip.appap.suxin.module.partner.enums.LogRecordConstants.*;

/**
 * 客户限制配置 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Validated
public class PartnerSalesLimitConfigServiceImpl implements PartnerSalesLimitConfigService {

    @Resource
    private PartnerSalesLimitConfigMapper customerLimitConfigMapper;

    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @LogRecord(type = CRM_CUSTOMER_LIMIT_CONFIG_TYPE, subType = CRM_CUSTOMER_LIMIT_CONFIG_CREATE_SUB_TYPE, bizNo = "{{#limitId}}",
            success = CRM_CUSTOMER_LIMIT_CONFIG_CREATE_SUCCESS)
    public Long createCustomerLimitConfig(PartnerSalesLimitConfigSaveReqVO createReqVO) {
        validateUserAndDept(createReqVO.getUserIds(), createReqVO.getDeptIds());
        // 插入
        PartnerSalesLimitConfigDO limitConfig = BeanUtils.toBean(createReqVO, PartnerSalesLimitConfigDO.class);
        customerLimitConfigMapper.insert(limitConfig);

        // 记录操作日志上下文
        LogRecordContext.putVariable("limitType", PartnerSalesLimitConfigTypeEnum.getNameByType(limitConfig.getType()));
        LogRecordContext.putVariable("limitId", limitConfig.getId());
        return limitConfig.getId();
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_LIMIT_CONFIG_TYPE, subType = CRM_CUSTOMER_LIMIT_CONFIG_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_CUSTOMER_LIMIT_CONFIG_UPDATE_SUCCESS)
    public void updateCustomerLimitConfig(PartnerSalesLimitConfigSaveReqVO updateReqVO) {
        // 校验存在
        PartnerSalesLimitConfigDO oldLimitConfig = validateCustomerLimitConfigExists(updateReqVO.getId());
        validateUserAndDept(updateReqVO.getUserIds(), updateReqVO.getDeptIds());
        // 更新
        PartnerSalesLimitConfigDO updateObj = BeanUtils.toBean(updateReqVO, PartnerSalesLimitConfigDO.class);
        customerLimitConfigMapper.updateById(updateObj);

        // 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldLimitConfig, PartnerSalesLimitConfigSaveReqVO.class));
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_LIMIT_CONFIG_TYPE, subType = CRM_CUSTOMER_LIMIT_CONFIG_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CUSTOMER_LIMIT_CONFIG_DELETE_SUCCESS)
    public void deleteCustomerLimitConfig(Long id) {
        // 校验存在
        PartnerSalesLimitConfigDO limitConfig = validateCustomerLimitConfigExists(id);
        // 删除
        customerLimitConfigMapper.deleteById(id);

        // 记录操作日志上下文
        LogRecordContext.putVariable("limitType", PartnerSalesLimitConfigTypeEnum.getNameByType(limitConfig.getType()));
    }

    @Override
    public PartnerSalesLimitConfigDO getCustomerLimitConfig(Long id) {
        return customerLimitConfigMapper.selectById(id);
    }

    @Override
    public PageResult<PartnerSalesLimitConfigDO> getCustomerLimitConfigPage(PartnerSalesLimitConfigPageReqVO pageReqVO) {
        return customerLimitConfigMapper.selectPage(pageReqVO);
    }

    private PartnerSalesLimitConfigDO validateCustomerLimitConfigExists(Long id) {
        PartnerSalesLimitConfigDO limitConfigDO = customerLimitConfigMapper.selectById(id);
        if (limitConfigDO == null) {
            throw exception(PARTNER_SALES_LIMIT_CONFIG_NOT_EXISTS);
        }
        return limitConfigDO;
    }

    /**
     * 校验入参的用户和部门
     *
     * @param userIds 用户 ids
     * @param deptIds 部门 ids
     */
    private void validateUserAndDept(Collection<Long> userIds, Collection<Long> deptIds) {
        deptApi.validateDeptList(deptIds);
        adminUserApi.validateUserList(userIds);
    }

    @Override
    public List<PartnerSalesLimitConfigDO> getCustomerLimitConfigListByUserId(Integer type, Long userId) {
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        Assert.notNull(user, "用户({})不存在", userId);
        return customerLimitConfigMapper.selectListByTypeAndUserIdAndDeptId(type, userId, user.getDeptId());
    }

}
