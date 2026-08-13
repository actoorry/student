package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPointRecordPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerPointRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员积分记录 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerPointRecordMapper extends BaseMapperX<PartnerPointRecordDO> {

    default PageResult<PartnerPointRecordDO> selectPage(PartnerPointRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerPointRecordDO>()
                .eqIfPresent(PartnerPointRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PartnerPointRecordDO::getBizType, reqVO.getBizType())
                .likeIfPresent(PartnerPointRecordDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(PartnerPointRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerPointRecordDO::getId));
    }

}
