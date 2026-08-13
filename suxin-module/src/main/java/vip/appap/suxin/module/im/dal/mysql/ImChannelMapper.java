package vip.appap.suxin.module.im.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.im.controller.admin.vo.ImChannelPageReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImChannelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 频道 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImChannelMapper extends BaseMapperX<ImChannelDO> {

    default ImChannelDO selectByCode(String code) {
        return selectOne(ImChannelDO::getCode, code);
    }

    default List<ImChannelDO> selectListByStatusOrderBySort(Integer status) {
        return selectList(new LambdaQueryWrapperX<ImChannelDO>()
                .eq(ImChannelDO::getStatus, status)
                .orderByAsc(ImChannelDO::getSort));
    }

    default PageResult<ImChannelDO> selectPage(ImChannelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImChannelDO>()
                .likeIfPresent(ImChannelDO::getCode, reqVO.getCode())
                .likeIfPresent(ImChannelDO::getName, reqVO.getName())
                .eqIfPresent(ImChannelDO::getStatus, reqVO.getStatus())
                .orderByAsc(ImChannelDO::getSort)
                .orderByDesc(ImChannelDO::getId));
    }

}
