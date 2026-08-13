package vip.appap.suxin.module.im.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.im.controller.admin.vo.ImFaceUserItemManagerPageReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImFaceUserItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 用户私有表情 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImFaceUserItemMapper extends BaseMapperX<ImFaceUserItemDO> {

    default List<ImFaceUserItemDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<ImFaceUserItemDO>()
                .eq(ImFaceUserItemDO::getUserId, userId)
                .orderByAsc(ImFaceUserItemDO::getSort)
                .orderByDesc(ImFaceUserItemDO::getId));
    }

    default ImFaceUserItemDO selectByUserIdAndUrl(Long userId, String url) {
        return selectOne(new LambdaQueryWrapperX<ImFaceUserItemDO>()
                .eq(ImFaceUserItemDO::getUserId, userId)
                .eq(ImFaceUserItemDO::getUrl, url));
    }

    default PageResult<ImFaceUserItemDO> selectPage(ImFaceUserItemManagerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImFaceUserItemDO>()
                .eqIfPresent(ImFaceUserItemDO::getUserId, reqVO.getUserId())
                .likeIfPresent(ImFaceUserItemDO::getName, reqVO.getName())
                .betweenIfPresent(ImFaceUserItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImFaceUserItemDO::getId));
    }

}
