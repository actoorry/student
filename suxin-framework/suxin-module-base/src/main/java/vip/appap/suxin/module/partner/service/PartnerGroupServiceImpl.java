package vip.appap.suxin.module.partner.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupUpdateReqVO;
import vip.appap.suxin.module.partner.convert.PartnerGroupConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerGroupDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerGroupMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.*;

/**
 * 用户分组 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class PartnerGroupServiceImpl implements PartnerGroupService {

    @Resource
    private PartnerGroupMapper partnerGroupMapper;

    @Resource
    private PartnerService partnerService;

    @Override
    public Long createGroup(PartnerGroupCreateReqVO createReqVO) {
        // 插入
        PartnerGroupDO group = PartnerGroupConvert.INSTANCE.convert(createReqVO);
        partnerGroupMapper.insert(group);
        // 返回
        return group.getId();
    }

    @Override
    public void updateGroup(PartnerGroupUpdateReqVO updateReqVO) {
        // 校验存在
        validateGroupExists(updateReqVO.getId());
        // 更新
        PartnerGroupDO updateObj = PartnerGroupConvert.INSTANCE.convert(updateReqVO);
        partnerGroupMapper.updateById(updateObj);
    }

    @Override
    public void deleteGroup(Long id) {
        // 校验存在
        validateGroupExists(id);
        // 校验分组下是否有用户
        validateGroupHasMember(id);
        // 删除
        partnerGroupMapper.deleteById(id);
    }

    void validateGroupExists(Long id) {
        if (partnerGroupMapper.selectById(id) == null) {
            throw exception(PARTNER_GROUP_NOT_EXISTS);
        }
    }

    void validateGroupHasMember(Long id) {
        Long count = partnerService.getPartnerCountByGroupId(id);
        if (count > 0) {
            throw exception(PARTNER_GROUP_EXISTS);
        }
    }

    @Override
    public PartnerGroupDO getGroup(Long id) {
        return partnerGroupMapper.selectById(id);
    }

    @Override
    public List<PartnerGroupDO> getGroupList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return partnerGroupMapper.selectByIds(ids);
    }

    @Override
    public PageResult<PartnerGroupDO> getGroupPage(PartnerGroupPageReqVO pageReqVO) {
        return partnerGroupMapper.selectPage(pageReqVO, new vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX<>());
    }

    @Override
    public List<PartnerGroupDO> getGroupListByStatus(Integer status) {
        return partnerGroupMapper.selectListByStatus(status);
    }

}
