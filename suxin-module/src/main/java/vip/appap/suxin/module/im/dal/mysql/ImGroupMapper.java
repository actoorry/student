package vip.appap.suxin.module.im.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.im.controller.admin.vo.ImGroupManagerPageReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImGroupDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IM 群 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupMapper extends BaseMapperX<ImGroupDO> {

    default PageResult<ImGroupDO> selectPage(ImGroupManagerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImGroupDO>()
                .likeIfPresent(ImGroupDO::getName, reqVO.getName())
                .eqIfPresent(ImGroupDO::getOwnerUserId, reqVO.getOwnerUserId())
                .eqIfPresent(ImGroupDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ImGroupDO::getBanned, reqVO.getBanned())
                .betweenIfPresent(ImGroupDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImGroupDO::getId));
    }

}