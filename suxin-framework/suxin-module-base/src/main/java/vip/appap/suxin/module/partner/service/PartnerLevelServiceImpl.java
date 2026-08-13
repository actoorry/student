package vip.appap.suxin.module.partner.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelUpdateReqVO;
import vip.appap.suxin.module.partner.convert.PartnerLevelConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerLevelDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerLevelMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.*;

/**
 * 会员等级 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class PartnerLevelServiceImpl implements PartnerLevelService {

    @Resource
    private PartnerLevelMapper partnerLevelMapper;

    @Resource
    private PartnerService partnerService;

    @Override
    public Long createLevel(PartnerLevelCreateReqVO createReqVO) {
        // 校验名称唯一
        validateLevelNameUnique(null, createReqVO.getName());
        // 校验等级唯一
        validateLevelValueUnique(null, createReqVO.getLevel());
        // 插入
        PartnerLevelDO level = PartnerLevelConvert.INSTANCE.convert(createReqVO);
        partnerLevelMapper.insert(level);
        // 返回
        return level.getId();
    }

    @Override
    public void updateLevel(PartnerLevelUpdateReqVO updateReqVO) {
        // 校验存在
        validateLevelExists(updateReqVO.getId());
        // 校验名称唯一
        validateLevelNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 校验等级唯一
        validateLevelValueUnique(updateReqVO.getId(), updateReqVO.getLevel());
        // 更新
        PartnerLevelDO updateObj = PartnerLevelConvert.INSTANCE.convert(updateReqVO);
        partnerLevelMapper.updateById(updateObj);
    }

    @Override
    public void deleteLevel(Long id) {
        // 校验存在
        validateLevelExists(id);
        // 校验等级下是否有用户
        validateLevelHasMember(id);
        // 删除
        partnerLevelMapper.deleteById(id);
    }

    private void validateLevelExists(Long id) {
        if (partnerLevelMapper.selectById(id) == null) {
            throw exception(PARTNER_LEVEL_NOT_EXISTS);
        }
    }

    private void validateLevelNameUnique(Long id, String name) {
        if (name == null) {
            return;
        }
        PartnerLevelDO level = partnerLevelMapper.selectOne(
                new vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX<PartnerLevelDO>()
                        .eq(PartnerLevelDO::getName, name));
        if (level == null) {
            return;
        }
        if (id == null) {
            throw exception(PARTNER_LEVEL_EXISTS);
        }
        if (!level.getId().equals(id)) {
            throw exception(PARTNER_LEVEL_EXISTS);
        }
    }

    private void validateLevelValueUnique(Long id, Integer levelValue) {
        if (levelValue == null) {
            return;
        }
        PartnerLevelDO level = partnerLevelMapper.selectOne(
                new vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX<PartnerLevelDO>()
                        .eq(PartnerLevelDO::getLevel, levelValue));
        if (level == null) {
            return;
        }
        if (id == null) {
            throw exception(PARTNER_LEVEL_EXISTS);
        }
        if (!level.getId().equals(id)) {
            throw exception(PARTNER_LEVEL_EXISTS);
        }
    }

    private void validateLevelHasMember(Long id) {
        Long count = partnerService.getPartnerCountByLevelId(id);
        if (count > 0) {
            throw exception(PARTNER_LEVEL_HAS_MEMBER);
        }
    }

    @Override
    public PartnerLevelDO getLevel(Long id) {
        return id != null && id > 0 ? partnerLevelMapper.selectById(id) : null;
    }

    @Override
    public List<PartnerLevelDO> getLevelList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return partnerLevelMapper.selectByIds(ids);
    }

    @Override
    public PageResult<PartnerLevelDO> getLevelPage(PartnerLevelPageReqVO pageReqVO) {
        return partnerLevelMapper.selectPage(pageReqVO, new vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX<>());
    }

    @Override
    public List<PartnerLevelDO> getLevelListByStatus(Integer status) {
        return partnerLevelMapper.selectListByStatus(status);
    }

}
