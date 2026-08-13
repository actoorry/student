package vip.appap.suxin.module.im.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.im.controller.admin.vo.ImFacePackPageReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImFacePackDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 表情包 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImFacePackMapper extends BaseMapperX<ImFacePackDO> {

    default List<ImFacePackDO> selectListByStatusOrderBySort(Integer status) {
        return selectList(new LambdaQueryWrapperX<ImFacePackDO>()
                .eq(ImFacePackDO::getStatus, status)
                .orderByAsc(ImFacePackDO::getSort)
                .orderByAsc(ImFacePackDO::getId));
    }

    default PageResult<ImFacePackDO> selectPage(ImFacePackPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImFacePackDO>()
                .likeIfPresent(ImFacePackDO::getName, reqVO.getName())
                .eqIfPresent(ImFacePackDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ImFacePackDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(ImFacePackDO::getSort)
                .orderByDesc(ImFacePackDO::getId));
    }

}
