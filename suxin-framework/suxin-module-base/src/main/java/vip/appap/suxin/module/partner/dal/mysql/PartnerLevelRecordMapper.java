package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelRecordPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerLevelRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级记录 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerLevelRecordMapper extends BaseMapperX<PartnerLevelRecordDO> {

    default PageResult<PartnerLevelRecordDO> selectPage(PartnerLevelRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerLevelRecordDO>()
                .eqIfPresent(PartnerLevelRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PartnerLevelRecordDO::getLevelId, reqVO.getLevelId())
                .betweenIfPresent(PartnerLevelRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerLevelRecordDO::getId));
    }

}
