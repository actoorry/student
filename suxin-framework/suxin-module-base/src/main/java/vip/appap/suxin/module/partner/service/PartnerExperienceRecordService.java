package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerExperienceRecordPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerExperienceRecordDO;

/**
 * 会员经验记录 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerExperienceRecordService {

    /**
     * 创建经验记录
     *
     * @param userId 用户编号
     * @param experience 经验
     * @param totalExperience 变更后的经验
     * @param bizType 业务类型
     * @param bizId 业务编号
     * @return 经验记录
     */
    PartnerExperienceRecordDO createExperienceRecord(Long userId, Integer experience,
                                                     Integer totalExperience, Integer bizType, String bizId);

    /**
     * 获得经验记录分页
     *
     * @param pageReqVO 分页查询
     * @return 经验记录分页
     */
    PageResult<PartnerExperienceRecordDO> getExperienceRecordPage(PartnerExperienceRecordPageReqVO pageReqVO);

}
