package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerExperienceRecordPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerExperienceRecordDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerExperienceRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_NOT_EXISTS;

/**
 * 会员经验记录 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class PartnerExperienceRecordServiceImpl implements PartnerExperienceRecordService {

    @Resource
    private PartnerExperienceRecordMapper partnerExperienceRecordMapper;

    @Resource
    private PartnerService partnerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerExperienceRecordDO createExperienceRecord(Long userId, Integer experience,
                                                            Integer totalExperience, Integer bizType, String bizId) {
        // 校验会员存在
        PartnerDO member = partnerService.getPartner(userId);
        if (member == null) {
            throw exception(PARTNER_NOT_EXISTS);
        }

        // 创建经验记录
        PartnerExperienceRecordDO record = PartnerExperienceRecordDO.builder()
                .userId(userId)
                .experience(experience)
                .totalExperience(totalExperience)
                .bizType(bizType)
                .bizId(bizId)
                .title("经验变动")
                .description("经验变动 " + (experience > 0 ? "+" : "") + experience)
                .build();
        partnerExperienceRecordMapper.insert(record);

        // 更新会员经验
        partnerService.updatePartnerExperience(userId, totalExperience);

        return record;
    }

    @Override
    public PageResult<PartnerExperienceRecordDO> getExperienceRecordPage(PartnerExperienceRecordPageReqVO pageReqVO) {
        return partnerExperienceRecordMapper.selectPage(pageReqVO, new vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX<>());
    }

}
