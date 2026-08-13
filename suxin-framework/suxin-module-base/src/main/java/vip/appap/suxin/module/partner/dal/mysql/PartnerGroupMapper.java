package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户分组 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerGroupMapper extends BaseMapperX<PartnerGroupDO> {

    default PageResult<PartnerGroupDO> selectPage(PartnerGroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerGroupDO>()
                .likeIfPresent(PartnerGroupDO::getName, reqVO.getName())
                .eqIfPresent(PartnerGroupDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PartnerGroupDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerGroupDO::getId));
    }

    default List<PartnerGroupDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<PartnerGroupDO>()
                .eqIfPresent(PartnerGroupDO::getStatus, status));
    }

}
