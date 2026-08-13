package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInRecordPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员签到记录 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerSignInRecordMapper extends BaseMapperX<PartnerSignInRecordDO> {

    default PageResult<PartnerSignInRecordDO> selectPage(PartnerSignInRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerSignInRecordDO>()
                .eqIfPresent(PartnerSignInRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PartnerSignInRecordDO::getDay, reqVO.getDay())
                .betweenIfPresent(PartnerSignInRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerSignInRecordDO::getId));
    }

    default PartnerSignInRecordDO selectByUserIdAndDate(Long userId, LocalDateTime date) {
        return selectOne(new LambdaQueryWrapperX<PartnerSignInRecordDO>()
                .eq(PartnerSignInRecordDO::getUserId, userId)
                .apply("DATE(create_time) = DATE({0})", date)
                .orderByDesc(PartnerSignInRecordDO::getId)
                .last("LIMIT 1"));
    }

    default List<PartnerSignInRecordDO> selectRecentListByUserId(Long userId, LocalDateTime endExclusive, Integer limit) {
        return selectList(new LambdaQueryWrapperX<PartnerSignInRecordDO>()
                .eq(PartnerSignInRecordDO::getUserId, userId)
                .lt(PartnerSignInRecordDO::getCreateTime, endExclusive)
                .orderByDesc(PartnerSignInRecordDO::getCreateTime)
                .orderByDesc(PartnerSignInRecordDO::getId)
                .last("LIMIT " + limit));
    }

}
