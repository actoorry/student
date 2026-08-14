package vip.appap.suxin.module.campus.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.campus.controller.admin.vo.StudentPageReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.StudentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface StudentMapper extends BaseMapperX<StudentDO> {

    default PageResult<StudentDO> selectPage(StudentPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<StudentDO>()
                .likeIfPresent(StudentDO::getName, pageReqVO.getName())
                .likeIfPresent(StudentDO::getStudentNo, pageReqVO.getStudentNo())
                .eqIfPresent(StudentDO::getGender, pageReqVO.getGender())
                .likeIfPresent(StudentDO::getClassName, pageReqVO.getClassName())
                .orderByDesc(StudentDO::getId));
    }

    default StudentDO selectByStudentNo(String studentNo) {
        return selectOne(StudentDO::getStudentNo, studentNo);
    }

    default StudentDO selectByUserId(Long userId) {
        return selectOne(StudentDO::getUserId, userId);
    }

}
