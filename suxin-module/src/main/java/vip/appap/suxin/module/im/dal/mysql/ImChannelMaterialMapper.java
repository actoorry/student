package vip.appap.suxin.module.im.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.im.controller.admin.vo.ImChannelMaterialPageReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImChannelMaterialDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 频道素材 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImChannelMaterialMapper extends BaseMapperX<ImChannelMaterialDO> {

    default Long selectCountByChannelId(Long channelId) {
        return selectCount(ImChannelMaterialDO::getChannelId, channelId);
    }

    default List<ImChannelMaterialDO> selectListByChannelId(Long channelId) {
        return selectList(new LambdaQueryWrapperX<ImChannelMaterialDO>()
                .eq(ImChannelMaterialDO::getChannelId, channelId)
                .orderByDesc(ImChannelMaterialDO::getId));
    }

    default PageResult<ImChannelMaterialDO> selectPage(ImChannelMaterialPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImChannelMaterialDO>()
                .eqIfPresent(ImChannelMaterialDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(ImChannelMaterialDO::getType, reqVO.getType())
                .likeIfPresent(ImChannelMaterialDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(ImChannelMaterialDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImChannelMaterialDO::getId));
    }

}
