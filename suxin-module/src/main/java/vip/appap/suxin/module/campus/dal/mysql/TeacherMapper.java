package vip.appap.suxin.module.campus.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.campus.controller.admin.vo.TeacherPageReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.TeacherDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教师 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface TeacherMapper extends BaseMapperX<TeacherDO> {

    default PageResult<TeacherDO> selectPage(TeacherPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<TeacherDO>()
                .likeIfPresent(TeacherDO::getName, pageReqVO.getName())
                .likeIfPresent(TeacherDO::getTeacherNo, pageReqVO.getTeacherNo())
                .eqIfPresent(TeacherDO::getGender, pageReqVO.getGender())
                .likeIfPresent(TeacherDO::getTitle, pageReqVO.getTitle())
                .orderByDesc(TeacherDO::getId));
    }

    default TeacherDO selectByTeacherNo(String teacherNo) {
        return selectOne(TeacherDO::getTeacherNo, teacherNo);
    }

}
