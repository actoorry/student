package vip.appap.suxin.module.campus.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.campus.controller.admin.vo.CoursePageReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.CourseDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface CourseMapper extends BaseMapperX<CourseDO> {

    default PageResult<CourseDO> selectPage(CoursePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<CourseDO>()
                .likeIfPresent(CourseDO::getCourseName, pageReqVO.getCourseName())
                .likeIfPresent(CourseDO::getCourseCode, pageReqVO.getCourseCode())
                .eqIfPresent(CourseDO::getTeacherId, pageReqVO.getTeacherId())
                .orderByDesc(CourseDO::getId));
    }

    default CourseDO selectByCourseCode(String courseCode) {
        return selectOne(CourseDO::getCourseCode, courseCode);
    }

}
