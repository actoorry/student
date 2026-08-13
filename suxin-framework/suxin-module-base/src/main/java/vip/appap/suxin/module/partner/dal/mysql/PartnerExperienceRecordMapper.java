package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerExperienceRecordPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerExperienceRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员经验记录 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerExperienceRecordMapper extends BaseMapperX<PartnerExperienceRecordDO> {

    default PageResult<PartnerExperienceRecordDO> selectPage(PartnerExperienceRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerExperienceRecordDO>()
                .eqIfPresent(PartnerExperienceRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PartnerExperienceRecordDO::getBizType, reqVO.getBizType())
                .likeIfPresent(PartnerExperienceRecordDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(PartnerExperienceRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerExperienceRecordDO::getId));
    }

}
