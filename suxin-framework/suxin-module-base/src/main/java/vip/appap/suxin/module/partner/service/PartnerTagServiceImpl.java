package vip.appap.suxin.module.partner.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagUpdateReqVO;
import vip.appap.suxin.module.partner.convert.PartnerTagConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerTagDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.*;

/**
 * 会员标签 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class PartnerTagServiceImpl implements PartnerTagService {

    @Resource
    private PartnerTagMapper partnerTagMapper;

    @Resource
    private PartnerService partnerService;

    @Override
    public Long createTag(PartnerTagCreateReqVO createReqVO) {
        // 校验名称唯一
        validateTagNameUnique(null, createReqVO.getName());
        // 插入
        PartnerTagDO tag = PartnerTagConvert.INSTANCE.convert(createReqVO);
        partnerTagMapper.insert(tag);
        // 返回
        return tag.getId();
    }

    @Override
    public void updateTag(PartnerTagUpdateReqVO updateReqVO) {
        // 校验存在
        validateTagExists(updateReqVO.getId());
        // 校验名称唯一
        validateTagNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        PartnerTagDO updateObj = PartnerTagConvert.INSTANCE.convert(updateReqVO);
        partnerTagMapper.updateById(updateObj);
    }

    @Override
    public void deleteTag(Long id) {
        // 校验存在
        validateTagExists(id);
        // 校验标签下是否有用户
        validateTagHasMember(id);
        // 删除
        partnerTagMapper.deleteById(id);
    }

    private void validateTagExists(Long id) {
        if (partnerTagMapper.selectById(id) == null) {
            throw exception(PARTNER_TAG_NOT_EXISTS);
        }
    }

    private void validateTagNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        PartnerTagDO tag = partnerTagMapper.selectByName(name);
        if (tag == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的标签
        if (id == null) {
            throw exception(PARTNER_TAG_EXISTS);
        }
        if (!tag.getId().equals(id)) {
            throw exception(PARTNER_TAG_EXISTS);
        }
    }

    void validateTagHasMember(Long id) {
        Long count = partnerService.getPartnerCountByTagId(id);
        if (count > 0) {
            throw exception(PARTNER_TAG_EXISTS);
        }
    }

    @Override
    public PartnerTagDO getTag(Long id) {
        return partnerTagMapper.selectById(id);
    }

    @Override
    public List<PartnerTagDO> getTagList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return partnerTagMapper.selectByIds(ids);
    }

    @Override
    public PageResult<PartnerTagDO> getTagPage(PartnerTagPageReqVO pageReqVO) {
        return partnerTagMapper.selectPage(pageReqVO, new vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX<>());
    }

    @Override
    public List<PartnerTagDO> getTagList() {
        return partnerTagMapper.selectList();
    }

}
