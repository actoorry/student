package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerTagDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员标签 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerTagMapper extends BaseMapperX<PartnerTagDO> {

    default PageResult<PartnerTagDO> selectPage(PartnerTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerTagDO>()
                .likeIfPresent(PartnerTagDO::getName, reqVO.getName())
                .betweenIfPresent(PartnerTagDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerTagDO::getId));
    }

    default PartnerTagDO selectByName(String name) {
        return selectOne(PartnerTagDO::getName, name);
    }
}
